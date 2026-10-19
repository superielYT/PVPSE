package com.codex.pvphud.hud;

import com.codex.pvphud.theme.Theme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public final class SessionHUD extends HudElement {
    private final long started = System.currentTimeMillis();
    public SessionHUD() { super("session", "Session", 150, 160); }
    protected void renderElement(GuiGraphics c, Minecraft mc, Theme t, boolean editor, float alpha) {
        long total = (System.currentTimeMillis() - started) / 1000L;
        String time = String.format("%02d:%02d:%02d", total / 3600, total / 60 % 60, total % 60);
        HudStyles.card(c, t, width(mc), height(mc), alpha);
        HudStyles.label(c, mc, Component.literal("SESSION"), 6, 6, t.mutedText());
        HudStyles.label(c, mc, Component.literal(time), 55, 6, t.accent());
    }
    public int width(Minecraft mc) { return 112; }
    public int height(Minecraft mc) { return 21; }
}
