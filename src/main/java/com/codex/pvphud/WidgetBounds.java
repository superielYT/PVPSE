package com.codex.pvphud;

record WidgetBounds(HudWidgetId id, int x, int y, int width, int height) {
    private static final int CLOSE_SIZE = 8;

    int closeX() {
        return x + width - CLOSE_SIZE - 2;
    }

    int closeY() {
        return y + 2;
    }

    boolean contains(double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    boolean closeContains(double mouseX, double mouseY) {
        int cx = closeX();
        int cy = closeY();
        return mouseX >= cx && mouseX < cx + CLOSE_SIZE && mouseY >= cy && mouseY < cy + CLOSE_SIZE;
    }

    boolean titleContains(double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + 14;
    }
}
