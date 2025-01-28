package com.refinedmods.refinedstorage.common.content;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.MOD_ID;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslationKey;

public final class ContentNames {
    public static final String MOD_TRANSLATION_KEY = "mod." + MOD_ID;
    public static final MutableComponent MOD = Component.translatable(MOD_TRANSLATION_KEY);
    public static final MutableComponent MOD_COLORIZED = Component.translatable(
        MOD_TRANSLATION_KEY + ".colorized"
    );

    public static final MutableComponent CABLE = name("cable");
    public static final MutableComponent GRID = name("grid");
    public static final MutableComponent CRAFTING_GRID = name("crafting_grid");
    public static final MutableComponent PATTERN_GRID = name("pattern_grid");
    public static final MutableComponent DETECTOR = name("detector");
    public static final MutableComponent IMPORTER = name("importer");
    public static final MutableComponent EXPORTER = name("exporter");
    public static final MutableComponent EXTERNAL_STORAGE = name("external_storage");
    public static final MutableComponent CONSTRUCTOR = name("constructor");
    public static final MutableComponent DESTRUCTOR = name("destructor");
    public static final MutableComponent CONTROLLER = name("controller");
    public static final MutableComponent CREATIVE_CONTROLLER = name("creative_controller");
    public static final MutableComponent WIRELESS_GRID = createTranslation("item", "wireless_grid");
    public static final MutableComponent CREATIVE_WIRELESS_GRID = createTranslation("item", "creative_wireless_grid");
    public static final MutableComponent WIRELESS_TRANSMITTER = name("wireless_transmitter");
    public static final MutableComponent REGULATOR_UPGRADE = createTranslation("item", "regulator_upgrade");
    public static final MutableComponent STORAGE_MONITOR = name("storage_monitor");
    public static final MutableComponent INTERFACE = name("interface");
    public static final MutableComponent DISK_DRIVE = name("disk_drive");
    public static final MutableComponent NETWORK_RECEIVER = name("network_receiver");
    public static final MutableComponent NETWORK_TRANSMITTER = name("network_transmitter");
    public static final MutableComponent PORTABLE_GRID = name("portable_grid");
    public static final MutableComponent CREATIVE_PORTABLE_GRID = name("creative_portable_grid");
    public static final MutableComponent SECURITY_CARD = createTranslation("item", "security_card");
    public static final MutableComponent FALLBACK_SECURITY_CARD = createTranslation("item", "fallback_security_card");
    public static final MutableComponent SECURITY_MANAGER = name("security_manager");
    public static final MutableComponent RELAY = name("relay");
    public static final MutableComponent DISK_INTERFACE = name("disk_interface");
    public static final MutableComponent AUTOCRAFTER = name("autocrafter");
    public static final MutableComponent AUTOCRAFTER_MANAGER = name("autocrafter_manager");
    public static final MutableComponent AUTOCRAFTING_MONITOR = name("autocrafting_monitor");
    public static final MutableComponent WIRELESS_AUTOCRAFTING_MONITOR = createTranslation(
        "item",
        "wireless_autocrafting_monitor"
    );
    public static final MutableComponent CREATIVE_WIRELESS_AUTOCRAFTING_MONITOR = createTranslation(
        "item",
        "creative_wireless_autocrafting_monitor"
    );

    public static final String CLEAR_CRAFTING_MATRIX_TO_NETWORK_TRANSLATION_KEY =
        createTranslationKey("key", "clear_crafting_grid_matrix_to_network");
    public static final String CLEAR_CRAFTING_MATRIX_TO_INVENTORY_TRANSLATION_KEY =
        createTranslationKey("key", "clear_crafting_grid_matrix_to_inventory");
    public static final String FOCUS_SEARCH_BAR_TRANSLATION_KEY = createTranslationKey("key", "focus_search_bar");
    public static final String OPEN_WIRELESS_GRID_TRANSLATION_KEY = createTranslationKey("key", "open_wireless_grid");
    public static final String OPEN_PORTABLE_GRID_TRANSLATION_KEY = createTranslationKey("key", "open_portable_grid");
    public static final String OPEN_WIRELESS_AUTOCRAFTING_MONITOR_TRANSLATION_KEY = createTranslationKey(
        "key",
        "open_wireless_autocrafting_monitor"
    );

    private ContentNames() {
    }

    private static MutableComponent name(final String name) {
        return createTranslation("block", name);
    }
}
