package com.refinedmods.refinedstorage.api.network.impl.node.exporter;

import com.refinedmods.refinedstorage.api.network.impl.node.AbstractNetworkNode;
import com.refinedmods.refinedstorage.api.network.node.NetworkNodeActor;
import com.refinedmods.refinedstorage.api.network.node.SchedulingMode;
import com.refinedmods.refinedstorage.api.network.node.exporter.ExporterTransferStrategy;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

public class ExporterNetworkNode extends AbstractNetworkNode {
    private long energyUsage;
    private final Actor actor = new NetworkNodeActor(this);
    private final List<ExporterTask> tasks = new ArrayList<>();
    @Nullable
    private ExporterTransferStrategy transferStrategy;
    @Nullable
    private SchedulingMode schedulingMode;

    public ExporterNetworkNode(final long energyUsage) {
        this.energyUsage = energyUsage;
    }

    public void setTransferStrategy(@Nullable final ExporterTransferStrategy transferStrategy) {
        this.transferStrategy = transferStrategy;
    }

    public void setSchedulingMode(@Nullable final SchedulingMode schedulingMode) {
        this.schedulingMode = schedulingMode;
    }

    @Override
    public void doWork() {
        super.doWork();
        if (network == null || !isActive() || schedulingMode == null) {
            return;
        }
        schedulingMode.execute(tasks);
    }

    public void setFilters(final List<ResourceKey> filters) {
        tasks.clear();
        tasks.addAll(filters.stream().map(ExporterTask::new).toList());
    }

    public void setEnergyUsage(final long energyUsage) {
        this.energyUsage = energyUsage;
    }

    @Override
    public long getEnergyUsage() {
        return energyUsage;
    }

    class ExporterTask implements SchedulingMode.ScheduledTask {
        private final ResourceKey filter;

        ExporterTask(final ResourceKey filter) {
            this.filter = filter;
        }

        @Override
        public boolean run() {
            if (transferStrategy == null || network == null) {
                return false;
            }
            return transferStrategy.transfer(filter, actor, network);
        }
    }
}
