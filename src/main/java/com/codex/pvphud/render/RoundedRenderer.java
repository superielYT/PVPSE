package com.codex.pvphud.render;

import net.minecraft.client.gui.DrawContext;

public final class RoundedRenderer {
    private RoundedRenderer() {}

    public static void fill(DrawContext context, int x, int y, int width, int height, int radius, int color) {
        if (width <= 0 || height <= 0) return;
        int r = Math.clamp(radius, 0, Math.min(width, height) / 2);
        if (r == 0) {
            context.fill(x, y, x + width, y + height, color);
            return;
        }
        context.fill(x + r, y, x + width - r, y + height, color);
        context.fill(x, y + r, x + width, y + height - r, color);
        for (int dy = 0; dy < r; dy++) {
            int inset = r - (int) Math.sqrt(Math.max(0, r * r - (r - dy) * (r - dy)));
            context.fill(x + inset, y + dy, x + width - inset, y + dy + 1, color);
            context.fill(x + inset, y + height - dy - 1, x + width - inset, y + height - dy, color);
        }
    }
}
