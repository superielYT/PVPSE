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
    private static final Component TITLE = Component.literal("INVENTORY");
    private final ItemStack[] icons = java.util.Arrays.stream(ITEMS).map(ItemStack::new).toArray(ItemStack[]::new);
    private final Component[] labels = java.util.stream.IntStream.range(0, ITEMS.length).mapToObj(i -> Component.literal("0")).toArray(Component[]::new);
    private java.lang.ref.WeakReference<Player> lastPlayer = new java.lang.ref.WeakReference<>(null);
    private int lastTick = Integer.MIN_VALUE;
    private int[] counts = new int[ITEMS.length];
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

    public static int[] countAll(Player player) {
        int[] result = new int[ITEMS.length];
        if (player == null) return result;
        for (int slot = 0; slot < 36; slot++) add(player.getInventory().getItem(slot), result);
        add(player.getOffhandItem(), result); return result;
    }

    private static void add(ItemStack stack, int[] result) {
        if (stack.isEmpty()) return;
        for (int i = 0; i < ITEMS.length; i++) if (stack.is(ITEMS[i])) { result[i] += stack.getCount(); break; }
    }

    private void refresh(Player player) {
        int tick = player == null ? 0 : player.tickCount;
        if (lastPlayer.get() == player && lastTick == tick) return;
        if (lastPlayer.get() != player) lastPlayer = new java.lang.ref.WeakReference<>(player);
        lastTick = tick;
        int[] next = countAll(player);
        for (int i = 0; i < next.length; i++) if (next[i] != counts[i]) labels[i] = Component.literal(Integer.toString(next[i]));
        counts = next;
    }

    @Override protected void renderElement(GuiGraphicsExtractor c, Minecraft mc, Theme theme, boolean editor, float alpha) {
        HudStyles.card(c, theme, width(mc), height(mc), alpha);
        refresh(mc.player);
        HudStyles.label(c, mc, TITLE, 6, 5, theme.mutedText());
        for (int i = 0; i < ITEMS.length; i++) {
            int x = 5 + i % 3 * 48, y = 18 + i / 3 * 21;
            c.item(icons[i], x, y);
            int count = counts[i];
            HudStyles.label(c, mc, labels[i], x + 18, y + 4,
                    count == 0 ? theme.mutedText() : theme.text());
        }
    }
    public int width(Minecraft mc) { return 154; }
    public int height(Minecraft mc) { return 62; }
}
