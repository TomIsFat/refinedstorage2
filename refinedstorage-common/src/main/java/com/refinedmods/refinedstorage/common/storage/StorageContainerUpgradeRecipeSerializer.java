package com.refinedmods.refinedstorage.common.storage;

import java.util.function.Function;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.crafting.RecipeSerializer;

import static com.refinedmods.refinedstorage.common.util.PlatformUtil.enumStreamCodec;

public class StorageContainerUpgradeRecipeSerializer<T extends Enum<T> & StorageVariant & StringRepresentable>
    implements RecipeSerializer<StorageContainerUpgradeRecipe<T>> {
    private final MapCodec<StorageContainerUpgradeRecipe<T>> codec;
    private final StreamCodec<RegistryFriendlyByteBuf, StorageContainerUpgradeRecipe<T>> streamCodec;

    public StorageContainerUpgradeRecipeSerializer(
        final T[] variants,
        final Function<T, StorageContainerUpgradeRecipe<T>> instanceFactory
    ) {
        this.codec = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                StringRepresentable.fromValues(() -> variants).fieldOf("to")
                    .forGetter(StorageContainerUpgradeRecipe::getTo)
            ).apply(instance, instanceFactory)
        );
        this.streamCodec = StreamCodec.composite(
            enumStreamCodec(variants), StorageContainerUpgradeRecipe::getTo,
            instanceFactory
        );
    }

    @Override
    public MapCodec<StorageContainerUpgradeRecipe<T>> codec() {
        return codec;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, StorageContainerUpgradeRecipe<T>> streamCodec() {
        return streamCodec;
    }
}
