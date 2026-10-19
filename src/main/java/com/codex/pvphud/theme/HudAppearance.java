package com.codex.pvphud.theme;

/** Per-profile HUD appearance. Menu surfaces stay readable independently. */
public final class HudAppearance {
    public int opacity = 100;
    public boolean accents = true;
    public boolean shadows = true;
    public boolean textShadow = true;

    public void normalize() { opacity = Math.clamp(opacity, 20, 100); }
    public int panelColor(int color, float visibility) {
        float fade = Float.isFinite(visibility) ? Math.clamp(visibility, 0, 1) : 1;
        int alpha = Math.round((color >>> 24) * Math.clamp(opacity, 20, 100) / 100.0F * fade);
        return (alpha << 24) | (color & 0xFFFFFF);
    }
}
