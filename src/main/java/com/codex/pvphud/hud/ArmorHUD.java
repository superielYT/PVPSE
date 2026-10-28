package com.codex.pvphud.hud;

import com.codex.pvphud.theme.Theme;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import com.codex.pvphud.render.RoundedRenderer;

public final class ArmorHUD extends HudElement {
    public ArmorHUD() { super("armor", "Armor", 8, 182); }
    protected void renderElement(GuiGraphicsExtractor c, Minecraft mc, Theme t, boolean editor, float alpha) {
        HudStyles.card(c, t, width(mc), height(mc), alpha);
        int x = 5;
        int index = 0;
        for (EquipmentSlot slot : List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)) {
            ItemStack stack = mc.player == null ? ItemStack.EMPTY : mc.player.getItemBySlot(slot);
            RoundedRenderer.fill(c, x, 4, 24, 24, 4, 0x3A000000);
            if (!stack.isEmpty()) {
                c.item(stack, x + 4, 5);
                int percent = durabilityPercent(stack.getDamageValue(), stack.getMaxDamage());
                int color = percent <= 15 ? t.danger() : t.text();
                String value = stack.isDamageableItem() ? percent + "%" : "--";
                c.centeredText(mc.font, net.minecraft.network.chat.Component.literal(value), x + 12, 30, color);
                if (stack.isDamageableItem()) {
                    c.fill(x + 3, 24, x + 21, 26, 0x66000000);
                    c.fill(x + 3, 24, x + 3 + Math.round(18 * percent / 100.0F), 26,
                            percent <= 15 ? t.danger() : t.accentSecondary());
                }
            } else {
                c.centeredText(mc.font, net.minecraft.network.chat.Component.literal(new String[]{"H", "C", "L", "B"}[index]), x + 12, 11, t.mutedText());
                c.centeredText(mc.font, net.minecraft.network.chat.Component.literal("--"), x + 12, 30, t.mutedText());
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
    public int height(Minecraft mc) { return 43; }
}
