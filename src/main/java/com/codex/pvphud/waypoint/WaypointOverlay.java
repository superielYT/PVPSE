package com.codex.pvphud.waypoint;

import com.codex.pvphud.render.RenderUtil;
import com.codex.pvphud.theme.Theme;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public final class WaypointOverlay {
    private WaypointOverlay() {}

    public record Route(double distance, int height, double angle, boolean arrived) {}
    public static Route route(double dx, double dy, double dz, float yaw) {
        double angle = Math.atan2(-dx, dz) - Math.toRadians(yaw);
        angle = Math.atan2(Math.sin(angle), Math.cos(angle));
        return new Route(Math.sqrt(dx*dx + dy*dy + dz*dz), (int)Math.round(dy), angle,
                Math.hypot(dx,dz) < 2 && Math.abs(dy) < 3);
    }

    public static void render(GuiGraphicsExtractor c, Minecraft mc, Theme theme) {
        var manager = WaypointManager.getInstance();
        if (mc.player == null || mc.level == null || !manager.labels() || mc.screen != null) return;
        var points = manager.visible(mc);
        if (points.isEmpty()) return;
        var point = points.getFirst();
        var route = route(point.x()+.5-mc.player.getX(), point.y()-mc.player.getY(),
                point.z()+.5-mc.player.getZ(), mc.player.getYRot());
        String turn = route.arrived() ? "Arrived" : Math.cos(route.angle()) < 0
                ? (route.angle() < 0 ? "Behind left" : "Behind right")
                : Math.abs(route.angle()) < .15 ? "Ahead" : route.angle() < 0 ? "Left" : "Right";
        String name = mc.font.plainSubstrByWidth(point.name(), Math.max(1, Math.min(160, c.guiWidth()-60)));
        String detail = String.format(Locale.ROOT, "%s  %.0fm  %+dY", turn, route.distance(), route.height());
        // Local coordinates inside the movable waypoint HUD, not a floating screen overlay.
        c.pose().pushMatrix();
        try {
            c.pose().translate(7F,137F);
            float scale=Math.min(1F,130F/Math.max(1,mc.font.width(detail)));
            c.pose().scale(scale,scale);
            c.text(mc.font,detail,0,0,point.tint(),false);
        } finally { c.pose().popMatrix(); }
    }
}
