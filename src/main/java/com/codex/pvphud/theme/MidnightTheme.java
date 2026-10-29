package com.codex.pvphud.theme;

public record MidnightTheme() implements Theme {
    public String id() { return "midnight"; }
    public String displayName() { return "Midnight Ember"; }
    public int background() { return 0xF408090D; }
    public int panel() { return 0xEB111319; }
    public int panelHeader() { return 0xFF1C2029; }
    public int accent() { return 0xFFFF954D; }
    public int accentSecondary() { return 0xFF88BFFF; }
    public int text() { return 0xFFF4F5F7; }
    public int mutedText() { return 0xFFAAB2C2; }
    public int danger() { return 0xFFFF6680; }
    public int glow() { return 0x18FF954D; }
}
