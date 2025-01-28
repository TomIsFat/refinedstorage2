package com.refinedmods.refinedstorage.common.support;

import java.util.UUID;

@FunctionalInterface
public interface PlayerAwareBlockEntity {
    void setPlacedBy(UUID playerId);
}
