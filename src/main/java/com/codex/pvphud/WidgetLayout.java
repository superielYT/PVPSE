package com.codex.pvphud;

final class WidgetLayout {
    float x;
    float y;
    boolean visible = true;

    WidgetLayout() {
    }

    WidgetLayout(float x, float y) {
        this.x = x;
        this.y = y;
    }

    void reset(HudWidgetId id) {
        x = id.defaultX;
        y = id.defaultY;
        visible = true;
    }
}
