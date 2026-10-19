package com.codex.pvphud;

import com.codex.pvphud.gui.ClickGuiScreen;
import com.codex.pvphud.hud.HudEditor;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.PauseScreen;

/** Checks the real entry points, settings, editor and visible HUD pixels. */
public final class PvpseClientGameTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        context.waitForScreen(TitleScreen.class);
        context.clickScreenButton("PVPSE");
        context.waitForScreen(ClickGuiScreen.class);
        context.waitTicks(5);
        context.takeScreenshot("pvpse-menu-title");
        context.clickScreenButton("Settings & profiles");
        context.waitForScreen(ClientSettingsScreen.class);
        for (String tab : new String[]{"COMBAT", "HITBOX", "EFFECTS", "STYLE", "PROFILES", "GENERAL"}) {
            context.clickScreenButton(tab);
            context.waitTicks(3);
            context.takeScreenshot("pvpse-settings-" + tab.toLowerCase(java.util.Locale.ROOT));
        }
        context.setScreen(TitleScreen::new);
        try (var world = context.worldBuilder().create()) {
            world.getClientLevel().waitForChunksRender();
            context.runOnClient(mc -> {
                PvpHudClient.getInstance().restoreHud();
                var arrangement = new com.codex.pvphud.hud.HudManager();
                arrangement.arrange(mc);
                var cards = arrangement.layoutElements();
                for (int i = 0; i < cards.size(); i++) {
                    var a = cards.get(i);
                    if (a.x() + a.width(mc) * a.scale() > mc.getWindow().getGuiScaledWidth()
                            || a.y() + a.height(mc) * a.scale() > mc.getWindow().getGuiScaledHeight() - 48)
                        throw new AssertionError("HUD card outside reserved viewport: " + a.id());
                    for (int j = i + 1; j < cards.size(); j++) {
                        var b = cards.get(j);
                        if (a.x() < b.x() + b.width(mc) * b.scale() && a.x() + a.width(mc) * a.scale() > b.x()
                                && a.y() < b.y() + b.height(mc) * b.scale() && a.y() + a.height(mc) * a.scale() > b.y())
                            throw new AssertionError("HUD overlap: " + a.id() + " / " + b.id());
                    }
                }
            });
            context.setScreen(() -> null);
            context.waitTicks(10);
            java.nio.file.Path screenshot = context.takeScreenshot("pvpse-hud-world");
            // Verify real PVPSE accent pixels in its watermark, not just a successful launch.
            int[] size = context.computeOnClient(mc -> new int[]{mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight()});
            int accent = context.computeOnClient(mc -> com.codex.pvphud.theme.ThemeManager.getInstance().getTheme().accent());
            requireAccent(screenshot, size[0], size[1], accent);
            context.runOnClient(mc -> {
                var slots = new net.minecraft.world.entity.EquipmentSlot[]{
                        net.minecraft.world.entity.EquipmentSlot.HEAD, net.minecraft.world.entity.EquipmentSlot.CHEST,
                        net.minecraft.world.entity.EquipmentSlot.LEGS, net.minecraft.world.entity.EquipmentSlot.FEET};
                var items = new net.minecraft.world.item.Item[]{
                        net.minecraft.world.item.Items.DIAMOND_HELMET, net.minecraft.world.item.Items.NETHERITE_CHESTPLATE,
                        net.minecraft.world.item.Items.IRON_LEGGINGS, net.minecraft.world.item.Items.GOLDEN_BOOTS};
                for (int i = 0; i < slots.length; i++) {
                    var stack = new net.minecraft.world.item.ItemStack(items[i]);
                    stack.setDamageValue(Math.round(stack.getMaxDamage() * i / 3.1F));
                    mc.player.setItemSlot(slots[i], stack);
                }
            });
            context.waitTicks(5);
            context.takeScreenshot("pvpse-armor-full-damaged");
            context.runOnClient(mc -> {
                mc.player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST, net.minecraft.world.item.ItemStack.EMPTY);
                mc.player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.FEET, net.minecraft.world.item.ItemStack.EMPTY);
            });
            context.waitTicks(5);
            context.takeScreenshot("pvpse-armor-missing-slots");
            context.setScreen(() -> new PauseScreen(true));
            context.clickScreenButton("PVPSE");
            context.waitForScreen(ClickGuiScreen.class);
            context.takeScreenshot("pvpse-menu-world");
            context.clickScreenButton("HUD editor");
            context.waitForScreen(HudEditor.class);
            context.waitTicks(5);
            context.takeScreenshot("pvpse-hud-editor");
            context.setScreen(() -> null);
        }
    }

    private static void requireAccent(java.nio.file.Path file, int guiWidth, int guiHeight, int color) {
        try {
            var image = javax.imageio.ImageIO.read(file.toFile());
            double sx = image.getWidth() / (double) guiWidth, sy = image.getHeight() / (double) guiHeight;
            int found = 0;
            for (int y = (int)(14 * sy); y < (int)(23 * sy); y++) {
                for (int x = (int)(15 * sx); x < (int)(48 * sx); x++) {
                    if ((image.getRGB(x, y) & 0xFFFFFF) == (color & 0xFFFFFF)) found++;
                }
            }
            if (found < 8) throw new AssertionError("PVPSE watermark did not render: " + found + " accent pixels");
        } catch (java.io.IOException e) { throw new AssertionError("Cannot read HUD screenshot", e); }
    }
}
