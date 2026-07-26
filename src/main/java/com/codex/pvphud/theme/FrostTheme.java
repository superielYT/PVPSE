package com.codex.pvphud.theme;

public record FrostTheme() implements Theme {
    public String id() { return "frost"; }
    public String displayName() { return "Arctic Frost"; }
    public int background() { return 0xED07131D; }
    public int panel() { return 0xE8122634; }
    public int panelHeader() { return 0xFF183A4E; }
    public int accent() { return 0xFF68D8FF; }
    public int accentSecondary() { return 0xFFA6F0FF; }
    public int text() { return 0xFFF2FCFF; }
    public int mutedText() { return 0xFF91B7C6; }
    public int danger() { return 0xFFFF6388; }
    public int glow() { return 0x5568D8FF; }
}
