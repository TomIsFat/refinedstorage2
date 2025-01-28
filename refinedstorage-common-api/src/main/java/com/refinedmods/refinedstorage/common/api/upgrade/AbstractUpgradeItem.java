package com.refinedmods.refinedstorage.common.api.upgrade;

import java.util.Optional;
import java.util.Set;

import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.2")
public abstract class AbstractUpgradeItem extends Item implements UpgradeItem {
    private final UpgradeRegistry registry;
    private final Component helpText;

    protected AbstractUpgradeItem(final Properties properties,
                                  final UpgradeRegistry registry,
                                  final Component helpText) {
        super(properties);
        this.registry = registry;
        this.helpText = helpText;
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(final ItemStack stack) {
        final Set<UpgradeMapping> destinations = getDestinations();
        if (destinations.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new UpgradeDestinationTooltipComponent(destinations, helpText));
    }

    protected final Set<UpgradeMapping> getDestinations() {
        return registry.getByUpgradeItem(this);
    }

    public record UpgradeDestinationTooltipComponent(Set<UpgradeMapping> destinations, Component helpText)
        implements TooltipComponent {
    }
}

