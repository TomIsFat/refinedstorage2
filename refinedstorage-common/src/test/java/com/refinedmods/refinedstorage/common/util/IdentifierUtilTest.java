package com.refinedmods.refinedstorage.common.util;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.MOD_ID;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.format;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.formatWithUnits;
import static org.assertj.core.api.Assertions.assertThat;

class IdentifierUtilTest {
    @Test
    void verifyModId() {
        assertThat(MOD_ID).isEqualTo("refinedstorage");
        assertThat(createIdentifier("test")).isEqualTo(
            ResourceLocation.fromNamespaceAndPath("refinedstorage", "test")
        );
    }

    @Test
    void shouldFormatWithUnitsForSmallNumber() {
        // Act & assert
        assertThat(formatWithUnits(0)).isEqualTo("0");
        assertThat(formatWithUnits(1)).isEqualTo("1");
        assertThat(formatWithUnits(10)).isEqualTo("10");
        assertThat(formatWithUnits(105)).isEqualTo("105");
    }

    @Test
    void shouldFormatWithUnitsForThousands() {
        // Act & assert
        assertThat(formatWithUnits(1000)).isEqualTo("1K");
        assertThat(formatWithUnits(1510)).isEqualTo("1.5K");

        assertThat(formatWithUnits(10_000)).isEqualTo("10K");
        assertThat(formatWithUnits(10_510)).isEqualTo("10.5K");
        assertThat(formatWithUnits(99_999)).isEqualTo("99.9K");

        assertThat(formatWithUnits(100_000)).isEqualTo("100K");
        assertThat(formatWithUnits(100_500)).isEqualTo("100K");
        assertThat(formatWithUnits(100_999)).isEqualTo("100K");

        assertThat(formatWithUnits(101_000)).isEqualTo("101K");
        assertThat(formatWithUnits(101_500)).isEqualTo("101K");
        assertThat(formatWithUnits(101_999)).isEqualTo("101K");
    }

    @Test
    void shouldFormatWithUnitsForMillions() {
        // Act & assert
        assertThat(formatWithUnits(1_000_000)).isEqualTo("1M");
        assertThat(formatWithUnits(1_510_000)).isEqualTo("1.5M");

        assertThat(formatWithUnits(10_000_000)).isEqualTo("10M");
        assertThat(formatWithUnits(10_510_000)).isEqualTo("10.5M");
        assertThat(formatWithUnits(99_999_999)).isEqualTo("99.9M");

        assertThat(formatWithUnits(100_000_000)).isEqualTo("100M");
        assertThat(formatWithUnits(100_510_000)).isEqualTo("100M");
        assertThat(formatWithUnits(100_999_000)).isEqualTo("100M");

        assertThat(formatWithUnits(101_000_000)).isEqualTo("101M");
        assertThat(formatWithUnits(101_510_000)).isEqualTo("101M");
        assertThat(formatWithUnits(101_999_000)).isEqualTo("101M");
    }

    @Test
    void shouldFormatWithUnitsForBillions() {
        // Act & assert
        assertThat(formatWithUnits(1_000_000_000)).isEqualTo("1B");
        assertThat(formatWithUnits(1_010_000_000)).isEqualTo("1B");
        assertThat(formatWithUnits(1_100_000_000)).isEqualTo("1.1B");
        assertThat(formatWithUnits(1_100_001_000)).isEqualTo("1.1B");
        assertThat(formatWithUnits(1_920_001_000)).isEqualTo("1.9B");
    }

    @Test
    void shouldFormatWithoutUnits() {
        // Act & assert
        assertThat(format(0)).isEqualTo("0");
        assertThat(format(1)).isEqualTo("1");
        assertThat(format(10)).isEqualTo("10");
        assertThat(format(105)).isEqualTo("105");
        assertThat(format(1050)).isEqualTo("1,050");
        assertThat(format(10500)).isEqualTo("10,500");
        assertThat(format(100500)).isEqualTo("100,500");
        assertThat(format(1000500)).isEqualTo("1,000,500");
    }
}
