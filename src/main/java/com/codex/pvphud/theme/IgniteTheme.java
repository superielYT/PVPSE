package com.codex.pvphud.theme;

public record IgniteTheme() implements Theme {
    public String id() { return "ignite"; }
    public String displayName() { return "PVPSE Ignite"; }
    public int background() { return 0xE80B0E12; }
    public int panel() { return 0xE8151A20; }
    public int panelHeader() { return 0xFF202832; }
    public int accent() { return 0xFFFF6500; }
    public int accentSecondary() { return 0xFFFFA12B; }
    public int text() { return 0xFFF4F7FA; }
    public int mutedText() { return 0xFF8D9AA6; }
    public int danger() { return 0xFFFF4655; }
    public int glow() { return 0x66FF6500; }
}
