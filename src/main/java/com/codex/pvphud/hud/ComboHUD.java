package com.codex.pvphud.hud;

import com.codex.pvphud.PvpStats;
import com.codex.pvphud.theme.Theme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public final class ComboHUD extends HudElement {
    public ComboHUD() { super("combo", "Combo", 10, 250); }

    protected void renderElement(DrawContext context, MinecraftClient client, Theme theme, boolean editorMode, float alpha) {
        int combo = PvpStats.getInstance().combo();
        HudStyles.card(context, theme, width(client), height(client), alpha);
        context.drawTextWithShadow(client.textRenderer, Text.literal("COMBO"), 8, 6, theme.mutedText());
        context.drawTextWithShadow(client.textRenderer, Text.literal((combo > 0 ? combo : 0) + " hits"), 8, 18,
                combo >= 3 ? theme.accentSecondary() : theme.text());
    }

    public int width(MinecraftClient client) { return 82; }
    public int height(MinecraftClient client) { return 34; }
}
