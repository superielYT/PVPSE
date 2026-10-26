package com.codex.pvphud.hud;

import com.codex.pvphud.theme.Theme;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

/** Optional, movable personal health card. Uses server-synchronized player health. */
public final class HealthIndicatorHUD extends HudElement {
    public HealthIndicatorHUD() { super("health_indicator", "Health Indicator", 150, 164, false); }
    protected void renderElement(GuiGraphicsExtractor c, Minecraft mc, Theme t, boolean editor, float alpha) {
        if (mc.player == null) return;
        float hp = mc.player.getHealth(), max = Math.max(1, mc.player.getMaxHealth());
        float absorption = mc.player.getAbsorptionAmount();
        int color = hp / max <= .3F ? t.danger() : t.accent();
        HudStyles.card(c, t, width(mc), height(mc), alpha);
        HudStyles.label(c, mc, Component.literal("HEALTH"), 7, 6, t.mutedText());
        String text = String.format(Locale.ROOT, "%.1f / %.0f HP", hp, max);
        HudStyles.label(c, mc, Component.literal(text), 7, 19, color);
        c.fill(7, 32, 125, 36, 0x55000000);
        c.fill(7, 32, 7 + Math.clamp(Math.round(hp / max * 118), 0, 118), 36, color);
        if (absorption > 0) {
            HudStyles.label(c, mc, Component.literal(String.format(Locale.ROOT, "+%.1f shield", absorption)), 7, 41, 0xFFFFD76A);
        }
    }
    public int width(Minecraft mc) { return 132; }
    public int height(Minecraft mc) { return 56; }
}
