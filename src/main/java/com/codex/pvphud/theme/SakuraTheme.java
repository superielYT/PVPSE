package com.codex.pvphud.theme;

public record SakuraTheme() implements Theme {
    public String id() { return "sakura"; }
    public String displayName() { return "Sakura Night"; }
    public int background() { return 0xEF160B16; }
    public int panel() { return 0xE9291629; }
    public int panelHeader() { return 0xFF41203B; }
    public int accent() { return 0xFFFF76B8; }
    public int accentSecondary() { return 0xFFFFB0D5; }
    public int text() { return 0xFFFFF3FA; }
    public int mutedText() { return 0xFFC9A0B8; }
    public int danger() { return 0xFFFF486D; }
    public int glow() { return 0x66FF76B8; }
}
