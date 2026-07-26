package com.codex.pvphud.hud;

import com.codex.pvphud.render.ColorUtil;
import com.codex.pvphud.render.RenderUtil;
import com.codex.pvphud.theme.Theme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

final class HudStyles {
    private HudStyles() {}

    static void card(DrawContext context, Theme theme, int width, int height, float alpha) {
        RenderUtil.panel(context, 0, 0, width, height, theme);
        context.fill(6, height - 2, width - 6, height - 1, ColorUtil.withAlpha(theme.accent(), Math.round(alpha * 180)));
    }

    static void label(DrawContext context, MinecraftClient client, Text text, int x, int y, int color) {
        context.drawTextWithShadow(client.textRenderer, text, x, y, color);
    }
}
