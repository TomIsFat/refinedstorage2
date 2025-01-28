package com.refinedmods.refinedstorage.common.storage;

import com.refinedmods.refinedstorage.api.resource.filter.FilterMode;
import com.refinedmods.refinedstorage.api.storage.AccessMode;
import com.refinedmods.refinedstorage.common.support.RedstoneMode;
import com.refinedmods.refinedstorage.common.support.containermenu.AbstractResourceContainerMenu;
import com.refinedmods.refinedstorage.common.support.containermenu.ClientProperty;
import com.refinedmods.refinedstorage.common.support.containermenu.PropertyTypes;
import com.refinedmods.refinedstorage.common.support.containermenu.ServerProperty;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;

public abstract class AbstractStorageContainerMenu extends AbstractResourceContainerMenu {
    protected AbstractStorageContainerMenu(final MenuType<?> type, final int syncId) {
        super(type, syncId);
        registerProperty(new ClientProperty<>(StoragePropertyTypes.INSERT_PRIORITY, 0));
        registerProperty(new ClientProperty<>(StoragePropertyTypes.EXTRACT_PRIORITY, 0));
        registerProperty(new ClientProperty<>(PropertyTypes.FILTER_MODE, FilterMode.BLOCK));
        registerProperty(new ClientProperty<>(PropertyTypes.FUZZY_MODE, false));
        registerProperty(new ClientProperty<>(StoragePropertyTypes.ACCESS_MODE, AccessMode.INSERT_EXTRACT));
        registerProperty(new ClientProperty<>(StoragePropertyTypes.VOID_EXCESS, false));
        registerProperty(new ClientProperty<>(PropertyTypes.REDSTONE_MODE, RedstoneMode.IGNORE));
    }

    protected AbstractStorageContainerMenu(final MenuType<?> type,
                                           final int syncId,
                                           final Player player,
                                           final StorageConfigurationContainer configContainer) {
        super(type, syncId, player);
        registerProperty(new ServerProperty<>(
            StoragePropertyTypes.INSERT_PRIORITY,
            configContainer::getInsertPriority,
            configContainer::setInsertPriority
        ));
        registerProperty(new ServerProperty<>(
            StoragePropertyTypes.EXTRACT_PRIORITY,
            configContainer::getExtractPriority,
            configContainer::setExtractPriority
        ));
        registerProperty(new ServerProperty<>(
            PropertyTypes.FILTER_MODE,
            configContainer::getFilterMode,
            configContainer::setFilterMode
        ));
        registerProperty(new ServerProperty<>(
            PropertyTypes.FUZZY_MODE,
            configContainer::isFuzzyMode,
            configContainer::setFuzzyMode
        ));
        registerProperty(new ServerProperty<>(
            StoragePropertyTypes.ACCESS_MODE,
            configContainer::getAccessMode,
            configContainer::setAccessMode
        ));
        registerProperty(new ServerProperty<>(
            StoragePropertyTypes.VOID_EXCESS,
            configContainer::isVoidExcess,
            configContainer::setVoidExcess
        ));
        registerProperty(new ServerProperty<>(
            PropertyTypes.REDSTONE_MODE,
            configContainer::getRedstoneMode,
            configContainer::setRedstoneMode
        ));
    }

    boolean shouldDisplayFilterModeWarning() {
        return getProperty(PropertyTypes.FILTER_MODE).getValue() == FilterMode.ALLOW && areAllResourceSlotsEmpty();
    }

    boolean shouldDisplayVoidExcessModeWarning() {
        return Boolean.TRUE.equals(getProperty(StoragePropertyTypes.VOID_EXCESS).getValue())
            && getProperty(PropertyTypes.FILTER_MODE).getValue() != FilterMode.ALLOW;
    }
}
