package com.refinedmods.refinedstorage.common.api.storage;

import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;

import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.apiguardian.api.API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.4")
public abstract class AbstractStorageContainerBlockItem extends BlockItem {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractStorageContainerBlockItem.class);

    protected final StorageContainerItemHelper helper;

    protected AbstractStorageContainerBlockItem(
        final Block block,
        final Properties properties,
        final StorageContainerItemHelper helper
    ) {
        super(block, properties);
        this.helper = helper;
    }

    @Override
    public void inventoryTick(final ItemStack stack,
                              final Level level,
                              final Entity entity,
                              final int slotId,
                              final boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        helper.transferStorageIfNecessary(stack, level, entity, this::createStorage);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level level, final Player player, final InteractionHand hand) {
        final ItemStack stack = player.getItemInHand(hand);
        return helper.tryDisassembly(
            level,
            player,
            stack,
            createPrimaryDisassemblyByproduct(stack.getCount()),
            createSecondaryDisassemblyByproduct(stack.getCount())
        );
    }

    @Override
    protected boolean updateCustomBlockEntityTag(final BlockPos pos,
                                                 final Level level,
                                                 @Nullable final Player player,
                                                 final ItemStack stack,
                                                 final BlockState state) {
        if (!level.isClientSide()) {
            updateBlockEntityTag(pos, level, stack);
        }
        return super.updateCustomBlockEntityTag(pos, level, player, stack, state);
    }

    private void updateBlockEntityTag(final BlockPos pos,
                                      final Level level,
                                      final ItemStack stack) {
        if (level.getBlockEntity(pos) instanceof StorageBlockEntity blockEntity) {
            helper.transferToBlockEntity(stack, blockEntity);
        } else {
            LOGGER.warn("Storage could not be set, block entity does not exist yet at {}", pos);
        }
    }

    @Override
    public void appendHoverText(final ItemStack stack,
                                final TooltipContext context,
                                final List<Component> tooltip,
                                final TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        final StorageRepository storageRepository = RefinedStorageApi.INSTANCE.getClientStorageRepository();
        helper.appendToTooltip(stack, storageRepository, tooltip, flag, this::formatAmount, getCapacity());
    }

    @Nullable
    protected abstract Long getCapacity();

    protected abstract String formatAmount(long amount);

    protected abstract SerializableStorage createStorage(StorageRepository storageRepository);

    protected abstract ItemStack createPrimaryDisassemblyByproduct(int count);

    @Nullable
    protected abstract ItemStack createSecondaryDisassemblyByproduct(int count);
}
