package com.refinedmods.refinedstorage.api.network.impl.node.relay;

import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.autocrafting.AutocraftingNetworkComponent;
import com.refinedmods.refinedstorage.api.resource.filter.FilterMode;
import com.refinedmods.refinedstorage.network.test.AddNetworkNode;
import com.refinedmods.refinedstorage.network.test.InjectNetwork;
import com.refinedmods.refinedstorage.network.test.InjectNetworkAutocraftingComponent;
import com.refinedmods.refinedstorage.network.test.NetworkTest;
import com.refinedmods.refinedstorage.network.test.SetupNetwork;

import java.util.Set;

import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage.api.network.impl.node.relay.RelayNetworkNodeTest.addPattern;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.A;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.A_ALTERNATIVE;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.B;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.C;
import static com.refinedmods.refinedstorage.network.test.fixtures.ResourceFixtures.D;
import static com.refinedmods.refinedstorage.network.test.nodefactory.AbstractNetworkNodeFactory.PROPERTY_ACTIVE;
import static org.assertj.core.api.Assertions.assertThat;

@NetworkTest
@SetupNetwork(id = "input")
@SetupNetwork(id = "output", setupEnergy = false)
class RelayAutocraftingNetworkNodeTest {
    @SuppressWarnings("DefaultAnnotationParam")
    @AddNetworkNode(properties = {
        @AddNetworkNode.Property(key = PROPERTY_ACTIVE, boolValue = false)
    }, networkId = "input")
    private RelayInputNetworkNode input;

    @AddNetworkNode(networkId = "output")
    private RelayOutputNetworkNode output;

    @Test
    void shouldPassAutocraftingComponent(
        @InjectNetworkAutocraftingComponent(networkId = "input") final AutocraftingNetworkComponent inputAutocrafting,
        @InjectNetworkAutocraftingComponent(networkId = "output") final AutocraftingNetworkComponent outputAutocrafting
    ) {
        // Arrange
        input.setActive(true);
        input.setOutputNode(output);

        addPattern(inputAutocrafting, A);

        // Act
        input.setComponentTypes(Set.of(RelayComponentType.AUTOCRAFTING));

        final var removeB = addPattern(inputAutocrafting, B);
        removeB.run();

        addPattern(inputAutocrafting, C);

        // Assert
        assertThat(inputAutocrafting.getOutputs()).containsExactlyInAnyOrder(A, C);
        assertThat(outputAutocrafting.getOutputs()).containsExactlyInAnyOrder(A, C);
        assertThat(input.hasComponentType(RelayComponentType.AUTOCRAFTING)).isTrue();
    }

    @Test
    void shouldRemovePatternsWhenNetworkIsRemoved(
        @InjectNetworkAutocraftingComponent(networkId = "input") final AutocraftingNetworkComponent inputAutocrafting,
        @InjectNetworkAutocraftingComponent(networkId = "output") final AutocraftingNetworkComponent outputAutocrafting
    ) {
        // Arrange
        input.setActive(true);
        input.setOutputNode(output);

        addPattern(inputAutocrafting, A);

        // Act
        input.setNetwork(null);

        addPattern(inputAutocrafting, B);

        // Assert
        assertThat(inputAutocrafting.getOutputs()).usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(A, B);
        assertThat(outputAutocrafting.getOutputs()).isEmpty();
    }

    @Test
    @SetupNetwork(id = "input_alt")
    void shouldNoLongerReceiveNotificationsFromOldInputNetwork(
        @InjectNetwork("input") final Network inputNetwork,
        @InjectNetwork("input_alt") final Network inputAlternativeNetwork,
        @InjectNetworkAutocraftingComponent(networkId = "input") final AutocraftingNetworkComponent inputAutocrafting,
        @InjectNetworkAutocraftingComponent(networkId = "input_alt")
        final AutocraftingNetworkComponent inputAlternativeAutocrafting,
        @InjectNetworkAutocraftingComponent(networkId = "output") final AutocraftingNetworkComponent outputAutocrafting
    ) {
        // Arrange
        input.setActive(true);
        input.setOutputNode(output);

        addPattern(inputAutocrafting, A);
        input.setComponentTypes(Set.of(RelayComponentType.AUTOCRAFTING));

        // Act
        inputNetwork.removeContainer(() -> input);
        inputAlternativeNetwork.addContainer(() -> input);
        input.setNetwork(inputAlternativeNetwork);

        addPattern(inputAlternativeAutocrafting, B);
        addPattern(inputAutocrafting, C);

        // Assert
        assertThat(inputAutocrafting.getOutputs()).usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(A, C);
        assertThat(inputAlternativeAutocrafting.getOutputs()).usingRecursiveFieldByFieldElementComparator()
            .containsExactly(B);
        assertThat(outputAutocrafting.getOutputs()).usingRecursiveFieldByFieldElementComparator().containsExactly(B);
    }

