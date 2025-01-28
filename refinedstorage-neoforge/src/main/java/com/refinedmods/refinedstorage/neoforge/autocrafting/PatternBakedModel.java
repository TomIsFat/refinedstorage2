package com.refinedmods.refinedstorage.neoforge.autocrafting;

import com.refinedmods.refinedstorage.common.autocrafting.PatternItemOverrides;

import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.neoforged.neoforge.client.model.BakedModelWrapper;

class PatternBakedModel extends BakedModelWrapper<BakedModel> {
    private final ItemOverrides itemOverrides;

    PatternBakedModel(final ModelBaker modelBaker,
                      final BakedModel emptyModel,
                      final BakedModel craftingModel,
                      final BakedModel processingModel,
                      final BakedModel stonecutterModel,
                      final BakedModel smithingTableModel) {
        super(emptyModel);
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
