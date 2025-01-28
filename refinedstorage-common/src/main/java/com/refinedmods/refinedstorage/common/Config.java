package com.refinedmods.refinedstorage.common;

import com.refinedmods.refinedstorage.api.grid.view.GridSortingDirection;
import com.refinedmods.refinedstorage.common.autocrafting.autocraftermanager.AutocrafterManagerSearchMode;
import com.refinedmods.refinedstorage.common.autocrafting.autocraftermanager.AutocrafterManagerViewType;
import com.refinedmods.refinedstorage.common.grid.CraftingGridMatrixCloseBehavior;
import com.refinedmods.refinedstorage.common.grid.GridSortingTypes;
import com.refinedmods.refinedstorage.common.grid.GridViewType;
import com.refinedmods.refinedstorage.common.support.stretching.ScreenSize;

import java.util.Optional;

import net.minecraft.resources.ResourceLocation;

public interface Config {
    ScreenSize getScreenSize();

    void setScreenSize(ScreenSize size);

    boolean isSmoothScrolling();

    boolean isSearchBoxAutoSelected();

    void setSearchBoxAutoSelected(boolean searchBoxAutoSelected);

    int getMaxRowsStretch();

    boolean isAutocraftingNotification();

    void setAutocraftingNotification(boolean autocraftingNotification);

    GridEntry getGrid();

    SimpleEnergyUsageEntry getPatternGrid();

    CraftingGridEntry getCraftingGrid();

    ControllerEntry getController();

    DiskDriveEntry getDiskDrive();

    DiskInterfaceEntry getDiskInterface();

    SimpleEnergyUsageEntry getCable();

    StorageBlockEntry getStorageBlock();

    FluidStorageBlockEntry getFluidStorageBlock();

    SimpleEnergyUsageEntry getImporter();

    SimpleEnergyUsageEntry getExporter();

    UpgradeEntry getUpgrade();

    SimpleEnergyUsageEntry getInterface();

    SimpleEnergyUsageEntry getExternalStorage();

    SimpleEnergyUsageEntry getDetector();

    SimpleEnergyUsageEntry getDestructor();

    SimpleEnergyUsageEntry getConstructor();

    WirelessGridEntry getWirelessGrid();

    WirelessTransmitterEntry getWirelessTransmitter();

    SimpleEnergyUsageEntry getStorageMonitor();

    SimpleEnergyUsageEntry getNetworkReceiver();

    SimpleEnergyUsageEntry getNetworkTransmitter();

    PortableGridEntry getPortableGrid();

    SimpleEnergyUsageEntry getSecurityCard();

    SimpleEnergyUsageEntry getFallbackSecurityCard();

    SimpleEnergyUsageEntry getSecurityManager();

    RelayEntry getRelay();

    AutocrafterEntry getAutocrafter();

    AutocrafterManagerEntry getAutocrafterManager();

    SimpleEnergyUsageEntry getAutocraftingMonitor();

    WirelessAutocraftingMonitorEntry getWirelessAutocraftingMonitor();

    interface SimpleEnergyUsageEntry {
        long getEnergyUsage();
    }

    interface GridEntry extends SimpleEnergyUsageEntry {
        boolean isLargeFont();

        boolean isPreventSortingWhileShiftIsDown();

        boolean isDetailedTooltip();

        boolean isRememberSearchQuery();

        Optional<ResourceLocation> getSynchronizer();

        void setSynchronizer(ResourceLocation synchronizerId);

        void clearSynchronizer();

        GridSortingDirection getSortingDirection();

        void setSortingDirection(GridSortingDirection sortingDirection);

        GridSortingTypes getSortingType();

        void setSortingType(GridSortingTypes sortingType);

        GridViewType getViewType();

        void setViewType(GridViewType viewType);

        Optional<ResourceLocation> getResourceType();

        void setResourceType(ResourceLocation resourceTypeId);

        void clearResourceType();
    }

    interface CraftingGridEntry extends SimpleEnergyUsageEntry {
        CraftingGridMatrixCloseBehavior getCraftingMatrixCloseBehavior();
    }

    interface DiskDriveEntry extends SimpleEnergyUsageEntry {
        long getEnergyUsagePerDisk();
    }

    interface DiskInterfaceEntry extends SimpleEnergyUsageEntry {
        long getEnergyUsagePerDisk();
    }

    interface ControllerEntry {
        long getEnergyCapacity();
    }

    interface StorageBlockEntry {
        long get1kEnergyUsage();

        long get4kEnergyUsage();

        long get16kEnergyUsage();

        long get64kEnergyUsage();

        long getCreativeEnergyUsage();
    }

    interface FluidStorageBlockEntry {
        long get64bEnergyUsage();

        long get256bEnergyUsage();

        long get1024bEnergyUsage();

        long get4096bEnergyUsage();

        long getCreativeEnergyUsage();
    }

    interface UpgradeEntry {
        long getSpeedUpgradeEnergyUsage();

        long getStackUpgradeEnergyUsage();

        long getFortune1UpgradeEnergyUsage();

        long getFortune2UpgradeEnergyUsage();

        long getFortune3UpgradeEnergyUsage();

        long getSilkTouchUpgradeEnergyUsage();

        long getRegulatorUpgradeEnergyUsage();

        long getRangeUpgradeEnergyUsage();

        long getCreativeRangeUpgradeEnergyUsage();

        int getRangeUpgradeRange();
    }

    interface WirelessGridEntry {
        long getEnergyCapacity();

        long getOpenEnergyUsage();

        long getInsertEnergyUsage();

        long getExtractEnergyUsage();
    }

    interface WirelessTransmitterEntry extends SimpleEnergyUsageEntry {
        int getBaseRange();
    }

    interface PortableGridEntry {
        long getEnergyCapacity();

        long getOpenEnergyUsage();

        long getInsertEnergyUsage();

        long getExtractEnergyUsage();
    }

    interface RelayEntry {
        long getInputNetworkEnergyUsage();

        long getOutputNetworkEnergyUsage();
    }

    interface AutocrafterEntry extends SimpleEnergyUsageEntry {
        long getEnergyUsagePerPattern();
    }

    interface AutocrafterManagerEntry extends SimpleEnergyUsageEntry {
        void setSearchMode(AutocrafterManagerSearchMode searchMode);

        AutocrafterManagerSearchMode getSearchMode();

        void setViewType(AutocrafterManagerViewType viewType);

        AutocrafterManagerViewType getViewType();
    }

    interface WirelessAutocraftingMonitorEntry {
        long getEnergyCapacity();

        long getOpenEnergyUsage();

        long getCancelEnergyUsage();

        long getCancelAllEnergyUsage();
    }
}
