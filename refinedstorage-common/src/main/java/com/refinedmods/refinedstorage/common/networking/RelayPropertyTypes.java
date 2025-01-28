package com.refinedmods.refinedstorage.common.networking;

import com.refinedmods.refinedstorage.common.support.containermenu.PropertyType;
import com.refinedmods.refinedstorage.common.support.containermenu.PropertyTypes;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

final class RelayPropertyTypes {
    static final PropertyType<Boolean> PASS_THROUGH = PropertyTypes.createBooleanProperty(
        createIdentifier("pass_through")
    );
    static final PropertyType<Boolean> PASS_ENERGY = PropertyTypes.createBooleanProperty(
        createIdentifier("pass_energy")
    );
    static final PropertyType<Boolean> PASS_STORAGE = PropertyTypes.createBooleanProperty(
        createIdentifier("pass_storage")
    );
    static final PropertyType<Boolean> PASS_SECURITY = PropertyTypes.createBooleanProperty(
        createIdentifier("pass_security")
    );
    static final PropertyType<Boolean> PASS_AUTOCRAFTING = PropertyTypes.createBooleanProperty(
        createIdentifier("pass_autocrafting")
    );

    private RelayPropertyTypes() {
    }
}
