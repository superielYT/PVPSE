package com.codex.pvphud.render;

import net.minecraft.client.gui.DrawContext;

public final class BlurRenderer {
    private BlurRenderer() {}

    public static void drawGlass(DrawContext context, int x, int y, int width, int height, int radius, int tint) {
        ShadowRenderer.draw(context, x, y, width, height, radius, 0x66000000);
        RoundedRenderer.fill(context, x, y, width, height, radius, ColorUtil.withAlpha(tint, 220));
        RoundedRenderer.fill(context, x + 1, y + 1, width - 2, 1, 0, 0x35FFFFFF);
    }
}
