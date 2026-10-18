package com.codex.pvphud.waypoint;

public final class RadarMathTest {
    private static void near(double actual, double expected) {
        if (Math.abs(actual - expected) > 0.00001) throw new AssertionError(actual + " != " + expected);
    }
    private static void point(double dx, double dz, double yaw, boolean north, double range, double x, double y) {
        var p = RadarMath.project(dx, dz, yaw, north, range, 40);
        near(p.x(), x); near(p.y(), y);
    }
    public static void main(String[] args) {
        point(0, 32, 0, false, 64, 0, -20); // South is forward at yaw zero.
        point(-32, 0, 90, false, 64, 0, -20); // West is forward at yaw 90.
        point(32, 0, 0, false, 64, -20, 0); // East is left when facing south.
        point(32, 0, 180, false, 64, 20, 0);
        point(32, -32, 123, true, 64, 20, -20); // North-up ignores yaw.
        point(32, 0, 0, true, 128, 10, 0); // Zoom changes every marker's scale equally.
        var clipped = RadarMath.project(128, 64, 0, true, 64, 40);
        near(clipped.x(), 40); near(clipped.y(), 20);
        if (!clipped.outside()) throw new AssertionError("Distant point should be on the edge");
        if (RadarMath.project(64, 0, 0, true, 64, 40).outside()) throw new AssertionError("Boundary is visible");
        point(0, 0, 0, false, 0, 0, 0);
        System.out.println("Radar direction, zoom and edge checks passed");
    }
}
