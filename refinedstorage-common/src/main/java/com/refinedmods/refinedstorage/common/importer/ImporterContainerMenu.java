package com.refinedmods.refinedstorage.common.importer;

import com.refinedmods.refinedstorage.api.resource.filter.FilterMode;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceContainer;
import com.refinedmods.refinedstorage.common.content.Menus;
import com.refinedmods.refinedstorage.common.support.RedstoneMode;
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

public class ImporterContainerMenu extends AbstractSimpleFilterContainerMenu<AbstractImporterBlockEntity> {
    private static final MutableComponent FILTER_HELP = createTranslation("gui", "importer.filter_help");

    public ImporterContainerMenu(final int syncId,
                                 final Inventory playerInventory,
                                 final ResourceContainerData resourceContainerData) {
        super(
            Menus.INSTANCE.getImporter(),
            syncId,
            playerInventory.player,
            resourceContainerData,
            UpgradeDestinations.IMPORTER,
            FILTER_HELP
        );
    }

    ImporterContainerMenu(final int syncId,
                          final Player player,
                          final AbstractImporterBlockEntity importer,
                          final ResourceContainer resourceContainer,
                          final UpgradeContainer upgradeContainer) {
        super(
            Menus.INSTANCE.getImporter(),
            syncId,
            player,
            resourceContainer,
            upgradeContainer,
            importer,
            FILTER_HELP
        );
    }

    @Override
    protected void registerClientProperties() {
        registerProperty(new ClientProperty<>(PropertyTypes.FILTER_MODE, FilterMode.BLOCK));
        registerProperty(new ClientProperty<>(PropertyTypes.FUZZY_MODE, false));
        registerProperty(new ClientProperty<>(PropertyTypes.REDSTONE_MODE, RedstoneMode.IGNORE));
    }

    @Override
    protected void registerServerProperties(final AbstractImporterBlockEntity blockEntity) {
        registerProperty(new ServerProperty<>(
            PropertyTypes.FILTER_MODE,
            blockEntity::getFilterMode,
            blockEntity::setFilterMode
        ));
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
    }
}
