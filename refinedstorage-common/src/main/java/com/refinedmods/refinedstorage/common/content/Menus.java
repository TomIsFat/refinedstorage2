package com.refinedmods.refinedstorage.common.content;

import com.refinedmods.refinedstorage.common.autocrafting.PatternGridContainerMenu;
import com.refinedmods.refinedstorage.common.constructordestructor.ConstructorContainerMenu;
import com.refinedmods.refinedstorage.common.constructordestructor.DestructorContainerMenu;
import com.refinedmods.refinedstorage.common.controller.ControllerContainerMenu;
import com.refinedmods.refinedstorage.common.detector.DetectorContainerMenu;
import com.refinedmods.refinedstorage.common.exporter.ExporterContainerMenu;
import com.refinedmods.refinedstorage.common.grid.CraftingGridContainerMenu;
import com.refinedmods.refinedstorage.common.grid.GridContainerMenu;
import com.refinedmods.refinedstorage.common.grid.WirelessGridContainerMenu;
import com.refinedmods.refinedstorage.common.iface.InterfaceContainerMenu;
import com.refinedmods.refinedstorage.common.importer.ImporterContainerMenu;
import com.refinedmods.refinedstorage.common.networking.NetworkTransmitterContainerMenu;
import com.refinedmods.refinedstorage.common.networking.RelayContainerMenu;
import com.refinedmods.refinedstorage.common.security.FallbackSecurityCardContainerMenu;
import com.refinedmods.refinedstorage.common.security.SecurityCardContainerMenu;
import com.refinedmods.refinedstorage.common.security.SecurityManagerContainerMenu;
import com.refinedmods.refinedstorage.common.storage.diskdrive.DiskDriveContainerMenu;
import com.refinedmods.refinedstorage.common.storage.diskinterface.DiskInterfaceContainerMenu;
import com.refinedmods.refinedstorage.common.storage.externalstorage.ExternalStorageContainerMenu;
import com.refinedmods.refinedstorage.common.storage.portablegrid.PortableGridBlockContainerMenu;
import com.refinedmods.refinedstorage.common.storage.portablegrid.PortableGridItemContainerMenu;
import com.refinedmods.refinedstorage.common.storage.storageblock.FluidStorageBlockContainerMenu;
import com.refinedmods.refinedstorage.common.storage.storageblock.ItemStorageBlockContainerMenu;
import com.refinedmods.refinedstorage.common.storagemonitor.StorageMonitorContainerMenu;
import com.refinedmods.refinedstorage.common.upgrade.RegulatorUpgradeContainerMenu;
import com.refinedmods.refinedstorage.common.wirelesstransmitter.WirelessTransmitterContainerMenu;

import java.util.function.Supplier;
import javax.annotation.Nullable;

import net.minecraft.world.inventory.MenuType;

import static java.util.Objects.requireNonNull;

public final class Menus {
    public static final Menus INSTANCE = new Menus();

    @Nullable
    private Supplier<MenuType<DiskDriveContainerMenu>> diskDrive;
    @Nullable
    private Supplier<MenuType<GridContainerMenu>> grid;
    @Nullable
    private Supplier<MenuType<CraftingGridContainerMenu>> craftingGrid;
    @Nullable
    private Supplier<MenuType<PatternGridContainerMenu>> patternGrid;
    @Nullable
    private Supplier<MenuType<WirelessGridContainerMenu>> wirelessGrid;
    @Nullable
    private Supplier<MenuType<ControllerContainerMenu>> controller;
    @Nullable
    private Supplier<MenuType<ItemStorageBlockContainerMenu>> itemStorage;
    @Nullable
    private Supplier<MenuType<FluidStorageBlockContainerMenu>> fluidStorage;
    @Nullable
    private Supplier<MenuType<ImporterContainerMenu>> importer;
    @Nullable
    private Supplier<MenuType<ExporterContainerMenu>> exporter;
    @Nullable
    private Supplier<MenuType<InterfaceContainerMenu>> iface;
    @Nullable
    private Supplier<MenuType<ExternalStorageContainerMenu>> externalStorage;
    @Nullable
    private Supplier<MenuType<DetectorContainerMenu>> detector;
    @Nullable
    private Supplier<MenuType<DestructorContainerMenu>> destructor;
    @Nullable
    private Supplier<MenuType<ConstructorContainerMenu>> constructor;
    @Nullable
    private Supplier<MenuType<RegulatorUpgradeContainerMenu>> regulatorUpgrade;
    @Nullable
    private Supplier<MenuType<WirelessTransmitterContainerMenu>> wirelessTransmitter;
    @Nullable
    private Supplier<MenuType<StorageMonitorContainerMenu>> storageMonitor;
    @Nullable
    private Supplier<MenuType<NetworkTransmitterContainerMenu>> networkTransmitter;
    @Nullable
    private Supplier<MenuType<PortableGridBlockContainerMenu>> portableGridBlock;
    @Nullable
    private Supplier<MenuType<PortableGridItemContainerMenu>> portableGridItem;
    @Nullable
    private Supplier<MenuType<SecurityCardContainerMenu>> securityCard;
    @Nullable
    private Supplier<MenuType<FallbackSecurityCardContainerMenu>> fallbackSecurityCard;
    @Nullable
    private Supplier<MenuType<SecurityManagerContainerMenu>> securityManager;
    @Nullable
    private Supplier<MenuType<RelayContainerMenu>> relay;
    @Nullable
    private Supplier<MenuType<DiskInterfaceContainerMenu>> diskInterface;

