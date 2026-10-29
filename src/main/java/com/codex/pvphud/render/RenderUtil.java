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
        int opacity = tint >>> 24;
        if (RenderBudget.lightweight()) {
            context.fill(x, y, x + width, y + height, tint);
        } else {
            if (shadows) RoundedRenderer.fill(context, x, y + 2, width, height, 5,
                    ColorUtil.withAlpha(0, opacity / 5));
            RoundedRenderer.fill(context, x, y, width, height, 5, tint);
        }
        if (width > 12) {
            context.fill(x + 5, y, x + width - 5, y + 1, ColorUtil.withAlpha(0xFFFFFF, opacity / 14));
            if (accents) {
                // A short ember signature instead of two full-width decorative lines.
                context.fill(x + 6, y, x + Math.min(width - 6, 22), y + 1,
                        ColorUtil.withAlpha(theme.accent(), Math.round(opacity * .8F)));
                context.fill(x + width - 9, y, x + width - 6, y + 1,
                        ColorUtil.withAlpha(theme.accentSecondary(), opacity / 3));
            }
        }
    }

    public static boolean hovered(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }
}
