package com.refinedmods.refinedstorage.common.networking;

import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.upgrade.UpgradeState;
import com.refinedmods.refinedstorage.common.api.wirelesstransmitter.WirelessTransmitterRangeModifier;

public class BaseWirelessTransmitterRangeModifier implements WirelessTransmitterRangeModifier {
    @Override
    public int modifyRange(final UpgradeState upgradeState, final int range) {
        return Platform.INSTANCE.getConfig().getWirelessTransmitter().getBaseRange() + range;
    }
}
