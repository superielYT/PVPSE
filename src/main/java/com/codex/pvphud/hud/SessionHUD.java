package com.codex.pvphud.hud;

import com.codex.pvphud.theme.Theme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public final class SessionHUD extends HudElement {
    private final long started = System.currentTimeMillis();
    public SessionHUD() { super("session", "Session", 150, 160); }
    protected void renderElement(DrawContext c, MinecraftClient mc, Theme t, boolean editor, float alpha) {
        long total = (System.currentTimeMillis() - started) / 1000L;
        String time = String.format("%02d:%02d:%02d", total / 3600, total / 60 % 60, total % 60);
        HudStyles.card(c, t, width(mc), height(mc), alpha);
        HudStyles.label(c, mc, Text.literal("SESSION"), 6, 6, t.mutedText());
        HudStyles.label(c, mc, Text.literal(time), 55, 6, t.accent());
    }
    public int width(MinecraftClient mc) { return 108; }
    public int height(MinecraftClient mc) { return 21; }
}
