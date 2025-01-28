package com.refinedmods.refinedstorage.api.storage;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.limited.LimitedStorageImpl;

import java.util.Objects;
import java.util.stream.Stream;
import javax.annotation.Nullable;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static com.refinedmods.refinedstorage.api.storage.TestResource.A;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Named.named;

class TransferHelperTest {
    static Stream<Arguments> provideTransfers() {
        return Stream.of(
            Arguments.of(named("partly, space in destination", TransferBuilder.create()
                .amountInSource(A, 100)
                .amountToTransfer(A, 1)
                .amountExpectedToBeTransferred(1)
                .amountExpectedAfterTransferInSource(A, 99)
                .amountExpectedAfterTransferInDestination(A, 1)
                .build())),
            Arguments.of(named("completely, space in destination", TransferBuilder.create()
                .amountInSource(A, 100)
                .amountToTransfer(A, 100)
                .amountExpectedToBeTransferred(100)
                .amountExpectedAfterTransferInDestination(A, 100)
                .build())),
            Arguments.of(named("more than is available, space in destination", TransferBuilder.create()
                .amountInSource(A, 10)
                .amountToTransfer(A, 11)
                .amountExpectedToBeTransferred(10)
                .amountExpectedAfterTransferInDestination(A, 10)
                .build())),
            Arguments.of(named("resource not existing in source", TransferBuilder.create()
                .amountToTransfer(A, 1)
                .amountExpectedToBeTransferred(0)
                .build())),
            Arguments.of(named("with remainder, space in destination", TransferBuilder.create()
                .amountInSource(A, 50)
                .amountInDestination(A, 51)
                .amountToTransfer(A, 50)
                .amountExpectedToBeTransferred(49)
                .amountExpectedAfterTransferInSource(A, 1)
                .amountExpectedAfterTransferInDestination(A, 100)
                .build())),
            Arguments.of(named("with remainder, no space in destination", TransferBuilder.create()
                .amountInSource(A, 50)
                .amountInDestination(A, 100)
                .amountToTransfer(A, 50)
                .amountExpectedToBeTransferred(0)
                .amountExpectedAfterTransferInSource(A, 50)
                .amountExpectedAfterTransferInDestination(A, 100)
                .build()))
        );
    }