    @Test
    @SetupNetwork(id = "output_alt")
    void shouldNotNotifyOldOutputNetworkWhenOutputNetworkHasChanged(
        @InjectNetworkAutocraftingComponent(networkId = "input") final AutocraftingNetworkComponent inputAutocrafting,
        @InjectNetwork("output") final Network outputNetwork,
        @InjectNetworkAutocraftingComponent(networkId = "output_alt")
        final AutocraftingNetworkComponent outputAlternativeAutocrafting,
        @InjectNetwork("output_alt") final Network outputAlternativeNetwork,
        @InjectNetworkAutocraftingComponent(networkId = "output") final AutocraftingNetworkComponent outputAutocrafting
    ) {
        // Arrange
        input.setActive(true);
        input.setOutputNode(output);

        addPattern(inputAutocrafting, A);
        input.setComponentTypes(Set.of(RelayComponentType.AUTOCRAFTING));

        // Act
        outputNetwork.removeContainer(() -> output);
        outputAlternativeNetwork.addContainer(() -> output);
        output.setNetwork(outputAlternativeNetwork);

        addPattern(inputAutocrafting, B);

        // Assert
        assertThat(inputAutocrafting.getOutputs()).usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(A, B);
        assertThat(outputAlternativeAutocrafting.getOutputs()).usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(A, B);
        assertThat(outputAutocrafting.getOutputs()).isEmpty();
    }

    @Test
    @SetupNetwork(id = "output_alt")
    void shouldAddPatternsToNewOutputNetworkIfTheOutputNetworkChanges(
        @InjectNetworkAutocraftingComponent(networkId = "input") final AutocraftingNetworkComponent inputAutocrafting,
        @InjectNetwork("output") final Network outputNetwork,
        @InjectNetworkAutocraftingComponent(networkId = "output_alt")
        final AutocraftingNetworkComponent outputAlternativeAutocrafting,
        @InjectNetwork("output_alt") final Network outputAlternativeNetwork,
        @InjectNetworkAutocraftingComponent(networkId = "output") final AutocraftingNetworkComponent outputAutocrafting
    ) {
        // Arrange
        input.setActive(true);
        input.setOutputNode(output);

        addPattern(inputAutocrafting, A);
        input.setComponentTypes(Set.of(RelayComponentType.AUTOCRAFTING));

        // Act
        outputNetwork.removeContainer(() -> output);
        outputAlternativeNetwork.addContainer(() -> output);
        output.setNetwork(outputAlternativeNetwork);

        // Assert
        assertThat(inputAutocrafting.getOutputs()).usingRecursiveFieldByFieldElementComparator()
            .containsExactly(A);
        assertThat(outputAlternativeAutocrafting.getOutputs()).usingRecursiveFieldByFieldElementComparator()
            .containsExactly(A);
        assertThat(outputAutocrafting.getOutputs()).isEmpty();
    }

    @Test
    void shouldRespectAllowlistFilter(
        @InjectNetworkAutocraftingComponent(networkId = "input") final AutocraftingNetworkComponent inputAutocrafting,
        @InjectNetworkAutocraftingComponent(networkId = "output") final AutocraftingNetworkComponent outputAutocrafting
    ) {
        // Arrange
        input.setActive(true);
        input.setOutputNode(output);
        input.setFilters(Set.of(A, C));
        input.setFilterMode(FilterMode.ALLOW);

        addPattern(inputAutocrafting, A);
        addPattern(inputAutocrafting, B);

        // Act
        input.setComponentTypes(Set.of(RelayComponentType.AUTOCRAFTING));

        // This update should arrive.
        addPattern(inputAutocrafting, C);
        // This one shouldn't.
        addPattern(inputAutocrafting, D);

        // Assert
        assertThat(inputAutocrafting.getOutputs()).usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(A, B, C, D);
        assertThat(outputAutocrafting.getOutputs()).usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(A, C);
    }

