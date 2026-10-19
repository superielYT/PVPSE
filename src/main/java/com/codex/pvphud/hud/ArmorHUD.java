package com.codex.pvphud.hud;

import com.codex.pvphud.theme.Theme;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public final class ArmorHUD extends HudElement {
    public ArmorHUD() { super("armor", "Armor", 8, 182); }
    protected void renderElement(GuiGraphicsExtractor c, Minecraft mc, Theme t, boolean editor, float alpha) {
        HudStyles.card(c, t, width(mc), height(mc), alpha);
        int x = 5;
        for (EquipmentSlot slot : List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)) {
            ItemStack stack = mc.player.getItemBySlot(slot);
            if (!stack.isEmpty()) {
                c.item(stack, x, 5);
                c.itemDecorations(mc.font, stack, x, 5);
            }
            x += 20;
        }
    }
    public int width(Minecraft mc) { return 90; }
    public int height(Minecraft mc) { return 28; }
}
