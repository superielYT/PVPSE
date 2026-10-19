package com.codex.pvphud.theme;

/** Optional neutral, translucent theme; other palettes keep their identity. */
public record GlassTheme() implements Theme {
    public String id() { return "glass"; }
    public String displayName() { return "Clear Glass"; }
    public int background() { return 0x900B1018; }
    public int panel() { return 0x48121A24; }
    public int panelHeader() { return 0x60202C38; }
    public int accent() { return 0xFFD4E6F5; }
    public int accentSecondary() { return 0xFFA8C2D8; }
    public int text() { return 0xFFF5F8FC; }
    public int mutedText() { return 0xFFBAC8D6; }
    public int danger() { return 0xFFFF7886; }
    public int glow() { return 0x18000000; }
}
