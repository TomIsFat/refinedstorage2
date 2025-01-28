package com.refinedmods.refinedstorage.common.support;

import com.refinedmods.refinedstorage.api.network.node.SchedulingMode;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;

public class SchedulingModeContainer {
    private static final String TAG_SCHEDULING_MODE = "sm";

    private final Consumer<SchedulingMode> listener;

    private SchedulingModeType type;
    private SchedulingMode schedulingMode;

    public SchedulingModeContainer(final Consumer<SchedulingMode> listener) {
        this.listener = listener;
        this.type = SchedulingModeType.DEFAULT;
        this.schedulingMode = type.createSchedulingMode(
            null,
            tasks -> Collections.shuffle(tasks, new Random()),
            this::notifyListener
        );
        notifyListener();
    }

    public SchedulingModeType getType() {
        return type;
    }

    public void setType(final SchedulingModeType type) {
        setType(null, type);
    }

    private void setType(@Nullable final CompoundTag tag, final SchedulingModeType newType) {
        this.type = newType;
        this.schedulingMode = newType.createSchedulingMode(
            tag,
            tasks -> Collections.shuffle(tasks, new Random()),
            this::notifyListener
        );
        notifyListener();
    }

    private void notifyListener() {
        listener.accept(schedulingMode);
    }

    public void loadFromTag(final CompoundTag tag) {
        if (tag.contains(TAG_SCHEDULING_MODE)) {
            setType(tag, SchedulingModeType.getById(tag.getInt(TAG_SCHEDULING_MODE)));
        }
    }

    public void writeToTag(final CompoundTag tag) {
        tag.putInt(TAG_SCHEDULING_MODE, type.getId());
        type.writeToTag(tag, schedulingMode);
    }

    public void execute(final List<? extends SchedulingMode.ScheduledTask> tasks) {
        schedulingMode.execute(tasks);
    }
}
