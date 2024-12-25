package com.refinedmods.refinedstorage.api.network.impl.node.patternprovider;

import com.refinedmods.refinedstorage.api.network.autocrafting.AutocraftingNetworkComponent;
import com.refinedmods.refinedstorage.api.network.impl.autocrafting.PatternImpl;
import com.refinedmods.refinedstorage.network.test.AddNetworkNode;
import com.refinedmods.refinedstorage.network.test.InjectNetworkAutocraftingComponent;
import com.refinedmods.refinedstorage.network.test.NetworkTest;
import com.refinedmods.refinedstorage.network.test.SetupNetwork;
import com.refinedmods.refinedstorage.network.test.nodefactory.PatternProviderNetworkNodeFactory;

import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.A;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.B;
import static org.assertj.core.api.Assertions.assertThat;

@NetworkTest
@SetupNetwork
class PatternProviderNetworkNodeTest {
    @AddNetworkNode(properties = {
        @AddNetworkNode.Property(key = PatternProviderNetworkNodeFactory.PROPERTY_SIZE, intValue = 3)
    })
    private PatternProviderNetworkNode sut;

    @Test
    void testDefaultState(
        @InjectNetworkAutocraftingComponent final AutocraftingNetworkComponent autocrafting
    ) {
        // Assert
        assertThat(autocrafting.getOutputs()).isEmpty();
    }

    @Test
    void shouldSetPatternAndNotifyNetwork(
        @InjectNetworkAutocraftingComponent final AutocraftingNetworkComponent autocrafting
    ) {
        // Act
        final PatternImpl pattern = new PatternImpl(A);
        sut.setPattern(0, pattern);

        // Assert
        assertThat(autocrafting.getOutputs()).containsExactly(A);
    }

    @Test
    void shouldRemovePatternAndNotifyNetwork(
        @InjectNetworkAutocraftingComponent final AutocraftingNetworkComponent autocrafting
    ) {
        // Arrange
        final PatternImpl pattern = new PatternImpl(A);
        sut.setPattern(0, pattern);

        // Act
        sut.setPattern(0, null);

        // Assert
        assertThat(autocrafting.getOutputs()).isEmpty();
    }

    @Test
    void shouldReplacePatternAndNotifyNetwork(
        @InjectNetworkAutocraftingComponent final AutocraftingNetworkComponent autocrafting
    ) {
        // Arrange
        final PatternImpl pattern = new PatternImpl(A);
        sut.setPattern(0, pattern);

        // Act
        final PatternImpl replacedPattern = new PatternImpl(B);
        sut.setPattern(0, replacedPattern);

        // Assert
        assertThat(autocrafting.getOutputs()).containsExactly(B);
    }

    @Test
    void shouldRemovePatternsFromNetworkWhenInactive(
        @InjectNetworkAutocraftingComponent final AutocraftingNetworkComponent autocrafting
    ) {
        // Arrange
        final PatternImpl pattern = new PatternImpl(A);
        sut.setPattern(0, pattern);

        // Act
        sut.setActive(false);

        // Assert
        assertThat(autocrafting.getOutputs()).isEmpty();
    }

    @Test
    void shouldAddPatternsFromNetworkWhenActive(
        @InjectNetworkAutocraftingComponent final AutocraftingNetworkComponent autocrafting
    ) {
        // Arrange
        final PatternImpl pattern = new PatternImpl(A);
        sut.setPattern(0, pattern);
        sut.setActive(false);

        // Act
        sut.setActive(true);

        // Assert
        assertThat(autocrafting.getOutputs()).containsExactly(A);
    }
}