    private Menus() {
    }

    public MenuType<DiskDriveContainerMenu> getDiskDrive() {
        return requireNonNull(diskDrive).get();
    }

    public MenuType<GridContainerMenu> getGrid() {
        return requireNonNull(grid).get();
    }

    public MenuType<CraftingGridContainerMenu> getCraftingGrid() {
        return requireNonNull(craftingGrid).get();
    }

    public void setCraftingGrid(final Supplier<MenuType<CraftingGridContainerMenu>> supplier) {
        this.craftingGrid = supplier;
    }

    public MenuType<PatternGridContainerMenu> getPatternGrid() {
        return requireNonNull(patternGrid).get();
    }

    public void setPatternGrid(final Supplier<MenuType<PatternGridContainerMenu>> supplier) {
        this.patternGrid = supplier;
    }

    public MenuType<WirelessGridContainerMenu> getWirelessGrid() {
        return requireNonNull(wirelessGrid).get();
    }

    public void setWirelessGrid(final Supplier<MenuType<WirelessGridContainerMenu>> supplier) {
        this.wirelessGrid = supplier;
    }

    public MenuType<ControllerContainerMenu> getController() {
        return requireNonNull(controller).get();
    }

    public void setDiskDrive(final Supplier<MenuType<DiskDriveContainerMenu>> supplier) {
        this.diskDrive = supplier;
    }

    public void setGrid(final Supplier<MenuType<GridContainerMenu>> supplier) {
        this.grid = supplier;
    }

    public void setController(final Supplier<MenuType<ControllerContainerMenu>> supplier) {
        this.controller = supplier;
    }

    public MenuType<ItemStorageBlockContainerMenu> getItemStorage() {
        return requireNonNull(itemStorage).get();
    }

    public void setItemStorage(final Supplier<MenuType<ItemStorageBlockContainerMenu>> supplier) {
        this.itemStorage = supplier;
    }

    public MenuType<FluidStorageBlockContainerMenu> getFluidStorage() {
        return requireNonNull(fluidStorage).get();
    }

    public void setFluidStorage(final Supplier<MenuType<FluidStorageBlockContainerMenu>> supplier) {
        this.fluidStorage = supplier;
    }

    public MenuType<ImporterContainerMenu> getImporter() {
        return requireNonNull(importer).get();
    }

    public void setImporter(final Supplier<MenuType<ImporterContainerMenu>> supplier) {
        this.importer = supplier;
    }

    public MenuType<ExporterContainerMenu> getExporter() {
        return requireNonNull(exporter).get();
    }

    public void setExporter(final Supplier<MenuType<ExporterContainerMenu>> supplier) {
        this.exporter = supplier;
    }

    public MenuType<InterfaceContainerMenu> getInterface() {
        return requireNonNull(iface).get();
    }

    public void setInterface(final Supplier<MenuType<InterfaceContainerMenu>> supplier) {
        this.iface = supplier;
    }

    public MenuType<ExternalStorageContainerMenu> getExternalStorage() {
        return requireNonNull(externalStorage).get();
    }

