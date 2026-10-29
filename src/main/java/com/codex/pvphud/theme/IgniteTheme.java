package com.codex.pvphud.theme;

public record IgniteTheme() implements Theme {
    public String id() { return "ignite"; }
    public String displayName() { return "PVPSE Ignite"; }
    public int background() { return 0xF0080C13; }
    public int panel() { return 0xE9121720; }
    public int panelHeader() { return 0xFF1D2532; }
    public int accent() { return 0xFFFF6500; }
    public int accentSecondary() { return 0xFFFFA12B; }
    public int text() { return 0xFFF4F7FA; }
    public int mutedText() { return 0xFFA3ADBC; }
    public int danger() { return 0xFFFF4655; }
    public int glow() { return 0x20FF6500; }
}
