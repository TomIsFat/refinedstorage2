package com.refinedmods.refinedstorage.common.exporter;

import com.refinedmods.refinedstorage.common.api.support.resource.ResourceContainer;
import com.refinedmods.refinedstorage.common.content.Menus;
import com.refinedmods.refinedstorage.common.support.RedstoneMode;
import com.refinedmods.refinedstorage.common.support.SchedulingModeType;
import com.refinedmods.refinedstorage.common.support.containermenu.AbstractSimpleFilterContainerMenu;
import com.refinedmods.refinedstorage.common.support.containermenu.ClientProperty;
import com.refinedmods.refinedstorage.common.support.containermenu.PropertyTypes;
import com.refinedmods.refinedstorage.common.support.containermenu.ServerProperty;
import com.refinedmods.refinedstorage.common.support.resource.ResourceContainerData;
import com.refinedmods.refinedstorage.common.upgrade.UpgradeContainer;
import com.refinedmods.refinedstorage.common.upgrade.UpgradeDestinations;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

public class ExporterContainerMenu extends AbstractSimpleFilterContainerMenu<AbstractExporterBlockEntity> {
    private static final MutableComponent FILTER_HELP = createTranslation("gui", "exporter.filter_help");

    public ExporterContainerMenu(final int syncId,
                                 final Inventory playerInventory,
                                 final ResourceContainerData resourceContainerData) {
        super(
            Menus.INSTANCE.getExporter(),
            syncId,
            playerInventory.player,
            resourceContainerData,
            UpgradeDestinations.EXPORTER,
            FILTER_HELP
        );
    }

    ExporterContainerMenu(final int syncId,
                          final Player player,
                          final AbstractExporterBlockEntity exporter,
                          final ResourceContainer resourceContainer,
                          final UpgradeContainer upgradeContainer) {
        super(
            Menus.INSTANCE.getExporter(),
            syncId,
            player,
            resourceContainer,
            upgradeContainer,
            exporter,
            FILTER_HELP
        );
    }

    @Override
    protected void registerClientProperties() {
        registerProperty(new ClientProperty<>(PropertyTypes.FUZZY_MODE, false));
        registerProperty(new ClientProperty<>(PropertyTypes.REDSTONE_MODE, RedstoneMode.IGNORE));
        registerProperty(new ClientProperty<>(PropertyTypes.SCHEDULING_MODE, SchedulingModeType.DEFAULT));
    }

    @Override
    protected void registerServerProperties(final AbstractExporterBlockEntity blockEntity) {
        registerProperty(new ServerProperty<>(
            PropertyTypes.FUZZY_MODE,
            blockEntity::isFuzzyMode,
            blockEntity::setFuzzyMode
        ));
        registerProperty(new ServerProperty<>(
            PropertyTypes.REDSTONE_MODE,
            blockEntity::getRedstoneMode,
            blockEntity::setRedstoneMode
        ));
        registerProperty(new ServerProperty<>(
            PropertyTypes.SCHEDULING_MODE,
            blockEntity::getSchedulingModeType,
            blockEntity::setSchedulingModeType
        ));
    }
}
