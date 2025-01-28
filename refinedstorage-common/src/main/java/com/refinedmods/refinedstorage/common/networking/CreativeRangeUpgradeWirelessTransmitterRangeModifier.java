package com.refinedmods.refinedstorage.common.networking;

import com.refinedmods.refinedstorage.common.api.upgrade.UpgradeState;
import com.refinedmods.refinedstorage.common.api.wirelesstransmitter.WirelessTransmitterRangeModifier;
import com.refinedmods.refinedstorage.common.content.Items;

public class CreativeRangeUpgradeWirelessTransmitterRangeModifier implements WirelessTransmitterRangeModifier {
    @Override
    public int modifyRange(final UpgradeState upgradeState, final int range) {
        if (upgradeState.has(Items.INSTANCE.getCreativeRangeUpgrade())) {
            return Integer.MAX_VALUE;
        }
        return range;
    }

    @Override
    public int getPriority() {
        return Integer.MAX_VALUE;
    }
}
