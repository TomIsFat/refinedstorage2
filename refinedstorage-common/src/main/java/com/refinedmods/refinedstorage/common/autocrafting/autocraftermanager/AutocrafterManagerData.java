package com.refinedmods.refinedstorage.common.autocrafting.autocraftermanager;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record AutocrafterManagerData(List<Group> groups, boolean active) {
    private static final StreamCodec<RegistryFriendlyByteBuf, SubGroup> SUB_GROUP_STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT, SubGroup::slotCount,
        ByteBufCodecs.BOOL, SubGroup::visibleToTheAutocrafterManager,
        ByteBufCodecs.BOOL, SubGroup::full,
        SubGroup::new
    );
    private static final StreamCodec<RegistryFriendlyByteBuf, Group> GROUP_STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, Group::name,
        ByteBufCodecs.collection(ArrayList::new, SUB_GROUP_STREAM_CODEC), Group::subGroups,
        Group::new
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, AutocrafterManagerData> STREAM_CODEC = StreamCodec
        .composite(
            ByteBufCodecs.collection(ArrayList::new, GROUP_STREAM_CODEC), AutocrafterManagerData::groups,
            ByteBufCodecs.BOOL, AutocrafterManagerData::active,
            AutocrafterManagerData::new
        );

    public record Group(String name, List<SubGroup> subGroups) {
        static Group of(final AutocrafterManagerBlockEntity.Group group) {
            return new Group(group.name(), group.subGroups().stream().map(SubGroup::of).toList());
        }
    }

    public record SubGroup(int slotCount, boolean visibleToTheAutocrafterManager, boolean full) {
        private static SubGroup of(final AutocrafterManagerBlockEntity.SubGroup subGroup) {
            return new SubGroup(
                subGroup.container().getContainerSize(),
                subGroup.visibleToTheAutocrafterManager(),
                subGroup.full()
            );
        }
    }
}
