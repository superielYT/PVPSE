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
                    "textures/block/obsidian.png", "textures/gui/container/inventory.png",
                    "textures/block/fire_0.png", "textures/environment/celestial/sun.png",
                    "textures/environment/celestial/moon/full_moon.png", "textures/item/mace_breach.png",
                    "textures/entity/end_crystal/end_crystal.png"}) {
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
        for (String tab : new String[]{"COMBAT", "HITBOX", "EFFECTS", "STYLE", "PROFILE", "FPS", "AIM", "GENERAL"}) {
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
                var optional = arrangement.find("held_items").orElseThrow();
                optional.setPosition(101, 102); optional.setScale(1.2F); optional.resetSettings();
                if (optional.enabled() || arrangement.find("resource_packs").orElseThrow().enabled())
                    throw new AssertionError("Optional HUDs must default off, including after profile resets");
                optional.setPosition(101, 102); optional.setScale(1.2F);
                arrangement.arrange(mc);
                if (optional.x() != 101 || optional.y() != 102 || optional.scale() != 1.2F)
                    throw new AssertionError("Auto arrange moved a hidden card");
                var hiddenManager = new com.codex.pvphud.hud.HudManager();
                hiddenManager.elements().forEach(e -> e.setEnabled(false));
                var hiddenPack = hiddenManager.find("resource_packs").orElseThrow();
                hiddenPack.setPosition(10000, 10000);
                hiddenManager.render(null, mc, false);
                if (hiddenPack.x() != 10000 || hiddenPack.y() != 10000)
                    throw new AssertionError("Gameplay rendering moved a hidden card");
                var cards = arrangement.layoutElements().stream().filter(com.codex.pvphud.hud.HudElement::enabled).toList();
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
                if (com.codex.pvphud.hud.InventoryHUD.countAll(mc.player)[1] != 28)
                    throw new AssertionError("Single-pass inventory totals changed");
            });
            context.runOnClient(mc -> PvpHudClient.getInstance().openMenu(null));
            context.clickScreenButton("Settings & profiles");
            context.clickScreenButton("Block Overlay: ON");
            context.runOnClient(mc -> PvpHudClient.getInstance().restoreHud());
            context.clickScreenButton("COMBAT"); context.clickScreenButton("GENERAL");
            context.clickScreenButton("Block Overlay: OFF"); // Restore must keep it off, then explicitly turn it back on.
            context.setScreen(() -> null);
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
            testOptionalHuds(context);
            testPerformance(context);
            testCombatUpdate(context);
            testReforgedPack(context, world.getServer());
        }
    }

    private static void testCombatUpdate(ClientGameTestContext context) {
        context.runOnClient(mc -> {
            var card = new com.codex.pvphud.hud.HealthIndicatorHUD();
            if (card.enabled()) throw new AssertionError("Health HUD must default off");
            card.setPosition(12, 24); card.setScale(1.5F);
            card.setEnabled(true); card.setEnabled(false); card.setEnabled(true);
            if (card.x() != 12 || card.y() != 24 || card.scale() != 1.5F)
                throw new AssertionError("Health HUD toggle lost layout");
            PvpHudClient.getInstance().openMenu(null);
        });
        context.clickScreenButton("Settings & profiles"); context.clickScreenButton("AIM");
        context.clickScreenButton("Cooldown circle: ON");
        context.clickScreenButton("Crosshair reactions: ON");
        context.clickScreenButton("Circle radius: 13 >");
        context.runOnClient(mc -> {
            var saved = HudConfig.load();
            if (saved.cooldownRing || saved.crosshairReactions || saved.cooldownRingRadius != 15)
                throw new AssertionError("Aim settings did not persist");
        });
        context.clickScreenButton("Cooldown circle: OFF");
        context.clickScreenButton("Crosshair reactions: OFF");
        context.clickScreenButton("Health indicator: OFF");
        context.takeScreenshot("pvpse-combat-aim-settings");
        context.setScreen(() -> null);
        double attackSpeed = context.computeOnClient(mc -> mc.player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_SPEED).getBaseValue());
        context.runOnClient(mc -> {
            mc.player.getInventory().setItem(0, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_SWORD));
            mc.player.getInventory().setSelectedSlot(0);
            // Keep a genuine cooldown active long enough for software-rendered screenshot capture.
            mc.player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_SPEED).setBaseValue(2.5);
            mc.player.resetAttackStrengthTicker();
        });
        context.waitTick();
        context.takeScreenshot("pvpse-combat-ring-health");
        context.runOnClient(mc -> mc.player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_SPEED).setBaseValue(attackSpeed));
        context.waitTicks(30);
        context.takeScreenshot("pvpse-combat-ready-health");
        context.runOnClient(mc -> PvpHudClient.getInstance().openMenu(null));
        context.clickScreenButton("Settings & profiles"); context.clickScreenButton("AIM");
        context.clickScreenButton("Health indicator: ON");
        context.setScreen(() -> null);
    }

    private static void testReforgedPack(ClientGameTestContext context,
            net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext server) {
        server.runCommand("time set noon"); server.runCommand("weather clear");
        server.runCommand("tp @p 0 -60 -4 0 0");
        context.runOnClient(mc -> {
            var registry = mc.level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
            for (int i = 0; i < 36; i++) mc.player.getInventory().setItem(i, net.minecraft.world.item.ItemStack.EMPTY);
            String[] names = {"breach", "density", "wind_burst"};
            for (int i = 0; i < names.length; i++) {
                var stack = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.MACE);
                var values = new net.minecraft.world.item.enchantment.ItemEnchantments.Mutable(net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY);
                var key = net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ENCHANTMENT,
                        net.minecraft.resources.Identifier.fromNamespaceAndPath("minecraft", names[i]));
                values.set(registry.getOrThrow(key), 1); stack.set(net.minecraft.core.component.DataComponents.ENCHANTMENTS, values.toImmutable());
                mc.player.getInventory().setItem(9 + i, stack);
            }
            mc.player.getInventory().setItem(12, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_SWORD));
            mc.player.getInventory().setItem(13, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_AXE));
            mc.player.getInventory().setItem(14, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.END_CRYSTAL));
            mc.player.getInventory().setItem(15, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.GOLDEN_APPLE));
            mc.setScreen(new net.minecraft.client.gui.screens.inventory.InventoryScreen(mc.player));
        });
        context.waitTicks(5); context.takeScreenshot("pvpse-reforged-mace-variants"); context.setScreen(() -> null);
        server.runCommand("summon minecraft:end_crystal 0 -58 2 {ShowBottom:1b}");
        context.waitTicks(10); context.takeScreenshot("pvpse-reforged-crystal-world");
        context.runOnClient(mc -> {
            mc.player.getInventory().setSelectedSlot(0);
            mc.player.getInventory().setItem(0, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_SWORD));
            mc.player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.OFFHAND, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.SHIELD));
        });
        context.waitTicks(5); context.takeScreenshot("pvpse-reforged-low-shield");
        context.getInput().holdMouse(1); context.waitTicks(12);
        context.takeScreenshot("pvpse-reforged-blocking-shield"); context.getInput().releaseMouse(1);
        server.runCommand("gamemode survival");
        server.runCommand("effect give @p minecraft:fire_resistance 30 0 true");
        server.runCommand("setblock 0 -60 -4 minecraft:fire");
        context.waitTicks(15);
        context.runOnClient(mc -> mc.player.setRemainingFireTicks(200));
        context.waitTick(); context.takeScreenshot("pvpse-reforged-low-fire");
        server.runCommand("setblock 0 -60 -4 minecraft:air");
        context.runOnClient(mc -> mc.player.setRemainingFireTicks(0));
        server.runCommand("effect clear @p");
        server.runCommand("time set midnight"); server.runCommand("tp @p 0 -60 -4 0 -60");
        context.waitTicks(10); context.takeScreenshot("pvpse-reforged-native-moon");
        server.runCommand("time set noon");
    }

    private static void testPerformance(ClientGameTestContext context) {
        var original = context.computeOnClient(PerformanceController.Snapshot::capture);
        context.runOnClient(mc -> {
            mc.options.renderDistance().set(16); mc.options.simulationDistance().set(12);
            mc.options.entityShadows().set(true); mc.options.cloudStatus().set(net.minecraft.client.CloudStatus.FANCY);
            mc.options.particles().set(net.minecraft.server.level.ParticleStatus.ALL);
            PvpHudClient.getInstance().openMenu(null);
        });
        context.clickScreenButton("Settings & profiles"); context.clickScreenButton("FPS");
        context.clickScreenButton("Balanced");
        context.runOnClient(mc -> {
            if (mc.options.renderDistance().get() != 12 || mc.options.simulationDistance().get() != 8
                    || mc.options.entityShadows().get() || mc.options.cloudStatus().get() != net.minecraft.client.CloudStatus.OFF
                    || mc.options.particles().get() != net.minecraft.server.level.ParticleStatus.DECREASED)
                throw new AssertionError("Balanced did not apply real video settings");
        });
        context.takeScreenshot("pvpse-fps-balanced-settings");
        context.clickScreenButton("Max FPS");
        context.runOnClient(mc -> {
            if (mc.options.renderDistance().get() != 8 || mc.options.simulationDistance().get() != 5
                    || mc.options.particles().get() != net.minecraft.server.level.ParticleStatus.MINIMAL)
                throw new AssertionError("Max FPS preset is incorrect");
            var saved = HudConfig.load();
            var restarted = new PerformanceController(saved); restarted.initialize(mc);
            mc.options.renderDistance().set(6);
            restarted.setMode(mc, PerformanceMode.OFF);
            if (mc.options.renderDistance().get() != 6 || mc.options.simulationDistance().get() != 12
                    || !mc.options.entityShadows().get() || mc.options.cloudStatus().get() != net.minecraft.client.CloudStatus.FANCY
                    || mc.options.particles().get() != net.minecraft.server.level.ParticleStatus.ALL)
                throw new AssertionError("Restart/restore overwrote user changes or lost previous settings");
            PvpHudClient.getInstance().performance().refreshBudget();
        });
        context.clickScreenButton("Light HUD: ON");
        context.runOnClient(mc -> {
            if (com.codex.pvphud.render.RenderBudget.lightweight()) throw new AssertionError("HUD detail opt-out ignored");
        });
        context.clickScreenButton("Light HUD: OFF");
        context.setScreen(() -> null); context.waitTicks(5);
        context.takeScreenshot("pvpse-fps-light-hud-world");
        context.runOnClient(mc -> PvpHudClient.getInstance().openMenu(null));
        context.clickScreenButton("Settings & profiles"); context.clickScreenButton("FPS");
        context.clickScreenButton("Restore previous settings");
        context.runOnClient(mc -> {
            if (com.codex.pvphud.render.RenderBudget.lightweight()) throw new AssertionError("Restore left the light HUD active");
            mc.options.renderDistance().set(original.view); mc.options.simulationDistance().set(original.simulation);
            mc.options.entityShadows().set(original.shadows); mc.options.cloudStatus().set(original.clouds);
            mc.options.particles().set(original.particles); mc.options.save();
        });
        context.setScreen(() -> null);
    }

    private static void testOptionalHuds(ClientGameTestContext context) {
        context.runOnClient(mc -> {
            var sword = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_SWORD);
            sword.setDamageValue(sword.getMaxDamage() - 9);
            mc.player.getInventory().setItem(0, sword);
            mc.player.getInventory().setSelectedSlot(0);
            mc.player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.OFFHAND,
                    new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.WIND_CHARGE, 4));
            if (com.codex.pvphud.hud.HeldItemHUD.remaining(sword) != 9)
                throw new AssertionError("Held item remaining durability is incorrect");
            if (com.codex.pvphud.hud.HeldItemHUD.remaining(mc.player.getOffhandItem()) != 0)
                throw new AssertionError("Non-damageable offhand must not show durability");
            if (com.codex.pvphud.hud.PackDisplayHUD.activeNames(mc).stream().noneMatch(n -> n.contains("Ignite")))
                throw new AssertionError("Active resource pack HUD did not detect the enabled Ignite pack");
            PvpHudClient.getInstance().openMenu(null);
        });
        context.clickScreenButton("HUD editor");
        context.clickScreenButton("HUDs");
        context.clickScreenButton("Hidden");
        searchHud(context, "held");
        context.clickScreenButton("Held Items: OFF");
        acceptOptionalArrangement(context, "Held Items: OFF");
        context.clickScreenButton("Shown");
        context.takeScreenshot("pvpse-hud-search-shown");
        context.clickScreenButton("Hidden");
        searchHud(context, "resource packs");
        context.clickScreenButton("Resource Packs: OFF");
        acceptOptionalArrangement(context, "Resource Packs: OFF");
        context.clickScreenButton("Done");
        context.clickScreenButton("Done");
        context.clickScreenButton("Done");
        context.waitTicks(5);
        context.takeScreenshot("pvpse-optional-huds-world");
        context.runOnClient(mc -> mc.player.getInventory().setItem(0, net.minecraft.world.item.ItemStack.EMPTY));
        context.waitTicks(3);
        context.takeScreenshot("pvpse-held-empty-main-hand");
    }

    private static void acceptOptionalArrangement(ClientGameTestContext context, String retryLabel) {
        if (context.computeOnClient(mc -> mc.screen instanceof net.minecraft.client.gui.screens.ConfirmScreen)) {
            context.takeScreenshot("pvpse-hud-room-choice");
            context.clickScreenButton("No");
            context.clickScreenButton(retryLabel);
            if (!context.computeOnClient(mc -> mc.screen instanceof net.minecraft.client.gui.screens.ConfirmScreen))
                throw new AssertionError("Declining arrangement changed the optional HUD layout or enabled it");
            context.clickScreenButton("Yes");
        }
    }

    private static void searchHud(ClientGameTestContext context, String query) {
        context.runOnClient(mc -> {
            var box = mc.screen.children().stream().filter(e -> e instanceof net.minecraft.client.gui.components.EditBox)
                    .map(e -> (net.minecraft.client.gui.components.EditBox)e).findFirst().orElseThrow();
            box.setValue("");
        });
        context.runOnClient(mc -> {
            var box = mc.screen.children().stream().filter(e -> e instanceof net.minecraft.client.gui.components.EditBox)
                    .map(e -> (net.minecraft.client.gui.components.EditBox)e).findFirst().orElseThrow();
            mc.screen.setFocused(box);
        });
        context.getInput().typeChars(query); context.waitTicks(2);
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

        int viewportHeight = context.computeOnClient(mc -> mc.getWindow().getGuiScaledHeight());
        moveTo(context, card.x() + 8, card.y() + 8);
        context.getInput().holdMouse(0); context.waitTick();
        moveTo(context, card.x() + 8, viewportHeight - 1); context.waitTicks(2);
        context.getInput().releaseMouse(0); context.waitTick();
        context.runOnClient(mc -> {
            if (Math.abs(card.y() + card.height(mc) * card.scale() - viewportHeight) > 0.6F)
                throw new AssertionError("HUD cannot reach the exact bottom edge");
        });
        context.takeScreenshot("pvpse-hud-bottom-edge");
        context.getInput().pressKey(org.lwjgl.glfw.GLFW.GLFW_KEY_TAB);
        context.runOnClient(mc -> {
            if (mc.screen.children().stream().filter(e -> e instanceof net.minecraft.client.gui.components.Button)
                    .map(e -> (net.minecraft.client.gui.components.Button)e).anyMatch(e -> e.visible))
                throw new AssertionError("Tab did not hide the toolbar");
        });
        context.getInput().pressKey(org.lwjgl.glfw.GLFW.GLFW_KEY_TAB);
        context.clickScreenButton("Hide");
        if (card.enabled()) throw new AssertionError("Hide did not disable the card");
        // Disabled cards must return before touching the render context, even in editor mode.
        context.runOnClient(mc -> card.render(null, mc, null, true));
        before = card.scale();
        moveTo(context, card.x() + 8, card.y() + 8);
        context.getInput().scroll(2); context.waitTick();
        if (card.scale() != before) throw new AssertionError("Hidden card intercepted wheel input");
        context.takeScreenshot("pvpse-hud-hidden");
        context.clickScreenButton("HUDs");
        context.waitForScreen(com.codex.pvphud.hud.HudModulesScreen.class);
        context.clickScreenButton("Hidden");
        context.takeScreenshot("pvpse-hud-picker-hidden");
        float savedY = card.y(), savedScale = card.scale();
        context.clickScreenButton("Inventory Counter: OFF");
        context.clickScreenButton("Done");
        context.waitForScreen(HudEditor.class);
        if (!card.enabled() || card.y() != savedY || card.scale() != savedScale)
            throw new AssertionError("Restoring a hidden card lost its layout");
        float closeX = context.computeOnClient(mc -> card.x() + card.width(mc) * card.scale() - 7);
        moveTo(context, closeX, card.y() + 6);
        context.getInput().pressMouse(0);
        if (card.enabled()) throw new AssertionError("Card X did not close it");
        context.clickScreenButton("Done");
        if (saves.get() == 0) throw new AssertionError("Closing the editor did not save");
        context.runOnClient(mc -> mc.setScreen(new HudEditor(manager, null, saves::incrementAndGet)));
        if (card.enabled()) throw new AssertionError("Reopening the editor re-enabled a hidden card");
        context.setScreen(() -> null);
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

