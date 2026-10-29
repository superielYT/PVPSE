package com.codex.pvphud.waypoint;

import com.codex.pvphud.theme.Theme;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public final class WaypointRadar {
    private WaypointRadar() {}
    public static void render(GuiGraphicsExtractor c, Minecraft mc, Theme t) {
        var manager = WaypointManager.getInstance();
        int cx = 72, cy = 86, radius = 43;
        c.fill(cx - 46, cy - 46, cx + 47, cy + 47, 0xE00C1016);
        for (int i = -1; i <= 1; i++) {
            c.fill(cx + i * 22, cy - radius, cx + i * 22 + 1, cy + radius, 0x333D5368);
            c.fill(cx - radius, cy + i * 22, cx + radius, cy + i * 22 + 1, 0x333D5368);
        }
        c.centeredText(mc.font, Component.literal(manager.northUp() ? "N" : "FWD"), cx, cy - 43, t.mutedText());
        if (manager.northUp()) {
            c.centeredText(mc.font, Component.literal("S"), cx, cy + 34, t.mutedText());
            c.text(mc.font, Component.literal("W"), cx - 42, cy - 4, t.mutedText());
            c.text(mc.font, Component.literal("E"), cx + 36, cy - 4, t.mutedText());
        }
        if (mc.player == null) return;
        var points = manager.visible(mc);
        // Draw the destination last so nearby points cannot obscure it.
        for (int i = points.size() - 1; i >= 0; i--) {
            var p = points.get(i);
            var projection = RadarMath.project(p.x() + .5 - mc.player.getX(), p.z() + .5 - mc.player.getZ(), mc.player.getYRot(), manager.northUp(), manager.mapRange(), radius - 4);
            int x = cx + (int) Math.round(projection.x()), y = cy + (int) Math.round(projection.y());
            c.fill(x - 2, y - 2, x + 3, y + 3, 0xCC000000);
            if (manager.tracked(p)) {
                c.fill(x - 4, y - 4, x + 5, y - 3, t.text());
                c.fill(x - 4, y + 4, x + 5, y + 5, t.text());
                c.fill(x - 4, y - 3, x - 3, y + 4, t.text());
                c.fill(x + 4, y - 3, x + 5, y + 4, t.text());
            }
            c.fill(x - 1, y - 1, x + 2, y + 2, p.tint());
            if (projection.outside()) c.fill(x - 2, y + 3, x + 3, y + 4, p.tint());
            if (manager.tracked(p)) {
                int dy = p.y() - mc.player.blockPosition().getY();
                if (Math.abs(dy) >= 4)
                    c.text(mc.font, Component.literal(dy > 0 ? "+" : "-"), x + 5, y - 4, p.tint());
            }
        }
        c.fill(cx - 2, cy - 2, cx + 3, cy + 3, t.text());
        double heading = manager.northUp() ? Math.toRadians(mc.player.getYRot()) : Math.PI;
        int hx = cx - (int) Math.round(Math.sin(heading) * 6), hy = cy + (int) Math.round(Math.cos(heading) * 6);
        c.fill(hx - 1, hy - 1, hx + 2, hy + 2, t.accent());
        String detail = points.isEmpty() ? "B: here / Shift+B: aim" : points.getFirst().name();
        if (!points.isEmpty()) {
            var p = points.getFirst();
            double distance = Math.hypot(p.x() + .5 - mc.player.getX(), p.z() + .5 - mc.player.getZ());
            detail = (manager.tracked(p) ? "> " : "") + detail + String.format(Locale.ROOT, " %.0fm", distance);
        }
        c.text(mc.font, Component.literal(mc.font.plainSubstrByWidth(detail, 130)), 7, 20, points.isEmpty() ? t.mutedText() : points.getFirst().tint());
        c.centeredText(mc.font, Component.literal(manager.mapRange() + "m | " + points.size() + " points"), cx, 137, t.mutedText());
    }
}
