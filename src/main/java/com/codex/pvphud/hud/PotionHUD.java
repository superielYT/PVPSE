package com.codex.pvphud.hud;

import com.codex.pvphud.theme.Theme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.text.Text;

import java.util.Comparator;
import java.util.List;

public final class PotionHUD extends HudElement {
    public PotionHUD() { super("potions", "Potions", 150, 188); }
    protected void renderElement(DrawContext c, MinecraftClient mc, Theme t, boolean editor, float alpha) {
        List<StatusEffectInstance> effects = mc.player.getStatusEffects().stream()
                .sorted(Comparator.comparing(effect -> effect.getEffectType().value().getTranslationKey())).limit(5).toList();
        if (effects.isEmpty() && !editor) return;
        HudStyles.card(c, t, width(mc), heightFor(effects, mc), alpha);
        int y = 6;
        if (effects.isEmpty()) HudStyles.label(c, mc, Text.literal("No active effects"), 6, y, t.mutedText());
        for (StatusEffectInstance effect : effects) {
            int seconds = effect.getDuration() / 20;
            String line = Text.translatable(effect.getTranslationKey()).getString() + "  " + seconds / 60 + ":" + String.format("%02d", seconds % 60);
            HudStyles.label(c, mc, Text.literal(line), 6, y, t.text());
            y += 12;
        }
    }
    private int heightFor(List<StatusEffectInstance> effects, MinecraftClient mc) { return 12 + Math.max(1, effects.size()) * 12; }
    public int width(MinecraftClient mc) { return 118; }
    public int height(MinecraftClient mc) { return heightFor(mc.player == null ? List.of() : List.copyOf(mc.player.getStatusEffects()), mc); }
}
