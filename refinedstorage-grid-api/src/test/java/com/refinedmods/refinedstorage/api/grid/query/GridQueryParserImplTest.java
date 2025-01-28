package com.refinedmods.refinedstorage.api.grid.query;

import com.refinedmods.refinedstorage.api.grid.view.GridResource;
import com.refinedmods.refinedstorage.api.grid.view.GridResourceAttributeKey;
import com.refinedmods.refinedstorage.api.grid.view.GridResourceAttributeKeys;
import com.refinedmods.refinedstorage.api.grid.view.GridResourceImpl;
import com.refinedmods.refinedstorage.api.grid.view.GridView;
import com.refinedmods.refinedstorage.api.grid.view.GridViewImpl;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceListImpl;
import com.refinedmods.refinedstorage.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage.query.lexer.LexerTokenMappings;
import com.refinedmods.refinedstorage.query.parser.ParserOperatorMappings;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GridQueryParserImplTest {
    private final GridQueryParser queryParser = new GridQueryParserImpl(
        LexerTokenMappings.DEFAULT_MAPPINGS,
        ParserOperatorMappings.DEFAULT_MAPPINGS,
        GridResourceAttributeKeys.UNARY_OPERATOR_TO_ATTRIBUTE_KEY_MAPPING
    );

    private final GridView view = new GridViewImpl(
        (resource, craftable) -> Optional.of(new GridResourceImpl(resource)),
        MutableResourceListImpl.create(),
        new HashMap<>(),
        new HashSet<>(),
        v -> Comparator.comparing(GridResource::getName),
        v -> Comparator.comparingLong(resource -> resource.getAmount(v))
    );

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void testEmptyQuery(final String query) throws GridQueryParserException {
        // Act
        final var predicate = queryParser.parse(query);

        // Assert
        assertThat(predicate.test(view, new R("Dirt"))).isTrue();
        assertThat(predicate.test(view, new R("Glass"))).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"dirt", "Dirt", "DiRt", "Di", "irt"})
    void testNameQuery(final String query) throws GridQueryParserException {
        // Act
        final var predicate = queryParser.parse(query);

        // Assert
        assertThat(predicate.test(view, new R("Dirt"))).isTrue();
        assertThat(predicate.test(view, new R("Glass"))).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"@refined", "@\"Refined Storage\"", "@ReFiNe", "@Storage", "@rs", "@RS"})
    void testModQuery(final String query) throws GridQueryParserException {
        // Act
        final var predicate = queryParser.parse(query);

        // Assert
        assertThat(predicate.test(view, new R("Sponge", 1, "rs", "Refined Storage", Set.of()))).isTrue();
        assertThat(predicate.test(view, new R("Glass"))).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"$underwater", "$UnDerWate", "$water", "$unrelated", "$UNREL", "$laTed"})
    void testTagQuery(final String query) throws GridQueryParserException {
        // Act
        final var predicate = queryParser.parse(query);

        // Assert
        assertThat(predicate.test(view,
            new R("Sponge", 1, "mc", "Minecraft", Set.of("underwater", "unrelated")))).isTrue();
        assertThat(predicate.test(view, new R("Dirt", 1, "mc", "Minecraft", Set.of("transparent")))).isFalse();
    }

    @Test
    void testAttributeQueryWithInvalidNode() {
        // Act
        final Executable action = () -> queryParser.parse("@!true");

        // Assert
        final GridQueryParserException e = assertThrows(GridQueryParserException.class, action);
        assertThat(e.getMessage()).isEqualTo("Expected a literal");
    }

    @Test
    void testImplicitAndQuery() throws GridQueryParserException {
        // Act
        final var predicate = queryParser.parse("DirT di RT");

        // Assert
        assertThat(predicate.test(view, new R("Dirt"))).isTrue();
        assertThat(predicate.test(view, new R("Glass"))).isFalse();
    }

    @Test
    void testImplicitAndQueryInParenthesis() throws GridQueryParserException {
        // Act
        final var predicate = queryParser.parse("(DirT di RT) || (sto stone)");

        // Assert
        assertThat(predicate.test(view, new R("Dirt"))).isTrue();
        assertThat(predicate.test(view, new R("Glass"))).isFalse();
        assertThat(predicate.test(view, new R("Stone"))).isTrue();
    }

    @Test
    void testImplicitAndQueryWithUnaryOperator() throws GridQueryParserException {
        // Act
        final var predicate = queryParser.parse("@minecraft >5");

        // Assert
        assertThat(predicate.test(view, new R("Dirt", 6, "minecraft", "Minecraft", Set.of()))).isTrue();
        assertThat(predicate.test(view, new R("Glass", 5, "minecraft", "Minecraft", Set.of()))).isFalse();
        assertThat(predicate.test(view, new R("Sponge", 5, "rs", "Refined Storage", Set.of()))).isFalse();
        assertThat(predicate.test(view, new R("Cobblestone", 6, "rs", "Refined Storage", Set.of()))).isFalse();
    }

    @Test
    void testAndQuery() throws GridQueryParserException {
        // Act
        final var predicate = queryParser.parse("DirT && di && RT");

        // Assert
        assertThat(predicate.test(view, new R("Dirt"))).isTrue();
        assertThat(predicate.test(view, new R("Glass"))).isFalse();
    }

    @Test
    void testOrQuery() throws GridQueryParserException {
        // Act
        final var predicate = queryParser.parse("dir || glass || StoNe");

        // Assert
        assertThat(predicate.test(view, new R("Dirt"))).isTrue();
        assertThat(predicate.test(view, new R("Glass"))).isTrue();
        assertThat(predicate.test(view, new R("Stone"))).isTrue();
        assertThat(predicate.test(view, new R("Cobblestone"))).isTrue();

        assertThat(predicate.test(view, new R("Sponge"))).isFalse();
        assertThat(predicate.test(view, new R("Furnace"))).isFalse();
    }

    @Test
    void testSimpleNotQuery() throws GridQueryParserException {
        // Act
        final var predicate = queryParser.parse("!stone");

        // Assert
        assertThat(predicate.test(view, new R("Dirt"))).isTrue();
        assertThat(predicate.test(view, new R("Glass"))).isTrue();

        assertThat(predicate.test(view, new R("Stone"))).isFalse();
        assertThat(predicate.test(view, new R("Cobblestone"))).isFalse();
    }

    @Test
    void testNotQueryWithMultipleOrParts() throws GridQueryParserException {
        // Act
        final var predicate = queryParser.parse("!(stone || dirt)");

        // Assert
        assertThat(predicate.test(view, new R("Sponge"))).isTrue();
        assertThat(predicate.test(view, new R("Glass"))).isTrue();

        assertThat(predicate.test(view, new R("Stone"))).isFalse();
        assertThat(predicate.test(view, new R("Dirt"))).isFalse();
    }

    @Test
    void testComplexModQuery() throws GridQueryParserException {
        // Act
        final var predicate = queryParser.parse(
            "((spo || buck) && @refined) || (glass && @mine)"
        );

        // Assert
        assertThat(predicate.test(view, new R("Sponge", 1, "rs", "Refined Storage", Set.of()))).isTrue();
        assertThat(predicate.test(view, new R("Bucket", 1, "rs", "Refined Storage", Set.of()))).isTrue();
        assertThat(predicate.test(view, new R("Saddle", 1, "rs", "Refined Storage", Set.of()))).isFalse();

        assertThat(predicate.test(view, new R("Glass", 1, "mc", "Minecraft", Set.of()))).isTrue();
        assertThat(predicate.test(view, new R("Furnace", 1, "mc", "Minecraft", Set.of()))).isFalse();
    }

    @Test
    void testLessThanUnaryCountQuery() throws GridQueryParserException {
        // Act
        final var predicate = queryParser.parse("<5");

        // Assert
        assertThat(predicate.test(view, new R("Glass", 5))).isFalse();
        assertThat(predicate.test(view, new R("Glass", 4))).isTrue();
    }

    @Test
    void testLessThanEqualsUnaryCountQuery() throws GridQueryParserException {
        // Act
        final var predicate = queryParser.parse("<=5");

        // Assert
        assertThat(predicate.test(view, new R("Glass", 6))).isFalse();
        assertThat(predicate.test(view, new R("Glass", 5))).isTrue();
        assertThat(predicate.test(view, new R("Glass", 4))).isTrue();
    }

    @Test
    void testGreaterThanUnaryCountQuery() throws GridQueryParserException {
        // Act
        final var predicate = queryParser.parse(">5");

        // Assert
        assertThat(predicate.test(view, new R("Glass", 5))).isFalse();
        assertThat(predicate.test(view, new R("Glass", 6))).isTrue();
    }

    @Test
    void testGreaterThanEqualsUnaryCountQuery() throws GridQueryParserException {
        // Act
        final var predicate = queryParser.parse(">=5");

        // Assert
        assertThat(predicate.test(view, new R("Glass", 4))).isFalse();
        assertThat(predicate.test(view, new R("Glass", 5))).isTrue();
        assertThat(predicate.test(view, new R("Glass", 6))).isTrue();
    }

    @Test
    void testEqualsUnaryCountQuery() throws GridQueryParserException {
        // Act
        final var predicate = queryParser.parse("=5");

        // Assert
        assertThat(predicate.test(view, new R("Glass", 4))).isFalse();
        assertThat(predicate.test(view, new R("Glass", 5))).isTrue();
        assertThat(predicate.test(view, new R("Glass", 6))).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {">", ">=", "<", "<=", "="})
    void testInvalidNodeInUnaryCountQuery(final String operator) {
        // Act
        final GridQueryParserException e =
            assertThrows(GridQueryParserException.class, () -> queryParser.parse(operator + "(1 && 1)"));

        // Assert
        assertThat(e.getMessage()).isEqualTo("Count filtering expects a literal");
    }

    @ParameterizedTest
    @ValueSource(strings = {">", ">=", "<", "<=", "="})
    void testInvalidTokenInUnaryCountQuery(final String operator) {
        // Act
        final GridQueryParserException e =
            assertThrows(GridQueryParserException.class, () -> queryParser.parse(operator + "hello"));

        // Assert
        assertThat(e.getMessage()).isEqualTo("Count filtering expects an integer number");
    }

    private static class R implements GridResource {
        private final String name;
        private final long amount;
        private final Map<GridResourceAttributeKey, Set<String>> attributes;

        R(final String name) {
            this(name, 1);
        }

        R(final String name, final long amount) {
            this.name = name;
            this.amount = amount;
            this.attributes = Map.of();
        }

        R(
            final String name,
            final long amount,
            final String modId,
            final String modName,
            final Set<String> tags
        ) {
            this.name = name;
            this.amount = amount;
            this.attributes = Map.of(
                GridResourceAttributeKeys.MOD_ID, Set.of(modId),
                GridResourceAttributeKeys.MOD_NAME, Set.of(modName),
                GridResourceAttributeKeys.TAGS, tags
            );
        }

        @Override
        public Optional<TrackedResource> getTrackedResource(final GridView view) {
            return Optional.empty();
        }

        @Override
        public long getAmount(final GridView view) {
            return amount;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Set<String> getAttribute(final GridResourceAttributeKey key) {
            return attributes.getOrDefault(key, Set.of());
        }

        @Override
        public boolean isAutocraftable() {
            return false;
        }
    }
}
