package com.refinedmods.refinedstorage.common.grid.screen;

import com.refinedmods.refinedstorage.common.grid.GridSearchBox;
import com.refinedmods.refinedstorage.common.support.widget.History;
import com.refinedmods.refinedstorage.common.support.widget.SearchFieldWidget;
import com.refinedmods.refinedstorage.query.lexer.Lexer;
import com.refinedmods.refinedstorage.query.lexer.LexerException;
import com.refinedmods.refinedstorage.query.lexer.LexerTokenMappings;
import com.refinedmods.refinedstorage.query.lexer.Source;
import com.refinedmods.refinedstorage.query.lexer.SyntaxHighlightedCharacter;
import com.refinedmods.refinedstorage.query.lexer.SyntaxHighlighter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

class GridSearchBoxWidget extends SearchFieldWidget implements GridSearchBox {
    private final Set<Consumer<String>> listeners = new HashSet<>();
    private boolean valid = true;

    GridSearchBoxWidget(final Font textRenderer,
                        final int x,
                        final int y,
                        final int width,
                        final SyntaxHighlighter syntaxHighlighter,
                        final History history) {
        super(textRenderer, x, y, width, history);
        setFormatter((text, firstCharacterIndex) -> format(syntaxHighlighter, text));
        setResponder(text -> listeners.forEach(l -> l.accept(text)));
    }

    private FormattedCharSequence format(final SyntaxHighlighter syntaxHighlighter, final String text) {
        if (!valid) {
            return invalidText(text);
        }
        final Lexer lexer = createLexer(text);
        try {
            lexer.scan();
        } catch (LexerException e) {
            return invalidText(text);
        }
        final List<SyntaxHighlightedCharacter> characters = syntaxHighlighter.highlight(text, lexer.getTokens());
        return convertCharactersToOrderedText(characters);
    }

    private FormattedCharSequence invalidText(final String text) {
        return FormattedCharSequence.forward(text, Style.EMPTY.applyFormat(ChatFormatting.RED));
    }

    private FormattedCharSequence convertCharactersToOrderedText(final List<SyntaxHighlightedCharacter> characters) {
        FormattedCharSequence orderedText = FormattedCharSequence.EMPTY;
        for (final SyntaxHighlightedCharacter character : characters) {
            orderedText = FormattedCharSequence.composite(orderedText, convertCharacterToOrderedText(character));
        }
        return orderedText;
    }

    private FormattedCharSequence convertCharacterToOrderedText(final SyntaxHighlightedCharacter character) {
        final ChatFormatting color = ChatFormatting.getByName(character.getColor());
        return FormattedCharSequence.forward(character.getCharacter(), Style.EMPTY.withColor(color));
    }

    private Lexer createLexer(final String text) {
        return new Lexer(new Source("Grid search box syntax highlighting", text), LexerTokenMappings.DEFAULT_MAPPINGS);
    }

    @Override
    public void addListener(final Consumer<String> listener) {
        this.listeners.add(listener);
    }

    @Override
    public void setValid(final boolean valid) {
        this.valid = valid;
        setTextColor(valid ? 0xFFFFFF : 0xFF5555);
    }
}
