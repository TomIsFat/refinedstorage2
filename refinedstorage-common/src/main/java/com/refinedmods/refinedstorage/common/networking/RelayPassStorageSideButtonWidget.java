package com.refinedmods.refinedstorage.common.networking;

import com.refinedmods.refinedstorage.common.support.containermenu.ClientProperty;
import com.refinedmods.refinedstorage.common.support.widget.AbstractYesNoSideButtonWidget;

import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

class RelayPassStorageSideButtonWidget extends AbstractYesNoSideButtonWidget {
    private static final MutableComponent TITLE = createTranslation("gui", "relay.pass_storage");
    private static final MutableComponent HELP = createTranslation("gui", "relay.pass_storage.help");
    private static final ResourceLocation YES = createIdentifier("widget/side_button/relay/pass_storage/yes");
    private static final ResourceLocation NO = createIdentifier("widget/side_button/relay/pass_storage/no");

    RelayPassStorageSideButtonWidget(final ClientProperty<Boolean> property) {
        super(property, TITLE, YES, NO);
    }

    @Nullable
    @Override
    protected Component getHelpText() {
        return HELP;
    }
}
