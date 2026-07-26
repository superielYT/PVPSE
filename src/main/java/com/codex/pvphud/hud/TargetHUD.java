package com.codex.pvphud.hud;

import com.codex.pvphud.theme.Theme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;

public final class TargetHUD extends HudElement {
    public TargetHUD() { super("target", "Target", 150, 112); }
    protected void renderElement(DrawContext c, MinecraftClient mc, Theme t, boolean editor, float alpha) {
        LivingEntity target = mc.crosshairTarget instanceof EntityHitResult hit && hit.getEntity() instanceof LivingEntity living ? living : null;
        if (target == null && !editor) return;
        HudStyles.card(c, t, width(mc), height(mc), alpha);
        String name = target == null ? "Target preview" : target.getName().getString();
        float hp = target == null ? 20.0F : target.getHealth();
        HudStyles.label(c, mc, Text.literal(name), 7, 6, t.text());
        HudStyles.label(c, mc, Text.literal(String.format("%.1f HP", hp)), 7, 19, t.accent());
        int bar = Math.clamp(Math.round(hp / Math.max(1.0F, target == null ? 20.0F : target.getMaxHealth()) * 104), 0, 104);
        c.fill(7, 32, 111, 35, 0x55000000);
        c.fill(7, 32, 7 + bar, 35, t.accent());
    }
    public int width(MinecraftClient mc) { return 118; }
    public int height(MinecraftClient mc) { return 42; }
}
