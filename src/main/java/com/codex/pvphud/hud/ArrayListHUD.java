package com.codex.pvphud.hud;

import com.codex.pvphud.theme.Theme;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public final class ArrayListHUD extends HudElement {
    private final List<String> labels = List.of("Premium HUD", "Combat Feedback", "Custom Crosshair");
    public ArrayListHUD() { super("array_list", "Array List", 8, 34); }
    protected void renderElement(GuiGraphics c, Minecraft mc, Theme t, boolean editor, float alpha) {
        HudStyles.card(c, t, width(mc), height(mc), alpha);
        int y = 6;
        for (String label : labels) {
            HudStyles.label(c, mc, Component.literal(label), 7, y, t.text());
            c.fill(width(mc) - 3, y - 1, width(mc) - 1, y + mc.font.lineHeight, t.accent());
            y += 12;
        }
    }
    public int width(Minecraft mc) { return 112; }
    public int height(Minecraft mc) { return 43; }
}
