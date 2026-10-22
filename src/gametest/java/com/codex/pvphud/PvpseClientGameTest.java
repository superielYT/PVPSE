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
        context.runOnClient(mc -> {
            for (String path : new String[]{"textures/item/mace.png", "textures/item/books/mending.png",
                    "textures/block/obsidian.png", "textures/gui/container/inventory.png"}) {
                var resource = mc.getResourceManager().getResource(net.minecraft.resources.Identifier.fromNamespaceAndPath("minecraft", path)).orElseThrow();
                if (!resource.sourcePackId().contains("PVPSE-Ignite")) throw new AssertionError("Ignite pack not active: " + path);
            }
        });
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
            testBlockOverlay(context, world.getServer());
            testTexturePack(context, world.getServer());
        }
    }

    private static void testBlockOverlay(ClientGameTestContext context,
            net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext server) {
        server.runCommand("setblock 0 -59 0 minecraft:stone");
        server.runCommand("tp @p 0.5 -60 -3 0 0");
        context.waitTicks(12);
        context.runOnClient(mc -> {
            var target = com.codex.pvphud.hud.BlockOverlayHUD.target(mc);
            if (target == null || !target.state().is(net.minecraft.world.level.block.Blocks.STONE)
                    || !target.position().equals(new net.minecraft.core.BlockPos(0, -59, 0))
                    || target.distance() < 2 || target.distance() > 4)
                throw new AssertionError("Block overlay must use the real crosshair raycast: " + target);
        });
        context.takeScreenshot("pvpse-block-overlay-stone");
        server.runCommand("setblock 0 -59 0 minecraft:oak_slab[type=top]");
        context.waitTicks(8);
        context.runOnClient(mc -> {
            var target = com.codex.pvphud.hud.BlockOverlayHUD.target(mc);
            if (target == null || !target.state().is(net.minecraft.world.level.block.Blocks.OAK_SLAB))
                throw new AssertionError("Block overlay did not refresh after target changed");
        });
        context.takeScreenshot("pvpse-block-overlay-slab");
        server.runCommand("tp @p 0.5 -60 -3 180 -70");
        context.waitTicks(8);
        context.runOnClient(mc -> {
            if (com.codex.pvphud.hud.BlockOverlayHUD.target(mc) != null)
                throw new AssertionError("Block overlay kept a stale target after looking at the sky");
            mc.hitResult = new net.minecraft.world.phys.EntityHitResult(mc.player);
            if (com.codex.pvphud.hud.BlockOverlayHUD.target(mc) != null)
                throw new AssertionError("Entity target must not be treated as a block");
        });
        context.waitTicks(2);
        context.takeScreenshot("pvpse-block-overlay-no-target");
    }

    private static void testTexturePack(ClientGameTestContext context,
            net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext server) {
        String[] enchants = {"wind_burst", "breach", "density", "lunge", "riptide", "channeling", "frost_walker",
                "soul_speed", "swift_sneak", "sharpness", "smite", "bane_of_arthropods", "fire_aspect", "knockback",
                "sweeping_edge", "protection", "blast_protection", "fire_protection", "projectile_protection",
                "feather_falling", "thorns", "respiration", "aqua_affinity", "depth_strider", "power", "punch",
                "flame", "infinity", "multishot", "piercing", "quick_charge", "impaling", "loyalty", "binding_curse",
                "vanishing_curse", "mending", "unbreaking", "efficiency", "fortune", "silk_touch", "looting", "luck_of_the_sea", "lure"};
        for (int page = 0; page < 2; page++) {
            final int offset = page * 27;
            context.runOnClient(mc -> {
                var registry = mc.level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
                for (int i = 0; i < 36; i++) mc.player.getInventory().setItem(i, net.minecraft.world.item.ItemStack.EMPTY);
                for (int i = 0; i < 27 && offset + i < enchants.length; i++) {
                    var stack = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.ENCHANTED_BOOK);
                    var values = new net.minecraft.world.item.enchantment.ItemEnchantments.Mutable(net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY);
                    var key = net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ENCHANTMENT,
                            net.minecraft.resources.Identifier.fromNamespaceAndPath("minecraft", enchants[offset + i]));
                    values.set(registry.getOrThrow(key), 1);
                    stack.set(net.minecraft.core.component.DataComponents.STORED_ENCHANTMENTS, values.toImmutable());
                    mc.player.getInventory().setItem(i + 9, stack);
                }
                mc.setScreen(new net.minecraft.client.gui.screens.inventory.InventoryScreen(mc.player));
            });
            context.waitTicks(5);
            context.takeScreenshot("pvpse-dark-inventory-books-" + page);
            context.setScreen(() -> null);
        }
        server.runCommand("time set noon");
        server.runCommand("weather clear");
        server.runCommand("fill -10 -60 4 10 -58 4 minecraft:stone_bricks");
        String[] blocks = {"obsidian", "crying_obsidian", "diamond_ore", "deepslate_emerald_ore", "oak_planks",
                "cherry_planks", "pink_wool", "light_blue_wool", "cobblestone"};
        for (int i = 0; i < blocks.length; i++) server.runCommand("setblock " + (i * 2 - 8) + " -59 3 minecraft:" + blocks[i]);
        server.runCommand("tp @p 0 -60 -4 0 0");
        context.waitTicks(20);
        context.takeScreenshot("pvpse-blocks-world");
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

