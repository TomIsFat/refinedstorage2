package com.refinedmods.refinedstorage.api.network.impl.node.detector;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.StorageImpl;
import com.refinedmods.refinedstorage.network.test.AddNetworkNode;
import com.refinedmods.refinedstorage.network.test.InjectNetworkStorageComponent;
import com.refinedmods.refinedstorage.network.test.NetworkTest;
import com.refinedmods.refinedstorage.network.test.SetupNetwork;
import com.refinedmods.refinedstorage.network.test.nodefactory.AbstractNetworkNodeFactory;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.A;
import static org.assertj.core.api.Assertions.assertThat;

@NetworkTest
@SetupNetwork
class DetectorNetworkNodeTest {
    private static final long ENERGY_USAGE = 3;

    @AddNetworkNode(properties = {
        @AddNetworkNode.Property(key = AbstractNetworkNodeFactory.PROPERTY_ENERGY_USAGE, longValue = ENERGY_USAGE)
    })
    DetectorNetworkNode sut;

    @BeforeEach
    void setUp(@InjectNetworkStorageComponent final StorageNetworkComponent storage) {
        storage.addSource(new StorageImpl());
        sut.setAmountStrategy(new DetectorAmountStrategyImpl());
    }

    @Test
    void testWithoutNetwork() {
        // Act
        sut.setConfiguredResource(A);
        sut.setNetwork(null);

        // Assert
        assertThat(sut.isActivated()).isFalse();
        assertThat(sut.getAmount()).isZero();
        assertThat(sut.getMode()).isEqualTo(DetectorMode.EQUAL);
        assertThat(sut.getEnergyUsage()).isEqualTo(ENERGY_USAGE);
    }

    @Test
    void testWithoutActiveness() {
        // Act
        sut.setConfiguredResource(A);
        sut.setActive(false);

        // Assert
        assertThat(sut.isActivated()).isFalse();
        assertThat(sut.getAmount()).isZero();
        assertThat(sut.getMode()).isEqualTo(DetectorMode.EQUAL);
        assertThat(sut.getEnergyUsage()).isEqualTo(ENERGY_USAGE);
    }

    @Test
    void testWithoutConfiguredResource() {
        // Assert
        assertThat(sut.isActivated()).isFalse();
        assertThat(sut.getAmount()).isZero();
        assertThat(sut.getMode()).isEqualTo(DetectorMode.EQUAL);
        assertThat(sut.getEnergyUsage()).isEqualTo(ENERGY_USAGE);
    }

    @ParameterizedTest
    @EnumSource(DetectorMode.class)
    void testWithConfiguredResourceButWithoutResourceInNetwork(final DetectorMode mode) {
        // Arrange
        sut.setConfiguredResource(A);
        sut.setMode(mode);

        // Act
        final boolean activated = sut.isActivated();

        // Assert
        if (mode == DetectorMode.EQUAL) {
            assertThat(activated).isTrue();
        } else {
            assertThat(activated).isFalse();
        }
        assertThat(sut.getAmount()).isZero();
        assertThat(sut.getMode()).isEqualTo(mode);
    }

    public static Stream<Arguments> testCases() {
        return Stream.of(
            Arguments.of(DetectorMode.EQUAL, 10, 0, false),
            Arguments.of(DetectorMode.EQUAL, 10, 9, false),
            Arguments.of(DetectorMode.EQUAL, 10, 10, true),
            Arguments.of(DetectorMode.EQUAL, 10, 11, false),

            Arguments.of(DetectorMode.ABOVE, 10, 0, false),
            Arguments.of(DetectorMode.ABOVE, 10, 10, false),
            Arguments.of(DetectorMode.ABOVE, 10, 11, true),
            Arguments.of(DetectorMode.ABOVE, 10, 12, true),

            Arguments.of(DetectorMode.UNDER, 10, 0, true),
            Arguments.of(DetectorMode.UNDER, 10, 9, true),
            Arguments.of(DetectorMode.UNDER, 10, 10, false),
            Arguments.of(DetectorMode.UNDER, 10, 11, false)
        );
    }

    @ParameterizedTest
    @MethodSource("testCases")
    void testModes(final DetectorMode mode,
                   final long comparisonAmount,
                   final long amountInNetwork,
                   final boolean expectedActivated,
                   @InjectNetworkStorageComponent final StorageNetworkComponent storage) {
        // Arrange
        sut.setConfiguredResource(A);
        sut.setMode(mode);
        sut.setAmount(comparisonAmount);

        if (amountInNetwork > 0) {
            storage.insert(A, amountInNetwork, Action.EXECUTE, Actor.EMPTY);
        }

        // Act
        final boolean activated = sut.isActivated();

        // Assert
        assertThat(activated).isEqualTo(expectedActivated);
        assertThat(sut.getAmount()).isEqualTo(comparisonAmount);
    }
}
