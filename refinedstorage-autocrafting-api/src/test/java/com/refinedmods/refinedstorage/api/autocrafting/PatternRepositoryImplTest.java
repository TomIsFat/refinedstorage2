package com.refinedmods.refinedstorage.api.autocrafting;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.A;
import static com.refinedmods.refinedstorage.api.autocrafting.ResourceFixtures.B;
import static org.assertj.core.api.Assertions.assertThat;

class PatternRepositoryImplTest {
    private PatternRepositoryImpl sut;

    @BeforeEach
    void setUp() {
        sut = new PatternRepositoryImpl();
    }

    @Test
    void testDefaultState() {
        // Assert
        assertThat(sut.getOutputs()).isEmpty();
        assertThat(sut.getAll()).isEmpty();
    }

    @Test
    void shouldAddPattern() {
        // Act
        sut.add(new PatternImpl(A));

        // Assert
        assertThat(sut.getOutputs()).usingRecursiveFieldByFieldElementComparator().containsExactly(A);
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new PatternImpl(A)
        );
        assertThat(sut.getByOutput(A)).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new PatternImpl(A)
        );
    }

    @Test
    void shouldAddMultiplePatterns() {
        // Act
        sut.add(new PatternImpl(A));
        sut.add(new PatternImpl(B));

        // Assert
        assertThat(sut.getOutputs()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            A,
            B
        );
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new PatternImpl(A),
            new PatternImpl(B)
        );
        assertThat(sut.getByOutput(A)).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new PatternImpl(A)
        );
        assertThat(sut.getByOutput(B)).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new PatternImpl(B)
        );
        assertThat(sut.getByOutput(ResourceFixtures.C)).isEmpty();
    }

    @Test
    void shouldAddMultiplePatternsAndSomeWithTheSameOutput() {
        // Arrange
        final PatternImpl a = new PatternImpl(A);
        final PatternImpl b = new PatternImpl(B, A);

        // Act
        sut.add(a);
        sut.add(b);

        // Assert
        assertThat(sut.getOutputs()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            A,
            B
        );
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new PatternImpl(A),
            new PatternImpl(B, A)
        );
        assertThat(sut.getByOutput(A)).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new PatternImpl(A),
            new PatternImpl(B, A)
        );
        assertThat(sut.getByOutput(B)).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new PatternImpl(B, A)
        );
        assertThat(sut.getByOutput(ResourceFixtures.C)).isEmpty();
    }

    @Test
    void shouldRemovePattern() {
        // Arrange
        final PatternImpl a = new PatternImpl(A);
        final PatternImpl b = new PatternImpl(B);

        sut.add(a);
        sut.add(b);

        // Act
        sut.remove(a);

        // Assert
        assertThat(sut.getOutputs()).usingRecursiveFieldByFieldElementComparator().containsExactly(B);
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new PatternImpl(B)
        );
        assertThat(sut.getByOutput(A)).isEmpty();
        assertThat(sut.getByOutput(B)).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new PatternImpl(B)
        );
    }

    @Test
    void shouldRemoveMultiplePatterns() {
        // Arrange
        final PatternImpl a = new PatternImpl(A);
        final PatternImpl b = new PatternImpl(B);

        sut.add(a);
        sut.add(b);

        // Act
        sut.remove(a);
        sut.remove(b);

        // Assert
        assertThat(sut.getOutputs()).isEmpty();
        assertThat(sut.getAll()).isEmpty();
        assertThat(sut.getByOutput(A)).isEmpty();
        assertThat(sut.getByOutput(B)).isEmpty();
    }

    @Test
    void shouldRemovePatternButNotRemoveOutputIfAnotherPatternStillHasThatOutput() {
        // Arrange
        final PatternImpl a = new PatternImpl(A);
        final PatternImpl b = new PatternImpl(B, A);

        sut.add(a);
        sut.add(b);

        // Act
        sut.remove(a);

        // Assert
        assertThat(sut.getOutputs()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            A,
            B
        );
        assertThat(sut.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new PatternImpl(B, A)
        );
        assertThat(sut.getByOutput(A)).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new PatternImpl(B, A)
        );
        assertThat(sut.getByOutput(B)).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new PatternImpl(B, A)
        );
    }

    @Test
    void shouldRemovePatternThatWasNeverAddedInTheFirstPlace() {
        // Arrange
        final PatternImpl a = new PatternImpl(A);

        // Act
        sut.remove(a);

        // Assert
        assertThat(sut.getOutputs()).isEmpty();
        assertThat(sut.getAll()).isEmpty();
        assertThat(sut.getByOutput(A)).isEmpty();
    }
}
