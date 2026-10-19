package com.codex.pvphud.hud;

import com.codex.pvphud.theme.Theme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;

public final class PingHUD extends HudElement {
    public PingHUD() { super("ping", "Ping", 74, 84); }
    protected void renderElement(GuiGraphicsExtractor c, Minecraft mc, Theme t, boolean editor, float alpha) {
        HudStyles.card(c, t, width(mc), height(mc), alpha);
        HudStyles.label(c, mc, Component.literal("PING "), 6, 6, t.mutedText());
        HudStyles.label(c, mc, Component.literal(ping(mc) + "ms"), 33, 6, t.accent());
    }
    private int ping(Minecraft mc) {
        if (mc.getConnection() == null || mc.player == null) return 0;
        PlayerInfo entry = mc.getConnection().getPlayerInfo(mc.player.getUUID());
        return entry == null ? 0 : entry.getLatency();
    }
    public int width(Minecraft mc) { return 76; }
    public int height(Minecraft mc) { return 21; }
}
