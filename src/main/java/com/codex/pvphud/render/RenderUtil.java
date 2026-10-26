package com.codex.pvphud.render;

import com.codex.pvphud.theme.Theme;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class RenderUtil {
    private RenderUtil() {}

    public static void panel(GuiGraphicsExtractor context, int x, int y, int width, int height, Theme theme) {
        panel(context, x, y, width, height, theme, theme.panel(), true, true);
    }

    public static void panel(GuiGraphicsExtractor context, int x, int y, int width, int height,
                             Theme theme, int tint, boolean accents, boolean shadows) {
        if (width <= 0 || height <= 0) return;
        if (RenderBudget.lightweight()) {
            context.fill(x, y, x + width, y + height, tint);
            if (accents && width > 12) context.fill(x + 6, y, x + width - 6, y + 1, ColorUtil.withAlpha(theme.accent(), 100));
            return;
        }
        if (shadows) ShadowRenderer.draw(context, x, y, width, height, 6,
                ColorUtil.withAlpha(theme.glow(), (theme.glow() >>> 24) / 3));
        BlurRenderer.drawGlass(context, x, y, width, height, 6, tint);
        if (accents && width > 12) context.fill(x + 6, y, x + width - 6, y + 1,
                ColorUtil.withAlpha(theme.accent(), 100));
    }

    public static boolean hovered(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }
}
