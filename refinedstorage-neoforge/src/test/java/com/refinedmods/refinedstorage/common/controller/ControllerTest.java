package com.refinedmods.refinedstorage.common.controller;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.util.IdentifierUtil;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.refinedmods.refinedstorage.common.GameTestUtil.RSBLOCKS;
import static com.refinedmods.refinedstorage.common.GameTestUtil.energyStoredExactly;
import static com.refinedmods.refinedstorage.common.GameTestUtil.networkIsAvailable;
import static com.refinedmods.refinedstorage.common.controller.ControllerTestPlots.preparePlot;

@GameTestHolder(IdentifierUtil.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ControllerTest {
    private ControllerTest() {
    }

    @GameTest(template = "empty_15x15")
    public static void shouldConsumeEnergy(final GameTestHelper helper) {
        preparePlot(helper, false, (controller, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
            }));

            // Act
            final EnergyStorage energyStorage = controller.getEnergyStorage();
            energyStorage.receive(energyStorage.getCapacity(), Action.EXECUTE);

            // Assert
            sequence
                .thenIdle(20)
                .thenExecute(() -> energyStoredExactly(energyStorage.getStored(), energyStorage.getCapacity()))
                .thenWaitUntil(() -> helper.setBlock(pos.above(), RSBLOCKS.getGrid().getDefault()))
                .thenIdle(1)
                .thenExecute(() -> energyStoredExactly(
                    energyStorage.getStored(),
                    energyStorage.getCapacity() - Platform.INSTANCE.getConfig().getGrid().getEnergyUsage()
                ))
                .thenIdle(9)
                .thenExecute(() -> energyStoredExactly(
                    energyStorage.getStored(),
                    energyStorage.getCapacity() - Platform.INSTANCE.getConfig().getGrid().getEnergyUsage() * 10
                ))
                .thenSucceed();
        });
    }

    @GameTest(template = "empty_15x15")
    public static void shouldNotConsumeEnergy(final GameTestHelper helper) {
        preparePlot(helper, true, (controller, pos, sequence) -> {
            // Arrange
            final EnergyStorage energyStorage = controller.getEnergyStorage();

            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
            }));

            // Assert
            sequence
                .thenIdle(20)
                .thenExecute(() -> energyStoredExactly(energyStorage.getStored(), energyStorage.getCapacity()))
                .thenWaitUntil(() -> helper.setBlock(pos.above(), RSBLOCKS.getGrid().getDefault()))
                .thenIdle(20)
                .thenExecute(() -> energyStoredExactly(energyStorage.getStored(), energyStorage.getCapacity()))
                .thenSucceed();
        });
    }
}
