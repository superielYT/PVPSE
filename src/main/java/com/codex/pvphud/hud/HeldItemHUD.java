package com.codex.pvphud.hud;

import com.codex.pvphud.theme.Theme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Optional hand status: actual stacks and durability, refreshed when the player switches items. */
public final class HeldItemHUD extends HudElement {
    public HeldItemHUD() { super("held_items", "Held Items", 8, 120, false); }

    public static int remaining(ItemStack stack) {
        return stack.isDamageableItem() ? Math.max(0, stack.getMaxDamage() - stack.getDamageValue()) : 0;
    }

    @Override protected void renderElement(GuiGraphicsExtractor c, Minecraft mc, Theme theme, boolean editor, float alpha) {
        HudStyles.card(c, theme, width(mc), height(mc), alpha);
        HudStyles.label(c, mc, Component.literal("HELD ITEMS"), 6, 5, theme.mutedText());
        ItemStack main = mc.player == null ? ItemStack.EMPTY : mc.player.getMainHandItem();
        ItemStack offhand = mc.player == null ? ItemStack.EMPTY : mc.player.getOffhandItem();
        if (editor && main.isEmpty() && offhand.isEmpty()) main = new ItemStack(Items.DIAMOND_SWORD);
        drawHand(c, mc, theme, main, "Main", 18);
        drawHand(c, mc, theme, offhand, "Offhand", 46);
    }

    private void drawHand(GuiGraphicsExtractor c, Minecraft mc, Theme theme, ItemStack stack, String hand, int y) {
        if (stack.isEmpty()) {
            HudStyles.label(c, mc, Component.literal(hand + ": empty"), 7, y + 4, theme.mutedText());
            return;
        }
        c.item(stack, 6, y + 1);
        HudStyles.label(c, mc, Component.literal(mc.font.plainSubstrByWidth(stack.getHoverName().getString(), 149)), 27, y, theme.text());
        String detail;
        int color = theme.mutedText();
        if (stack.isDamageableItem()) {
            int percent = ArmorHUD.durabilityPercent(stack.getDamageValue(), stack.getMaxDamage());
            detail = remaining(stack) + " / " + stack.getMaxDamage() + "  |  " + percent + "%";
            color = percent <= 15 ? theme.danger() : theme.accentSecondary();
            c.fill(27, y + 23, 176, y + 25, 0x66000000);
            c.fill(27, y + 23, 27 + Math.round(149 * percent / 100F), y + 25, color);
        } else detail = hand + "  |  x" + stack.getCount();
        HudStyles.label(c, mc, Component.literal(mc.font.plainSubstrByWidth(detail, 149)), 27, y + 11, color);
    }

    public int width(Minecraft mc) { return 184; }
    public int height(Minecraft mc) { return 77; }
}
