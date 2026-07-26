package com.codex.pvphud.hud;

import com.codex.pvphud.theme.Theme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.List;

public final class ArrayListHUD extends HudElement {
    private final List<String> labels = List.of("Premium HUD", "Combat Feedback", "Custom Crosshair");
    public ArrayListHUD() { super("array_list", "Array List", 8, 34); }
    protected void renderElement(DrawContext c, MinecraftClient mc, Theme t, boolean editor, float alpha) {
        HudStyles.card(c, t, width(mc), height(mc), alpha);
        int y = 6;
        for (String label : labels) {
            HudStyles.label(c, mc, Text.literal(label), 7, y, t.text());
            c.fill(width(mc) - 3, y - 1, width(mc) - 1, y + mc.textRenderer.fontHeight, t.accent());
            y += 12;
        }
    }
    public int width(MinecraftClient mc) { return 112; }
    public int height(MinecraftClient mc) { return 43; }
}
