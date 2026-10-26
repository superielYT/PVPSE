package com.codex.pvphud.render;

import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class RoundedRenderer {
    private RoundedRenderer() {}

    public static void fill(GuiGraphicsExtractor context, int x, int y, int width, int height, int radius, int color) {
        if (width <= 0 || height <= 0) return;
        int r = Math.clamp(radius, 0, Math.min(width, height) / 2);
        if (r == 0) {
            context.fill(x, y, x + width, y + height, color);
            return;
        }
        if (height > 2 * r) context.fill(x, y + r, x + width, y + height - r, color);
        for (var span : RoundedGeometry.spans(r)) {
            int dy = span.offset(), rows = span.rows(), inset = span.inset();
            context.fill(x + inset, y + dy, x + width - inset, y + dy + rows, color);
            context.fill(x + inset, y + height - dy - rows, x + width - inset, y + height - dy, color);
        }
    }
}