    @Test
    void shouldRespectFilterNormalizer(
        @InjectNetworkAutocraftingComponent(networkId = "input") final AutocraftingNetworkComponent inputAutocrafting,
        @InjectNetworkAutocraftingComponent(networkId = "output") final AutocraftingNetworkComponent outputAutocrafting
    ) {
        // Arrange
        input.setActive(true);
        input.setOutputNode(output);
        input.setFilters(Set.of(A, B));
        input.setFilterMode(FilterMode.ALLOW);
        input.setFilterNormalizer(resource -> {
            if (resource == A_ALTERNATIVE) {
                return A;
            }
            return resource;
        });

        addPattern(inputAutocrafting, A);
        addPattern(inputAutocrafting, C);

        // Act
        input.setComponentTypes(Set.of(RelayComponentType.AUTOCRAFTING));

        // These updates should arrive.
        addPattern(inputAutocrafting, A_ALTERNATIVE);
        addPattern(inputAutocrafting, B);
        // This one shouldn't.
        addPattern(inputAutocrafting, D);

        // Assert
        assertThat(inputAutocrafting.getOutputs()).usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(A, B, C, A_ALTERNATIVE, D);
        assertThat(outputAutocrafting.getOutputs()).usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(A, A_ALTERNATIVE, B);
    }

    @Test
    void shouldUpdateOutputPatternsWhenFiltersAreChanged(
        @InjectNetworkAutocraftingComponent(networkId = "input") final AutocraftingNetworkComponent inputAutocrafting,
        @InjectNetworkAutocraftingComponent(networkId = "output") final AutocraftingNetworkComponent outputAutocrafting
    ) {
        // Arrange
        input.setActive(true);
        input.setOutputNode(output);
        input.setFilters(Set.of(A));
        input.setFilterMode(FilterMode.BLOCK);

        addPattern(inputAutocrafting, A);
        addPattern(inputAutocrafting, B);

        // Act
        input.setComponentTypes(Set.of(RelayComponentType.AUTOCRAFTING));
        input.setFilters(Set.of(B));

        // Assert
        assertThat(inputAutocrafting.getOutputs()).usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(A, B);
        assertThat(outputAutocrafting.getOutputs()).usingRecursiveFieldByFieldElementComparator()
            .containsExactly(A);
    }

    @Test
    void shouldUpdateOutputPatternsWhenFilterModeIsChanged(
        @InjectNetworkAutocraftingComponent(networkId = "input") final AutocraftingNetworkComponent inputAutocrafting,
        @InjectNetworkAutocraftingComponent(networkId = "output") final AutocraftingNetworkComponent outputAutocrafting
    ) {
        // Arrange
        input.setActive(true);
        input.setOutputNode(output);
        input.setFilters(Set.of(A));
        input.setFilterMode(FilterMode.BLOCK);

        addPattern(inputAutocrafting, A);
        addPattern(inputAutocrafting, B);

        // Act
        input.setComponentTypes(Set.of(RelayComponentType.AUTOCRAFTING));
        input.setFilterMode(FilterMode.ALLOW);

        // Assert
        assertThat(inputAutocrafting.getOutputs()).usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(A, B);
        assertThat(outputAutocrafting.getOutputs()).usingRecursiveFieldByFieldElementComparator()
            .containsExactly(A);
    }

    @Test
    @SetupNetwork(id = "cycle_input", energyStored = 1, energyCapacity = 2)
    @SetupNetwork(id = "cycle_input_alt", energyStored = 3, energyCapacity = 4)
    void shouldDetectCycles(
        @InjectNetwork("cycle_input") final Network inputNetwork,
        @InjectNetworkAutocraftingComponent(networkId = "cycle_input")
        final AutocraftingNetworkComponent inputAutocrafting,
        @InjectNetwork("cycle_input_alt") final Network inputAlternativeNetwork,
        @InjectNetworkAutocraftingComponent(networkId = "cycle_input_alt")
        final AutocraftingNetworkComponent inputAlternativeAutocrafting
    ) {
        // Act
        final RelayOutputNetworkNode cycleOutput = new RelayOutputNetworkNode(0);
        cycleOutput.setAutocraftingDelegate(inputAlternativeAutocrafting);
        cycleOutput.setNetwork(inputNetwork);
        inputNetwork.addContainer(() -> cycleOutput);

        final RelayOutputNetworkNode cycleOutputAlternative = new RelayOutputNetworkNode(0);
        cycleOutputAlternative.setAutocraftingDelegate(inputAutocrafting);
        cycleOutputAlternative.setNetwork(inputAlternativeNetwork);
        inputAlternativeNetwork.addContainer(() -> cycleOutputAlternative);

        addPattern(inputAutocrafting, A);
        final Runnable removeB = addPattern(inputAutocrafting, B);
        removeB.run();

        // Assert
        assertThat(inputAutocrafting.getOutputs()).usingRecursiveFieldByFieldElementComparator().containsExactly(A);
        assertThat(inputAlternativeAutocrafting.getOutputs()).isEmpty();
    }
}
