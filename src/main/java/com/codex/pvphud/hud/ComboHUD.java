package com.codex.pvphud.hud;

import com.codex.pvphud.PvpStats;
import com.codex.pvphud.theme.Theme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public final class ComboHUD extends HudElement {
    public ComboHUD() { super("combo", "Combo", 10, 250); }

    protected void renderElement(GuiGraphics context, Minecraft client, Theme theme, boolean editorMode, float alpha) {
        int combo = PvpStats.getInstance().combo();
        HudStyles.card(context, theme, width(client), height(client), alpha);
        context.drawString(client.font, Component.literal("COMBO"), 8, 6, theme.mutedText());
        context.drawString(client.font, Component.literal((combo > 0 ? combo : 0) + " hits"), 8, 18,
                combo >= 3 ? theme.accentSecondary() : theme.text());
    }

    public int width(Minecraft client) { return 82; }
    public int height(Minecraft client) { return 34; }
}
