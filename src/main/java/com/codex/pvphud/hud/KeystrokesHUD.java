package com.codex.pvphud.hud;

import com.codex.pvphud.render.RoundedRenderer;
import com.codex.pvphud.theme.Theme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public final class KeystrokesHUD extends HudElement {
    public KeystrokesHUD() { super("keystrokes", "Keystrokes", 8, 112); }
    protected void renderElement(GuiGraphicsExtractor c, Minecraft mc, Theme t, boolean editor, float alpha) {
        key(c, mc, t, "W", 22, 0, mc.options.keyUp.isDown());
        key(c, mc, t, "A", 0, 22, mc.options.keyLeft.isDown());
        key(c, mc, t, "S", 22, 22, mc.options.keyDown.isDown());
        key(c, mc, t, "D", 44, 22, mc.options.keyRight.isDown());
        key(c, mc, t, "SPACE", 0, 44, mc.options.keyJump.isDown(), 64);
    }
    private void key(GuiGraphicsExtractor c, Minecraft mc, Theme t, String label, int x, int y, boolean down) { key(c, mc, t, label, x, y, down, 20); }
    private void key(GuiGraphicsExtractor c, Minecraft mc, Theme t, String label, int x, int y, boolean down, int w) {
        var style = com.codex.pvphud.theme.ThemeManager.getInstance().appearance();
        com.codex.pvphud.render.RenderUtil.panel(c, x, y, w, 20, t,
                style.panelColor(down ? t.accent() : t.panel(), 1), style.accents, false);
        int color = down ? 0xFF101010 : t.text();
        c.centeredText(mc.font, Component.literal(label), x + w / 2, y + 6, color);
    }
    public int width(Minecraft mc) { return 64; }
    public int height(Minecraft mc) { return 64; }
}
