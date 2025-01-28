package com.refinedmods.refinedstorage.common.networking;

import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.upgrade.UpgradeState;
import com.refinedmods.refinedstorage.common.api.wirelesstransmitter.WirelessTransmitterRangeModifier;
import com.refinedmods.refinedstorage.common.content.Items;

public class RangeUpgradeWirelessTransmitterRangeModifier implements WirelessTransmitterRangeModifier {
    @Override
    public int modifyRange(final UpgradeState upgradeState, final int range) {
        final int amountOfRangeUpgrades = upgradeState.getAmount(Items.INSTANCE.getRangeUpgrade());
        final int rangePerUpgrade = Platform.INSTANCE.getConfig().getUpgrade().getRangeUpgradeRange();
        return range + (amountOfRangeUpgrades * rangePerUpgrade);
    }
}
