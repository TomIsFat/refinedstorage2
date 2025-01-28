package com.refinedmods.refinedstorage.common.constructordestructor;

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

public class ConstructorContainerMenu extends AbstractSimpleFilterContainerMenu<AbstractConstructorBlockEntity> {
    private static final MutableComponent FILTER_HELP = createTranslation("gui", "constructor.filter_help");

    public ConstructorContainerMenu(final int syncId,
                                    final Inventory playerInventory,
                                    final ResourceContainerData resourceContainerData) {
        super(
            Menus.INSTANCE.getConstructor(),
            syncId,
            playerInventory.player,
            resourceContainerData,
            UpgradeDestinations.CONSTRUCTOR,
            FILTER_HELP
        );
    }

    ConstructorContainerMenu(final int syncId,
                             final Player player,
                             final AbstractConstructorBlockEntity constructor,
                             final ResourceContainer resourceContainer,
                             final UpgradeContainer upgradeContainer) {
        super(
            Menus.INSTANCE.getConstructor(),
            syncId,
            player,
            resourceContainer,
            upgradeContainer,
            constructor,
            FILTER_HELP
        );
    }

    @Override
    protected void registerClientProperties() {
        registerProperty(new ClientProperty<>(PropertyTypes.FUZZY_MODE, false));
        registerProperty(new ClientProperty<>(PropertyTypes.REDSTONE_MODE, RedstoneMode.IGNORE));
        registerProperty(new ClientProperty<>(PropertyTypes.SCHEDULING_MODE, SchedulingModeType.DEFAULT));
        registerProperty(new ClientProperty<>(ConstructorDestructorPropertyTypes.DROP_ITEMS, false));
    }

    @Override
    protected void registerServerProperties(final AbstractConstructorBlockEntity blockEntity) {
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
        registerProperty(new ServerProperty<>(
            ConstructorDestructorPropertyTypes.DROP_ITEMS,
            blockEntity::isDropItems,
            blockEntity::setDropItems
        ));
    }
}
