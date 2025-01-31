package com.refinedmods.refinedstorage.common;

import com.refinedmods.refinedstorage.api.network.energy.EnergyNetworkComponent;
import com.refinedmods.refinedstorage.api.network.impl.energy.EnergyNetworkComponentImpl;
import com.refinedmods.refinedstorage.api.network.impl.node.GraphNetworkComponentImpl;
import com.refinedmods.refinedstorage.api.network.impl.node.SimpleNetworkNode;
import com.refinedmods.refinedstorage.api.network.impl.security.SecurityNetworkComponentImpl;
import com.refinedmods.refinedstorage.api.network.node.GraphNetworkComponent;
import com.refinedmods.refinedstorage.api.network.security.SecurityNetworkComponent;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApiProxy;
import com.refinedmods.refinedstorage.common.api.security.PlatformSecurityNetworkComponent;
import com.refinedmods.refinedstorage.common.api.upgrade.AbstractUpgradeItem;
import com.refinedmods.refinedstorage.common.autocrafting.CraftingPatternState;
import com.refinedmods.refinedstorage.common.autocrafting.PatternGridBlockEntity;
import com.refinedmods.refinedstorage.common.autocrafting.PatternGridContainerMenu;
import com.refinedmods.refinedstorage.common.autocrafting.PatternGridData;
import com.refinedmods.refinedstorage.common.autocrafting.PatternItem;
import com.refinedmods.refinedstorage.common.autocrafting.PatternState;
import com.refinedmods.refinedstorage.common.autocrafting.ProcessingPatternState;
import com.refinedmods.refinedstorage.common.configurationcard.ConfigurationCardItem;
import com.refinedmods.refinedstorage.common.configurationcard.ConfigurationCardState;
import com.refinedmods.refinedstorage.common.constructordestructor.BlockBreakDestructorStrategyFactory;
import com.refinedmods.refinedstorage.common.constructordestructor.ConstructorBlockEntity;
import com.refinedmods.refinedstorage.common.constructordestructor.ConstructorContainerMenu;
import com.refinedmods.refinedstorage.common.constructordestructor.DestructorBlockEntity;
import com.refinedmods.refinedstorage.common.constructordestructor.DestructorContainerMenu;
import com.refinedmods.refinedstorage.common.constructordestructor.FluidBreakDestructorStrategyFactory;
import com.refinedmods.refinedstorage.common.constructordestructor.ItemDropConstructorStrategyFactory;
import com.refinedmods.refinedstorage.common.constructordestructor.ItemPickupDestructorStrategyFactory;
import com.refinedmods.refinedstorage.common.constructordestructor.PlaceBlockConstructorStrategy;
import com.refinedmods.refinedstorage.common.constructordestructor.PlaceFireworksConstructorStrategy;
import com.refinedmods.refinedstorage.common.constructordestructor.PlaceFluidConstructorStrategy;
import com.refinedmods.refinedstorage.common.content.BlockEntities;
import com.refinedmods.refinedstorage.common.content.BlockEntityTypeFactory;
import com.refinedmods.refinedstorage.common.content.Blocks;
import com.refinedmods.refinedstorage.common.content.ContentIds;
import com.refinedmods.refinedstorage.common.content.DataComponents;
import com.refinedmods.refinedstorage.common.content.ExtendedMenuTypeFactory;
import com.refinedmods.refinedstorage.common.content.Items;
import com.refinedmods.refinedstorage.common.content.LootFunctions;
import com.refinedmods.refinedstorage.common.content.MenuTypeFactory;
import com.refinedmods.refinedstorage.common.content.Menus;
import com.refinedmods.refinedstorage.common.content.RegistryCallback;
import com.refinedmods.refinedstorage.common.content.Sounds;
import com.refinedmods.refinedstorage.common.controller.ControllerBlockEntity;
import com.refinedmods.refinedstorage.common.controller.ControllerContainerMenu;
import com.refinedmods.refinedstorage.common.controller.ControllerData;
import com.refinedmods.refinedstorage.common.controller.ControllerType;
import com.refinedmods.refinedstorage.common.detector.DetectorBlockEntity;
import com.refinedmods.refinedstorage.common.detector.DetectorContainerMenu;
import com.refinedmods.refinedstorage.common.exporter.ExporterBlockEntity;
import com.refinedmods.refinedstorage.common.exporter.ExporterContainerMenu;
import com.refinedmods.refinedstorage.common.grid.CraftingGridBlockEntity;
import com.refinedmods.refinedstorage.common.grid.CraftingGridContainerMenu;
import com.refinedmods.refinedstorage.common.grid.GridBlockEntity;
import com.refinedmods.refinedstorage.common.grid.GridContainerMenu;
import com.refinedmods.refinedstorage.common.grid.GridData;
import com.refinedmods.refinedstorage.common.grid.PortableGridData;
import com.refinedmods.refinedstorage.common.grid.WirelessGridContainerMenu;
import com.refinedmods.refinedstorage.common.grid.WirelessGridData;
import com.refinedmods.refinedstorage.common.iface.InterfaceBlock;
import com.refinedmods.refinedstorage.common.iface.InterfaceBlockEntity;
import com.refinedmods.refinedstorage.common.iface.InterfaceContainerMenu;
import com.refinedmods.refinedstorage.common.iface.InterfaceData;
import com.refinedmods.refinedstorage.common.importer.ImporterBlockEntity;
import com.refinedmods.refinedstorage.common.importer.ImporterContainerMenu;
import com.refinedmods.refinedstorage.common.misc.ProcessorItem;
import com.refinedmods.refinedstorage.common.misc.WrenchItem;
import com.refinedmods.refinedstorage.common.networking.NetworkCardItem;
import com.refinedmods.refinedstorage.common.networking.NetworkReceiverBlockEntity;
import com.refinedmods.refinedstorage.common.networking.NetworkTransmitterBlockEntity;
import com.refinedmods.refinedstorage.common.networking.NetworkTransmitterContainerMenu;
import com.refinedmods.refinedstorage.common.networking.NetworkTransmitterData;
import com.refinedmods.refinedstorage.common.networking.RelayBlockEntity;
import com.refinedmods.refinedstorage.common.networking.RelayContainerMenu;
import com.refinedmods.refinedstorage.common.security.BuiltinPermission;
import com.refinedmods.refinedstorage.common.security.FallbackSecurityCardContainerMenu;
import com.refinedmods.refinedstorage.common.security.PlatformSecurityNetworkComponentImpl;
import com.refinedmods.refinedstorage.common.security.PlayerBoundSecurityCardData;
import com.refinedmods.refinedstorage.common.security.SecurityCardBoundPlayer;
import com.refinedmods.refinedstorage.common.security.SecurityCardContainerMenu;
import com.refinedmods.refinedstorage.common.security.SecurityCardData;
import com.refinedmods.refinedstorage.common.security.SecurityCardPermissions;
import com.refinedmods.refinedstorage.common.security.SecurityManagerBlockEntity;
import com.refinedmods.refinedstorage.common.security.SecurityManagerContainerMenu;
import com.refinedmods.refinedstorage.common.storage.FluidStorageVariant;
import com.refinedmods.refinedstorage.common.storage.ItemStorageVariant;
import com.refinedmods.refinedstorage.common.storage.StorageTypes;
import com.refinedmods.refinedstorage.common.storage.diskdrive.AbstractDiskDriveBlockEntity;
import com.refinedmods.refinedstorage.common.storage.diskdrive.DiskDriveBlock;
import com.refinedmods.refinedstorage.common.storage.diskdrive.DiskDriveContainerMenu;
import com.refinedmods.refinedstorage.common.storage.diskinterface.AbstractDiskInterfaceBlockEntity;
import com.refinedmods.refinedstorage.common.storage.diskinterface.DiskInterfaceContainerMenu;
import com.refinedmods.refinedstorage.common.storage.externalstorage.ExternalStorageBlockEntity;
import com.refinedmods.refinedstorage.common.storage.externalstorage.ExternalStorageContainerMenu;
import com.refinedmods.refinedstorage.common.storage.portablegrid.AbstractPortableGridBlockEntity;
import com.refinedmods.refinedstorage.common.storage.portablegrid.PortableGridBlock;
import com.refinedmods.refinedstorage.common.storage.portablegrid.PortableGridBlockContainerMenu;
import com.refinedmods.refinedstorage.common.storage.portablegrid.PortableGridItemContainerMenu;
import com.refinedmods.refinedstorage.common.storage.portablegrid.PortableGridLootItemFunction;
import com.refinedmods.refinedstorage.common.storage.portablegrid.PortableGridType;
import com.refinedmods.refinedstorage.common.storage.storageblock.FluidStorageBlock;
import com.refinedmods.refinedstorage.common.storage.storageblock.FluidStorageBlockBlockEntity;
import com.refinedmods.refinedstorage.common.storage.storageblock.FluidStorageBlockBlockItem;
import com.refinedmods.refinedstorage.common.storage.storageblock.FluidStorageBlockContainerMenu;
import com.refinedmods.refinedstorage.common.storage.storageblock.ItemStorageBlock;
import com.refinedmods.refinedstorage.common.storage.storageblock.ItemStorageBlockBlockEntity;
import com.refinedmods.refinedstorage.common.storage.storageblock.ItemStorageBlockBlockItem;
import com.refinedmods.refinedstorage.common.storage.storageblock.ItemStorageBlockContainerMenu;
import com.refinedmods.refinedstorage.common.storage.storageblock.StorageBlockData;
import com.refinedmods.refinedstorage.common.storage.storageblock.StorageBlockLootItemFunction;
import com.refinedmods.refinedstorage.common.storage.storagedisk.FluidStorageDiskItem;
import com.refinedmods.refinedstorage.common.storage.storagedisk.ItemStorageDiskItem;
import com.refinedmods.refinedstorage.common.storagemonitor.FluidStorageMonitorExtractionStrategy;
import com.refinedmods.refinedstorage.common.storagemonitor.FluidStorageMonitorInsertionStrategy;
import com.refinedmods.refinedstorage.common.storagemonitor.ItemStorageMonitorExtractionStrategy;
import com.refinedmods.refinedstorage.common.storagemonitor.ItemStorageMonitorInsertionStrategy;
import com.refinedmods.refinedstorage.common.storagemonitor.StorageMonitorBlock;
import com.refinedmods.refinedstorage.common.storagemonitor.StorageMonitorBlockEntity;
import com.refinedmods.refinedstorage.common.storagemonitor.StorageMonitorContainerMenu;
import com.refinedmods.refinedstorage.common.support.BaseBlockItem;
import com.refinedmods.refinedstorage.common.support.SimpleBlock;
import com.refinedmods.refinedstorage.common.support.SimpleItem;
import com.refinedmods.refinedstorage.common.support.containermenu.SingleAmountData;
import com.refinedmods.refinedstorage.common.support.energy.EnergyLootItemFunction;
import com.refinedmods.refinedstorage.common.support.network.BaseNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage.common.support.network.component.PlatformStorageNetworkComponent;
import com.refinedmods.refinedstorage.common.support.resource.FluidResourceContainerInsertStrategy;
import com.refinedmods.refinedstorage.common.support.resource.FluidResourceFactory;
import com.refinedmods.refinedstorage.common.support.resource.ResourceContainerData;
import com.refinedmods.refinedstorage.common.support.resource.ResourceTypes;
import com.refinedmods.refinedstorage.common.support.slotreference.InventorySlotReferenceFactory;
import com.refinedmods.refinedstorage.common.upgrade.FortuneUpgradeItem;
import com.refinedmods.refinedstorage.common.upgrade.RangeUpgradeItem;
import com.refinedmods.refinedstorage.common.upgrade.RegulatorUpgradeContainerMenu;
import com.refinedmods.refinedstorage.common.upgrade.RegulatorUpgradeState;
import com.refinedmods.refinedstorage.common.upgrade.SimpleUpgradeItem;
import com.refinedmods.refinedstorage.common.upgrade.UpgradeDestinations;
import com.refinedmods.refinedstorage.common.upgrade.UpgradeWithEnchantedBookRecipeSerializer;
import com.refinedmods.refinedstorage.common.wirelesstransmitter.BaseWirelessTransmitterRangeModifier;
import com.refinedmods.refinedstorage.common.wirelesstransmitter.CreativeRangeUpgradeWirelessTransmitterRangeModifier;
import com.refinedmods.refinedstorage.common.wirelesstransmitter.RangeUpgradeWirelessTransmitterRangeModifier;
import com.refinedmods.refinedstorage.common.wirelesstransmitter.WirelessTransmitterBlockEntity;
import com.refinedmods.refinedstorage.common.wirelesstransmitter.WirelessTransmitterContainerMenu;
import com.refinedmods.refinedstorage.common.wirelesstransmitter.WirelessTransmitterData;

