package com.codex.pvphud.render;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public final class FontRenderer {
    private final Font delegate;

    public FontRenderer(Font delegate) {
        this.delegate = delegate;
    }

    public int width(Component text) {
        return delegate.width(text);
    }

    public int height() {
        return delegate.lineHeight;
    }

    public void draw(GuiGraphicsExtractor context, Component text, int x, int y, int color) {
        context.text(delegate, text, x, y, color, false);
    }

    public void drawShadow(GuiGraphicsExtractor context, Component text, int x, int y, int color) {
        context.text(delegate, text, x, y, color);
    }
}
