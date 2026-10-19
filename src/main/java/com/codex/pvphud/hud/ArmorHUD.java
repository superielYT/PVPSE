package com.codex.pvphud.hud;

import com.codex.pvphud.theme.Theme;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import com.codex.pvphud.render.RoundedRenderer;

public final class ArmorHUD extends HudElement {
    public ArmorHUD() { super("armor", "Armor", 8, 182); }
    protected void renderElement(GuiGraphicsExtractor c, Minecraft mc, Theme t, boolean editor, float alpha) {
        HudStyles.card(c, t, width(mc), height(mc), alpha);
        HudStyles.label(c, mc, Component.literal("ARMOR"), 6, 5, t.mutedText());
        int x = 5;
        int index = 0;
        for (EquipmentSlot slot : List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)) {
            ItemStack stack = mc.player == null ? ItemStack.EMPTY : mc.player.getItemBySlot(slot);
            RoundedRenderer.fill(c, x, 17, 24, 24, 4, 0x3A000000);
            if (!stack.isEmpty()) {
                c.item(stack, x + 4, 18);
                int percent = durabilityPercent(stack.getDamageValue(), stack.getMaxDamage());
                int color = percent <= 15 ? t.danger() : t.text();
                String value = stack.isDamageableItem() ? percent + "%" : "--";
                c.centeredText(mc.font, Component.literal(value), x + 12, 43, color);
                if (stack.isDamageableItem()) {
                    c.fill(x + 3, 37, x + 21, 39, 0x66000000);
                    c.fill(x + 3, 37, x + 3 + Math.round(18 * percent / 100.0F), 39,
                            percent <= 15 ? t.danger() : t.accentSecondary());
                }
            } else {
                c.centeredText(mc.font, Component.literal(new String[]{"H", "C", "L", "B"}[index]), x + 12, 24, t.mutedText());
                c.centeredText(mc.font, Component.literal("--"), x + 12, 43, t.mutedText());
            }
            x += 27;
            index++;
        }
    }
    public static int durabilityPercent(int damage, int maximum) {
        if (maximum <= 0) return 0;
        return Math.clamp(Math.round(100.0F * ((long) maximum - damage) / maximum), 0, 100);
    }
    public int width(Minecraft mc) { return 115; }
    public int height(Minecraft mc) { return 56; }
}
