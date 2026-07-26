package com.codex.pvphud.render;

import com.codex.pvphud.theme.Theme;
import net.minecraft.client.gui.DrawContext;

public final class RenderUtil {
    private RenderUtil() {}

    public static void panel(DrawContext context, int x, int y, int width, int height, Theme theme) {
        ShadowRenderer.draw(context, x, y, width, height, 6, theme.glow());
        BlurRenderer.drawGlass(context, x, y, width, height, 6, theme.panel());
        context.fill(x + 6, y, x + width - 6, y + 2, theme.accent());
    }

    public static boolean hovered(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }
}
