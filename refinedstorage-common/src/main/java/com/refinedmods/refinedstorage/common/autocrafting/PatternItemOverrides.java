package com.refinedmods.refinedstorage.common.autocrafting;

import com.refinedmods.refinedstorage.common.content.DataComponents;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class PatternItemOverrides extends ItemOverrides {
    private final BakedModel emptyModel;
    private final BakedModel craftingModel;
    private final BakedModel processingModel;
    private final BakedModel stonecutterModel;
    private final BakedModel smithingTableModel;

    @SuppressWarnings({"DataFlowIssue", "deprecation"}) // null is allowed as long as we don't pass overrides
    public PatternItemOverrides(final ModelBaker modelBaker,
                                final BakedModel emptyModel,
                                final BakedModel craftingModel,
                                final BakedModel processingModel,
                                final BakedModel stonecutterModel,
                                final BakedModel smithingTableModel) {
        super(modelBaker, null, List.of());
        this.emptyModel = emptyModel;
        this.craftingModel = craftingModel;
        this.processingModel = processingModel;
        this.stonecutterModel = stonecutterModel;
        this.smithingTableModel = smithingTableModel;
    }

    @Override
    public BakedModel resolve(final BakedModel model,
                              final ItemStack stack,
                              @Nullable final ClientLevel level,
                              @Nullable final LivingEntity entity,
                              final int seed) {
        final PatternState state = stack.get(DataComponents.INSTANCE.getPatternState());
        if (state == null) {
            return emptyModel;
        }
        return switch (state.type()) {
            case CRAFTING -> getOutputModel(stack, level, entity, seed).orElse(craftingModel);
            case PROCESSING -> getOutputModel(stack, level, entity, seed).orElse(processingModel);
            case STONECUTTER -> getOutputModel(stack, level, entity, seed).orElse(stonecutterModel);
            case SMITHING_TABLE -> getOutputModel(stack, level, entity, seed).orElse(smithingTableModel);
        };
    }

    private Optional<BakedModel> getOutputModel(final ItemStack stack,
                                                @Nullable final ClientLevel level,
                                                @Nullable final LivingEntity entity,
                                                final int seed) {
        if (PatternRendering.canDisplayOutput(stack)) {
            return PatternRendering.getOutput(stack).map(
                output -> Minecraft.getInstance().getItemRenderer().getModel(output, level, entity, seed)
            );
        }
        return Optional.empty();
    }
}
