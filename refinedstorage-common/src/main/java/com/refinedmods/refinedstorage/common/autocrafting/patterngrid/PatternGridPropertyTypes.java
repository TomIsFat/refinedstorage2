package com.refinedmods.refinedstorage.common.autocrafting.patterngrid;

import com.refinedmods.refinedstorage.common.support.containermenu.PropertyType;
import com.refinedmods.refinedstorage.common.support.containermenu.PropertyTypes;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

final class PatternGridPropertyTypes {
    static final PropertyType<PatternType> PATTERN_TYPE = new PropertyType<>(
        createIdentifier("pattern_type"),
        PatternTypeSettings::getPatternType,
        PatternTypeSettings::getPatternType
    );
    static final PropertyType<Integer> STONECUTTER_SELECTED_RECIPE = PropertyTypes.createIntegerProperty(
        createIdentifier("stonecutter_selected_recipe")
    );

    private PatternGridPropertyTypes() {
    }
}
