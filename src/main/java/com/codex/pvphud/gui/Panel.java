package com.codex.pvphud.gui;

import com.codex.pvphud.hud.HudElement;
import com.codex.pvphud.render.RenderUtil;
import com.codex.pvphud.theme.Theme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public final class Panel {
    private final String title;
    private final List<ModuleButton> buttons = new ArrayList<>();
    private int x;
    private int y;
    private int width;
    private int viewportHeight;
    private int scrollOffset;
    private boolean scrollbarDragging;
    private int scrollbarDragOffset;
    private int thumbY;
    private int thumbHeight;
    private int trackTop;
    private int trackBottom;

    public Panel(String title, List<HudElement> elements) {
        this.title = title;
        elements.forEach(element -> buttons.add(new ModuleButton(element)));
    }

    public void render(DrawContext context, MinecraftClient client, Theme theme, int x, int y, int width, int viewportHeight,
                       int mouseX, int mouseY, String query) {
        this.x = x; this.y = y; this.width = width; this.viewportHeight = Math.max(80, viewportHeight);
        clampScroll(query);
        RenderUtil.panel(context, x, y, width, this.viewportHeight, theme);
        context.drawTextWithShadow(client.textRenderer, Text.literal(title), x + 9, y + 8, theme.accent());
        int contentTop = y + 27;
        int contentBottom = y + this.viewportHeight - 5;
        context.enableScissor(x + 2, contentTop, x + width - 2, contentBottom);
        int by = y + 28 - scrollOffset;
        for (ModuleButton button : filtered(query)) {
            button.render(context, client, theme, x + 5, by, width - 10, mouseX, mouseY);
            by += button.totalHeight() + 4;
        }
        context.disableScissor();
        drawScrollbar(context, theme, query, contentTop, contentBottom);
    }

    public boolean click(double mouseX, double mouseY, int button, String query) {
        if (mouseX < x || mouseX >= x + width || mouseY < y + 27 || mouseY >= y + viewportHeight) return false;
        if (button == 0 && contentHeight(query) > viewportHeight - 32
                && mouseX >= x + width - 9 && mouseX < x + width) {
            scrollbarDragging = true;
            scrollbarDragOffset = mouseY >= thumbY && mouseY < thumbY + thumbHeight
                    ? (int) mouseY - thumbY : thumbHeight / 2;
            drag(mouseX, mouseY, query);
            return true;
        }
        for (ModuleButton module : filtered(query)) if (module.click(mouseX, mouseY, button)) return true;
        return false;
    }

    public boolean drag(double mouseX, double mouseY, String query) {
        if (!scrollbarDragging) return false;
        int available = trackBottom - trackTop;
        int travel = Math.max(1, available - thumbHeight - 4);
        int target = Math.clamp((int) mouseY - scrollbarDragOffset - trackTop - 2, 0, travel);
        int maximum = Math.max(0, contentHeight(query) - available);
        scrollOffset = Math.round(maximum * target / (float) travel);
        return true;
    }

    public void release() {
        scrollbarDragging = false;
    }

    public boolean scroll(double mouseX, double mouseY, double verticalAmount, String query) {
        if (mouseX < x || mouseX >= x + width || mouseY < y || mouseY >= y + viewportHeight) return false;
        scrollOffset -= (int) Math.round(verticalAmount * 24.0D);
        clampScroll(query);
        return true;
    }

    private List<ModuleButton> filtered(String query) {
        if (query == null || query.isBlank()) return buttons;
        String needle = query.toLowerCase(java.util.Locale.ROOT);
        return buttons.stream().filter(button -> button.element().name().toLowerCase(java.util.Locale.ROOT).contains(needle)).toList();
    }

    private int contentHeight(String query) {
        return filtered(query).stream().mapToInt(button -> button.totalHeight() + 4).sum() + 3;
    }

    private void clampScroll(String query) {
        int available = Math.max(1, viewportHeight - 32);
        int maximum = Math.max(0, contentHeight(query) - available);
        scrollOffset = Math.clamp(scrollOffset, 0, maximum);
    }

    private void drawScrollbar(DrawContext context, Theme theme, String query, int top, int bottom) {
        trackTop = top;
        trackBottom = bottom;
        int content = contentHeight(query);
        int available = bottom - top;
        if (content <= available) {
            scrollbarDragging = false;
            return;
        }
        int trackX = x + width - 4;
        context.fill(trackX - 2, top + 2, trackX + 3, bottom - 2, 0x553B424C);
        thumbHeight = Math.max(16, Math.round(available * (available / (float) content)));
        int maximum = Math.max(1, content - available);
        int travel = Math.max(1, available - thumbHeight - 4);
        thumbY = top + 2 + Math.round(travel * (scrollOffset / (float) maximum));
        context.fill(trackX - 2, thumbY, trackX + 3, thumbY + thumbHeight, theme.accent());
    }
}
