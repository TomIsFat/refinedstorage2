package com.refinedmods.refinedstorage.common.autocrafting;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceContainer;
import com.refinedmods.refinedstorage.common.content.BlockEntities;
import com.refinedmods.refinedstorage.common.content.ContentNames;
import com.refinedmods.refinedstorage.common.content.DataComponents;
import com.refinedmods.refinedstorage.common.content.Items;
import com.refinedmods.refinedstorage.common.grid.AbstractGridBlockEntity;
import com.refinedmods.refinedstorage.common.grid.AbstractGridContainerMenu;
import com.refinedmods.refinedstorage.common.grid.CraftingState;
import com.refinedmods.refinedstorage.common.grid.GridData;
import com.refinedmods.refinedstorage.common.support.BlockEntityWithDrops;
import com.refinedmods.refinedstorage.common.support.CraftingMatrix;
import com.refinedmods.refinedstorage.common.support.FilteredContainer;
import com.refinedmods.refinedstorage.common.support.containermenu.NetworkNodeExtendedMenuProvider;
import com.refinedmods.refinedstorage.common.support.resource.ResourceContainerData;
import com.refinedmods.refinedstorage.common.support.resource.ResourceContainerImpl;
import com.refinedmods.refinedstorage.common.util.ContainerUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import static com.refinedmods.refinedstorage.common.support.resource.ResourceContainerImpl.setResourceContainerData;

