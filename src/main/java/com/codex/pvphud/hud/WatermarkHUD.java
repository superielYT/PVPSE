package com.codex.pvphud.hud;

import com.codex.pvphud.theme.Theme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public final class WatermarkHUD extends HudElement {
    public WatermarkHUD() { super("watermark", "Watermark", 8, 8); }
    protected void renderElement(DrawContext c, MinecraftClient mc, Theme t, boolean editor, float alpha) {
        HudStyles.card(c, t, width(mc), height(mc), alpha);
        HudStyles.label(c, mc, Text.literal("PVPSE"), 7, 6, t.accent());
        HudStyles.label(c, mc, Text.literal("CLIENT  •  1.21.11"), 45, 6, t.text());
    }
    public int width(MinecraftClient mc) { return 140; }
    public int height(MinecraftClient mc) { return 21; }
}
