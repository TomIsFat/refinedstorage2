package com.refinedmods.refinedstorage.api.autocrafting.calculation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AmountTest {
    @ParameterizedTest
    @ValueSource(longs = {-1L, 0L})
    void testInvalidIterations(final long iterations) {
        // Act
        final Executable action = () -> new Amount(iterations, 1);

        // Assert
        assertThrows(IllegalArgumentException.class, action);
    }

    @ParameterizedTest
    @ValueSource(longs = {-1L, 0L})
    void testInvalidAmountPerIteration(final long amountPerIteration) {
        // Act
        final Executable action = () -> new Amount(1, amountPerIteration);

        // Assert
        assertThrows(IllegalArgumentException.class, action);
    }

    @Test
    void testMinimumValid() {
        // Act
        final Amount amount = new Amount(1, 1);

        // Assert
        assertThat(amount.iterations()).isEqualTo(1);
        assertThat(amount.amountPerIteration()).isEqualTo(1);
    }

    @Test
    void testTotal() {
        // Arrange
        final Amount amount = new Amount(2, 3);

        // Act
        final long total = amount.getTotal();

        // Assert
        assertThat(total).isEqualTo(6);
    }
}
