package com.codex.pvphud.theme;

public record CyberpunkTheme() implements Theme {
    public String id() { return "cyberpunk"; }
    public String displayName() { return "Cyberpunk"; }
    public int background() { return 0xF0050608; }
    public int panel() { return 0xEE111317; }
    public int panelHeader() { return 0xFF1C2025; }
    public int accent() { return 0xFFF4FF27; }
    public int accentSecondary() { return 0xFFFF2FB3; }
    public int text() { return 0xFFF8FBE9; }
    public int mutedText() { return 0xFFA8AC8B; }
    public int danger() { return 0xFFFF315A; }
    public int glow() { return 0x66F4FF27; }
}
