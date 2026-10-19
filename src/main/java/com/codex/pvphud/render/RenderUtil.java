package com.codex.pvphud.render;

import com.codex.pvphud.theme.Theme;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class RenderUtil {
    private RenderUtil() {}

    public static void panel(GuiGraphicsExtractor context, int x, int y, int width, int height, Theme theme) {
        if (width <= 0 || height <= 0) return;
        ShadowRenderer.draw(context, x, y, width, height, 6,
                ColorUtil.withAlpha(theme.glow(), (theme.glow() >>> 24) / 3));
        BlurRenderer.drawGlass(context, x, y, width, height, 6, theme.panel());
        if (width > 12) context.fill(x + 6, y, x + width - 6, y + 1,
                ColorUtil.withAlpha(theme.accent(), 100));
    }

    public static boolean hovered(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }
}
