package com.refinedmods.refinedstorage.neoforge.autocrafting;

import com.refinedmods.refinedstorage.api.autocrafting.task.ExternalPatternInputSink;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.autocrafting.PatternProviderExternalPatternInputSink;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;
import com.refinedmods.refinedstorage.neoforge.storage.CapabilityCache;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ItemHandlerExternalPatternProviderInputSink implements PatternProviderExternalPatternInputSink {
    private static final Logger LOGGER = LoggerFactory.getLogger(ItemHandlerExternalPatternProviderInputSink.class);

    private final CapabilityCache capabilityCache;

    ItemHandlerExternalPatternProviderInputSink(final CapabilityCache capabilityCache) {
        this.capabilityCache = capabilityCache;
    }

    @Override
    public ExternalPatternInputSink.Result accept(final Collection<ResourceAmount> resources, final Action action) {
        return capabilityCache.getItemHandler()
            .map(handler -> accept(resources, action, handler))
            .orElse(ExternalPatternInputSink.Result.SKIPPED);
    }

    private ExternalPatternInputSink.Result accept(final Collection<ResourceAmount> resources,
                                                   final Action action,
                                                   final IItemHandler handler) {
        final Deque<ItemStack> stacks = getStacks(resources);
        ItemStack current = stacks.poll();
        final List<Integer> availableSlots = IntStream.range(0, handler.getSlots())
            .boxed()
            .collect(Collectors.toList());
        while (current != null && !availableSlots.isEmpty()) {
            final ItemStack remainder = insert(action, handler, availableSlots, current);
            if (remainder.isEmpty()) {
                current = stacks.poll();
            } else if (current.getCount() == remainder.getCount()) {
                break;
            } else {
                current = remainder;
            }
        }
        final boolean success = current == null && stacks.isEmpty();
        if (!success && action == Action.EXECUTE) {
            LOGGER.warn(
                "{} unexpectedly left {} as a remainder, which has been voided",
                handler,
                stacks
            );
        }
        return success ? ExternalPatternInputSink.Result.ACCEPTED : ExternalPatternInputSink.Result.REJECTED;
    }

    private ItemStack insert(final Action action,
                             final IItemHandler handler,
                             final List<Integer> availableSlots,
                             final ItemStack current) {
        ItemStack remainder = ItemStack.EMPTY;
        for (int i = 0; i < availableSlots.size(); ++i) {
            final int slot = availableSlots.get(i);
            remainder = handler.insertItem(slot, current.copy(), action == Action.SIMULATE);
            if (remainder.isEmpty() || current.getCount() != remainder.getCount()) {
                availableSlots.remove(i);
                break;
            }
        }
        return remainder;
    }

    private static ArrayDeque<ItemStack> getStacks(final Collection<ResourceAmount> resources) {
        return new ArrayDeque<>(resources.stream()
            .filter(resourceAmount -> resourceAmount.resource() instanceof ItemResource)
            .map(resourceAmount -> {
                final ItemResource itemResource = (ItemResource) resourceAmount.resource();
                return itemResource.toItemStack(resourceAmount.amount());
            }).toList());
    }
}
