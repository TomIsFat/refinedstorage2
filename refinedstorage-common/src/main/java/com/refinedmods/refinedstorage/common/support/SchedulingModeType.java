package com.refinedmods.refinedstorage.common.support;

import com.refinedmods.refinedstorage.api.network.impl.node.task.DefaultSchedulingMode;
import com.refinedmods.refinedstorage.api.network.impl.node.task.RandomSchedulingMode;
import com.refinedmods.refinedstorage.api.network.impl.node.task.RoundRobinSchedulingMode;
import com.refinedmods.refinedstorage.api.network.node.SchedulingMode;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

public enum SchedulingModeType {
    DEFAULT(0, "default"),
    ROUND_ROBIN(1, "round_robin"),
    RANDOM(2, "random");

    private static final String TAG_ROUND_ROBIN_INDEX = "rri";

    private final int id;
    private final MutableComponent name;
    private final Component help;

    SchedulingModeType(final int id, final String name) {
        this.id = id;
        this.name = createTranslation("gui", "scheduling_mode." + name);
        this.help = createTranslation("gui", "scheduling_mode." + name + ".help");
    }

    public static SchedulingModeType getById(final int id) {
        for (final SchedulingModeType mode : values()) {
            if (mode.id == id) {
                return mode;
            }
        }
        return DEFAULT;
    }

    public MutableComponent getName() {
        return name;
    }

    public Component getHelpText() {
        return help;
    }

    public int getId() {
        return id;
    }

    public SchedulingMode createSchedulingMode(@Nullable final CompoundTag tag,
                                               final RandomSchedulingMode.Randomizer randomizer,
                                               final Runnable listener) {
        return switch (this) {
            case DEFAULT -> new DefaultSchedulingMode();
            case RANDOM -> new RandomSchedulingMode(randomizer);
            case ROUND_ROBIN -> createRoundRobinSchedulingMode(tag, listener);
        };
    }

    private RoundRobinSchedulingMode createRoundRobinSchedulingMode(@Nullable final CompoundTag tag,
                                                                    final Runnable listener) {
        final int index = tag != null ? tag.getInt(TAG_ROUND_ROBIN_INDEX) : 0;
        return new RoundRobinSchedulingMode(new RoundRobinSchedulingMode.State(listener, index));
    }

    public void writeToTag(final CompoundTag tag, final SchedulingMode schedulingMode) {
        if (schedulingMode instanceof RoundRobinSchedulingMode roundRobin) {
            tag.putInt(TAG_ROUND_ROBIN_INDEX, roundRobin.getIndex());
        }
    }
}
