package com.refinedmods.refinedstorage.common.networking;

import net.minecraft.nbt.CompoundTag;

public record CableConnections(boolean north, boolean east, boolean south, boolean west, boolean up, boolean down) {
    public static final CableConnections NONE = new CableConnections(false, false, false, false, false, false);

    public static CableConnections fromTag(final CompoundTag tag) {
        return new CableConnections(
            tag.getBoolean("North"),
            tag.getBoolean("East"),
            tag.getBoolean("South"),
            tag.getBoolean("West"),
            tag.getBoolean("Up"),
            tag.getBoolean("Down")
        );
    }

    public CompoundTag writeToTag(final CompoundTag tag) {
        tag.putBoolean("North", north);
        tag.putBoolean("East", east);
        tag.putBoolean("South", south);
        tag.putBoolean("West", west);
        tag.putBoolean("Up", up);
        tag.putBoolean("Down", down);
        return tag;
    }
}
