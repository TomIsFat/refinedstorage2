package com.refinedmods.refinedstorage.common.api.support.resource;

import com.refinedmods.refinedstorage.api.grid.operations.GridOperations;
import com.refinedmods.refinedstorage.api.grid.view.GridResource;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.root.RootStorage;

import java.util.Optional;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.3.4")
public interface ResourceType {
    MapCodec<PlatformResourceKey> getMapCodec();

    StreamCodec<RegistryFriendlyByteBuf, PlatformResourceKey> getStreamCodec();

    MutableComponent getTitle();

    ResourceLocation getSprite();

    long normalizeAmount(double amount);

    double getDisplayAmount(long amount);

    Optional<GridResource> toGridResource(ResourceKey resource, boolean autocraftable);

    long getInterfaceExportLimit();

    GridOperations createGridOperations(RootStorage rootStorage, Actor actor);
}
