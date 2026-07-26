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

    public Panel(String title, List<HudElement> elements) {
        this.title = title;
        elements.forEach(element -> buttons.add(new ModuleButton(element)));
    }

    public void render(DrawContext context, MinecraftClient client, Theme theme, int x, int y, int width,
                       int mouseX, int mouseY, String query) {
        this.x = x; this.y = y; this.width = width;
        RenderUtil.panel(context, x, y, width, 26 + contentHeight(query), theme);
        context.drawTextWithShadow(client.textRenderer, Text.literal(title), x + 9, y + 8, theme.accent());
        int by = y + 28;
        for (ModuleButton button : filtered(query)) {
            button.render(context, client, theme, x + 5, by, width - 10, mouseX, mouseY);
            by += button.totalHeight() + 4;
        }
    }

    public boolean click(double mouseX, double mouseY, int button, String query) {
        for (ModuleButton module : filtered(query)) if (module.click(mouseX, mouseY, button)) return true;
        return false;
    }

    private List<ModuleButton> filtered(String query) {
        if (query == null || query.isBlank()) return buttons;
        String needle = query.toLowerCase(java.util.Locale.ROOT);
        return buttons.stream().filter(button -> button.element().name().toLowerCase(java.util.Locale.ROOT).contains(needle)).toList();
    }

    private int contentHeight(String query) {
        return filtered(query).stream().mapToInt(button -> button.totalHeight() + 4).sum() + 3;
    }
}
