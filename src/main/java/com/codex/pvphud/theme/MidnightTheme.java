package com.codex.pvphud.theme;

public record MidnightTheme() implements Theme {
    public String id() { return "midnight"; }
    public String displayName() { return "Midnight"; }
    public int background() { return 0xEC070A12; }
    public int panel() { return 0xE8111724; }
    public int panelHeader() { return 0xFF182238; }
    public int accent() { return 0xFF5EA8FF; }
    public int accentSecondary() { return 0xFF8CC7FF; }
    public int text() { return 0xFFF1F6FF; }
    public int mutedText() { return 0xFF8796AE; }
    public int danger() { return 0xFFFF6680; }
    public int glow() { return 0x555EA8FF; }
}