public class PatternGridBlockEntity extends AbstractGridBlockEntity implements BlockEntityWithDrops,
    NetworkNodeExtendedMenuProvider<PatternGridData> {
    private static final String TAG_PATTERN_INPUT = "pattern_input";
    private static final String TAG_PATTERN_OUTPUT = "pattern_output";
    private static final String TAG_PROCESSING_INPUT = "processing_input";
    private static final String TAG_PROCESSING_OUTPUT = "processing_output";
    private static final String TAG_FUZZY_MODE = "fuzzy_mode";
    private static final String TAG_PATTERN_TYPE = "processing";

    private final CraftingState craftingState = new CraftingState(this::setChanged, this::getLevel);
    private final ProcessingMatrixInputResourceContainer processingInput = createProcessingMatrixInputContainer();
    private final ResourceContainer processingOutput = createProcessingMatrixOutputContainer();
    private final FilteredContainer patternInput = new FilteredContainer(1, PatternGridBlockEntity::isValidPattern);
    private final FilteredContainer patternOutput = new PatternOutputContainer();
    private boolean fuzzyMode;
    private PatternType patternType = PatternType.CRAFTING;

    public PatternGridBlockEntity(final BlockPos pos, final BlockState state) {
        super(
            BlockEntities.INSTANCE.getPatternGrid(),
            pos,
            state,
            Platform.INSTANCE.getConfig().getPatternGrid().getEnergyUsage()
        );
        patternInput.addListener(container -> setChanged());
        patternOutput.addListener(container -> setChanged());
        processingInput.setListener(this::setChanged);
        processingOutput.setListener(this::setChanged);
    }

    CraftingMatrix getCraftingMatrix() {
        return craftingState.getCraftingMatrix();
    }

    ResultContainer getCraftingResult() {
        return craftingState.getCraftingResult();
    }

    ProcessingMatrixInputResourceContainer getProcessingInput() {
        return processingInput;
    }

    ResourceContainer getProcessingOutput() {
        return processingOutput;
    }

    FilteredContainer getPatternInput() {
        return patternInput;
    }

    FilteredContainer getPatternOutput() {
        return patternOutput;
    }

    @Override
    public void saveAdditional(final CompoundTag tag, final HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.put(TAG_PATTERN_INPUT, ContainerUtil.write(patternInput, provider));
        tag.put(TAG_PATTERN_OUTPUT, ContainerUtil.write(patternOutput, provider));
        tag.putBoolean(TAG_FUZZY_MODE, fuzzyMode);
        tag.putInt(TAG_PATTERN_TYPE, PatternTypeSettings.getPatternType(patternType));
        tag.put(TAG_PROCESSING_INPUT, processingInput.toTag(provider));
        tag.put(TAG_PROCESSING_OUTPUT, processingOutput.toTag(provider));
        craftingState.writeToTag(tag, provider);
    }

    @Override
    public void loadAdditional(final CompoundTag tag, final HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains(TAG_PATTERN_INPUT)) {
            ContainerUtil.read(tag.getCompound(TAG_PATTERN_INPUT), patternInput, provider);
        }
        if (tag.contains(TAG_PATTERN_OUTPUT)) {
            ContainerUtil.read(tag.getCompound(TAG_PATTERN_OUTPUT), patternOutput, provider);
        }
        fuzzyMode = tag.getBoolean(TAG_FUZZY_MODE);
        patternType = PatternTypeSettings.getPatternType(tag.getInt(TAG_PATTERN_TYPE));
        if (tag.contains(TAG_PROCESSING_INPUT)) {
            processingInput.fromTag(tag.getCompound(TAG_PROCESSING_INPUT), provider);
        }
        if (tag.contains(TAG_PROCESSING_OUTPUT)) {
            processingOutput.fromTag(tag.getCompound(TAG_PROCESSING_OUTPUT), provider);
        }
        craftingState.readFromTag(tag, provider);
    }

    @Override
    public void setLevel(final Level level) {
        super.setLevel(level);
        craftingState.updateResult(level);
    }

    boolean isFuzzyMode() {
        return fuzzyMode;
    }

    PatternType getPatternType() {
        return patternType;
    }

    void setFuzzyMode(final boolean fuzzyMode) {
        this.fuzzyMode = fuzzyMode;
        setChanged();
    }

    void setPatternType(final PatternType patternType) {
        this.patternType = patternType;
        setChanged();
    }

    @Override
    public Component getDisplayName() {
        return ContentNames.PATTERN_GRID;
    }

    @Override
    @Nullable
    public AbstractGridContainerMenu createMenu(final int syncId, final Inventory inventory, final Player player) {
        return new PatternGridContainerMenu(syncId, inventory, this);
    }

    @Override
    public PatternGridData getMenuData() {
        return new PatternGridData(
            GridData.of(this),
            patternType,
            ProcessingInputData.of(processingInput),
            ResourceContainerData.of(processingOutput)
        );
    }

    @Override
    public StreamEncoder<RegistryFriendlyByteBuf, PatternGridData> getMenuCodec() {
        return PatternGridData.STREAM_CODEC;
    }

    @Override
    public NonNullList<ItemStack> getDrops() {
        final NonNullList<ItemStack> drops = NonNullList.create();
        drops.add(patternInput.getItem(0));
        drops.add(patternOutput.getItem(0));
        for (int i = 0; i < craftingState.getCraftingMatrix().getContainerSize(); ++i) {
            drops.add(craftingState.getCraftingMatrix().getItem(i));
        }
        return drops;
    }

    void clear() {
        if (level == null) {
            return;
        }
        switch (patternType) {
            case CRAFTING -> {
                craftingState.getCraftingMatrix().clearContent();
                craftingState.updateResult(level);
            }
            case PROCESSING -> {
                processingInput.clear();
                processingOutput.clear();
            }
        }
        setChanged();
    }

    void createPattern() {
        if (level == null || !isPatternAvailable()) {
            return;
        }
        final ItemStack result = switch (patternType) {
            case CRAFTING -> createCraftingPattern();
            case PROCESSING -> createProcessingPattern();
            default -> null;
        };
        if (result != null) {
            final boolean shouldExtractInputPattern = patternOutput.getItem(0).isEmpty();
            if (shouldExtractInputPattern) {
                patternInput.removeItem(0, 1);
            }
            patternOutput.setItem(0, result);
        }
    }

    @Nullable
    private ItemStack createCraftingPattern() {
        if (!craftingState.hasCraftingResult()) {
            return null;
        }
        final ItemStack result = createPatternStack(PatternType.CRAFTING);
        final CraftingPatternState state = new CraftingPatternState(
            fuzzyMode,
            getCraftingMatrix().asPositionedCraftInput()
        );
        result.set(DataComponents.INSTANCE.getCraftingPatternState(), state);
        return result;
    }

    @Nullable
    private ItemStack createProcessingPattern() {
        if (processingInput.isEmpty() || processingOutput.isEmpty()) {
            return null;
        }
        final ItemStack result = createPatternStack(PatternType.PROCESSING);
        final List<Optional<ProcessingPatternState.Input>> inputs = new ArrayList<>();
        for (int i = 0; i < processingInput.size(); ++i) {
            inputs.add(processingInput.getInput(i));
        }
        final List<Optional<ResourceAmount>> outputs = new ArrayList<>();
        for (int i = 0; i < processingOutput.size(); ++i) {
            outputs.add(Optional.ofNullable(processingOutput.get(i)));
        }
        final ProcessingPatternState patternProcessingState = new ProcessingPatternState(
            inputs,
            outputs
        );
        result.set(DataComponents.INSTANCE.getProcessingPatternState(), patternProcessingState);
        return result;
    }

    private static ItemStack createPatternStack(final PatternType patternType) {
        final ItemStack result = new ItemStack(Items.INSTANCE.getPattern());
        final PatternState patternState = new PatternState(UUID.randomUUID(), patternType);
        result.set(DataComponents.INSTANCE.getPatternState(), patternState);
        return result;
    }

    void copyPattern(final ItemStack stack) {
        final PatternState state = stack.get(DataComponents.INSTANCE.getPatternState());
        if (state == null) {
            return;
        }
        this.patternType = state.type();
        switch (state.type()) {
            case CRAFTING -> {
                final CraftingPatternState patternCraftingState = stack.get(
                    DataComponents.INSTANCE.getCraftingPatternState()
                );
                if (patternCraftingState == null) {
                    return;
                }
                copyCraftingPattern(patternCraftingState);
            }
            case PROCESSING -> {
                final ProcessingPatternState patternProcessingState = stack.get(
                    DataComponents.INSTANCE.getProcessingPatternState()
                );
                if (patternProcessingState == null) {
                    return;
                }
                copyProcessingPattern(patternProcessingState);
            }
        }
        setChanged();
    }

    private void copyCraftingPattern(final CraftingPatternState state) {
        this.fuzzyMode = state.fuzzyMode();
        craftingState.getCraftingMatrix().clearContent();
        final CraftingInput.Positioned positionedInput = state.input();
        final int left = positionedInput.left();
        final int top = positionedInput.top();
        final CraftingInput input = positionedInput.input();
        for (int x = 0; x < input.width(); ++x) {
            for (int y = 0; y < input.height(); ++y) {
                final int matrixIndex = x + left + (y + top) * craftingState.getCraftingMatrix().getWidth();
                final int recipeIndex = x + y * input.width();
                final ItemStack stack = input.getItem(recipeIndex);
                craftingState.getCraftingMatrix().setItem(matrixIndex, stack);
            }
        }
        if (level != null) {
            craftingState.updateResult(level);
        }
    }

    private void copyProcessingPattern(final ProcessingPatternState state) {
        processingInput.clear();
        processingOutput.clear();
        for (int i = 0; i < state.inputs().size(); ++i) {
            final int ii = i;
            state.inputs().get(i).ifPresent(input -> processingInput.set(ii, input));
        }
        for (int i = 0; i < state.outputs().size(); ++i) {
            final int ii = i;
            state.outputs().get(i).ifPresent(amount -> processingOutput.set(ii, amount));
        }
    }

    private boolean isPatternAvailable() {
        return !patternInput.getItem(0).isEmpty() || !patternOutput.getItem(0).isEmpty();
    }

    static boolean isValidPattern(final ItemStack stack) {
        return stack.getItem() instanceof PatternItem;
    }

    static ProcessingMatrixInputResourceContainer createProcessingMatrixInputContainer() {
        return new ProcessingMatrixInputResourceContainer(
            81,
            PatternGridBlockEntity::getProcessingPatternLimit,
            RefinedStorageApi.INSTANCE.getItemResourceFactory(),
            RefinedStorageApi.INSTANCE.getAlternativeResourceFactories()
        );
    }

    static ProcessingMatrixInputResourceContainer createProcessingMatrixInputContainer(final ProcessingInputData data) {
        final ProcessingMatrixInputResourceContainer filterContainer = createProcessingMatrixInputContainer();
        setResourceContainerData(data.resourceContainerData(), filterContainer);
        for (int i = 0; i < data.allowedTagIds().size(); ++i) {
            filterContainer.setAllowedTagIds(i, data.allowedTagIds().get(i));
        }
        return filterContainer;
    }

    static ResourceContainer createProcessingMatrixOutputContainer() {
        return new ResourceContainerImpl(
            81,
            PatternGridBlockEntity::getProcessingPatternLimit,
            RefinedStorageApi.INSTANCE.getItemResourceFactory(),
            RefinedStorageApi.INSTANCE.getAlternativeResourceFactories()
        );
    }

    static ResourceContainer createProcessingMatrixOutputContainer(final ResourceContainerData data) {
        final ResourceContainer filterContainer = createProcessingMatrixOutputContainer();
        setResourceContainerData(data, filterContainer);
        return filterContainer;
    }

    private static long getProcessingPatternLimit(final ResourceKey resource) {
        return resource instanceof PlatformResourceKey platformResource
            ? platformResource.getProcessingPatternLimit()
            : 1;
    }
}
