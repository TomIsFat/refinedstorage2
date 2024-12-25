package com.refinedmods.refinedstorage.api.autocrafting.preview;

import java.util.Collections;
import java.util.List;

import org.assertj.core.api.ThrowableAssert;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.OAK_LOG;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.OAK_PLANKS;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.SPRUCE_LOG;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class PreviewBuilderTest {
    private static final RecursiveComparisonConfiguration CONFIG = RecursiveComparisonConfiguration.builder()
        .withIgnoredCollectionOrderInFields("items")
        .build();

    @Test
    void testDefaultState() {
        // Act
        final Preview preview = PreviewBuilder.ofType(PreviewType.SUCCESS).build();

        // Assert
        assertThat(preview).usingRecursiveComparison()
            .isEqualTo(new Preview(PreviewType.SUCCESS, Collections.emptyList()));
    }

    @Test
    void testPreview() {
        // Act
        final Preview preview = PreviewBuilder.ofType(PreviewType.MISSING_RESOURCES)
            .addToCraft(OAK_PLANKS, 4)
            .addToCraft(OAK_PLANKS, 1)
            .addAvailable(OAK_LOG, 1)
            .addAvailable(OAK_LOG, 2)
            .addMissing(SPRUCE_LOG, 1)
            .addMissing(SPRUCE_LOG, 2)
            .build();

        // Assert
        assertThat(preview)
            .usingRecursiveComparison(CONFIG)
            .isEqualTo(new Preview(PreviewType.MISSING_RESOURCES, List.of(
                new PreviewItem(OAK_PLANKS, 0, 0, 5),
                new PreviewItem(OAK_LOG, 3, 0, 0),
                new PreviewItem(SPRUCE_LOG, 0, 3, 0)
            )));
    }

    @ParameterizedTest
    @ValueSource(longs = {-1, 0})
    void testToCraftMustBeLargerThanZero(final long amount) {
        // Arrange
        final PreviewBuilder builder = PreviewBuilder.ofType(PreviewType.MISSING_RESOURCES);

        // Act
        final ThrowableAssert.ThrowingCallable action = () -> builder.addToCraft(OAK_PLANKS, amount);

        // Assert
        assertThatThrownBy(action)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("To craft amount must be larger than 0");
    }

    @ParameterizedTest
    @ValueSource(longs = {-1, 0})
    void testMissingMustBeLargerThanZero(final long amount) {
        // Arrange
        final PreviewBuilder builder = PreviewBuilder.ofType(PreviewType.MISSING_RESOURCES);

        // Act
        final ThrowableAssert.ThrowingCallable action = () -> builder.addMissing(OAK_PLANKS, amount);

        // Assert
        assertThatThrownBy(action)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Missing amount must be larger than 0");
    }

    @ParameterizedTest
    @ValueSource(longs = {-1, 0})
    void testAvailableMustBeLargerThanZero(final long amount) {
        // Arrange
        final PreviewBuilder builder = PreviewBuilder.ofType(PreviewType.MISSING_RESOURCES);

        // Act
        final ThrowableAssert.ThrowingCallable action = () -> builder.addAvailable(OAK_PLANKS, amount);

        // Assert
        assertThatThrownBy(action)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Available amount must be larger than 0");
    }
}
