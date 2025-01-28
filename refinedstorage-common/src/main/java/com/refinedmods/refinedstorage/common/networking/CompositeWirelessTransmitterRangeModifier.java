package com.refinedmods.refinedstorage.common.networking;

import com.refinedmods.refinedstorage.common.api.upgrade.UpgradeState;
import com.refinedmods.refinedstorage.common.api.wirelesstransmitter.WirelessTransmitterRangeModifier;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

public class CompositeWirelessTransmitterRangeModifier implements WirelessTransmitterRangeModifier {
    private final Queue<WirelessTransmitterRangeModifier> modifiers = new PriorityQueue<>(
        Comparator.comparingInt(WirelessTransmitterRangeModifier::getPriority)
    );

    public void addModifier(final WirelessTransmitterRangeModifier rangeModifier) {
        modifiers.add(rangeModifier);
    }

    @Override
    public int modifyRange(final UpgradeState upgradeState, final int range) {
        int result = range;
        for (final WirelessTransmitterRangeModifier rangeModifier : modifiers) {
            result = rangeModifier.modifyRange(upgradeState, result);
        }
        return result;
    }
}
