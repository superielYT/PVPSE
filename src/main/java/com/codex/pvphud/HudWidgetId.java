package com.codex.pvphud;

enum HudWidgetId {
    COMBAT("combat", 0.02f, 0.03f),
    PLAYER("player", 0.02f, 0.12f),
    LOCATION("location", 0.74f, 0.03f),
    HELD("held", 0.74f, 0.12f),
    TARGET("target", 0.36f, 0.03f),
    EFFECTS("effects", 0.02f, 0.72f),
    ARMOR("armor", 0.68f, 0.68f),
    STACK("stack", 0.02f, 0.03f);

    final String key;
    final float defaultX;
    final float defaultY;

    HudWidgetId(String key, float defaultX, float defaultY) {
        this.key = key;
        this.defaultX = defaultX;
        this.defaultY = defaultY;
    }
}
