package com.refinedmods.refinedstorage.common.autocrafting;

import com.refinedmods.refinedstorage.api.core.NullableType;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.support.FilteredContainer;

import java.util.Optional;
import java.util.function.Supplier;
import javax.annotation.Nullable;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import static com.refinedmods.refinedstorage.common.api.autocrafting.PatternProviderItem.isValid;

public class PatternInventory extends FilteredContainer {
    @Nullable
    private Listener listener;

    public PatternInventory(final int patterns, final Supplier<@NullableType Level> levelSupplier) {
        super(
            patterns,
            stack -> Optional.ofNullable(levelSupplier.get()).map(level -> isValid(stack, level)).orElse(false)
        );
    }

    public void setListener(@Nullable final Listener listener) {
        this.listener = listener;
    }

    @Override
    public ItemStack removeItem(final int slot, final int amount) {
        // Forge InvWrapper calls this instead of setItem.
        final ItemStack result = super.removeItem(slot, amount);
        if (listener != null) {
            listener.patternChanged(slot);
        }
        return result;
    }

    @Override
    public void setItem(final int slot, final ItemStack stack) {
        super.setItem(slot, stack);
        if (listener != null) {
            listener.patternChanged(slot);
        }
    }

    public long getEnergyUsage() {
        long patterns = 0;
        for (int i = 0; i < getContainerSize(); i++) {
            final ItemStack stack = getItem(i);
            if (!stack.isEmpty()) {
                patterns++;
            }
        }
        return patterns * Platform.INSTANCE.getConfig().getAutocrafter().getEnergyUsagePerPattern();
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public int getMaxStackSize(final ItemStack stack) {
        return 1;
    }

    public interface Listener {
        void patternChanged(int slot);
    }
}
