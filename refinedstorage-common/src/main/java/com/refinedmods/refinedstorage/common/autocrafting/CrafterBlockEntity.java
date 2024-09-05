package com.refinedmods.refinedstorage.common.autocrafting;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.network.impl.node.patternprovider.PatternProviderNetworkNode;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.autocrafting.PatternProviderItem;
import com.refinedmods.refinedstorage.common.api.support.network.InWorldNetworkNodeContainer;
import com.refinedmods.refinedstorage.common.content.BlockEntities;
import com.refinedmods.refinedstorage.common.content.ContentNames;
import com.refinedmods.refinedstorage.common.support.AbstractDirectionalBlock;
import com.refinedmods.refinedstorage.common.support.BlockEntityWithDrops;
import com.refinedmods.refinedstorage.common.support.FilteredContainer;
import com.refinedmods.refinedstorage.common.support.containermenu.ExtendedMenuProvider;
import com.refinedmods.refinedstorage.common.support.network.AbstractBaseNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage.common.upgrade.UpgradeContainer;
import com.refinedmods.refinedstorage.common.upgrade.UpgradeDestinations;
import com.refinedmods.refinedstorage.common.util.ContainerUtil;

import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import static com.refinedmods.refinedstorage.common.support.AbstractDirectionalBlock.tryExtractDirection;

