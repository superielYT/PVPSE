package com.codex.pvphud.theme;

public interface Theme {
    String id();
    String displayName();
    int background();
    int panel();
    int panelHeader();
    int accent();
    int accentSecondary();
    int text();
    int mutedText();
    int danger();
    int glow();
}
