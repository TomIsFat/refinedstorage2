package com.refinedmods.refinedstorage.common.storage.diskinterface;

import com.refinedmods.refinedstorage.api.network.impl.node.storagetransfer.StorageTransferMode;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.filter.FilterMode;
import com.refinedmods.refinedstorage.common.util.IdentifierUtil;

import java.util.Set;

import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.refinedmods.refinedstorage.common.GameTestUtil.RSITEMS;
import static com.refinedmods.refinedstorage.common.GameTestUtil.asResource;
import static com.refinedmods.refinedstorage.common.GameTestUtil.getItemAsDamaged;
import static com.refinedmods.refinedstorage.common.GameTestUtil.insert;
import static com.refinedmods.refinedstorage.common.GameTestUtil.networkIsAvailable;
import static com.refinedmods.refinedstorage.common.GameTestUtil.storageContainsExactly;
import static com.refinedmods.refinedstorage.common.GameTestUtil.storageIsEmpty;
import static com.refinedmods.refinedstorage.common.storage.diskinterface.DiskInterfaceTestPlots.addDiskToDiskInterface;
import static com.refinedmods.refinedstorage.common.storage.diskinterface.DiskInterfaceTestPlots.isDiskInOutputWithAmount;
import static com.refinedmods.refinedstorage.common.storage.diskinterface.DiskInterfaceTestPlots.preparePlot;
import static net.minecraft.world.item.Items.DIAMOND_CHESTPLATE;
import static net.minecraft.world.item.Items.DIRT;

@GameTestHolder(IdentifierUtil.MOD_ID)
@PrefixGameTestTemplate(false)
public final class DiskInterfaceTest {
    private DiskInterfaceTest() {
    }

