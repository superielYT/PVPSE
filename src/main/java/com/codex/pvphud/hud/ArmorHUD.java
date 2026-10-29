package com.codex.pvphud.hud;

import com.codex.pvphud.theme.Theme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import com.codex.pvphud.render.RoundedRenderer;

public final class ArmorHUD extends HudElement {
    private static final EquipmentSlot[] SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    public ArmorHUD() { super("armor", "Armor", 8, 182); }
    protected void renderElement(GuiGraphicsExtractor c, Minecraft mc, Theme t, boolean editor, float alpha) {
        HudStyles.card(c, t, width(mc), height(mc), alpha);
        HudStyles.label(c, mc, Component.literal("ARMOR"), 6, 5, t.mutedText());
        int threshold = com.codex.pvphud.PvpHudClient.getInstance().armorWarningThreshold();
        int weakest = 101;
        if (mc.player != null) for (var slot : SLOTS) {
            var stack = mc.player.getItemBySlot(slot);
            if (stack.isDamageableItem()) weakest = Math.min(weakest, durabilityPercent(stack.getDamageValue(), stack.getMaxDamage()));
        }
        if (weakest <= threshold && com.codex.pvphud.PvpHudClient.getInstance().armorWarningsEnabled())
            HudStyles.label(c,mc,Component.literal("LOW " + weakest + "%"),76,5,t.danger());
        int x = 5;
        int index = 0;
        for (EquipmentSlot slot : SLOTS) {
            ItemStack stack = mc.player == null ? ItemStack.EMPTY : mc.player.getItemBySlot(slot);
            RoundedRenderer.fill(c, x, 17, 29, 22, 3, 0x30000000);
            if (!stack.isEmpty()) {
                c.item(stack, x + 6, 18);
                int percent = durabilityPercent(stack.getDamageValue(), stack.getMaxDamage());
                int color = !stack.isDamageableItem() ? t.mutedText() : percent <= threshold ? t.danger() : percent <= 30 ? 0xFFFFC56B : t.text();
                String value = stack.isDamageableItem() ? percent + "%" : "--";
                c.centeredText(mc.font, Component.literal(value), x + 14, 43, color);
                if (stack.isDamageableItem()) {
                    c.fill(x + 2, 36, x + 27, 39, 0x66000000);
                    c.fill(x + 2, 36, x + 2 + Math.round(25 * percent / 100.0F), 39,
                            percent <= threshold ? t.danger() : percent <= 30 ? 0xFFFFC56B : t.accentSecondary());
                    String remaining = HeldItemHUD.remaining(stack) + " left";
                    c.pose().pushMatrix();
                    try {
                        c.pose().translate(x + 14F,55F); c.pose().scale(.65F,.65F);
                        c.text(mc.font,remaining,-mc.font.width(remaining)/2,0,t.mutedText(),false);
                    } finally { c.pose().popMatrix(); }
                }
            } else {
                c.centeredText(mc.font, Component.literal(new String[]{"H", "C", "L", "B"}[index]), x + 14, 23, t.mutedText());
                c.centeredText(mc.font, Component.literal("--"), x + 14, 43, t.mutedText());
            }
            x += 31;
            index++;
        }
    }
    public static int durabilityPercent(int damage, int maximum) {
        if (maximum <= 0) return 0;
        return Math.clamp(Math.round(100.0F * ((long) maximum - damage) / maximum), 0, 100);
    }
    public int width(Minecraft mc) { return 133; }
    public int height(Minecraft mc) { return 66; }
}
