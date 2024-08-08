package com.refinedmods.refinedstorage.common.storage;

import com.refinedmods.refinedstorage.api.resource.filter.FilterMode;
import com.refinedmods.refinedstorage.common.support.containermenu.ClientProperty;
import com.refinedmods.refinedstorage.common.support.widget.AbstractSideButtonWidget;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

public class FilterModeSideButtonWidget extends AbstractSideButtonWidget {
    private static final MutableComponent TITLE = createTranslation("gui", "filter_mode");
    private static final MutableComponent SUBTEXT_BLOCK = createTranslation("gui", "filter_mode.block");
    private static final MutableComponent SUBTEXT_ALLOW = createTranslation("gui", "filter_mode.allow");
    private static final Component FILTER_MODE_WARNING = createTranslation("gui", "storage.filter_mode.empty_warning");
    private static final ResourceLocation ALLOW = createIdentifier("widget/side_button/storage/filter_mode/allow");
    private static final ResourceLocation BLOCK = createIdentifier("widget/side_button/storage/filter_mode/block");

    private final ClientProperty<FilterMode> property;
    private final Component helpAllow;
    private final Component helpBlock;

    public FilterModeSideButtonWidget(final ClientProperty<FilterMode> property,
                                      final Component helpAllow,
                                      final Component helpBlock) {
        super(createPressAction(property));
        this.property = property;
        this.helpAllow = helpAllow;
        this.helpBlock = helpBlock;
    }

    public void setWarningVisible(final boolean visible) {
        if (visible) {
            setWarning(FILTER_MODE_WARNING);
        } else {
            setWarning(null);
        }
    }

    private static OnPress createPressAction(final ClientProperty<FilterMode> property) {
        return btn -> property.setValue(toggle(property.getValue()));
    }

    private static FilterMode toggle(final FilterMode filterMode) {
        return filterMode == FilterMode.ALLOW ? FilterMode.BLOCK : FilterMode.ALLOW;
    }

    @Override
    protected ResourceLocation getSprite() {
        return property.getValue() == FilterMode.BLOCK ? BLOCK : ALLOW;
    }

    @Override
    protected MutableComponent getTitle() {
        return TITLE;
    }

    @Override
    protected MutableComponent getSubText() {
        return property.getValue() == FilterMode.BLOCK ? SUBTEXT_BLOCK : SUBTEXT_ALLOW;
    }

    @Override
    protected Component getHelpText() {
        return property.getValue() == FilterMode.BLOCK ? helpBlock : helpAllow;
    }
}