    @GameTest(template = "empty_15x15")
    public static void shouldInsertItemsIntoNetwork(final GameTestHelper helper) {
        preparePlot(helper, Direction.NORTH, (diskInterface, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network ->
                insert(helper, network, DIRT, 64)));

            // Act
            diskInterface.setTransferMode(StorageTransferMode.INSERT_INTO_NETWORK);

            // Assert
            sequence
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(DIRT), 64)
                ))
                .thenExecute(() -> addDiskToDiskInterface(
                    helper,
                    pos,
                    new ResourceAmount(asResource(DIRT), 10)))
                .thenIdle(9 * 10)
                .thenExecute(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(DIRT), 74)
                ))
                .thenExecute(() -> isDiskInOutputWithAmount(helper, pos, 0))
                .thenSucceed();
        });
    }

    @GameTest(template = "empty_15x15")
    public static void shouldInsertItemsIntoNetworkWithStackUpgrade(final GameTestHelper helper) {
        preparePlot(helper, Direction.NORTH, (diskInterface, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network ->
                insert(helper, network, DIRT, 64)));

            // Act
            diskInterface.setTransferMode(StorageTransferMode.INSERT_INTO_NETWORK);
            diskInterface.addUpgrade(RSITEMS.getStackUpgrade().getDefaultInstance());

            // Assert
            sequence
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(DIRT), 64)
                ))
                .thenExecute(() -> addDiskToDiskInterface(
                    helper,
                    pos,
                    new ResourceAmount(asResource(DIRT), 64)))
                .thenIdle(9)
                .thenExecute(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(DIRT), 128)
                ))
                .thenExecute(() -> isDiskInOutputWithAmount(helper, pos, 0))
                .thenSucceed();
        });
    }

    @GameTest(template = "empty_15x15")
    public static void shouldInsertItemsIntoNetworkBlocklist(final GameTestHelper helper) {
        preparePlot(helper, Direction.NORTH, (diskInterface, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network ->
                insert(helper, network, DIRT, 64)));

            // Act
            final ItemStack damagedDiamondChestplate = getItemAsDamaged(DIAMOND_CHESTPLATE.getDefaultInstance(), 500);

            diskInterface.setTransferMode(StorageTransferMode.INSERT_INTO_NETWORK);
            diskInterface.setFuzzyMode(false);
            diskInterface.setFilters(Set.of(asResource(damagedDiamondChestplate)));
            diskInterface.setFilterMode(FilterMode.BLOCK);

            // Assert
            sequence
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(DIRT), 64)
                ))
                .thenExecute(() -> addDiskToDiskInterface(
                    helper,
                    pos,
                    new ResourceAmount(asResource(DIRT), 5),
                    new ResourceAmount(asResource(damagedDiamondChestplate), 1)
                ))
                .thenIdle(9 * 6)
                .thenExecute(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(DIRT), 69)
                ))
                .thenExecute(() -> isDiskInOutputWithAmount(helper, pos, 1))
                .thenSucceed();
        });
    }

    @GameTest(template = "empty_15x15")
    public static void shouldInsertItemsIntoNetworkFuzzyBlocklist(final GameTestHelper helper) {
        preparePlot(helper, Direction.NORTH, (diskInterface, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network ->
                insert(helper, network, DIRT, 64)));

            // Act
            final ItemStack damagedDiamondChestplate = getItemAsDamaged(DIAMOND_CHESTPLATE.getDefaultInstance(), 500);

            diskInterface.setTransferMode(StorageTransferMode.INSERT_INTO_NETWORK);
            diskInterface.setFuzzyMode(true);
            diskInterface.setFilters(Set.of(asResource(DIAMOND_CHESTPLATE.getDefaultInstance())));
            diskInterface.setFilterMode(FilterMode.BLOCK);

            // Assert
            sequence
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(DIRT), 64)
                ))
                .thenExecute(() -> addDiskToDiskInterface(
                    helper,
                    pos,
                    new ResourceAmount(asResource(DIRT), 5),
                    new ResourceAmount(asResource(damagedDiamondChestplate), 1),
                    new ResourceAmount(asResource(DIAMOND_CHESTPLATE.getDefaultInstance()), 1)
                ))
                .thenIdle(9 * 7)
                .thenExecute(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(DIRT), 69)
                ))
                .thenExecute(() -> isDiskInOutputWithAmount(helper, pos, 2))
                .thenSucceed();
        });
    }

    @GameTest(template = "empty_15x15")
    public static void shouldExtractItemsFromNetwork(final GameTestHelper helper) {
        preparePlot(helper, Direction.NORTH, (diskInterface, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network ->
                insert(helper, network, DIRT, 10)));

            // Act
            diskInterface.setTransferMode(StorageTransferMode.EXTRACT_FROM_NETWORK);

            // Assert
            sequence
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(DIRT), 10)
                ))
                .thenExecute(() -> addDiskToDiskInterface(helper, pos))
                .thenIdle(9 * 10)
                .thenExecute(storageIsEmpty(helper, pos))
                .thenExecute(() -> isDiskInOutputWithAmount(helper, pos, 10))
                .thenSucceed();
        });
    }

    @GameTest(template = "empty_15x15")
    public static void shouldExtractItemsFromNetworkWithStackUpgrade(final GameTestHelper helper) {
        preparePlot(helper, Direction.NORTH, (diskInterface, pos, sequence) -> {
            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network ->
                insert(helper, network, DIRT, 64)));

            // Act
            diskInterface.setTransferMode(StorageTransferMode.EXTRACT_FROM_NETWORK);
            diskInterface.addUpgrade(RSITEMS.getStackUpgrade().getDefaultInstance());

            // Assert
            sequence
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(DIRT), 64)
                ))
                .thenExecute(() -> addDiskToDiskInterface(helper, pos))
                .thenIdle(9)
                .thenExecute(storageIsEmpty(helper, pos))
                .thenExecute(() -> isDiskInOutputWithAmount(helper, pos, 64))
                .thenSucceed();
        });
    }

    @GameTest(template = "empty_15x15")
    public static void shouldExtractItemsFromNetworkBlocklist(final GameTestHelper helper) {
        preparePlot(helper, Direction.NORTH, (diskInterface, pos, sequence) -> {
            final ItemStack damagedDiamondChestplate = getItemAsDamaged(DIAMOND_CHESTPLATE.getDefaultInstance(), 500);

            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
                insert(helper, network, DIRT, 5);
                insert(helper, network, asResource(damagedDiamondChestplate), 1);
            }));

            // Act
            diskInterface.setTransferMode(StorageTransferMode.EXTRACT_FROM_NETWORK);
            diskInterface.setFuzzyMode(false);
            diskInterface.setFilters(Set.of(asResource(damagedDiamondChestplate)));
            diskInterface.setFilterMode(FilterMode.BLOCK);

            // Assert
            sequence
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(DIRT), 5),
                    new ResourceAmount(asResource(damagedDiamondChestplate), 1)
                ))
                .thenExecute(() -> addDiskToDiskInterface(helper, pos))
                .thenIdle(9 * 6)
                .thenExecute(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(damagedDiamondChestplate), 1)
                ))
                .thenExecute(() -> isDiskInOutputWithAmount(helper, pos, 5))
                .thenSucceed();
        });
    }

    @GameTest(template = "empty_15x15")
    public static void shouldExtractItemsFromNetworkFuzzyBlocklist(final GameTestHelper helper) {
        preparePlot(helper, Direction.NORTH, (diskInterface, pos, sequence) -> {
            final ItemStack damagedDiamondChestplate = getItemAsDamaged(DIAMOND_CHESTPLATE.getDefaultInstance(), 500);

            // Arrange
            sequence.thenWaitUntil(networkIsAvailable(helper, pos, network -> {
                insert(helper, network, DIRT, 5);
                insert(helper, network, asResource(damagedDiamondChestplate), 1);
                insert(helper, network, DIAMOND_CHESTPLATE, 1);
            }));

            // Act
            diskInterface.setTransferMode(StorageTransferMode.EXTRACT_FROM_NETWORK);
            diskInterface.setFuzzyMode(true);
            diskInterface.setFilters(Set.of(asResource(DIAMOND_CHESTPLATE.getDefaultInstance())));
            diskInterface.setFilterMode(FilterMode.BLOCK);

            // Assert
            sequence
                .thenWaitUntil(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(DIRT), 5),
                    new ResourceAmount(asResource(damagedDiamondChestplate), 1),
                    new ResourceAmount(asResource(DIAMOND_CHESTPLATE), 1)
                ))
                .thenExecute(() -> addDiskToDiskInterface(helper, pos))
                .thenIdle(9 * 7)
                .thenExecute(storageContainsExactly(
                    helper,
                    pos,
                    new ResourceAmount(asResource(damagedDiamondChestplate), 1),
                    new ResourceAmount(asResource(DIAMOND_CHESTPLATE), 1)
                ))
                .thenExecute(() -> isDiskInOutputWithAmount(helper, pos, 5))
                .thenSucceed();
        });
    }
}
