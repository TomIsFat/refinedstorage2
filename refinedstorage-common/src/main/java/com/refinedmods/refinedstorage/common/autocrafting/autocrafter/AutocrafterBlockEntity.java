package com.refinedmods.refinedstorage.common.autocrafting.autocrafter;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.autocrafting.task.StepBehavior;
import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.autocrafting.AutocraftingNetworkComponent;
import com.refinedmods.refinedstorage.api.network.autocrafting.PatternProvider;
import com.refinedmods.refinedstorage.api.network.impl.node.patternprovider.PatternProviderNetworkNode;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.support.network.InWorldNetworkNodeContainer;
import com.refinedmods.refinedstorage.common.autocrafting.PatternInventory;
import com.refinedmods.refinedstorage.common.content.BlockEntities;
import com.refinedmods.refinedstorage.common.content.ContentNames;
import com.refinedmods.refinedstorage.common.content.Items;
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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import static com.refinedmods.refinedstorage.common.support.AbstractDirectionalBlock.tryExtractDirection;

public class AutocrafterBlockEntity extends AbstractBaseNetworkNodeContainerBlockEntity<PatternProviderNetworkNode>
    implements ExtendedMenuProvider<AutocrafterData>, BlockEntityWithDrops, PatternInventory.Listener, StepBehavior {
    static final int PATTERNS = 9;

    private static final int MAX_CHAINED_AUTOCRAFTERS = 8;
    private static final String TAG_UPGRADES = "upgr";
    private static final String TAG_PATTERNS = "patterns";
    private static final String TAG_LOCK_MODE = "lm";
    private static final String TAG_PRIORITY = "pri";
    private static final String TAG_VISIBLE_TO_THE_AUTOCRAFTER_MANAGER = "vaum";

    private final PatternInventory patternContainer = new PatternInventory(PATTERNS, this::getLevel);
    private final UpgradeContainer upgradeContainer;
    private LockMode lockMode = LockMode.NEVER;
    private boolean visibleToTheAutocrafterManager = true;
    private int ticks;
    private int steps = getSteps(0);
    private int tickRate = getTickRate(0);

    public AutocrafterBlockEntity(final BlockPos pos, final BlockState state) {
        super(
            BlockEntities.INSTANCE.getAutocrafter(),
            pos,
            state,
            new PatternProviderNetworkNode(Platform.INSTANCE.getConfig().getAutocrafter().getEnergyUsage(), PATTERNS)
        );
        this.upgradeContainer = new UpgradeContainer(UpgradeDestinations.AUTOCRAFTER, (c, upgradeEnergyUsage) -> {
            final long baseEnergyUsage = Platform.INSTANCE.getConfig().getAutocrafter().getEnergyUsage();
            final long patternEnergyUsage = patternContainer.getEnergyUsage();
            mainNetworkNode.setEnergyUsage(baseEnergyUsage + patternEnergyUsage + upgradeEnergyUsage);
            final int amountOfSpeedUpgrades = c.getAmount(Items.INSTANCE.getSpeedUpgrade());
            tickRate = getTickRate(amountOfSpeedUpgrades);
            steps = getSteps(amountOfSpeedUpgrades);
            setChanged();
        });
        this.patternContainer.addListener(container -> {
            final long upgradeEnergyUsage = upgradeContainer.getEnergyUsage();
            final long baseEnergyUsage = Platform.INSTANCE.getConfig().getAutocrafter().getEnergyUsage();
            final long patternEnergyUsage = patternContainer.getEnergyUsage();
            mainNetworkNode.setEnergyUsage(baseEnergyUsage + patternEnergyUsage + upgradeEnergyUsage);
            setChanged();
        });
        this.patternContainer.setListener(this);
        this.mainNetworkNode.setStepBehavior(this);
    }

    @Override
    protected InWorldNetworkNodeContainer createMainContainer(final PatternProviderNetworkNode networkNode) {
        return new AutocrafterNetworkNodeContainer(
            this,
            networkNode,
            "main",
            new AutocrafterConnectionStrategy(this::getBlockState, getBlockPos())
        );
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

    // If there is another autocrafter next to us, that is pointing in our direction,
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
            if (neighbor instanceof AutocrafterBlockEntity neighborAutocrafter) {
                final Direction neighborDirection = tryExtractDirection(neighborAutocrafter.getBlockState());
                if (neighborDirection == direction.getOpposite()) {
                    return true;
                }
            }
        }
        return false;
    }

    private AutocrafterBlockEntity getChainingRoot() {
        return getChainingRoot(0, this);
    }

    private AutocrafterBlockEntity getChainingRoot(final int depth, final AutocrafterBlockEntity origin) {
        final Direction direction = tryExtractDirection(getBlockState());
        if (level == null || direction == null || depth >= MAX_CHAINED_AUTOCRAFTERS) {
            return origin;
        }
        final BlockEntity neighbor = getConnectedMachine();
        if (!(neighbor instanceof AutocrafterBlockEntity neighborCrafter)) {
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
        final AutocrafterBlockEntity root = getChainingRoot();
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
        // We don't handle autocrafters here, as autocrafters are also nameable, and we could have infinite recursion.
        if (connectedMachine instanceof Nameable nameable && !(connectedMachine instanceof AutocrafterBlockEntity)) {
            return nameable.getName();
        } else if (connectedMachine != null) {
            return connectedMachine.getBlockState().getBlock().getName();
        }
        return ContentNames.AUTOCRAFTER;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int syncId, final Inventory inventory, final Player player) {
        return new AutocrafterContainerMenu(syncId, inventory, this);
    }

    @Override
    public AutocrafterData getMenuData() {
        return new AutocrafterData(isPartOfChain(), isHeadOfChain());
    }

    @Override
    public StreamEncoder<RegistryFriendlyByteBuf, AutocrafterData> getMenuCodec() {
        return AutocrafterData.STREAM_CODEC;
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
        tag.putInt(TAG_PRIORITY, mainNetworkNode.getPriority());
        tag.putBoolean(TAG_VISIBLE_TO_THE_AUTOCRAFTER_MANAGER, visibleToTheAutocrafterManager);
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
            mainNetworkNode.setPriority(tag.getInt(TAG_PRIORITY));
        }
        if (tag.contains(TAG_VISIBLE_TO_THE_AUTOCRAFTER_MANAGER)) {
            visibleToTheAutocrafterManager = tag.getBoolean(TAG_VISIBLE_TO_THE_AUTOCRAFTER_MANAGER);
        }
    }

    @Override
    protected boolean hasRedstoneMode() {
        return false;
    }

    @Override
    public List<ItemStack> getUpgrades() {
        return upgradeContainer.getUpgrades();
    }

    @Override
    public boolean addUpgrade(final ItemStack upgradeStack) {
        return upgradeContainer.addUpgrade(upgradeStack);
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
        return mainNetworkNode.getPriority();
    }

    void setPriority(final int priority) {
        mainNetworkNode.setPriority(priority);
        setChanged();
    }

    boolean isVisibleToTheAutocrafterManager() {
        return visibleToTheAutocrafterManager;
    }

    void setVisibleToTheAutocrafterManager(final boolean visibleToTheAutocrafterManager) {
        this.visibleToTheAutocrafterManager = visibleToTheAutocrafterManager;
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
    protected void initialize(final ServerLevel level, final Direction direction) {
        super.initialize(level, direction);
        final Direction incomingDirection = direction.getOpposite();
        final BlockPos sourcePosition = worldPosition.relative(direction);
        mainNetworkNode.setExternalPatternInputSink(
            RefinedStorageApi.INSTANCE.getPatternProviderExternalPatternInputSinkFactory()
                .create(level, sourcePosition, incomingDirection));
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
    public void doWork() {
        super.doWork();
        if (mainNetworkNode.isActive()) {
            ticks++;
        }
    }

    @Override
    public boolean canStep(final Pattern pattern) {
        final PatternProvider provider = lookupProvider(pattern);
        if (provider == null) {
            return false;
        }
        if (provider == mainNetworkNode) {
            return mainNetworkNode.isActive() && ticks % tickRate == 0;
        }
        return provider.canStep(pattern);
    }

    @Override
    public int getSteps(final Pattern pattern) {
        final PatternProvider provider = lookupProvider(pattern);
        if (provider == null) {
            return 0;
        }
        if (provider == mainNetworkNode) {
            return steps;
        }
        return provider.getSteps(pattern);
    }

    private static int getSteps(final int amountOfSpeedUpgrades) {
        return switch (amountOfSpeedUpgrades) {
            case 1 -> 2;
            case 2 -> 3;
            case 3 -> 4;
            case 4 -> 5;
            default -> 1;
        };
    }

    private static int getTickRate(final int amountOfSpeedUpgrades) {
        return switch (amountOfSpeedUpgrades) {
            case 0 -> 10;
            case 1 -> 8;
            case 2 -> 6;
            case 3 -> 4;
            case 4 -> 2;
            default -> 0;
        };
    }

    @Nullable
    private PatternProvider lookupProvider(final Pattern pattern) {
        final Network network = mainNetworkNode.getNetwork();
        if (network == null) {
            return null;
        }
        return network.getComponent(AutocraftingNetworkComponent.class).getProviderByPattern(pattern);
    }

    @Override
    protected boolean doesBlockStateChangeWarrantNetworkNodeUpdate(final BlockState oldBlockState,
                                                                   final BlockState newBlockState) {
        return AbstractDirectionalBlock.didDirectionChange(oldBlockState, newBlockState);
    }
}