import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public abstract class AbstractModInitializer {
    private static final String ITEM_REGISTRY_KEY = "item";
    private static final String FLUID_REGISTRY_KEY = "fluid";

    protected final void initializePlatformApi() {
        ((RefinedStorageApiProxy) RefinedStorageApi.INSTANCE).setDelegate(new RefinedStorageApiImpl());
        registerStorageTypes();
        registerResourceTypes();
        registerAdditionalResourceFactories();
        registerResourceContainerInsertStrategies();
        registerDestructorStrategyFactories();
        registerConstructorStrategyFactories();
        registerStorageMonitorInsertionStrategies();
        registerStorageMonitorExtractionStrategies();
        registerNetworkComponents();
        registerWirelessTransmitterRangeModifiers();
        registerPermissions();
        registerInventorySlotReference();
    }

    private void registerStorageTypes() {
        RefinedStorageApi.INSTANCE.getStorageTypeRegistry().register(
            createIdentifier(ITEM_REGISTRY_KEY),
            StorageTypes.ITEM
        );
        RefinedStorageApi.INSTANCE.getStorageTypeRegistry().register(
            createIdentifier(FLUID_REGISTRY_KEY),
            StorageTypes.FLUID
        );
    }

    private void registerResourceTypes() {
        RefinedStorageApi.INSTANCE.getResourceTypeRegistry().register(
            createIdentifier(ITEM_REGISTRY_KEY),
            ResourceTypes.ITEM
        );
        RefinedStorageApi.INSTANCE.getResourceTypeRegistry().register(
            createIdentifier(FLUID_REGISTRY_KEY),
            ResourceTypes.FLUID
        );
    }

    private void registerAdditionalResourceFactories() {
        RefinedStorageApi.INSTANCE.addResourceFactory(new FluidResourceFactory());
    }

    private void registerResourceContainerInsertStrategies() {
        RefinedStorageApi.INSTANCE.addResourceContainerInsertStrategy(new FluidResourceContainerInsertStrategy());
    }

    private void registerDestructorStrategyFactories() {
        RefinedStorageApi.INSTANCE.addDestructorStrategyFactory(new BlockBreakDestructorStrategyFactory());
        RefinedStorageApi.INSTANCE.addDestructorStrategyFactory(new FluidBreakDestructorStrategyFactory());
        RefinedStorageApi.INSTANCE.addDestructorStrategyFactory(new ItemPickupDestructorStrategyFactory());
    }

    private void registerConstructorStrategyFactories() {
        RefinedStorageApi.INSTANCE.addConstructorStrategyFactory((level, pos, direction, upgradeState, dropItems) ->
            Optional.of(new PlaceBlockConstructorStrategy(level, pos, direction)));
        RefinedStorageApi.INSTANCE.addConstructorStrategyFactory((level, pos, direction, upgradeState, dropItems) ->
            Optional.of(new PlaceFireworksConstructorStrategy(level, pos, direction)));
        RefinedStorageApi.INSTANCE.addConstructorStrategyFactory((level, pos, direction, upgradeState, dropItems) ->
            Optional.of(new PlaceFluidConstructorStrategy(level, pos, direction)));
        RefinedStorageApi.INSTANCE.addConstructorStrategyFactory(new ItemDropConstructorStrategyFactory());
    }

    private void registerStorageMonitorInsertionStrategies() {
        RefinedStorageApi.INSTANCE.addStorageMonitorInsertionStrategy(new ItemStorageMonitorInsertionStrategy());
        RefinedStorageApi.INSTANCE.addStorageMonitorInsertionStrategy(new FluidStorageMonitorInsertionStrategy());
    }

    private void registerStorageMonitorExtractionStrategies() {
        RefinedStorageApi.INSTANCE.addStorageMonitorExtractionStrategy(new ItemStorageMonitorExtractionStrategy());
        RefinedStorageApi.INSTANCE.addStorageMonitorExtractionStrategy(new FluidStorageMonitorExtractionStrategy());
    }

    private void registerNetworkComponents() {
        RefinedStorageApi.INSTANCE.getNetworkComponentMapFactory().addFactory(
            EnergyNetworkComponent.class,
            network -> new EnergyNetworkComponentImpl()
        );
        RefinedStorageApi.INSTANCE.getNetworkComponentMapFactory().addFactory(
            GraphNetworkComponent.class,
            GraphNetworkComponentImpl::new
        );
        RefinedStorageApi.INSTANCE.getNetworkComponentMapFactory().addFactory(
            StorageNetworkComponent.class,
            network -> new PlatformStorageNetworkComponent()
        );
        RefinedStorageApi.INSTANCE.getNetworkComponentMapFactory().addFactory(
            PlatformSecurityNetworkComponent.class,
            network -> new PlatformSecurityNetworkComponentImpl(
                RefinedStorageApi.INSTANCE.createDefaultSecurityPolicy()
            )
        );
        RefinedStorageApi.INSTANCE.getNetworkComponentMapFactory().addFactory(
            SecurityNetworkComponent.class,
            network -> new SecurityNetworkComponentImpl(RefinedStorageApi.INSTANCE.createDefaultSecurityPolicy())
        );
    }

    private void registerWirelessTransmitterRangeModifiers() {
        RefinedStorageApi.INSTANCE.addWirelessTransmitterRangeModifier(new BaseWirelessTransmitterRangeModifier());
        RefinedStorageApi.INSTANCE.addWirelessTransmitterRangeModifier(
            new RangeUpgradeWirelessTransmitterRangeModifier()
        );
        RefinedStorageApi.INSTANCE.addWirelessTransmitterRangeModifier(
            new CreativeRangeUpgradeWirelessTransmitterRangeModifier()
        );
    }

    private void registerPermissions() {
        for (final BuiltinPermission permission : BuiltinPermission.values()) {
            RefinedStorageApi.INSTANCE.getPermissionRegistry().register(permission.getId(), permission);
        }
    }

    protected final void registerBlocks(
        final RegistryCallback<Block> callback,
        final BiFunction<BlockPos, BlockState, AbstractDiskDriveBlockEntity> diskDriveBlockEntityFactory,
        final BiFunction<BlockPos, BlockState, AbstractPortableGridBlockEntity> portableGridBlockEntityFactory,
        final BiFunction<BlockPos, BlockState, AbstractPortableGridBlockEntity> creativePortableGridBlockEntityFactory,
        final BiFunction<BlockPos, BlockState, AbstractDiskInterfaceBlockEntity> diskInterfaceBlockEntityFactory
    ) {
        Blocks.INSTANCE.setQuartzEnrichedIronBlock(callback.register(
            ContentIds.QUARTZ_ENRICHED_IRON_BLOCK, SimpleBlock::new));
        Blocks.INSTANCE.setQuartzEnrichedCopperBlock(
            callback.register(ContentIds.QUARTZ_ENRICHED_COPPER_BLOCK, SimpleBlock::new));
        Blocks.INSTANCE.setDiskDrive(
            callback.register(ContentIds.DISK_DRIVE, () -> new DiskDriveBlock(diskDriveBlockEntityFactory))
        );
        Blocks.INSTANCE.setMachineCasing(callback.register(ContentIds.MACHINE_CASING, SimpleBlock::new));
        for (final ItemStorageVariant variant : ItemStorageVariant.values()) {
            Blocks.INSTANCE.setItemStorageBlock(variant, callback.register(
                ContentIds.forItemStorageBlock(variant),
                () -> new ItemStorageBlock(variant)
            ));
        }
        for (final FluidStorageVariant variant : FluidStorageVariant.values()) {
            Blocks.INSTANCE.setFluidStorageBlock(variant, callback.register(
                ContentIds.forFluidStorageBlock(variant),
                () -> new FluidStorageBlock(variant)
            ));
        }
        Blocks.INSTANCE.getController().registerBlocks(callback);
        Blocks.INSTANCE.getCreativeController().registerBlocks(callback);
        Blocks.INSTANCE.getCable().registerBlocks(callback);
        Blocks.INSTANCE.getGrid().registerBlocks(callback);
        Blocks.INSTANCE.getCraftingGrid().registerBlocks(callback);
        Blocks.INSTANCE.getPatternGrid().registerBlocks(callback);
        Blocks.INSTANCE.getDetector().registerBlocks(callback);
        Blocks.INSTANCE.getImporter().registerBlocks(callback);
        Blocks.INSTANCE.getExporter().registerBlocks(callback);
        Blocks.INSTANCE.getExternalStorage().registerBlocks(callback);
        Blocks.INSTANCE.getConstructor().registerBlocks(callback);
        Blocks.INSTANCE.getDestructor().registerBlocks(callback);
        Blocks.INSTANCE.setInterface(callback.register(ContentIds.INTERFACE, InterfaceBlock::new));
        Blocks.INSTANCE.getWirelessTransmitter().registerBlocks(callback);
        Blocks.INSTANCE.setStorageMonitor(callback.register(ContentIds.STORAGE_MONITOR, StorageMonitorBlock::new));
        Blocks.INSTANCE.getNetworkReceiver().registerBlocks(callback);
        Blocks.INSTANCE.getNetworkTransmitter().registerBlocks(callback);
        Blocks.INSTANCE.setPortableGrid(callback.register(ContentIds.PORTABLE_GRID, () -> new PortableGridBlock(
            PortableGridType.NORMAL,
            portableGridBlockEntityFactory
        )));
        Blocks.INSTANCE.setCreativePortableGrid(
            callback.register(ContentIds.CREATIVE_PORTABLE_GRID, () -> new PortableGridBlock(
                PortableGridType.CREATIVE,
                creativePortableGridBlockEntityFactory
            )));
        Blocks.INSTANCE.getSecurityManager().registerBlocks(callback);
        Blocks.INSTANCE.getRelay().registerBlocks(callback);
        Blocks.INSTANCE.setDiskInterface(diskInterfaceBlockEntityFactory).registerBlocks(callback);
    }

    protected final void registerItems(final RegistryCallback<Item> callback) {
        registerSimpleItems(callback);
        Blocks.INSTANCE.getGrid().registerItems(callback);
        Blocks.INSTANCE.getCraftingGrid().registerItems(callback);
        Blocks.INSTANCE.getPatternGrid().registerItems(callback);
        Blocks.INSTANCE.getCable().registerItems(callback, Items.INSTANCE::addCable);
        Blocks.INSTANCE.getController().registerItems(callback, Items.INSTANCE::addController);
        Blocks.INSTANCE.getCreativeController().registerItems(callback, Items.INSTANCE::addCreativeController);
        Blocks.INSTANCE.getDetector().registerItems(callback, Items.INSTANCE::addDetector);
        Blocks.INSTANCE.getImporter().registerItems(callback, Items.INSTANCE::addImporter);
        Blocks.INSTANCE.getExporter().registerItems(callback, Items.INSTANCE::addExporter);
        Blocks.INSTANCE.getExternalStorage().registerItems(callback, Items.INSTANCE::addExternalStorage);
        Blocks.INSTANCE.getConstructor().registerItems(callback, Items.INSTANCE::addConstructor);
        Blocks.INSTANCE.getDestructor().registerItems(callback, Items.INSTANCE::addDestructor);
        Blocks.INSTANCE.getWirelessTransmitter().registerItems(callback, Items.INSTANCE::addWirelessTransmitter);
        Blocks.INSTANCE.getNetworkReceiver().registerItems(callback, Items.INSTANCE::addNetworkReceiver);
        Blocks.INSTANCE.getNetworkTransmitter().registerItems(callback, Items.INSTANCE::addNetworkTransmitter);
        Blocks.INSTANCE.getSecurityManager().registerItems(callback, Items.INSTANCE::addSecurityManager);
        Blocks.INSTANCE.getRelay().registerItems(callback, Items.INSTANCE::addRelay);
        Blocks.INSTANCE.getDiskInterface().registerItems(callback, Items.INSTANCE::addDiskInterface);
        registerStorageItems(callback);
        registerUpgrades(callback);
    }

    private void registerSimpleItems(final RegistryCallback<Item> callback) {
        Items.INSTANCE.setQuartzEnrichedIron(callback.register(ContentIds.QUARTZ_ENRICHED_IRON, SimpleItem::new));
        Items.INSTANCE.setQuartzEnrichedCopper(callback.register(ContentIds.QUARTZ_ENRICHED_COPPER, SimpleItem::new));
        callback.register(
            ContentIds.QUARTZ_ENRICHED_IRON_BLOCK,
            () -> new BaseBlockItem(Blocks.INSTANCE.getQuartzEnrichedIronBlock())
        );
        callback.register(
            ContentIds.QUARTZ_ENRICHED_COPPER_BLOCK,
            () -> new BaseBlockItem(Blocks.INSTANCE.getQuartzEnrichedCopperBlock())
        );
        Items.INSTANCE.setSilicon(callback.register(ContentIds.SILICON, SimpleItem::new));
        Items.INSTANCE.setProcessorBinding(callback.register(ContentIds.PROCESSOR_BINDING, SimpleItem::new));
        callback.register(ContentIds.DISK_DRIVE, () -> Blocks.INSTANCE.getDiskDrive().createBlockItem());
        Items.INSTANCE.setWrench(callback.register(ContentIds.WRENCH, WrenchItem::new));
        Items.INSTANCE.setStorageHousing(callback.register(ContentIds.STORAGE_HOUSING, SimpleItem::new));
        callback.register(ContentIds.MACHINE_CASING, () -> new BaseBlockItem(Blocks.INSTANCE.getMachineCasing()));
        callback.register(ContentIds.STORAGE_MONITOR, () -> Blocks.INSTANCE.getStorageMonitor().createBlockItem());
        callback.register(ContentIds.INTERFACE, () -> Blocks.INSTANCE.getInterface().createBlockItem());
        Items.INSTANCE.setConstructionCore(callback.register(ContentIds.CONSTRUCTION_CORE, SimpleItem::new));
        Items.INSTANCE.setDestructionCore(callback.register(ContentIds.DESTRUCTION_CORE, SimpleItem::new));
        for (final ProcessorItem.Type type : ProcessorItem.Type.values()) {
            registerProcessor(callback, type);
        }
        Items.INSTANCE.setConfigurationCard(callback.register(
            ContentIds.CONFIGURATION_CARD,
            ConfigurationCardItem::new
        ));
        Items.INSTANCE.setNetworkCard(callback.register(ContentIds.NETWORK_CARD, NetworkCardItem::new));
        Items.INSTANCE.setPattern(callback.register(ContentIds.PATTERN, PatternItem::new));
    }

    private void registerProcessor(final RegistryCallback<Item> callback, final ProcessorItem.Type type) {
        Items.INSTANCE.setProcessor(type, callback.register(ContentIds.forProcessor(type), ProcessorItem::new));
    }

    private void registerStorageItems(final RegistryCallback<Item> callback) {
        for (final ItemStorageVariant variant : ItemStorageVariant.values()) {
            registerItemStorageItems(callback, variant);
        }
        for (final FluidStorageVariant variant : FluidStorageVariant.values()) {
            registerFluidStorageItems(callback, variant);
        }
    }

    private void registerItemStorageItems(final RegistryCallback<Item> callback,
                                          final ItemStorageVariant variant) {
        if (variant != ItemStorageVariant.CREATIVE) {
            Items.INSTANCE.setItemStoragePart(variant, callback.register(
                ContentIds.forItemStoragePart(variant),
                SimpleItem::new)
            );
        }
        Items.INSTANCE.setItemStorageDisk(variant, callback.register(
            ContentIds.forStorageDisk(variant),
            () -> new ItemStorageDiskItem(variant)
        ));
        callback.register(
            ContentIds.forItemStorageBlock(variant),
            () -> new ItemStorageBlockBlockItem(Blocks.INSTANCE.getItemStorageBlock(variant), variant)
        );
    }

    private void registerFluidStorageItems(final RegistryCallback<Item> callback,
                                           final FluidStorageVariant variant) {
        if (variant != FluidStorageVariant.CREATIVE) {
            Items.INSTANCE.setFluidStoragePart(variant, callback.register(
                ContentIds.forFluidStoragePart(variant),
                SimpleItem::new)
            );
        }
        Items.INSTANCE.setFluidStorageDisk(variant, callback.register(
            ContentIds.forFluidStorageDisk(variant),
            () -> new FluidStorageDiskItem(variant)
        ));
        callback.register(
            ContentIds.forFluidStorageBlock(variant),
            () -> new FluidStorageBlockBlockItem(Blocks.INSTANCE.getFluidStorageBlock(variant), variant)
        );
    }

    private void registerUpgrades(final RegistryCallback<Item> callback) {
        Items.INSTANCE.setUpgrade(callback.register(
            ContentIds.UPGRADE,
            SimpleItem::new
        ));
        final Supplier<AbstractUpgradeItem> speedUpgrade = callback.register(
            ContentIds.SPEED_UPGRADE,
            SimpleUpgradeItem::speedUpgrade
        );
        Items.INSTANCE.setSpeedUpgrade(speedUpgrade);
        final Supplier<AbstractUpgradeItem> stackUpgrade = callback.register(
            ContentIds.STACK_UPGRADE,
            SimpleUpgradeItem::stackUpgrade
        );
        Items.INSTANCE.setStackUpgrade(stackUpgrade);
        final Supplier<AbstractUpgradeItem> fortune1Upgrade = callback.register(
            ContentIds.FORTUNE_1_UPGRADE,
            () -> new FortuneUpgradeItem(RefinedStorageApi.INSTANCE.getUpgradeRegistry(), 1)
        );
        Items.INSTANCE.setFortune1Upgrade(fortune1Upgrade);
        final Supplier<AbstractUpgradeItem> fortune2Upgrade = callback.register(
            ContentIds.FORTUNE_2_UPGRADE,
            () -> new FortuneUpgradeItem(RefinedStorageApi.INSTANCE.getUpgradeRegistry(), 2)
        );
        Items.INSTANCE.setFortune2Upgrade(fortune2Upgrade);
        final Supplier<AbstractUpgradeItem> fortune3Upgrade = callback.register(
            ContentIds.FORTUNE_3_UPGRADE,
            () -> new FortuneUpgradeItem(RefinedStorageApi.INSTANCE.getUpgradeRegistry(), 3)
        );
        Items.INSTANCE.setFortune3Upgrade(fortune3Upgrade);
        final Supplier<AbstractUpgradeItem> silkTouchUpgrade = callback.register(
            ContentIds.SILK_TOUCH_UPGRADE,
            SimpleUpgradeItem::silkTouchUpgrade
        );
        Items.INSTANCE.setSilkTouchUpgrade(silkTouchUpgrade);
        Items.INSTANCE.setRangeUpgrade(callback.register(
            ContentIds.RANGE_UPGRADE,
            () -> new RangeUpgradeItem(RefinedStorageApi.INSTANCE.getUpgradeRegistry(), false)
        ));
        Items.INSTANCE.setCreativeRangeUpgrade(callback.register(
            ContentIds.CREATIVE_RANGE_UPGRADE,
            () -> new RangeUpgradeItem(RefinedStorageApi.INSTANCE.getUpgradeRegistry(), true)
        ));
    }

    protected final void registerUpgradeMappings() {
        RefinedStorageApi.INSTANCE.getUpgradeRegistry().forDestination(UpgradeDestinations.IMPORTER)
            .add(Items.INSTANCE.getSpeedUpgrade(), 4)
            .add(Items.INSTANCE.getStackUpgrade())
            .add(Items.INSTANCE.getRegulatorUpgrade());

        RefinedStorageApi.INSTANCE.getUpgradeRegistry().forDestination(UpgradeDestinations.EXPORTER)
            .add(Items.INSTANCE.getSpeedUpgrade(), 4)
            .add(Items.INSTANCE.getStackUpgrade())
            .add(Items.INSTANCE.getRegulatorUpgrade());

        RefinedStorageApi.INSTANCE.getUpgradeRegistry().forDestination(UpgradeDestinations.DESTRUCTOR)
            .add(Items.INSTANCE.getSpeedUpgrade(), 4)
            .add(Items.INSTANCE.getFortune1Upgrade())
            .add(Items.INSTANCE.getFortune2Upgrade())
            .add(Items.INSTANCE.getFortune3Upgrade())
            .add(Items.INSTANCE.getSilkTouchUpgrade());

        RefinedStorageApi.INSTANCE.getUpgradeRegistry().forDestination(UpgradeDestinations.CONSTRUCTOR)
            .add(Items.INSTANCE.getSpeedUpgrade(), 4)
            .add(Items.INSTANCE.getStackUpgrade());

        RefinedStorageApi.INSTANCE.getUpgradeRegistry().forDestination(UpgradeDestinations.WIRELESS_TRANSMITTER)
            .add(Items.INSTANCE.getRangeUpgrade(), 4)
            .add(Items.INSTANCE.getCreativeRangeUpgrade());

        RefinedStorageApi.INSTANCE.getUpgradeRegistry().forDestination(UpgradeDestinations.DISK_INTERFACE)
            .add(Items.INSTANCE.getSpeedUpgrade(), 4)
            .add(Items.INSTANCE.getStackUpgrade());
    }

    protected final void registerBlockEntities(
        final RegistryCallback<BlockEntityType<?>> callback,
        final BlockEntityTypeFactory typeFactory,
        final BlockEntityTypeFactory.BlockEntitySupplier<AbstractDiskDriveBlockEntity> diskDriveBlockEntitySupplier,
        final BlockEntityTypeFactory.BlockEntitySupplier<? extends AbstractPortableGridBlockEntity>
            portableGridBlockEntitySupplier,
        final BlockEntityTypeFactory.BlockEntitySupplier<? extends AbstractPortableGridBlockEntity>
            creativePortableGridBlockEntitySupplier,
        final BlockEntityTypeFactory.BlockEntitySupplier<AbstractDiskInterfaceBlockEntity>
            diskInterfaceBlockEntitySupplier
    ) {
        BlockEntities.INSTANCE.setCable(callback.register(
            ContentIds.CABLE,
            () -> typeFactory.create((pos, state) -> new BaseNetworkNodeContainerBlockEntity<>(
                BlockEntities.INSTANCE.getCable(),
                pos,
                state,
                new SimpleNetworkNode(Platform.INSTANCE.getConfig().getCable().getEnergyUsage())
            ), Blocks.INSTANCE.getCable().toArray())
        ));
        BlockEntities.INSTANCE.setController(callback.register(
            ContentIds.CONTROLLER,
            () -> typeFactory.create(
                (pos, state) -> new ControllerBlockEntity(ControllerType.NORMAL, pos, state),
                Blocks.INSTANCE.getController().toArray()
            )
        ));
        BlockEntities.INSTANCE.setCreativeController(callback.register(
            ContentIds.CREATIVE_CONTROLLER,
            () -> typeFactory.create(
                (pos, state) -> new ControllerBlockEntity(ControllerType.CREATIVE, pos, state),
                Blocks.INSTANCE.getCreativeController().toArray()
            )
        ));
        BlockEntities.INSTANCE.setDiskDrive(callback.register(
            ContentIds.DISK_DRIVE,
            () -> typeFactory.create(diskDriveBlockEntitySupplier, Blocks.INSTANCE.getDiskDrive())
        ));
        BlockEntities.INSTANCE.setGrid(callback.register(
            ContentIds.GRID,
            () -> typeFactory.create(GridBlockEntity::new, Blocks.INSTANCE.getGrid().toArray())
        ));
        BlockEntities.INSTANCE.setCraftingGrid(callback.register(
            ContentIds.CRAFTING_GRID,
            () -> typeFactory.create(CraftingGridBlockEntity::new, Blocks.INSTANCE.getCraftingGrid().toArray())
        ));
        BlockEntities.INSTANCE.setPatternGrid(callback.register(
            ContentIds.PATTERN_GRID,
            () -> typeFactory.create(PatternGridBlockEntity::new, Blocks.INSTANCE.getPatternGrid().toArray())
        ));
        for (final ItemStorageVariant variant : ItemStorageVariant.values()) {
            BlockEntities.INSTANCE.setItemStorageBlock(variant, callback.register(
                ContentIds.forItemStorageBlock(variant),
                () -> typeFactory.create(
                    (pos, state) -> new ItemStorageBlockBlockEntity(pos, state, variant),
                    Blocks.INSTANCE.getItemStorageBlock(variant)
                )
            ));
        }
        for (final FluidStorageVariant variant : FluidStorageVariant.values()) {
            BlockEntities.INSTANCE.setFluidStorageBlock(variant, callback.register(
                ContentIds.forFluidStorageBlock(variant),
                () -> typeFactory.create(
                    (pos, state) -> new FluidStorageBlockBlockEntity(pos, state, variant),
                    Blocks.INSTANCE.getFluidStorageBlock(variant)
                )
            ));
        }
        BlockEntities.INSTANCE.setImporter(callback.register(
            ContentIds.IMPORTER,
            () -> typeFactory.create(ImporterBlockEntity::new, Blocks.INSTANCE.getImporter().toArray())

        ));
        BlockEntities.INSTANCE.setExporter(callback.register(
            ContentIds.EXPORTER,
            () -> typeFactory.create(ExporterBlockEntity::new, Blocks.INSTANCE.getExporter().toArray())

        ));
        BlockEntities.INSTANCE.setInterface(callback.register(
            ContentIds.INTERFACE,
            () -> typeFactory.create(InterfaceBlockEntity::new, Blocks.INSTANCE.getInterface())
        ));
        BlockEntities.INSTANCE.setExternalStorage(callback.register(
            ContentIds.EXTERNAL_STORAGE,
            () -> typeFactory.create(ExternalStorageBlockEntity::new, Blocks.INSTANCE.getExternalStorage().toArray())
        ));
        BlockEntities.INSTANCE.setDetector(callback.register(
            ContentIds.DETECTOR,
            () -> typeFactory.create(DetectorBlockEntity::new, Blocks.INSTANCE.getDetector().toArray())
        ));
        BlockEntities.INSTANCE.setConstructor(callback.register(
            ContentIds.CONSTRUCTOR,
            () -> typeFactory.create(ConstructorBlockEntity::new, Blocks.INSTANCE.getConstructor().toArray())
        ));
        BlockEntities.INSTANCE.setDestructor(callback.register(
            ContentIds.DESTRUCTOR,
            () -> typeFactory.create(DestructorBlockEntity::new, Blocks.INSTANCE.getDestructor().toArray())
        ));
        BlockEntities.INSTANCE.setWirelessTransmitter(callback.register(
            ContentIds.WIRELESS_TRANSMITTER,
            () -> typeFactory.create(
                WirelessTransmitterBlockEntity::new,
                Blocks.INSTANCE.getWirelessTransmitter().toArray()
            )
        ));
        BlockEntities.INSTANCE.setStorageMonitor(callback.register(
            ContentIds.STORAGE_MONITOR,
            () -> typeFactory.create(StorageMonitorBlockEntity::new, Blocks.INSTANCE.getStorageMonitor())
        ));
        BlockEntities.INSTANCE.setNetworkReceiver(callback.register(
            ContentIds.NETWORK_RECEIVER,
            () -> typeFactory.create(NetworkReceiverBlockEntity::new, Blocks.INSTANCE.getNetworkReceiver().toArray())
        ));
        BlockEntities.INSTANCE.setNetworkTransmitter(callback.register(
            ContentIds.NETWORK_TRANSMITTER,
            () -> typeFactory.create(
                NetworkTransmitterBlockEntity::new,
                Blocks.INSTANCE.getNetworkTransmitter().toArray()
            )
        ));
        BlockEntities.INSTANCE.setPortableGrid(callback.register(
            ContentIds.PORTABLE_GRID,
            () -> typeFactory.create(portableGridBlockEntitySupplier::create, Blocks.INSTANCE.getPortableGrid())
        ));
        BlockEntities.INSTANCE.setCreativePortableGrid(callback.register(
            ContentIds.CREATIVE_PORTABLE_GRID,
            () -> typeFactory.create(
                creativePortableGridBlockEntitySupplier::create,
                Blocks.INSTANCE.getCreativePortableGrid()
            )
        ));
        BlockEntities.INSTANCE.setSecurityManager(callback.register(
            ContentIds.SECURITY_MANAGER,
            () -> typeFactory.create(
                SecurityManagerBlockEntity::new,
                Blocks.INSTANCE.getSecurityManager().toArray()
            )
        ));
        BlockEntities.INSTANCE.setRelay(callback.register(
            ContentIds.RELAY,
            () -> typeFactory.create(RelayBlockEntity::new, Blocks.INSTANCE.getRelay().toArray())
        ));
        BlockEntities.INSTANCE.setDiskInterface(callback.register(
            ContentIds.DISK_INTERFACE,
            () -> typeFactory.create(diskInterfaceBlockEntitySupplier, Blocks.INSTANCE.getDiskInterface().toArray())
        ));
    }

    protected final void registerMenus(final RegistryCallback<MenuType<?>> callback,
                                       final MenuTypeFactory menuTypeFactory,
                                       final ExtendedMenuTypeFactory extendedMenuTypeFactory) {
        Menus.INSTANCE.setController(callback.register(
            ContentIds.CONTROLLER,
            () -> extendedMenuTypeFactory.create(ControllerContainerMenu::new, ControllerData.STREAM_CODEC)
        ));
        Menus.INSTANCE.setDiskDrive(callback.register(
            ContentIds.DISK_DRIVE,
            () -> extendedMenuTypeFactory.create(DiskDriveContainerMenu::new, ResourceContainerData.STREAM_CODEC)
        ));
        Menus.INSTANCE.setGrid(callback.register(
            ContentIds.GRID,
            () -> extendedMenuTypeFactory.create(GridContainerMenu::new, GridData.STREAM_CODEC)
        ));
        Menus.INSTANCE.setCraftingGrid(callback.register(
            ContentIds.CRAFTING_GRID,
            () -> extendedMenuTypeFactory.create(CraftingGridContainerMenu::new, GridData.STREAM_CODEC)
        ));
        Menus.INSTANCE.setPatternGrid(callback.register(
            ContentIds.PATTERN_GRID,
            () -> extendedMenuTypeFactory.create(PatternGridContainerMenu::new, PatternGridData.STREAM_CODEC)
        ));
        Menus.INSTANCE.setWirelessGrid(callback.register(
            ContentIds.WIRELESS_GRID,
            () -> extendedMenuTypeFactory.create(WirelessGridContainerMenu::new, WirelessGridData.STREAM_CODEC)
        ));
        Menus.INSTANCE.setItemStorage(callback.register(
            ContentIds.ITEM_STORAGE_BLOCK,
            () -> extendedMenuTypeFactory.create(ItemStorageBlockContainerMenu::new, StorageBlockData.STREAM_CODEC)
        ));
        Menus.INSTANCE.setFluidStorage(callback.register(
            ContentIds.FLUID_STORAGE_BLOCK,
            () -> extendedMenuTypeFactory.create(FluidStorageBlockContainerMenu::new, StorageBlockData.STREAM_CODEC)
        ));
        Menus.INSTANCE.setImporter(callback.register(
            ContentIds.IMPORTER,
            () -> extendedMenuTypeFactory.create(ImporterContainerMenu::new, ResourceContainerData.STREAM_CODEC)
        ));
        Menus.INSTANCE.setExporter(callback.register(
            ContentIds.EXPORTER,
            () -> extendedMenuTypeFactory.create(ExporterContainerMenu::new, ResourceContainerData.STREAM_CODEC)
        ));
        Menus.INSTANCE.setInterface(callback.register(
            ContentIds.INTERFACE,
            () -> extendedMenuTypeFactory.create(InterfaceContainerMenu::new, InterfaceData.STREAM_CODEC)
        ));
        Menus.INSTANCE.setExternalStorage(callback.register(
            ContentIds.EXTERNAL_STORAGE,
            () -> extendedMenuTypeFactory.create(ExternalStorageContainerMenu::new, ResourceContainerData.STREAM_CODEC)
        ));
        Menus.INSTANCE.setDetector(callback.register(
            ContentIds.DETECTOR,
            () -> extendedMenuTypeFactory.create(DetectorContainerMenu::new, SingleAmountData.STREAM_CODEC)
        ));
        Menus.INSTANCE.setDestructor(callback.register(
            ContentIds.DESTRUCTOR,
            () -> extendedMenuTypeFactory.create(DestructorContainerMenu::new, ResourceContainerData.STREAM_CODEC)
        ));
        Menus.INSTANCE.setConstructor(callback.register(
            ContentIds.CONSTRUCTOR,
            () -> extendedMenuTypeFactory.create(ConstructorContainerMenu::new, ResourceContainerData.STREAM_CODEC)
        ));
        Menus.INSTANCE.setRegulatorUpgrade(callback.register(
            ContentIds.REGULATOR_UPGRADE,
            () -> extendedMenuTypeFactory.create(RegulatorUpgradeContainerMenu::new, SingleAmountData.STREAM_CODEC)
        ));
        Menus.INSTANCE.setWirelessTransmitter(callback.register(
            ContentIds.WIRELESS_TRANSMITTER,
            () -> extendedMenuTypeFactory.create(WirelessTransmitterContainerMenu::new,
                WirelessTransmitterData.STREAM_CODEC)
        ));
        Menus.INSTANCE.setStorageMonitor(callback.register(
            ContentIds.STORAGE_MONITOR,
            () -> extendedMenuTypeFactory.create(StorageMonitorContainerMenu::new, ResourceContainerData.STREAM_CODEC)
        ));
        Menus.INSTANCE.setNetworkTransmitter(callback.register(
            ContentIds.NETWORK_TRANSMITTER,
            () -> extendedMenuTypeFactory.create(NetworkTransmitterContainerMenu::new,
                NetworkTransmitterData.STREAM_CODEC)
        ));
        Menus.INSTANCE.setPortableGridBlock(callback.register(
            createIdentifier("portable_grid_block"),
            () -> extendedMenuTypeFactory.create(PortableGridBlockContainerMenu::new, PortableGridData.STREAM_CODEC)
        ));
        Menus.INSTANCE.setPortableGridItem(callback.register(
            createIdentifier("portable_grid_item"),
            () -> extendedMenuTypeFactory.create(PortableGridItemContainerMenu::new, PortableGridData.STREAM_CODEC)
        ));
        Menus.INSTANCE.setSecurityCard(callback.register(
            ContentIds.SECURITY_CARD,
            () -> extendedMenuTypeFactory.create(SecurityCardContainerMenu::new,
                PlayerBoundSecurityCardData.STREAM_CODEC)
        ));
        Menus.INSTANCE.setFallbackSecurityCard(callback.register(
            ContentIds.FALLBACK_SECURITY_CARD,
            () -> extendedMenuTypeFactory.create(FallbackSecurityCardContainerMenu::new, SecurityCardData.STREAM_CODEC)
        ));
        Menus.INSTANCE.setSecurityManager(callback.register(
            ContentIds.SECURITY_MANAGER,
            () -> menuTypeFactory.create(SecurityManagerContainerMenu::new)
        ));
        Menus.INSTANCE.setRelay(callback.register(
            ContentIds.RELAY,
            () -> extendedMenuTypeFactory.create(RelayContainerMenu::new, ResourceContainerData.STREAM_CODEC)
        ));
        Menus.INSTANCE.setDiskInterface(callback.register(
            ContentIds.DISK_INTERFACE,
            () -> extendedMenuTypeFactory.create(DiskInterfaceContainerMenu::new, ResourceContainerData.STREAM_CODEC)
        ));
    }

    protected final void registerLootFunctions(final RegistryCallback<LootItemFunctionType<?>> callback) {
        LootFunctions.INSTANCE.setStorageBlock(callback.register(
            ContentIds.STORAGE_BLOCK,
            () -> new LootItemFunctionType<>(MapCodec.unit(new StorageBlockLootItemFunction()))
        ));
        LootFunctions.INSTANCE.setPortableGrid(callback.register(
            ContentIds.PORTABLE_GRID,
            () -> new LootItemFunctionType<>(MapCodec.unit(new PortableGridLootItemFunction()))
        ));
        LootFunctions.INSTANCE.setEnergy(callback.register(
            createIdentifier("energy"),
            () -> new LootItemFunctionType<>(MapCodec.unit(new EnergyLootItemFunction()))
        ));
    }

    protected final void registerSounds(final RegistryCallback<SoundEvent> callback) {
        Sounds.INSTANCE.setWrench(callback.register(
            ContentIds.WRENCH,
            () -> SoundEvent.createVariableRangeEvent(ContentIds.WRENCH)
        ));
    }

    protected final void registerRecipeSerializers(final RegistryCallback<RecipeSerializer<?>> callback) {
        callback.register(
            createIdentifier("upgrade_with_enchanted_book"),
            UpgradeWithEnchantedBookRecipeSerializer::new
        );
    }

    protected final void registerDataComponents(final RegistryCallback<DataComponentType<?>> callback) {
        DataComponents.INSTANCE.setEnergy(
            callback.register(createIdentifier("energy"), () -> DataComponentType.<Long>builder()
                .persistent(Codec.LONG)
                .networkSynchronized(ByteBufCodecs.VAR_LONG)
                .build()));
        DataComponents.INSTANCE.setNetworkLocation(
            callback.register(createIdentifier("network_location"), () -> DataComponentType.<GlobalPos>builder()
                .persistent(GlobalPos.CODEC)
                .networkSynchronized(GlobalPos.STREAM_CODEC)
                .build()));
        DataComponents.INSTANCE.setStorageReference(
            callback.register(createIdentifier("storage_reference"), () -> DataComponentType.<UUID>builder()
                .persistent(UUIDUtil.CODEC)
                .networkSynchronized(UUIDUtil.STREAM_CODEC)
                .build()));
        DataComponents.INSTANCE.setRegulatorUpgradeState(
            callback.register(createIdentifier("regulator_upgrade_state"),
                () -> DataComponentType.<RegulatorUpgradeState>builder()
                    .persistent(RegulatorUpgradeState.CODEC)
                    .networkSynchronized(RegulatorUpgradeState.STREAM_CODEC)
                    .build()));
        DataComponents.INSTANCE.setSecurityCardBoundPlayer(
            callback.register(createIdentifier("security_card_bound_player"),
                () -> DataComponentType.<SecurityCardBoundPlayer>builder()
                    .persistent(SecurityCardBoundPlayer.CODEC)
                    .networkSynchronized(SecurityCardBoundPlayer.STREAM_CODEC)
                    .build()));
        DataComponents.INSTANCE.setSecurityCardPermissions(
            callback.register(createIdentifier("security_card_permissions"),
                () -> DataComponentType.<SecurityCardPermissions>builder()
                    .persistent(SecurityCardPermissions.CODEC)
                    .networkSynchronized(SecurityCardPermissions.STREAM_CODEC)
                    .build()));
        DataComponents.INSTANCE.setConfigurationCardState(
            callback.register(createIdentifier("configuration_card_state"),
                () -> DataComponentType.<ConfigurationCardState>builder()
                    .persistent(ConfigurationCardState.CODEC)
                    .networkSynchronized(ConfigurationCardState.STREAM_CODEC)
                    .build()));
        DataComponents.INSTANCE.setPatternState(
            callback.register(createIdentifier("pattern_state"),
                () -> DataComponentType.<PatternState>builder()
                    .persistent(PatternState.CODEC)
                    .networkSynchronized(PatternState.STREAM_CODEC)
                    .build()));
        DataComponents.INSTANCE.setCraftingPatternState(
            callback.register(createIdentifier("crafting_pattern_state"),
                () -> DataComponentType.<CraftingPatternState>builder()
                    .persistent(CraftingPatternState.CODEC)
                    .networkSynchronized(CraftingPatternState.STREAM_CODEC)
                    .build()));
        DataComponents.INSTANCE.setProcessingPatternState(
            callback.register(createIdentifier("processing_pattern_state"),
                () -> DataComponentType.<ProcessingPatternState>builder()
                    .persistent(ProcessingPatternState.CODEC)
                    .networkSynchronized(ProcessingPatternState.STREAM_CODEC)
                    .build()));
    }

    protected final void registerInventorySlotReference() {
        RefinedStorageApi.INSTANCE.getSlotReferenceFactoryRegistry().register(
            createIdentifier("inventory"),
            InventorySlotReferenceFactory.INSTANCE
        );
    }

    protected static boolean allowComponentsUpdateAnimation(final ItemStack oldStack, final ItemStack newStack) {
        return oldStack.getItem() != newStack.getItem();
    }
}
