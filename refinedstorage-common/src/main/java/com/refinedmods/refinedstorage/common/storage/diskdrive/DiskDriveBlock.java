package com.refinedmods.refinedstorage.common.storage.diskdrive;

import com.refinedmods.refinedstorage.api.network.impl.node.storage.StorageNetworkNode;
import com.refinedmods.refinedstorage.common.content.BlockConstants;
import com.refinedmods.refinedstorage.common.content.BlockEntities;
import com.refinedmods.refinedstorage.common.content.BlockEntityProvider;
import com.refinedmods.refinedstorage.common.storage.DiskContainerBlockEntityTicker;
import com.refinedmods.refinedstorage.common.support.AbstractDirectionalBlock;
import com.refinedmods.refinedstorage.common.support.NetworkNodeBlockItem;
import com.refinedmods.refinedstorage.common.support.direction.BiDirection;
import com.refinedmods.refinedstorage.common.support.direction.BiDirectionType;
import com.refinedmods.refinedstorage.common.support.direction.DirectionType;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

public class DiskDriveBlock extends AbstractDirectionalBlock<BiDirection> implements EntityBlock {
    private static final Component HELP = createTranslation("item", "disk_drive.help");
    private static final DiskContainerBlockEntityTicker<StorageNetworkNode, AbstractDiskDriveBlockEntity> TICKER =
        new DiskContainerBlockEntityTicker<>(BlockEntities.INSTANCE::getDiskDrive);

    private final BlockEntityProvider<AbstractDiskDriveBlockEntity> blockEntityProvider;

    public DiskDriveBlock(final BlockEntityProvider<AbstractDiskDriveBlockEntity> blockEntityProvider) {
        super(BlockConstants.PROPERTIES);
        this.blockEntityProvider = blockEntityProvider;
    }

    @Override
    protected DirectionType<BiDirection> getDirectionType() {
        return BiDirectionType.INSTANCE;
    }

    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state) {
        return blockEntityProvider.create(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(final Level level,
                                                                  final BlockState state,
                                                                  final BlockEntityType<T> type) {
        return TICKER.get(level, type);
    }

    public BlockItem createBlockItem() {
        return new NetworkNodeBlockItem(this, HELP);
    }
}
