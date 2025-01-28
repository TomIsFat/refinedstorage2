package com.refinedmods.refinedstorage.common.content;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public final class Tags {
    public static final TagKey<Item> CABLES = createTag("cables");
    public static final TagKey<Item> CONTROLLERS = createTag("controllers");
    public static final TagKey<Item> CREATIVE_CONTROLLERS = createTag("creative_controllers");
    public static final TagKey<Item> FLUID_STORAGE_DISKS = createTag("fluid_storage_disks");
    public static final TagKey<Item> GRIDS = createTag("grids");
    public static final TagKey<Item> CRAFTING_GRIDS = createTag("crafting_grids");
    public static final TagKey<Item> PATTERN_GRIDS = createTag("pattern_grids");
    public static final TagKey<Item> STORAGE_DISKS = createTag("storage_disks");
    public static final TagKey<Item> IMPORTERS = createTag("importers");
    public static final TagKey<Item> EXPORTERS = createTag("exporters");
    public static final TagKey<Item> EXTERNAL_STORAGES = createTag("external_storages");
    public static final TagKey<Item> DETECTORS = createTag("detectors");
    public static final TagKey<Item> CONSTRUCTORS = createTag("constructors");
    public static final TagKey<Item> DESTRUCTORS = createTag("destructors");
    public static final TagKey<Item> WIRELESS_TRANSMITTERS = createTag("wireless_transmitters");
    public static final TagKey<Item> NETWORK_RECEIVERS = createTag("network_receivers");
    public static final TagKey<Item> NETWORK_TRANSMITTERS = createTag("network_transmitters");
    public static final TagKey<Item> SECURITY_MANAGERS = createTag("security_managers");
    public static final TagKey<Item> RELAYS = createTag("relays");
    public static final TagKey<Item> DISK_INTERFACES = createTag("disk_interfaces");
    public static final TagKey<Item> AUTOCRAFTERS = createTag("autocrafters");
    public static final TagKey<Item> AUTOCRAFTER_MANAGERS = createTag("autocrafter_managers");
    public static final TagKey<Item> AUTOCRAFTING_MONITORS = createTag("autocrafting_monitors");

    private Tags() {
    }

    private static TagKey<Item> createTag(final String id) {
        return TagKey.create(Registries.ITEM, createIdentifier(id));
    }
}
