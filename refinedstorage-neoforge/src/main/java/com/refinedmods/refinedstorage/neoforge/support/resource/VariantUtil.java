package com.refinedmods.refinedstorage.neoforge.support.resource;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.common.support.resource.FluidResource;

import java.util.Objects;

import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class VariantUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(VariantUtil.class);

    private VariantUtil() {
    }

    public static boolean isSame(final FluidResource resource, final FluidStack stack) {
        return resource.fluid() == stack.getFluid() && Objects.equals(
            resource.components(),
            stack.getComponents().asPatch()
        );
    }

    public static FluidResource ofFluidStack(final FluidStack fluidStack) {
        return new FluidResource(fluidStack.getFluid(), fluidStack.getComponents().asPatch());
    }

    public static FluidStack toFluidStack(final FluidResource fluidResource, final long amount) {
        if (amount > Integer.MAX_VALUE) {
            LOGGER.warn("Truncating too large amount for {} to fit into FluidStack {}", fluidResource, amount);
        }
        return new FluidStack(
            BuiltInRegistries.FLUID.wrapAsHolder(fluidResource.fluid()),
            (int) amount,
            fluidResource.components()
        );
    }

    public static Action toAction(final IFluidHandler.FluidAction action) {
        return action == IFluidHandler.FluidAction.SIMULATE ? Action.SIMULATE : Action.EXECUTE;
    }

    public static IFluidHandler.FluidAction toFluidAction(final Action action) {
        return action == Action.SIMULATE ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE;
    }
}
