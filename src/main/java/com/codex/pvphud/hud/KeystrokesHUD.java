package com.codex.pvphud.hud;

import com.codex.pvphud.render.RoundedRenderer;
import com.codex.pvphud.theme.Theme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public final class KeystrokesHUD extends HudElement {
    public KeystrokesHUD() { super("keystrokes", "Keystrokes", 8, 112); }
    protected void renderElement(DrawContext c, MinecraftClient mc, Theme t, boolean editor, float alpha) {
        key(c, mc, t, "W", 22, 0, mc.options.forwardKey.isPressed());
        key(c, mc, t, "A", 0, 22, mc.options.leftKey.isPressed());
        key(c, mc, t, "S", 22, 22, mc.options.backKey.isPressed());
        key(c, mc, t, "D", 44, 22, mc.options.rightKey.isPressed());
        key(c, mc, t, "SPACE", 0, 44, mc.options.jumpKey.isPressed(), 64);
    }
    private void key(DrawContext c, MinecraftClient mc, Theme t, String label, int x, int y, boolean down) { key(c, mc, t, label, x, y, down, 20); }
    private void key(DrawContext c, MinecraftClient mc, Theme t, String label, int x, int y, boolean down, int w) {
        RoundedRenderer.fill(c, x, y, w, 20, 4, down ? t.accent() : t.panel());
        int color = down ? 0xFF101010 : t.text();
        c.drawCenteredTextWithShadow(mc.textRenderer, Text.literal(label), x + w / 2, y + 6, color);
    }
    public int width(MinecraftClient mc) { return 64; }
    public int height(MinecraftClient mc) { return 64; }
}
