package com.codex.pvphud.hud;

import com.codex.pvphud.render.RenderUtil;
import com.codex.pvphud.theme.Theme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

final class HudStyles {
    private HudStyles() {}

    static void card(GuiGraphicsExtractor context, Theme theme, int width, int height, float alpha) {
        var style = com.codex.pvphud.theme.ThemeManager.getInstance().appearance();
        RenderUtil.panel(context, 0, 0, width, height, theme,
                style.panelColor(theme.panel(), alpha), style.accents, style.shadows);
    }

    static void label(GuiGraphicsExtractor context, Minecraft client, Component text, int x, int y, int color) {
        context.text(client.font, text, x, y, color, com.codex.pvphud.theme.ThemeManager.getInstance().appearance().textShadow);
    }
}
