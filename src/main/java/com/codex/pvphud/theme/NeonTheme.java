package com.codex.pvphud.theme;

public record NeonTheme() implements Theme {
    public String id() { return "neon"; }
    public String displayName() { return "Neon Synthwave"; }
    public int background() { return 0xEE110822; }
    public int panel() { return 0xE8201038; }
    public int panelHeader() { return 0xFF311552; }
    public int accent() { return 0xFF24F1FF; }
    public int accentSecondary() { return 0xFFFF3BC8; }
    public int text() { return 0xFFFDF3FF; }
    public int mutedText() { return 0xFFC2A6D8; }
    public int danger() { return 0xFFFF477E; }
    public int glow() { return 0x6624F1FF; }
}
