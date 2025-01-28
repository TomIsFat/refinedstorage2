package com.refinedmods.refinedstorage.common.support.containermenu;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.support.AbstractBaseContainerMenu;
import com.refinedmods.refinedstorage.common.support.packet.c2s.C2SPackets;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public abstract class AbstractResourceContainerMenu extends AbstractBaseContainerMenu {
    @Nullable
    protected final Player player;

    private final List<ResourceSlot> resourceSlots = new ArrayList<>();

    protected AbstractResourceContainerMenu(@Nullable final MenuType<?> type, final int syncId, final Player player) {
        super(type, syncId);
        this.player = player;
    }

    protected AbstractResourceContainerMenu(@Nullable final MenuType<?> type, final int syncId) {
        super(type, syncId);
        this.player = null;
    }

    private Optional<ResourceSlot> getResourceSlot(final int slotIndex) {
        if (slotIndex < 0 || slotIndex >= slots.size()) {
            return Optional.empty();
        }
        if (slots.get(slotIndex) instanceof ResourceSlot resourceSlot) {
            return Optional.of(resourceSlot);
        }
        return Optional.empty();
    }

    public void handleResourceSlotUpdate(final int slotIndex, @Nullable final ResourceAmount resourceAmount) {
        getResourceSlot(slotIndex).ifPresent(slot -> slot.change(resourceAmount));
    }

    public void handleResourceFilterSlotUpdate(final int slotIndex, final PlatformResourceKey resource) {
        getResourceSlot(slotIndex).ifPresent(slot -> slot.setFilter(resource));
    }

    public void handleResourceSlotChange(final int slotIndex, final boolean tryAlternatives) {
        getResourceSlot(slotIndex).ifPresent(slot -> slot.change(getCarried(), tryAlternatives));
    }

    public void sendResourceSlotChange(final int slotIndex, final boolean tryAlternatives) {
        C2SPackets.sendResourceSlotChange(slotIndex, tryAlternatives);
    }

    public void handleResourceSlotAmountChange(final int slotIndex, final long amount) {
        getResourceSlot(slotIndex).ifPresent(slot -> slot.changeAmount(amount));
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (player == null) {
            return;
        }
        for (final ResourceSlot resourceSlot : resourceSlots) {
            resourceSlot.broadcastChanges(player);
        }
    }

    public List<ResourceSlot> getResourceSlots() {
        return resourceSlots;
    }

    @Override
    protected Slot addSlot(final Slot slot) {
        if (slot instanceof ResourceSlot resourceSlot) {
            resourceSlots.add(resourceSlot);
        }
        return super.addSlot(slot);
    }

    @Override
    protected void resetSlots() {
        super.resetSlots();
        resourceSlots.clear();
    }

    public void addToResourceSlotIfNotExisting(final ItemStack stack) {
        for (final ResourceSlot resourceSlot : resourceSlots) {
            if (resourceSlot.contains(stack)) {
                return;
            }
        }
        for (final ResourceSlot resourceSlot : resourceSlots) {
            if (resourceSlot.changeIfEmpty(stack)) {
                return;
            }
        }
    }

    protected final boolean areAllResourceSlotsEmpty() {
        for (final ResourceSlot resourceSlot : resourceSlots) {
            if (!resourceSlot.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean canTakeItemForPickAll(final ItemStack stack, final Slot slot) {
        if (slot instanceof ResourceSlot resourceSlot) {
            return resourceSlot.supportsItemSlotInteractions();
        }
        return super.canTakeItemForPickAll(stack, slot);
    }

    @Override
    public void clicked(final int id, final int dragType, final ClickType clickType, final Player p) {
        final Slot slot = id >= 0 ? getSlot(id) : null;
        if (slot instanceof ResourceSlot resourceSlot
            && resourceSlot.supportsItemSlotInteractions()
            && !resourceSlot.isEmpty()
            && !getCarried().isEmpty()
        ) {
            final ItemStack result = resourceSlot.insertInto(getCarried());
            if (result != null) {
                setCarried(result);
                return;
            }
        }
        super.clicked(id, dragType, clickType, p);
    }
}
