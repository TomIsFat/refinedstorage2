package com.refinedmods.refinedstorage.common.storage.storagedisk;

import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.RefinedStorageClientApi;
import com.refinedmods.refinedstorage.common.api.storage.AbstractStorageContainerItem;
import com.refinedmods.refinedstorage.common.api.storage.SerializableStorage;
import com.refinedmods.refinedstorage.common.api.storage.StorageRepository;
import com.refinedmods.refinedstorage.common.api.support.HelpTooltipComponent;
import com.refinedmods.refinedstorage.common.content.Items;
import com.refinedmods.refinedstorage.common.storage.FluidStorageVariant;
import com.refinedmods.refinedstorage.common.storage.StorageTypes;
import com.refinedmods.refinedstorage.common.storage.StorageVariant;
import com.refinedmods.refinedstorage.common.storage.UpgradeableStorageContainer;
import com.refinedmods.refinedstorage.common.support.resource.FluidResource;

import java.util.Optional;
import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.format;

public class FluidStorageDiskItem extends AbstractStorageContainerItem implements UpgradeableStorageContainer {
    private static final Component CREATIVE_HELP = createTranslation("item", "creative_fluid_storage_disk.help");

    private final FluidStorageVariant variant;
    private final Component helpText;

    public FluidStorageDiskItem(final FluidStorageVariant variant) {
        super(
            new Item.Properties().stacksTo(1).fireResistant(),
            RefinedStorageApi.INSTANCE.getStorageContainerItemHelper()
        );
        this.variant = variant;
        this.helpText = getHelpText(variant);
    }

    private static Component getHelpText(final FluidStorageVariant variant) {
        if (variant.getCapacityInBuckets() == null) {
            return CREATIVE_HELP;
        }
        return createTranslation("item", "fluid_storage_disk.help", format(variant.getCapacityInBuckets()));
    }

    @Nullable
    @Override
    protected Long getCapacity() {
        return variant.getCapacity();
    }

    @Override
    protected String formatAmount(final long amount) {
        return RefinedStorageClientApi.INSTANCE.getResourceRendering(FluidResource.class).formatAmount(amount);
    }

    @Override
    protected SerializableStorage createStorage(final StorageRepository storageRepository) {
        return StorageTypes.FLUID.create(variant.getCapacity(), storageRepository::markAsChanged);
    }

    @Override
    protected ItemStack createPrimaryDisassemblyByproduct(final int count) {
        return new ItemStack(Items.INSTANCE.getStorageHousing(), count);
    }

    @Override
    @Nullable
    protected ItemStack createSecondaryDisassemblyByproduct(final int count) {
        if (variant == FluidStorageVariant.CREATIVE) {
            return null;
        }
        return new ItemStack(Items.INSTANCE.getFluidStoragePart(variant), count);
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(final ItemStack stack) {
        return Optional.of(new HelpTooltipComponent(helpText));
    }

    @Override
    public StorageVariant getVariant() {
        return variant;
    }

    @Override
    public void transferTo(final ItemStack from, final ItemStack to) {
        helper.markAsToTransfer(from, to);
    }
}
