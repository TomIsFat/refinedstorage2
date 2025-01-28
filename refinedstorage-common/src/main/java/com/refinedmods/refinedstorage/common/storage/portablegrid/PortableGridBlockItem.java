package com.refinedmods.refinedstorage.common.storage.portablegrid;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage.api.network.impl.energy.AbstractProxyEnergyStorage;
import com.refinedmods.refinedstorage.api.network.impl.energy.EnergyStorageImpl;
import com.refinedmods.refinedstorage.api.storage.StateTrackedStorage;
import com.refinedmods.refinedstorage.api.storage.StorageState;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.storage.StorageContainerItem;
import com.refinedmods.refinedstorage.common.api.storage.StorageRepository;
import com.refinedmods.refinedstorage.common.api.support.HelpTooltipComponent;
import com.refinedmods.refinedstorage.common.api.support.energy.AbstractEnergyBlockItem;
import com.refinedmods.refinedstorage.common.api.support.slotreference.SlotReference;
import com.refinedmods.refinedstorage.common.api.support.slotreference.SlotReferenceHandlerItem;
import com.refinedmods.refinedstorage.common.content.BlockEntities;
import com.refinedmods.refinedstorage.common.content.ContentNames;
import com.refinedmods.refinedstorage.common.storage.Disk;
import com.refinedmods.refinedstorage.common.storage.DiskInventory;
import com.refinedmods.refinedstorage.common.support.energy.CreativeEnergyStorage;

import java.util.Optional;
import javax.annotation.Nullable;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;
import static java.util.Objects.requireNonNullElse;

public class PortableGridBlockItem extends AbstractEnergyBlockItem implements SlotReferenceHandlerItem {
    private static final Component HELP = createTranslation("item", "portable_grid.help");

    private final PortableGridType type;

    public PortableGridBlockItem(final Block block, final PortableGridType type) {
        super(block, new Item.Properties().stacksTo(1), RefinedStorageApi.INSTANCE.getEnergyItemHelper());
        this.type = type;
    }

    public static PortableGridBlockItemRenderInfo getRenderInfo(final ItemStack stack, final Level level) {
        final boolean creative = isCreative(stack);
        final boolean hasEnergy = creative || createEnergyStorage(stack).getStored() > 0;
        final ItemStack diskStack = getDisk(stack, level.registryAccess());
        final boolean active = hasEnergy && !diskStack.isEmpty();
        final Disk disk = new Disk(
            diskStack.isEmpty() ? null : diskStack.getItem(),
            getState(diskStack, active)
        );
        return new PortableGridBlockItemRenderInfo(active, disk);
    }

    private static boolean isCreative(final ItemStack stack) {
        return stack.getItem() instanceof PortableGridBlockItem portableGridBlockItem
            && portableGridBlockItem.type == PortableGridType.CREATIVE;
    }

    private static StorageState getState(final ItemStack diskStack, final boolean active) {
        if (diskStack.isEmpty() || !(diskStack.getItem() instanceof StorageContainerItem storageContainerItem)) {
            return StorageState.NONE;
        }
        if (!active) {
            return StorageState.INACTIVE;
        }
        final StorageRepository storageRepository = RefinedStorageApi.INSTANCE.getClientStorageRepository();
        return storageContainerItem.getInfo(storageRepository, diskStack)
            .map(storageInfo -> StateTrackedStorage.computeState(storageInfo.capacity(), storageInfo.stored()))
            .orElse(StorageState.INACTIVE);
    }

    private static ItemStack getDisk(final ItemStack stack, final HolderLookup.Provider provider) {
        final CustomData blockEntityData = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (blockEntityData == null) {
            return ItemStack.EMPTY;
        }
        return AbstractPortableGridBlockEntity.getDisk(blockEntityData, provider);
    }

    static void setDiskInventory(final ItemStack stack,
                                 final DiskInventory diskInventory,
                                 final HolderLookup.Provider provider) {
        final CompoundTag tag = new CompoundTag();
        AbstractPortableGridBlockEntity.writeDiskInventory(tag, diskInventory, provider);
        setBlockEntityData(
            stack,
            isCreative(stack)
                ? BlockEntities.INSTANCE.getCreativePortableGrid()
                : BlockEntities.INSTANCE.getPortableGrid(),
            tag
        );
    }

