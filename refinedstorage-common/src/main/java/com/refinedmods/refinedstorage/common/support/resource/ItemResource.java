package com.refinedmods.refinedstorage.common.support.resource;

import com.refinedmods.refinedstorage.api.core.CoreValidations;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.common.api.support.resource.FuzzyModeNormalizer;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceTag;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceType;

import java.util.List;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apiguardian.api.API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@API(status = API.Status.INTERNAL)
public record ItemResource(Item item, DataComponentPatch components)
    implements PlatformResourceKey, FuzzyModeNormalizer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ItemResource.class);

    public ItemResource(final Item item) {
        this(item, DataComponentPatch.EMPTY);
    }

    public ItemResource(final Item item, final DataComponentPatch components) {
        this.item = CoreValidations.validateNotNull(item, "Item must not be null");
        this.components = CoreValidations.validateNotNull(components, "Components must not be null");
    }

    public ItemStack toItemStack() {
        return toItemStack(1);
    }

    public ItemStack toItemStack(final long amount) {
        if (amount > Integer.MAX_VALUE) {
            LOGGER.warn("Truncating too large amount for {} to fit into ItemStack {}", this, amount);
        }
        return new ItemStack(BuiltInRegistries.ITEM.wrapAsHolder(item), (int) amount, components);
    }

    @Override
    public ResourceKey normalize() {
        return new ItemResource(item);
    }

    @Override
    public long getInterfaceExportLimit() {
        return item.getDefaultMaxStackSize();
    }

    @Override
    public long getProcessingPatternLimit() {
        return item.getDefaultMaxStackSize();
    }

    @Override
    public List<ResourceTag> getTags() {
        return BuiltInRegistries.ITEM.wrapAsHolder(item)
            .tags()
            .flatMap(tagKey -> BuiltInRegistries.ITEM.getTag(tagKey).stream())
            .map(tag -> new ResourceTag(
                tag.key(),
                tag.stream().map(holder -> (PlatformResourceKey) new ItemResource(holder.value())).toList()
            )).toList();
    }

    @Override
    public ResourceType getResourceType() {
        return ResourceTypes.ITEM;
    }

    public static ItemResource ofItemStack(final ItemStack itemStack) {
        return new ItemResource(itemStack.getItem(), itemStack.getComponentsPatch());
    }
}
