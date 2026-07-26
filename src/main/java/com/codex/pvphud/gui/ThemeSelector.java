package com.codex.pvphud.gui;

import com.codex.pvphud.render.RenderUtil;
import com.codex.pvphud.render.RoundedRenderer;
import com.codex.pvphud.theme.Theme;
import com.codex.pvphud.theme.ThemeManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public final class ThemeSelector {
    private int x;
    private int y;
    private int width;

    public void render(DrawContext context, MinecraftClient client, int x, int y, int width, int mouseX, int mouseY) {
        this.x = x; this.y = y; this.width = width;
        ThemeManager manager = ThemeManager.getInstance();
        Theme active = manager.getTheme();
        RenderUtil.panel(context, x, y, width, 32, active);
        context.drawText(client.textRenderer, Text.literal(active.displayName()), x + 9, y + 12, active.text(), false);
        RoundedRenderer.fill(context, x + width - 24, y + 8, 16, 16, 8, active.accent());
    }

    public boolean click(double mouseX, double mouseY, int button) {
        if (button == 0 && RenderUtil.hovered(mouseX, mouseY, x, y, width, 32)) {
            ThemeManager.getInstance().cycle();
            return true;
        }
        return false;
    }
}
