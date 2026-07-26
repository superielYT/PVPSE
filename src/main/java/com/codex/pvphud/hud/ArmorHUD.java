package com.codex.pvphud.hud;

import com.codex.pvphud.theme.Theme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

import java.util.List;

public final class ArmorHUD extends HudElement {
    public ArmorHUD() { super("armor", "Armor", 8, 182); }
    protected void renderElement(DrawContext c, MinecraftClient mc, Theme t, boolean editor, float alpha) {
        HudStyles.card(c, t, width(mc), height(mc), alpha);
        int x = 5;
        for (EquipmentSlot slot : List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)) {
            ItemStack stack = mc.player.getEquippedStack(slot);
            if (!stack.isEmpty()) {
                c.drawItem(stack, x, 5);
                c.drawStackOverlay(mc.textRenderer, stack, x, 5);
            }
            x += 20;
        }
    }
    public int width(MinecraftClient mc) { return 90; }
    public int height(MinecraftClient mc) { return 28; }
}
