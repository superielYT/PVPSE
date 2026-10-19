package com.codex.pvphud.render;

import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class ShadowRenderer {
    private ShadowRenderer() {}

    public static void draw(GuiGraphicsExtractor context, int x, int y, int width, int height, int radius, int color) {
        if ((color >>> 24) == 0 || width <= 0 || height <= 0) return;
        for (int spread = 5; spread >= 1; spread--) {
            int alpha = Math.max(3, ((color >>> 24) & 0xFF) / (spread + 2));
            RoundedRenderer.fill(context, x - spread, y - spread, width + spread * 2, height + spread * 2,
                    radius + spread, ColorUtil.withAlpha(color, alpha));
        }
    }
}
