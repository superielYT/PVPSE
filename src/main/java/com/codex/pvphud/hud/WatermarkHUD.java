package com.codex.pvphud.hud;

import com.codex.pvphud.theme.Theme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public final class WatermarkHUD extends HudElement {
    public WatermarkHUD() { super("watermark", "Watermark", 8, 8); }
    protected void renderElement(GuiGraphicsExtractor c, Minecraft mc, Theme t, boolean editor, float alpha) {
        HudStyles.card(c, t, width(mc), height(mc), alpha);
        HudStyles.label(c, mc, Component.literal("PVPSE"), 7, 6, t.accent());
        HudStyles.label(c, mc, Component.literal("CLIENT  •  26.1.2"), 45, 6, t.text());
    }
    public int width(Minecraft mc) { return 140; }
    public int height(Minecraft mc) { return 21; }
}
