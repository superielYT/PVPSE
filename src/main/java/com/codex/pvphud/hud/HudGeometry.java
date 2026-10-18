package com.codex.pvphud.hud;

public final class HudGeometry {
    private HudGeometry() {}
    public static float coordinate(float position, float viewport, float size) {
        if (!Float.isFinite(position)) return 0;
        return Math.clamp(position, 0, Math.max(0, viewport - size));
    }
}
