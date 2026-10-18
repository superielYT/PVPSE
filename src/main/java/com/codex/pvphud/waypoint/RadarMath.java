package com.codex.pvphud.waypoint;

/** Coordinate projection shared by the radar and its deterministic checks. */
public final class RadarMath {
    private RadarMath() {}
    public record Point(double x, double y, boolean outside) {}
    public static Point project(double dx, double dz, double yawDegrees, boolean northUp, double range, double radius) {
        double yaw = Math.toRadians(yawDegrees);
        double x = northUp ? dx : -dx * Math.cos(yaw) - dz * Math.sin(yaw);
        double y = northUp ? dz : dx * Math.sin(yaw) - dz * Math.cos(yaw);
        double scale = radius / Math.max(1, range);
        x *= scale; y *= scale;
        double edge = Math.max(Math.abs(x), Math.abs(y));
        boolean outside = edge > radius;
        if (outside) { x *= radius / edge; y *= radius / edge; }
        return new Point(x, y, outside);
    }
}
