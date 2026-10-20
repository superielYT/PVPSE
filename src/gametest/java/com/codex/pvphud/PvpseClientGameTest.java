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
                var pearl = net.minecraft.world.item.Items.ENDER_PEARL;
                mc.player.getInventory().setItem(0, new net.minecraft.world.item.ItemStack(pearl, 16));
                mc.player.getInventory().setItem(9, new net.minecraft.world.item.ItemStack(pearl, 8));
                mc.player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.OFFHAND, new net.minecraft.world.item.ItemStack(pearl, 4));
                int count = com.codex.pvphud.hud.InventoryHUD.countCarried(mc.player, pearl);
                if (count != 28) throw new AssertionError("Storage/hotbar/offhand count: " + count);
            });
            for (String preset : new String[]{"Sword preset", "Crystal preset", "Mace preset"}) {
                context.runOnClient(mc -> PvpHudClient.getInstance().openMenu(null));
                context.clickScreenButton("Settings & profiles");
                context.waitForScreen(ClientSettingsScreen.class);
                context.clickScreenButton(preset);
                context.setScreen(() -> null);
                context.waitTicks(5);
                context.takeScreenshot("pvpse-" + preset.replace(' ', '-'));
            }
            context.runOnClient(mc -> PvpHudClient.getInstance().openMenu(null));
            context.clickScreenButton("Settings & profiles");
            context.runOnClient(mc -> {
                var box = mc.screen.children().stream().filter(e -> e instanceof net.minecraft.client.gui.components.EditBox)
                        .map(e -> (net.minecraft.client.gui.components.EditBox)e).findFirst().orElseThrow();
                mc.screen.setFocused(box);
            });
            context.getInput().typeChars("armor geluid");
            context.waitTicks(2);
            context.takeScreenshot("pvpse-settings-search");
            context.clickScreenButton("Armor sound  > COMBAT");
            context.setScreen(() -> null);
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
            testEditorInput(context);
        }
    }

    private static void testEditorInput(ClientGameTestContext context) {
        var manager = context.computeOnClient(mc -> new com.codex.pvphud.hud.HudManager());
        var card = manager.find("inventory").orElseThrow();
        var saves = new java.util.concurrent.atomic.AtomicInteger();
        context.runOnClient(mc -> {
            manager.layoutElements().forEach(e -> e.setPosition(10000, 10000));
            card.setPosition(20, 60); card.setScale(1);
            mc.setScreen(new HudEditor(manager, null, saves::incrementAndGet));
        });
        moveTo(context, 28, 70);
        context.getInput().holdMouse(0); context.waitTick();
        moveTo(context, 58, 90); context.waitTicks(2);
        context.getInput().releaseMouse(0); context.waitTick();
        if (card.x() <= 21 || card.y() <= 61) throw new AssertionError("HUD dragging did not move the card");
        float before = card.scale();
        context.clickScreenButton("+");
        if (card.scale() <= before) throw new AssertionError("HUD plus button did not enlarge");
        context.clickScreenButton("-");
        if (card.scale() >= before + 0.09F) throw new AssertionError("HUD minus button did not shrink");
        moveTo(context, card.x() + 8, card.y() + 8);
        before = card.scale(); context.getInput().scroll(2); context.waitTick();
        if (card.scale() <= before) throw new AssertionError("HUD wheel scaling failed");
        before = card.scale();
        float[] corner = context.computeOnClient(mc -> new float[] {
                card.x() + card.width(mc) * card.scale(),
                card.y() + card.height(mc) * card.scale()
        });
        float right = corner[0];
        float bottom = corner[1];
        moveTo(context, right, bottom);
        context.getInput().holdMouse(0); context.waitTick();
        moveTo(context, right + 25, bottom + 10); context.waitTicks(2);
        context.getInput().releaseMouse(0); context.waitTick();
        if (card.scale() <= before) throw new AssertionError("HUD corner scaling failed");
        context.takeScreenshot("pvpse-hud-drag-resize");
        context.setScreen(() -> null);
        if (saves.get() == 0) throw new AssertionError("Closing the editor did not save");
    }

    private static void moveTo(ClientGameTestContext context, double x, double y) {
        double scale = context.computeOnClient(mc -> mc.getWindow().getWidth() / (double)mc.getWindow().getGuiScaledWidth());
        context.getInput().setCursorPos(x * scale, y * scale);
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

