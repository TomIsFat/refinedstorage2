package com.refinedmods.refinedstorage.api.network.impl.node.patternprovider;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.network.autocrafting.AutocraftingNetworkComponent;
import com.refinedmods.refinedstorage.network.test.AddNetworkNode;
import com.refinedmods.refinedstorage.network.test.InjectNetworkAutocraftingComponent;
import com.refinedmods.refinedstorage.network.test.NetworkTest;
import com.refinedmods.refinedstorage.network.test.SetupNetwork;
import com.refinedmods.refinedstorage.network.test.nodefactory.PatternProviderNetworkNodeFactory;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage.api.autocrafting.PatternBuilder.pattern;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.A;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.B;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.C;
import static org.assertj.core.api.Assertions.assertThat;

@NetworkTest
@SetupNetwork
class PatternProviderNetworkNodeTest {
    private static final Pattern PATTERN_A = pattern().output(A, 1).ingredient(C, 1).build();
    private static final Pattern PATTERN_B = pattern().output(B, 1).ingredient(C, 1).build();

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
        sut.setPattern(0, PATTERN_A);

        // Assert
        assertThat(autocrafting.getOutputs()).containsExactly(A);
    }

    @Test
    void shouldRemovePatternAndNotifyNetwork(
        @InjectNetworkAutocraftingComponent final AutocraftingNetworkComponent autocrafting
    ) {
        // Arrange
        sut.setPattern(0, PATTERN_A);

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
        sut.setPattern(0, PATTERN_A);

        // Act
        sut.setPattern(0, PATTERN_B);

        // Assert
        assertThat(autocrafting.getOutputs()).containsExactly(B);
    }

    @Test
    void shouldRemovePatternsFromNetworkWhenInactive(
        @InjectNetworkAutocraftingComponent final AutocraftingNetworkComponent autocrafting
    ) {
        // Arrange
        sut.setPattern(0, PATTERN_A);

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
        sut.setPattern(0, PATTERN_A);
        sut.setActive(false);

        // Act
        sut.setActive(true);

        // Assert
        assertThat(autocrafting.getOutputs()).containsExactly(A);
    }

    @Nested
    class PriorityTest {
        @AddNetworkNode(properties = {
            @AddNetworkNode.Property(key = PatternProviderNetworkNodeFactory.PROPERTY_SIZE, intValue = 3)
        })
        private PatternProviderNetworkNode other;

        @Test
        void shouldSetPatternsRespectingPriority(
            @InjectNetworkAutocraftingComponent final AutocraftingNetworkComponent autocrafting
        ) {
            // Arrange
            other.setPriority(1);
            sut.setPriority(0);

            // Act
            final Pattern patternWithLowestPriority = pattern().output(A, 1).ingredient(C, 1).build();
            sut.setPattern(0, patternWithLowestPriority);

            final Pattern patternWithHighestPriority = pattern().output(A, 1).output(B, 1).ingredient(C, 1).build();
            other.setPattern(0, patternWithHighestPriority);

            // Assert
            assertThat(autocrafting.getOutputs()).containsExactlyInAnyOrder(A, B);
            assertThat(autocrafting.getPatternsByOutput(A))
                .containsExactly(patternWithHighestPriority, patternWithLowestPriority);
        }

        @Test
        void shouldRemovePatternsRespectingPriorityWhenInactive(
            @InjectNetworkAutocraftingComponent final AutocraftingNetworkComponent autocrafting
        ) {
            // Arrange
            other.setPriority(1);
            sut.setPriority(0);

            final Pattern patternWithLowestPriority = pattern().output(A, 1).ingredient(C, 1).build();
            sut.setPattern(0, patternWithLowestPriority);

            final Pattern patternWithHighestPriority = pattern().output(A, 1).output(B, 1).ingredient(C, 1).build();
            other.setPattern(0, patternWithHighestPriority);

            // Act
            other.setActive(false);

            // Assert
            assertThat(autocrafting.getOutputs()).containsExactly(A);
            assertThat(autocrafting.getPatternsByOutput(A)).containsExactly(patternWithLowestPriority);
        }

        @Test
        void shouldAddPatternsRespectingPriorityWhenActive(
            @InjectNetworkAutocraftingComponent final AutocraftingNetworkComponent autocrafting
        ) {
            // Arrange
            other.setPriority(1);
            sut.setPriority(0);

            final Pattern patternWithLowestPriority = pattern().output(A, 1).ingredient(C, 1).build();
            sut.setPattern(0, patternWithLowestPriority);

            final Pattern patternWithHighestPriority = pattern().output(A, 1).output(B, 1).ingredient(C, 1).build();
            other.setPattern(0, patternWithHighestPriority);

            sut.setActive(false);
            other.setActive(false);

            // Act
            sut.setActive(true);
            other.setActive(true);

            // Assert
            assertThat(autocrafting.getOutputs()).containsExactlyInAnyOrder(A, B);
            assertThat(autocrafting.getPatternsByOutput(A))
                .containsExactly(patternWithHighestPriority, patternWithLowestPriority);
        }

        @Test
        void shouldModifyPriorityAfterAddingPatterns(
            @InjectNetworkAutocraftingComponent final AutocraftingNetworkComponent autocrafting
        ) {
            // Arrange
            other.setPriority(1);
            sut.setPriority(0);

            final Pattern patternWithLowestPriority = pattern().output(A, 1).ingredient(C, 1).build();
            sut.setPattern(0, patternWithLowestPriority);

            final Pattern patternWithHighestPriority = pattern().output(A, 1).output(B, 1).ingredient(C, 1).build();
            other.setPattern(0, patternWithHighestPriority);

            // Act
            sut.setPriority(2);

            // Assert
            assertThat(autocrafting.getOutputs()).containsExactlyInAnyOrder(A, B);
            assertThat(autocrafting.getPatternsByOutput(A))
                .containsExactly(patternWithLowestPriority, patternWithHighestPriority);
        }
    }
}
