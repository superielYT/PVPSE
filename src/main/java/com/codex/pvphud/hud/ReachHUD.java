package com.codex.pvphud.hud;

import com.codex.pvphud.PvpStats;
import com.codex.pvphud.theme.Theme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.Locale;

public final class ReachHUD extends HudElement {
    public ReachHUD() { super("reach", "Reach", 98, 250); }

    protected void renderElement(DrawContext context, MinecraftClient client, Theme theme, boolean editorMode, float alpha) {
        HudStyles.card(context, 0, 0, width(client), height(client), theme, alpha);
        context.drawTextWithShadow(client.textRenderer, Text.literal("REACH"), 8, 6, theme.mutedText());
        String value = PvpStats.getInstance().lastReach() <= 0 ? "--" :
                String.format(Locale.ROOT, "%.2f blocks", PvpStats.getInstance().lastReach());
        context.drawTextWithShadow(client.textRenderer, Text.literal(value), 8, 18, theme.text());
    }

    public int width(MinecraftClient client) { return 94; }
    public int height(MinecraftClient client) { return 34; }
}