    public static EnergyStorage createEnergyStorage(final ItemStack stack) {
        final EnergyStorage energyStorage = new EnergyStorageImpl(
            Platform.INSTANCE.getConfig().getPortableGrid().getEnergyCapacity()
        );
        return RefinedStorageApi.INSTANCE.asBlockItemEnergyStorage(
            energyStorage,
            stack,
            BlockEntities.INSTANCE.getPortableGrid()
        );
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level level, final Player player, final InteractionHand hand) {
        final ItemStack stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer serverPlayer && level.getServer() != null) {
            final SlotReference slotReference = RefinedStorageApi.INSTANCE.createInventorySlotReference(player, hand);
            slotReference.resolve(player).ifPresent(s -> use(serverPlayer, s, slotReference));
        }
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void use(final ServerPlayer player, final ItemStack stack, final SlotReference slotReference) {
        final PortableGridEnergyStorage energyStorage = createEnergyStorageInternal(stack);
        final Level level = player.serverLevel();
        final DiskInventoryListenerImpl listener = new DiskInventoryListenerImpl(stack, level.registryAccess());
        final DiskInventory diskInventory = createDiskInventory(stack, listener, level.registryAccess());
        diskInventory.setStorageRepository(RefinedStorageApi.INSTANCE.getStorageRepository(level));
        final PortableGrid portableGrid = new PortableGrid(
            energyStorage,
            diskInventory,
            () -> {
            }
        );
        listener.portableGrid = portableGrid;
        energyStorage.portableGrid = portableGrid;
        portableGrid.updateStorage();
        Platform.INSTANCE.getMenuOpener().openMenu(player, new PortableGridItemExtendedMenuProvider(
            requireNonNullElse(stack.get(DataComponents.CUSTOM_NAME),
                type == PortableGridType.CREATIVE ? ContentNames.CREATIVE_PORTABLE_GRID : ContentNames.PORTABLE_GRID),
            portableGrid,
            energyStorage,
            diskInventory,
            slotReference
        ));
    }

    private PortableGridEnergyStorage createEnergyStorageInternal(final ItemStack stack) {
        if (type == PortableGridType.CREATIVE) {
            return new PortableGridEnergyStorage(CreativeEnergyStorage.INSTANCE);
        }
        return new PortableGridEnergyStorage(createEnergyStorage(stack));
    }

    private DiskInventory createDiskInventory(final ItemStack stack,
                                              final DiskInventoryListenerImpl listener,
                                              final HolderLookup.Provider provider) {
        final DiskInventory diskInventory = new DiskInventory(listener, 1);
        final CustomData customData = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (customData != null) {
            AbstractPortableGridBlockEntity.readDiskInventory(customData.copyTag(), diskInventory, provider);
        }
        return diskInventory;
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(final ItemStack stack) {
        return Optional.of(new HelpTooltipComponent(HELP));
    }

    private static class DiskInventoryListenerImpl implements DiskInventory.DiskListener {
        private final ItemStack portableGridStack;
        private final HolderLookup.Provider provider;
        @Nullable
        private PortableGrid portableGrid;

        private DiskInventoryListenerImpl(final ItemStack portableGridStack, final HolderLookup.Provider provider) {
            this.portableGridStack = portableGridStack;
            this.provider = provider;
        }

        @Override
        public void onDiskChanged(final DiskInventory inventory, final int slot) {
            final boolean stillLoading = portableGrid == null;
            if (stillLoading) {
                return;
            }
            setDiskInventory(portableGridStack, inventory, provider);
            final boolean wasActive = portableGrid.isGridActive();
            portableGrid.updateStorage();
            final boolean isActive = portableGrid.isGridActive();
            if (wasActive != isActive) {
                portableGrid.activeChanged(isActive);
            }
        }
    }

    private static class PortableGridEnergyStorage extends AbstractProxyEnergyStorage {
        @Nullable
        private PortableGrid portableGrid;

        private PortableGridEnergyStorage(final EnergyStorage energyStorage) {
            super(energyStorage);
        }

        @Override
        public long extract(final long amount, final Action action) {
            if (action == Action.EXECUTE && portableGrid != null) {
                final boolean wasActive = portableGrid.isGridActive();
                final long extracted = super.extract(amount, action);
                final boolean isActive = portableGrid.isGridActive();
                if (wasActive != isActive) {
                    portableGrid.activeChanged(isActive);
                }
                return extracted;
            }
            return super.extract(amount, action);
        }
    }
}
