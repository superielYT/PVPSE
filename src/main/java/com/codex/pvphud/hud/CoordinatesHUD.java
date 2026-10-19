package com.codex.pvphud.hud;

import com.codex.pvphud.theme.Theme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public final class CoordinatesHUD extends HudElement {
    public CoordinatesHUD() { super("coordinates", "Coordinates", 8, 216); }
    protected void renderElement(GuiGraphics c, Minecraft mc, Theme t, boolean editor, float alpha) {
        HudStyles.card(c, t, width(mc), height(mc), alpha);
        String value = String.format("X %.0f  Y %.0f  Z %.0f", mc.player.getX(), mc.player.getY(), mc.player.getZ());
        HudStyles.label(c, mc, Component.literal(value), 6, 6, t.text());
    }
    public int width(Minecraft mc) { return 132; }
    public int height(Minecraft mc) { return 21; }
}
