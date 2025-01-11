package com.refinedmods.refinedstorage.api.storage.root;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class RootStorageListenerTest {
    @Test
    void testZero() {
        // Arrange
        final RootStorageListener.InterceptResult interceptResult = new RootStorageListener.InterceptResult(0, 0);

        // Act
        final long reserved = interceptResult.reserved();
        final long intercepted = interceptResult.intercepted();

        // Assert
        assertThat(reserved).isZero();
        assertThat(intercepted).isZero();
    }

    @Test
    void shouldNotAllowNegativeReserved() {
        // Act
        final Throwable thrown = catchThrowable(() -> new RootStorageListener.InterceptResult(-1, 0));

        // Assert
        assertThat(thrown).isInstanceOf(IllegalArgumentException.class).hasMessage("Reserved may not be negative");
    }

    @Test
    void shouldNotAllowNegativeIntercepted() {
        // Act
        final Throwable thrown = catchThrowable(() -> new RootStorageListener.InterceptResult(0, -1));

        // Assert
        assertThat(thrown).isInstanceOf(IllegalArgumentException.class).hasMessage("Intercepted may not be negative");
    }

    @Test
    void shouldAllowEqualReservedAndIntercepted() {
        // Act
        final RootStorageListener.InterceptResult interceptResult = new RootStorageListener.InterceptResult(1, 1);

        // Assert
        assertThat(interceptResult.reserved()).isEqualTo(1);
        assertThat(interceptResult.intercepted()).isEqualTo(1);
    }

    @Test
    void shouldAllowInterceptedLessThanReserved() {
        // Act
        final RootStorageListener.InterceptResult interceptResult = new RootStorageListener.InterceptResult(2, 1);

        // Assert
        assertThat(interceptResult.reserved()).isEqualTo(2);
        assertThat(interceptResult.intercepted()).isEqualTo(1);
    }

    @Test
    void shouldNotAllowInterceptedGreaterThanReserved() {
        // Act
        final Throwable thrown = catchThrowable(() -> new RootStorageListener.InterceptResult(1, 2));

        // Assert
        assertThat(thrown).isInstanceOf(IllegalArgumentException.class)
            .hasMessage("May not intercept 2 when only 1 is reserved");
    }
}
