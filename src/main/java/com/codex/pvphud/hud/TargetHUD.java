package com.codex.pvphud.hud;

import com.codex.pvphud.theme.Theme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;

public final class TargetHUD extends HudElement {
    public TargetHUD() { super("target", "Target", 150, 112); }
    protected void renderElement(GuiGraphics c, Minecraft mc, Theme t, boolean editor, float alpha) {
        LivingEntity target = mc.hitResult instanceof EntityHitResult hit && hit.getEntity() instanceof LivingEntity living ? living : null;
        if (target == null && !editor) return;
        HudStyles.card(c, t, width(mc), height(mc), alpha);
        String name = target == null ? "Target preview" : target.getName().getString();
        float hp = target == null ? 20.0F : target.getHealth();
        HudStyles.label(c, mc, Component.literal(mc.font.plainSubstrByWidth(name, width(mc) - 14)), 7, 6, t.text());
        HudStyles.label(c, mc, Component.literal(String.format("%.1f HP", hp)), 7, 19, t.accent());
        int bar = Math.clamp(Math.round(hp / Math.max(1.0F, target == null ? 20.0F : target.getMaxHealth()) * 104), 0, 104);
        c.fill(7, 32, 111, 35, 0x55000000);
        c.fill(7, 32, 7 + bar, 35, t.accent());
    }
    public int width(Minecraft mc) { return 118; }
    public int height(Minecraft mc) { return 42; }
}
