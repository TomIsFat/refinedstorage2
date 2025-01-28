package com.refinedmods.refinedstorage.common.support.widget;

import com.refinedmods.refinedstorage.common.support.amount.PriorityScreen;
import com.refinedmods.refinedstorage.common.support.containermenu.ClientProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.IntConsumer;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;
import static net.minecraft.client.gui.screens.Screen.hasAltDown;
import static net.minecraft.client.gui.screens.Screen.hasControlDown;

public class StoragePrioritySideButtonWidget extends AbstractSideButtonWidget {
    private static final MutableComponent TITLE = createTranslation("gui", "priority");
    private static final MutableComponent INSERT_EXTRACT_TITLE = createTranslation("gui", "insert_extract_priority");
    private static final MutableComponent INSERT_TITLE = createTranslation("gui", "insert_priority");
    private static final MutableComponent EXTRACT_TITLE = createTranslation("gui", "extract_priority");
    private static final Component HELP = createTranslation("gui", "priority.storage_help");
    private static final ResourceLocation SPRITE = createIdentifier("widget/side_button/priority");

    private final ClientProperty<Integer> insertProperty;
    private final ClientProperty<Integer> extractProperty;

    public StoragePrioritySideButtonWidget(final ClientProperty<Integer> insertProperty,
                                           final ClientProperty<Integer> extractProperty,
                                           final Inventory playerInventory,
                                           final Screen parent) {
        super(createPressAction(insertProperty, extractProperty, playerInventory, parent));
        this.insertProperty = insertProperty;
        this.extractProperty = extractProperty;
    }

    private static OnPress createPressAction(final ClientProperty<Integer> insertProperty,
                                             final ClientProperty<Integer> extractProperty,
                                             final Inventory playerInventory,
                                             final Screen parent) {
        return btn -> {
            final MutableComponent title;
            final int priority;
            final IntConsumer listener;
            if (isModifyingInsert()) {
                title = INSERT_TITLE;
                priority = insertProperty.get();
                listener = insertProperty::setValue;
            } else if (isModifyingExtract()) {
                title = EXTRACT_TITLE;
                priority = extractProperty.get();
                listener = extractProperty::setValue;
            } else {
                title = INSERT_EXTRACT_TITLE;
                priority = insertProperty.get();
                listener = value -> {
                    insertProperty.setValue(value);
                    extractProperty.setValue(value);
                };
            }
            Minecraft.getInstance().setScreen(new PriorityScreen(title, priority, listener, parent, playerInventory));
        };
    }

    @Override
    protected ResourceLocation getSprite() {
        return SPRITE;
    }

    @Override
    protected MutableComponent getTitle() {
        return TITLE;
    }

    @Override
    protected List<MutableComponent> getSubText() {
        final List<MutableComponent> subText = new ArrayList<>();
        final boolean modifyingInsert = isModifyingInsert();
        final boolean modifyingExtract = isModifyingExtract();
        if (Objects.equals(insertProperty.getValue(), extractProperty.getValue())
            && !modifyingInsert
            && !modifyingExtract) {
            subText.add(createTranslation("gui", "priority.insert_extract", insertProperty.getValue())
                .withStyle(ChatFormatting.GRAY));
        } else {
            subText.add(createTranslation("gui", "priority.insert", insertProperty.getValue())
                .withStyle(modifyingInsert ? ChatFormatting.YELLOW : ChatFormatting.GRAY));
            subText.add(createTranslation("gui", "priority.extract", extractProperty.getValue())
                .withStyle(!modifyingInsert && modifyingExtract ? ChatFormatting.YELLOW : ChatFormatting.GRAY));
        }
        return subText;
    }

    private static boolean isModifyingInsert() {
        return hasControlDown();
    }

    private static boolean isModifyingExtract() {
        return hasAltDown();
    }

    @Override
    protected Component getHelpText() {
        return HELP;
    }
}
