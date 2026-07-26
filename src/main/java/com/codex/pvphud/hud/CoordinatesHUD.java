package com.codex.pvphud.hud;

import com.codex.pvphud.theme.Theme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public final class CoordinatesHUD extends HudElement {
    public CoordinatesHUD() { super("coordinates", "Coordinates", 8, 216); }
    protected void renderElement(DrawContext c, MinecraftClient mc, Theme t, boolean editor, float alpha) {
        HudStyles.card(c, t, width(mc), height(mc), alpha);
        String value = String.format("X %.0f  Y %.0f  Z %.0f", mc.player.getX(), mc.player.getY(), mc.player.getZ());
        HudStyles.label(c, mc, Text.literal(value), 6, 6, t.text());
    }
    public int width(MinecraftClient mc) { return 132; }
    public int height(MinecraftClient mc) { return 21; }
}