    public void setExternalStorage(final Supplier<MenuType<ExternalStorageContainerMenu>> supplier) {
        this.externalStorage = supplier;
    }

    public MenuType<DetectorContainerMenu> getDetector() {
        return requireNonNull(detector).get();
    }

    public void setDetector(final Supplier<MenuType<DetectorContainerMenu>> supplier) {
        this.detector = supplier;
    }

    public MenuType<DestructorContainerMenu> getDestructor() {
        return requireNonNull(destructor).get();
    }

    public void setDestructor(final Supplier<MenuType<DestructorContainerMenu>> supplier) {
        this.destructor = supplier;
    }

    public MenuType<ConstructorContainerMenu> getConstructor() {
        return requireNonNull(constructor).get();
    }

    public void setConstructor(final Supplier<MenuType<ConstructorContainerMenu>> supplier) {
        this.constructor = supplier;
    }

    public MenuType<RegulatorUpgradeContainerMenu> getRegulatorUpgrade() {
        return requireNonNull(regulatorUpgrade).get();
    }

    public void setRegulatorUpgrade(final Supplier<MenuType<RegulatorUpgradeContainerMenu>> supplier) {
        this.regulatorUpgrade = supplier;
    }

    public MenuType<WirelessTransmitterContainerMenu> getWirelessTransmitter() {
        return requireNonNull(wirelessTransmitter).get();
    }

    public void setWirelessTransmitter(final Supplier<MenuType<WirelessTransmitterContainerMenu>> supplier) {
        this.wirelessTransmitter = supplier;
    }

    public MenuType<StorageMonitorContainerMenu> getStorageMonitor() {
        return requireNonNull(storageMonitor).get();
    }

    public void setStorageMonitor(final Supplier<MenuType<StorageMonitorContainerMenu>> supplier) {
        this.storageMonitor = supplier;
    }

    public MenuType<NetworkTransmitterContainerMenu> getNetworkTransmitter() {
        return requireNonNull(networkTransmitter).get();
    }

    public void setNetworkTransmitter(final Supplier<MenuType<NetworkTransmitterContainerMenu>> supplier) {
        this.networkTransmitter = supplier;
    }

    public MenuType<PortableGridBlockContainerMenu> getPortableGridBlock() {
        return requireNonNull(portableGridBlock).get();
    }

    public void setPortableGridBlock(final Supplier<MenuType<PortableGridBlockContainerMenu>> portableGridBlock) {
        this.portableGridBlock = portableGridBlock;
    }

    public MenuType<PortableGridItemContainerMenu> getPortableGridItem() {
        return requireNonNull(portableGridItem).get();
    }

    public void setPortableGridItem(final Supplier<MenuType<PortableGridItemContainerMenu>> portableGridItem) {
        this.portableGridItem = portableGridItem;
    }

    public MenuType<SecurityCardContainerMenu> getSecurityCard() {
        return requireNonNull(securityCard).get();
    }

    public void setSecurityCard(final Supplier<MenuType<SecurityCardContainerMenu>> securityCard) {
        this.securityCard = securityCard;
    }

    public MenuType<FallbackSecurityCardContainerMenu> getFallbackSecurityCard() {
        return requireNonNull(fallbackSecurityCard).get();
    }

    public void setFallbackSecurityCard(
        final Supplier<MenuType<FallbackSecurityCardContainerMenu>> fallbackSecurityCard
    ) {
        this.fallbackSecurityCard = fallbackSecurityCard;
    }

    public MenuType<SecurityManagerContainerMenu> getSecurityManager() {
        return requireNonNull(securityManager).get();
    }

    public void setSecurityManager(final Supplier<MenuType<SecurityManagerContainerMenu>> securityManager) {
        this.securityManager = securityManager;
    }

    public MenuType<RelayContainerMenu> getRelay() {
        return requireNonNull(relay).get();
    }

    public void setRelay(final Supplier<MenuType<RelayContainerMenu>> relay) {
        this.relay = relay;
    }

    public MenuType<DiskInterfaceContainerMenu> getDiskInterface() {
        return requireNonNull(diskInterface).get();
    }

    public void setDiskInterface(final Supplier<MenuType<DiskInterfaceContainerMenu>> diskInterface) {
        this.diskInterface = diskInterface;
    }
}
