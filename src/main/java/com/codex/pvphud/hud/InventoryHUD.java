package com.codex.pvphud.hud;

import com.codex.pvphud.theme.Theme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Counts carried storage/hotbar items plus the offhand exactly once. */
public final class InventoryHUD extends HudElement {
    private static final Item[] ITEMS = {Items.TOTEM_OF_UNDYING, Items.ENDER_PEARL, Items.GOLDEN_APPLE,
            Items.ENCHANTED_GOLDEN_APPLE, Items.WIND_CHARGE, Items.END_CRYSTAL};
    public InventoryHUD() { super("inventory", "Inventory Counter", 280, 150); }

    public static int countCarried(Player player, Item item) {
        if (player == null) return 0;
        int count = 0;
        for (int i = 0; i < 36; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(item)) count += stack.getCount();
        }
        ItemStack offhand = player.getOffhandItem();
        if (offhand.is(item)) count += offhand.getCount();
        return count;
    }

    @Override protected void renderElement(GuiGraphicsExtractor c, Minecraft mc, Theme theme, boolean editor, float alpha) {
        HudStyles.card(c, theme, width(mc), height(mc), alpha);
        HudStyles.label(c, mc, Component.literal("INVENTORY"), 6, 5, theme.mutedText());
        for (int i = 0; i < ITEMS.length; i++) {
            int x = 5 + i % 3 * 48, y = 18 + i / 3 * 21;
            c.item(new ItemStack(ITEMS[i]), x, y);
            int count = countCarried(mc.player, ITEMS[i]);
            HudStyles.label(c, mc, Component.literal(Integer.toString(count)), x + 18, y + 4,
                    count == 0 ? theme.mutedText() : theme.text());
        }
    }
    public int width(Minecraft mc) { return 154; }
    public int height(Minecraft mc) { return 62; }
}
