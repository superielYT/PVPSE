package com.codex.pvphud.render;

import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class BlurRenderer {
    private BlurRenderer() {}

    public static void drawGlass(GuiGraphicsExtractor context, int x, int y, int width, int height, int radius, int tint) {
        if (width <= 0 || height <= 0) return;
        RoundedRenderer.fill(context, x, y, width, height, radius, tint);
        // A restrained edge and surface sheen, with the palette's alpha preserved.
        int sheen = Math.round((tint >>> 24) * 0.06F);
        RoundedRenderer.fill(context, x + 1, y + 1, width - 2, Math.max(1, height / 3),
                Math.max(0, radius - 1), ColorUtil.withAlpha(0xFFFFFF, sheen));
        if (width > radius * 2) {
            context.fill(x + radius, y, x + width - radius, y + 1, 0x28FFFFFF);
            context.fill(x + radius, y + height - 1, x + width - radius, y + height, 0x28000000);
        }
    }
}
