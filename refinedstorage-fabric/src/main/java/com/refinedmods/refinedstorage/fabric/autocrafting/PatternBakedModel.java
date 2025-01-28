package com.refinedmods.refinedstorage.fabric.autocrafting;

import com.refinedmods.refinedstorage.common.autocrafting.PatternItemOverrides;

import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBaker;

class PatternBakedModel extends ForwardingBakedModel {
    private final ItemOverrides itemOverrides;

    PatternBakedModel(final ModelBaker modelBaker,
                      final BakedModel emptyModel,
                      final BakedModel craftingModel,
                      final BakedModel processingModel,
                      final BakedModel stonecutterModel,
                      final BakedModel smithingTableModel) {
        this.wrapped = emptyModel;
        this.itemOverrides = new PatternItemOverrides(
            modelBaker,
            emptyModel,
            craftingModel,
            processingModel,
            stonecutterModel,
            smithingTableModel
        );
    }

    @Override
    public ItemOverrides getOverrides() {
        return itemOverrides;
    }
}
