package com.codex.pvphud.hud;

import com.codex.pvphud.theme.Theme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public final class FPSHUD extends HudElement {
    public FPSHUD() { super("fps", "FPS", 8, 84); }
    protected void renderElement(GuiGraphicsExtractor c, Minecraft mc, Theme t, boolean editor, float alpha) {
        HudStyles.card(c, t, width(mc), height(mc), alpha);
        HudStyles.label(c, mc, Component.literal("FPS "), 6, 6, t.mutedText());
        HudStyles.label(c, mc, Component.literal(String.valueOf(mc.getFps())), 29, 6, t.accent());
    }
    public int width(Minecraft mc) { return 62; }
    public int height(Minecraft mc) { return 21; }
}
