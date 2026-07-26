package com.codex.pvphud.gui;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public final class SearchBar extends TextFieldWidget {
    public SearchBar(TextRenderer renderer, int x, int y, int width) {
        super(renderer, x, y, width, 20, Text.literal("Search HUD"));
        setPlaceholder(Text.literal("Search HUD elements..."));
        setMaxLength(32);
    }
}
