package com.codex.pvphud.render;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public final class FontRenderer {
    private final TextRenderer delegate;

    public FontRenderer(TextRenderer delegate) {
        this.delegate = delegate;
    }

    public int width(Text text) {
        return delegate.getWidth(text);
    }

    public int height() {
        return delegate.fontHeight;
    }

    public void draw(DrawContext context, Text text, int x, int y, int color) {
        context.drawText(delegate, text, x, y, color, false);
    }

    public void drawShadow(DrawContext context, Text text, int x, int y, int color) {
        context.drawTextWithShadow(delegate, text, x, y, color);
    }
}
