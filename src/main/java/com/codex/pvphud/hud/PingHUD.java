package com.codex.pvphud.hud;

import com.codex.pvphud.theme.Theme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;

public final class PingHUD extends HudElement {
    public PingHUD() { super("ping", "Ping", 74, 84); }
    protected void renderElement(DrawContext c, MinecraftClient mc, Theme t, boolean editor, float alpha) {
        HudStyles.card(c, t, width(mc), height(mc), alpha);
        HudStyles.label(c, mc, Text.literal("PING "), 6, 6, t.mutedText());
        HudStyles.label(c, mc, Text.literal(ping(mc) + "ms"), 33, 6, t.accent());
    }
    private int ping(MinecraftClient mc) {
        if (mc.getNetworkHandler() == null || mc.player == null) return 0;
        PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
        return entry == null ? 0 : entry.getLatency();
    }
    public int width(MinecraftClient mc) { return 76; }
    public int height(MinecraftClient mc) { return 21; }
}
