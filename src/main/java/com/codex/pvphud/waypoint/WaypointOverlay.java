package com.codex.pvphud.waypoint;

import com.codex.pvphud.render.RoundedRenderer;
import com.codex.pvphud.theme.Theme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.Locale;

public final class WaypointOverlay {
    private WaypointOverlay() {}

    public static void render(DrawContext context, MinecraftClient client, Theme theme) {
        if (client.player == null || client.world == null) return;
        String dimension = client.world.getRegistryKey().getValue().toString();
        int index = 0;
        for (var point : WaypointManager.getInstance().waypoints()) {
            if (!point.dimension().equals(dimension) || index >= 8) continue;
            double dx = point.x() + 0.5 - client.player.getX();
            double dz = point.z() + 0.5 - client.player.getZ();
            double distance = Math.sqrt(dx * dx + dz * dz);
            double bearing = Math.atan2(-dx, dz);
            double relative = wrap(bearing - Math.toRadians(client.player.getYaw()));
            int centerX = context.getScaledWindowWidth() / 2;
            int x = centerX + (int) Math.clamp(Math.round(Math.sin(relative) * centerX * 0.72), -centerX + 55, centerX - 55);
            int y = 48 + index++ * 18;
            String label = point.name() + "  " + String.format(Locale.ROOT, "%.0fm", distance);
            int width = client.textRenderer.getWidth(label) + 14;
            RoundedRenderer.fill(context, x - width / 2, y, width, 15, 4, theme.panel());
            context.drawCenteredTextWithShadow(client.textRenderer, Text.literal(label), x, y + 4, theme.accent());
        }
    }

    private static double wrap(double angle) {
        while (angle > Math.PI) angle -= Math.PI * 2;
        while (angle < -Math.PI) angle += Math.PI * 2;
        return angle;
    }
}
