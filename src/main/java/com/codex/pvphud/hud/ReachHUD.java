package com.codex.pvphud.hud;

import com.codex.pvphud.PvpStats;
import com.codex.pvphud.theme.Theme;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public final class ReachHUD extends HudElement {
    public ReachHUD() { super("reach", "Reach", 98, 250); }

    protected void renderElement(GuiGraphicsExtractor context, Minecraft client, Theme theme, boolean editorMode, float alpha) {
        HudStyles.card(context, theme, width(client), height(client), alpha);
        context.text(client.font, Component.literal("REACH"), 8, 6, theme.mutedText());
        String value = PvpStats.getInstance().lastReach() <= 0 ? "--" :
                String.format(Locale.ROOT, "%.2f blocks", PvpStats.getInstance().lastReach());
        context.text(client.font, Component.literal(value), 8, 18, theme.text());
    }

    public int width(Minecraft client) { return 94; }
    public int height(Minecraft client) { return 34; }
}