    @ParameterizedTest
    @MethodSource("provideTransfers")
    void shouldTransferCorrectly(final Transfer transfer) {
        // Arrange
        final Storage source = new LimitedStorageImpl(100);
        final Storage destination = new LimitedStorageImpl(100);

        if (transfer.amountInSource != null) {
            source.insert(
                transfer.amountInSource.resource(),
                transfer.amountInSource.amount(),
                Action.EXECUTE,
                Actor.EMPTY
            );
        }

        if (transfer.amountInDestination != null) {
            destination.insert(
                transfer.amountInDestination.resource(),
                transfer.amountInDestination.amount(),
                Action.EXECUTE,
                Actor.EMPTY
            );
        }

        // Act
        final long transferred = TransferHelper.transfer(
            transfer.amountToTransfer.resource(),
            transfer.amountToTransfer.amount(),
            Actor.EMPTY,
            source,
            destination,
            null
        );

        // Assert
        assertThat(transferred).isEqualTo(transfer.amountExpectedToBeTransferred);
        if (transfer.amountExpectedAfterTransferInSource != null) {
            assertThat(source.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                transfer.amountExpectedAfterTransferInSource
            );
        } else {
            assertThat(source.getAll()).isEmpty();
        }
        if (transfer.amountExpectedAfterTransferInDestination != null) {
            assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                transfer.amountExpectedAfterTransferInDestination
            );
        } else {
            assertThat(destination.getAll()).isEmpty();
        }
    }

    @Test
    void shouldNotTransferWhenEventualExecutedExtractFromSourceFailed() {
        // Arrange
        final Storage source = new LimitedStorageImpl(100) {
            @Override
            public long extract(final ResourceKey resource, final long amount, final Action action, final Actor actor) {
                if (action == Action.EXECUTE) {
                    return 0L;
                }
                return super.extract(resource, amount, action, actor);
            }
        };
        final Storage destination = new LimitedStorageImpl(100);

        source.insert(A, 100, Action.EXECUTE, Actor.EMPTY);

        // Act
        final long transferred = TransferHelper.transfer(A, 50, Actor.EMPTY, source, destination, null);

        // Assert
        assertThat(transferred).isZero();
        assertThat(source.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 100)
        );
        assertThat(destination.getAll()).isEmpty();
    }

    @Test
    void shouldThrowExceptionWhenEventualExecutedInsertToDestinationFailed() {
        // Arrange
        final Storage source = new LimitedStorageImpl(100);
        final Storage destination = new LimitedStorageImpl(100) {
            @Override
            public long insert(final ResourceKey resource, final long amount, final Action action, final Actor actor) {
                if (action == Action.EXECUTE) {
                    return 0L;
                }
                return super.insert(resource, amount, action, actor);
            }
        };

        source.insert(A, 100, Action.EXECUTE, Actor.EMPTY);

        // Act & assert
        assertThrows(
            IllegalStateException.class,
            () -> TransferHelper.transfer(A, 50, Actor.EMPTY, source, destination, null)
        );
    }

    @Test
    void shouldRefundLeftoversToFallbackWhenEventualExecutedInsertToDestinationFailed() {
        // Arrange
        final Storage source = new LimitedStorageImpl(100);
        final Storage destination = new LimitedStorageImpl(100) {
            @Override
            public long insert(final ResourceKey resource, final long amount, final Action action, final Actor actor) {
                if (action == Action.EXECUTE) {
                    return super.insert(resource, Math.min(amount, 25), action, actor);
                }
                return super.insert(resource, amount, action, actor);
            }
        };

        source.insert(A, 100, Action.EXECUTE, Actor.EMPTY);

        // Act
        TransferHelper.transfer(A, 50, Actor.EMPTY, source, destination, source);

        // Assert
        assertThat(source.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 75)
        );
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 25)
        );
    }

    @Test
    void shouldRefundLeftoversToFallbackWhenEventualExecutedInsertToDestinationFailedEvenIfFallbackDoesNotAcceptAll() {
        // Arrange
        final StorageImpl underlyingSource = new StorageImpl();
        final Storage source = new LimitedStorageImpl(underlyingSource, 100) {
            @Override
            public long insert(final ResourceKey resource, final long amount, final Action action, final Actor actor) {
                if (action == Action.EXECUTE) {
                    // we'll try to reinsert 25, but only accept 10.
                    return super.insert(resource, Math.min(amount, 10), action, actor);
                }
                return super.insert(resource, amount, action, actor);
            }
        };
        final Storage destination = new LimitedStorageImpl(100) {
            @Override
            public long insert(final ResourceKey resource, final long amount, final Action action, final Actor actor) {
                if (action == Action.EXECUTE) {
                    return super.insert(resource, Math.min(amount, 25), action, actor);
                }
                return super.insert(resource, amount, action, actor);
            }
        };

        underlyingSource.insert(A, 100, Action.EXECUTE, Actor.EMPTY);

        // Act
        TransferHelper.transfer(A, 50, Actor.EMPTY, source, destination, source);

        // Assert
        assertThat(source.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 60)
        );
        assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount(A, 25)
        );
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void shouldNotTransferInvalidAmount() {
        // Arrange
        final Storage source = new LimitedStorageImpl(100);
        final Storage destination = new LimitedStorageImpl(100);

        // Act
        final Executable action1 = () -> TransferHelper.transfer(
            A,
            0,
            Actor.EMPTY,
            source,
            destination,
            null
        );
        final Executable action2 = () -> TransferHelper.transfer(
            A,
            -1,
            Actor.EMPTY,
            source,
            destination,
            null
        );
        final Executable action3 = () -> TransferHelper.transfer(
            null,
            1,
            Actor.EMPTY,
            source,
            destination,
            null
        );

        // Assert
        assertThrows(IllegalArgumentException.class, action1);
        assertThrows(IllegalArgumentException.class, action2);
        assertThrows(NullPointerException.class, action3);
    }

    public record Transfer(@Nullable ResourceAmount amountInSource,
                           @Nullable ResourceAmount amountInDestination,
                           ResourceAmount amountToTransfer,
                           long amountExpectedToBeTransferred,
                           @Nullable ResourceAmount amountExpectedAfterTransferInSource,
                           @Nullable ResourceAmount amountExpectedAfterTransferInDestination) {
    }

    public static class TransferBuilder {
        @Nullable
        private ResourceAmount amountInSource;
        @Nullable
        private ResourceAmount amountInDestination;
        @Nullable
        private ResourceAmount amountToTransfer;
        private long amountExpectedToBeTransferred;
        @Nullable
        private ResourceAmount amountExpectedAfterTransferInSource;
        @Nullable
        private ResourceAmount amountExpectedAfterTransferInDestination;

        private TransferBuilder() {
        }

        public static TransferBuilder create() {
            return new TransferBuilder();
        }

        public TransferBuilder amountInSource(final ResourceKey resource, final long amount) {
            this.amountInSource = new ResourceAmount(resource, amount);
            return this;
        }

        public TransferBuilder amountInDestination(final ResourceKey resource, final long amount) {
            this.amountInDestination = new ResourceAmount(resource, amount);
            return this;
        }

        public TransferBuilder amountToTransfer(final ResourceKey resource, final long amount) {
            this.amountToTransfer = new ResourceAmount(resource, amount);
            return this;
        }

        public TransferBuilder amountExpectedAfterTransferInSource(final ResourceKey resource, final long amount) {
            this.amountExpectedAfterTransferInSource = new ResourceAmount(resource, amount);
            return this;
        }

        public TransferBuilder amountExpectedAfterTransferInDestination(final ResourceKey resource, final long amount) {
            this.amountExpectedAfterTransferInDestination = new ResourceAmount(resource, amount);
            return this;
        }

        public TransferBuilder amountExpectedToBeTransferred(final long amount) {
            this.amountExpectedToBeTransferred = amount;
            return this;
        }

        public Transfer build() {
            return new Transfer(
                amountInSource,
                amountInDestination,
                Objects.requireNonNull(amountToTransfer),
                amountExpectedToBeTransferred,
                amountExpectedAfterTransferInSource,
                amountExpectedAfterTransferInDestination
            );
        }
    }
}
