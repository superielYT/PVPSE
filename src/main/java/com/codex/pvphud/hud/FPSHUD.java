package com.codex.pvphud.hud;

import com.codex.pvphud.theme.Theme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public final class FPSHUD extends HudElement {
    public FPSHUD() { super("fps", "FPS", 8, 84); }
    protected void renderElement(DrawContext c, MinecraftClient mc, Theme t, boolean editor, float alpha) {
        HudStyles.card(c, t, width(mc), height(mc), alpha);
        HudStyles.label(c, mc, Text.literal("FPS "), 6, 6, t.mutedText());
        HudStyles.label(c, mc, Text.literal(String.valueOf(mc.getCurrentFps())), 29, 6, t.accent());
    }
    public int width(MinecraftClient mc) { return 62; }
    public int height(MinecraftClient mc) { return 21; }
}