// TODO: More energy usage for more patterns.
public class CrafterBlockEntity extends AbstractBaseNetworkNodeContainerBlockEntity<PatternProviderNetworkNode>
    implements ExtendedMenuProvider<CrafterData>, BlockEntityWithDrops, PatternInventory.Listener {
    static final int PATTERNS = 9;

    private static final int MAX_CHAINED_CRAFTERS = 8;
    private static final String TAG_UPGRADES = "upgr";
    private static final String TAG_PATTERNS = "patterns";
    private static final String TAG_LOCK_MODE = "lm";
    private static final String TAG_PRIORITY = "pri";

    private final PatternInventory patternContainer = new PatternInventory(this::getLevel);
    private final UpgradeContainer upgradeContainer;
    private LockMode lockMode = LockMode.NEVER;
    private int priority;

    public CrafterBlockEntity(final BlockPos pos, final BlockState state) {
        super(
            BlockEntities.INSTANCE.getCrafter(),
            pos,
            state,
            new PatternProviderNetworkNode(Platform.INSTANCE.getConfig().getCrafter().getEnergyUsage(), PATTERNS)
        );
        this.upgradeContainer = new UpgradeContainer(UpgradeDestinations.CRAFTER, upgradeEnergyUsage -> {
            final long baseEnergyUsage = Platform.INSTANCE.getConfig().getCrafter().getEnergyUsage();
            mainNetworkNode.setEnergyUsage(baseEnergyUsage + upgradeEnergyUsage);
            setChanged();
        });
        patternContainer.addListener(container -> setChanged());
        patternContainer.setListener(this);
    }

    @Override
    protected InWorldNetworkNodeContainer createMainContainer(final PatternProviderNetworkNode networkNode) {
        return RefinedStorageApi.INSTANCE.createNetworkNodeContainer(this, networkNode)
            .connectionStrategy(new CrafterConnectionStrategy(this::getBlockState, getBlockPos()))
            .build();
    }

    FilteredContainer getPatternContainer() {
        return patternContainer;
    }

    UpgradeContainer getUpgradeContainer() {
        return upgradeContainer;
    }

    private boolean isPartOfChain() {
        return getChainingRoot() != this;
    }

    // if there is another crafter next to us, that is pointing in our direction,
    // and we are not part of a chain, we are the head of the chain
    private boolean isHeadOfChain() {
        if (level == null || isPartOfChain()) {
            return false;
        }
        for (final Direction direction : Direction.values()) {
            final BlockPos pos = getBlockPos().relative(direction);
            if (!level.isLoaded(pos)) {
                continue;
            }
            final BlockEntity neighbor = level.getBlockEntity(pos);
            if (neighbor instanceof CrafterBlockEntity neighborCrafter) {
                final Direction neighborDirection = tryExtractDirection(neighborCrafter.getBlockState());
                if (neighborDirection == direction.getOpposite()) {
                    return true;
                }
            }
        }
        return false;
    }

    private CrafterBlockEntity getChainingRoot() {
        return getChainingRoot(0, this);
    }

    private CrafterBlockEntity getChainingRoot(final int depth, final CrafterBlockEntity origin) {
        final Direction direction = tryExtractDirection(getBlockState());
        if (level == null || direction == null || depth >= MAX_CHAINED_CRAFTERS) {
            return origin;
        }
        final BlockEntity neighbor = getConnectedMachine();
        if (!(neighbor instanceof CrafterBlockEntity neighborCrafter)) {
            return this;
        }
        return neighborCrafter.getChainingRoot(depth + 1, origin);
    }

    @Nullable
    private BlockEntity getConnectedMachine() {
        final Direction direction = tryExtractDirection(getBlockState());
        if (level == null || direction == null) {
            return null;
        }
        final BlockPos neighborPos = getBlockPos().relative(direction);
        if (!level.isLoaded(neighborPos)) {
            return null;
        }
        return level.getBlockEntity(neighborPos);
    }

    @Override
    public Component getName() {
        final CrafterBlockEntity root = getChainingRoot();
        if (root == this) {
            return doGetName();
        }
        return root.getName();
    }

    private Component doGetName() {
        final Component customName = getCustomName();
        if (customName != null) {
            return customName;
        }
        final BlockEntity connectedMachine = getConnectedMachine();
        // We don't handle crafters here, as crafters are also nameable, and we could have infinite recursion.
        if (connectedMachine instanceof Nameable nameable && !(connectedMachine instanceof CrafterBlockEntity)) {
            return nameable.getName();
        } else if (connectedMachine != null) {
            return connectedMachine.getBlockState().getBlock().getName();
        }
        return ContentNames.CRAFTER;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int syncId, final Inventory inventory, final Player player) {
        return new CrafterContainerMenu(syncId, inventory, this);
    }

    @Override
    public CrafterData getMenuData() {
        return new CrafterData(isPartOfChain(), isHeadOfChain());
    }

    @Override
    public StreamEncoder<RegistryFriendlyByteBuf, CrafterData> getMenuCodec() {
        return CrafterData.STREAM_CODEC;
    }

    @Override
    public void saveAdditional(final CompoundTag tag, final HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.put(TAG_PATTERNS, ContainerUtil.write(patternContainer, provider));
        tag.put(TAG_UPGRADES, ContainerUtil.write(upgradeContainer, provider));
    }

    @Override
    public void writeConfiguration(final CompoundTag tag, final HolderLookup.Provider provider) {
        super.writeConfiguration(tag, provider);
        tag.putInt(TAG_LOCK_MODE, LockModeSettings.getLockMode(lockMode));
        tag.putInt(TAG_PRIORITY, priority);
    }

    @Override
    public void loadAdditional(final CompoundTag tag, final HolderLookup.Provider provider) {
        if (tag.contains(TAG_PATTERNS)) {
            ContainerUtil.read(tag.getCompound(TAG_PATTERNS), patternContainer, provider);
        }
        if (tag.contains(TAG_UPGRADES)) {
            ContainerUtil.read(tag.getCompound(TAG_UPGRADES), upgradeContainer, provider);
        }
        super.loadAdditional(tag, provider);
    }

    @Override
    public void readConfiguration(final CompoundTag tag, final HolderLookup.Provider provider) {
        super.readConfiguration(tag, provider);
        if (tag.contains(TAG_LOCK_MODE)) {
            lockMode = LockModeSettings.getLockMode(tag.getInt(TAG_LOCK_MODE));
        }
        if (tag.contains(TAG_PRIORITY)) {
            priority = tag.getInt(TAG_PRIORITY);
        }
    }

    @Override
    protected boolean hasRedstoneMode() {
        return false;
    }

    @Override
    public List<Item> getUpgradeItems() {
        return upgradeContainer.getUpgradeItems();
    }

    @Override
    public boolean addUpgradeItem(final Item upgradeItem) {
        return upgradeContainer.addUpgradeItem(upgradeItem);
    }

    @Override
    public NonNullList<ItemStack> getDrops() {
        final NonNullList<ItemStack> drops = NonNullList.create();
        drops.addAll(upgradeContainer.getDrops());
        for (int i = 0; i < patternContainer.getContainerSize(); ++i) {
            drops.add(patternContainer.getItem(i));
        }
        return drops;
    }

    void setCustomName(final String name) {
        if (isPartOfChain()) {
            return;
        }
        setCustomName(name.trim().isBlank() ? null : Component.literal(name));
        setChanged();
    }

    LockMode getLockMode() {
        return lockMode;
    }

    void setLockMode(final LockMode lockMode) {
        this.lockMode = lockMode;
        setChanged();
    }

    int getPriority() {
        return priority;
    }

    void setPriority(final int priority) {
        this.priority = priority;
        setChanged();
    }

    @Override
    public void setLevel(final Level level) {
        super.setLevel(level);
        if (level.isClientSide()) {
            return;
        }
        for (int i = 0; i < patternContainer.getContainerSize(); ++i) {
            patternChanged(i);
        }
    }

    @Override
    public void patternChanged(final int slot) {
        if (level == null) {
            return;
        }
        final Pattern pattern = RefinedStorageApi.INSTANCE.getPattern(patternContainer.getItem(slot), level)
            .orElse(null);
        mainNetworkNode.setPattern(slot, pattern);
    }

    @Override
    protected boolean doesBlockStateChangeWarrantNetworkNodeUpdate(final BlockState oldBlockState,
                                                                   final BlockState newBlockState) {
        return AbstractDirectionalBlock.didDirectionChange(oldBlockState, newBlockState);
    }

    static boolean isValidPattern(final ItemStack stack, final Level level) {
        return stack.getItem() instanceof PatternProviderItem patternProviderItem
            && patternProviderItem.getPattern(stack, level).isPresent();
    }
}
