package com.codex.pvphud.hud;

import com.codex.pvphud.theme.Theme;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;

public final class PotionHUD extends HudElement {
    public PotionHUD() { super("potions", "Potions", 150, 188); }
    protected void renderElement(GuiGraphicsExtractor c, Minecraft mc, Theme t, boolean editor, float alpha) {
        List<MobEffectInstance> effects = mc.player.getActiveEffects().stream()
                .sorted(Comparator.comparing(effect -> effect.getEffect().value().getDescriptionId())).limit(5).toList();
        if (effects.isEmpty() && !editor) return;
        HudStyles.card(c, t, width(mc), heightFor(effects, mc), alpha);
        int y = 6;
        if (effects.isEmpty()) HudStyles.label(c, mc, Component.literal("No active effects"), 6, y, t.mutedText());
        for (MobEffectInstance effect : effects) {
            int seconds = effect.getDuration() / 20;
            String line = Component.translatable(effect.getDescriptionId()).getString() + "  " + seconds / 60 + ":" + String.format("%02d", seconds % 60);
            HudStyles.label(c, mc, Component.literal(line), 6, y, t.text());
            y += 12;
        }
    }
    private int heightFor(List<MobEffectInstance> effects, Minecraft mc) { return 12 + Math.max(1, effects.size()) * 12; }
    public int width(Minecraft mc) { return 118; }
    public int height(Minecraft mc) { return heightFor(mc.player == null ? List.of() : List.copyOf(mc.player.getActiveEffects()), mc); }
}
