package com.codex.pvphud.waypoint;

import com.codex.pvphud.render.RoundedRenderer;
import com.codex.pvphud.theme.Theme;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public final class WaypointOverlay {
    private WaypointOverlay() {}

    public static void render(GuiGraphics context, Minecraft client, Theme theme) {
        if (client.player == null || client.level == null || !WaypointManager.getInstance().labels()) return;
        String dimension = client.level.dimension().identifier().toString();
        int index = 0;
        for (var point : WaypointManager.getInstance().visible(client)) {
            if (!point.dimension().equals(dimension) || index >= Math.min(6, Math.max(1, (context.guiHeight() - 90) / 18))) continue;
            double dx = point.x() + 0.5 - client.player.getX();
            double dz = point.z() + 0.5 - client.player.getZ();
            double distance = Math.sqrt(dx * dx + dz * dz);
            double bearing = Math.atan2(-dx, dz);
            double relative = wrap(bearing - Math.toRadians(client.player.getYRot()));
            int centerX = context.guiWidth() / 2;
            int x = centerX + (int) Math.clamp(Math.round(Math.sin(relative) * centerX * 0.72), -centerX + 55, centerX - 55);
            int y = 48 + index++ * 18;
            String direction = Math.cos(relative) < 0 ? (Math.sin(relative) < 0 ? "< " : "> ") : "";
            int dy = (int) Math.round(point.y() - client.player.getY());
            String label = direction + (WaypointManager.getInstance().tracked(point) ? "* " : "") + point.name() + String.format(Locale.ROOT, " %.0fm (%+dY)", distance, dy);
            label = client.font.plainSubstrByWidth(label, Math.max(1, context.guiWidth() - 28));
            int width = client.font.width(label) + 14;
            if (Math.cos(relative) < 0) x = Math.sin(relative) < 0 ? width / 2 + 4 : context.guiWidth() - width / 2 - 4;
            x = Math.clamp(x, width / 2 + 4, context.guiWidth() - width / 2 - 4);
            RoundedRenderer.fill(context, x - width / 2, y, width, 15, 4, theme.panel());
            context.drawCenteredString(client.font, Component.literal(label), x, y + 4, point.tint());
        }
    }

    private static double wrap(double angle) {
        while (angle > Math.PI) angle -= Math.PI * 2;
        while (angle < -Math.PI) angle += Math.PI * 2;
        return angle;
    }
}
