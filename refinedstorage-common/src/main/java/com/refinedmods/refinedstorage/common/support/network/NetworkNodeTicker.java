package com.refinedmods.refinedstorage.common.support.network;

import com.refinedmods.refinedstorage.api.network.impl.node.AbstractNetworkNode;

@FunctionalInterface
public interface NetworkNodeTicker {
    NetworkNodeTicker IMMEDIATE = AbstractNetworkNode::doWork;

    void tick(AbstractNetworkNode networkNode);
}
