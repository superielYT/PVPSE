package com.codex.pvphud.render;

import net.minecraft.client.gui.DrawContext;

public final class ShadowRenderer {
    private ShadowRenderer() {}

    public static void draw(DrawContext context, int x, int y, int width, int height, int radius, int color) {
        for (int spread = 5; spread >= 1; spread--) {
            int alpha = Math.max(3, ((color >>> 24) & 0xFF) / (spread + 2));
            RoundedRenderer.fill(context, x - spread, y - spread, width + spread * 2, height + spread * 2,
                    radius + spread, ColorUtil.withAlpha(color, alpha));
        }
    }
}
