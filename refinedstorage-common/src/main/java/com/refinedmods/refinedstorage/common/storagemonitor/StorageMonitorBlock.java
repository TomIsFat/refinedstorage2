package com.refinedmods.refinedstorage.common.storagemonitor;

import com.refinedmods.refinedstorage.common.content.BlockConstants;
import com.refinedmods.refinedstorage.common.content.BlockEntities;
import com.refinedmods.refinedstorage.common.support.AbstractBlockEntityTicker;
import com.refinedmods.refinedstorage.common.support.AbstractDirectionalBlock;
import com.refinedmods.refinedstorage.common.support.NetworkNodeBlockItem;
import com.refinedmods.refinedstorage.common.support.direction.BiDirection;
import com.refinedmods.refinedstorage.common.support.direction.BiDirectionType;
import com.refinedmods.refinedstorage.common.support.direction.DirectionType;
import com.refinedmods.refinedstorage.common.support.network.NetworkNodeBlockEntityTicker;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

public class StorageMonitorBlock extends AbstractDirectionalBlock<BiDirection> implements EntityBlock {
    private static final Component HELP = createTranslation("item", "storage_monitor.help");
    private static final AbstractBlockEntityTicker<StorageMonitorBlockEntity> TICKER =
        new NetworkNodeBlockEntityTicker<>(BlockEntities.INSTANCE::getStorageMonitor);

    public StorageMonitorBlock() {
        super(BlockConstants.PROPERTIES.strength(2.5F, 6.0F));
    }

    @Override
    protected DirectionType<BiDirection> getDirectionType() {
        return BiDirectionType.INSTANCE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state) {
        return new StorageMonitorBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(final Level level,
                                                                  final BlockState state,
                                                                  final BlockEntityType<T> type) {
        return TICKER.get(level, type);
    }

    @Override
    protected ItemInteractionResult useItemOn(final ItemStack stack,
                                              final BlockState state,
                                              final Level level,
                                              final BlockPos pos,
                                              final Player player,
                                              final InteractionHand hand,
                                              final BlockHitResult hitResult) {
        if (player.isCrouching()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!level.isClientSide()) {
            final BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof StorageMonitorBlockEntity storageMonitor) {
                storageMonitor.insert(player, hand);
            }
        }
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    public void attack(final BlockState state, final Level level, final BlockPos pos, final Player player) {
        super.attack(state, level, pos, player);
        if (level.isClientSide()) {
            return;
        }
        if (!(level.getBlockEntity(pos) instanceof StorageMonitorBlockEntity storageMonitor)) {
            return;
        }
        final BiDirection direction = getDirection(state);
        if (direction == null) {
            return;
        }
        final Direction hitDirection = getHitDirection(level, player);
        if (hitDirection != direction.asDirection()) {
            return;
        }
        storageMonitor.extract((ServerPlayer) player);
    }

    private Direction getHitDirection(final Level level, final Player player) {
        final Vec3 base = player.getEyePosition(1.0F);
        final Vec3 look = player.getLookAngle();
        final Vec3 target = base.add(look.x * 20, look.y * 20, look.z * 20);
        return level.clip(new ClipContext(
            base,
            target,
            ClipContext.Block.OUTLINE,
            ClipContext.Fluid.NONE,
            player
        )).getDirection();
    }

    public BlockItem createBlockItem() {
        return new NetworkNodeBlockItem(this, HELP);
    }
}
