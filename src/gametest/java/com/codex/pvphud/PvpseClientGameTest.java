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
        for (String tab : new String[]{"COMBAT", "HITBOX", "EFFECTS", "STYLE", "PROFILE", "FPS", "AIM", "ITEMS", "GENERAL"}) {
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
            testRangeAndFullbright(context, world.getServer());
            testAuditFixes(context, world.getServer());
            testNativeHud(context, world.getServer());
            testCleanUpdate(context, world.getServer());
        }
    }

    private static void testCleanUpdate(ClientGameTestContext context,
            net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext server) {
        server.runCommand("time set noon");
        server.runCommand("weather clear");
        server.runCommand("fill -3 -59 -3 3 -53 6 minecraft:air");
        server.runCommand("setblock 0 -59 0 minecraft:oak_slab[type=top]");
        server.runCommand("tp @p 0.5 -60 -3 0 0");
        server.runCommand("item replace entity @p weapon.mainhand with minecraft:cooked_beef");
        context.runOnClient(mc -> {
            var theme = com.codex.pvphud.theme.ThemeManager.getInstance();
            theme.setTheme("midnight");
            var manager = PvpHudClient.getInstance().hudManager();
            if (manager.find("block_overlay").isPresent()) throw new AssertionError("Block overlay is still a card");
            for (String id : new String[]{"watermark","armor","inventory","health_indicator"}) manager.find(id).orElseThrow().setEnabled(true);
            manager.arrange(mc);
            manager.feature(com.codex.pvphud.hud.FeatureModule.Type.WAYPOINT).orElseThrow().setEnabled(true);
            var waypoints = com.codex.pvphud.waypoint.WaypointManager.getInstance();
            waypoints.clear();
            mc.getSingleplayerServer().execute(() -> {
                for (var player : mc.getSingleplayerServer().getPlayerList().getPlayers()) {
                    player.setHealth(player.getMaxHealth());
                    player.getFoodData().setFoodLevel(10); player.getFoodData().setSaturation(2);
                }
            });
            var helmet = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_HELMET);
            helmet.setDamageValue(helmet.getMaxDamage()-9);
            mc.player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD,helmet);
        });
        context.waitTicks(12);
        context.runOnClient(mc -> {
            var preview = FoodForecast.held(mc.player,SaturationState.value(mc));
            if (preview == null || preview.hunger()!=18 || preview.foodGain()!=8 || Math.abs(preview.saturationGain()-12.8F)>.15)
                throw new AssertionError("Held food prediction incorrect: "+preview);
            if (FoodForecast.held(mc.player,Float.NaN)==null || Float.isFinite(FoodForecast.held(mc.player,Float.NaN).saturation()))
                throw new AssertionError("Unknown multiplayer saturation must remain unknown");
            var manager=com.codex.pvphud.waypoint.WaypointManager.getInstance();
            var point=manager.addLookTarget(mc);
            if(point==null || !manager.waypoints().isEmpty() || manager.visible(mc).isEmpty() || !manager.tracked(point))
                throw new AssertionError("Temporary looked-at waypoint was not tracked separately");
            var route=com.codex.pvphud.waypoint.WaypointOverlay.route(3,4,0,0);
            if(Math.abs(route.distance()-5)>.001 || route.height()!=4 || route.arrived()) throw new AssertionError("Route distance/height incorrect");
        });
        context.waitTicks(3); context.takeScreenshot("pvpse-clean-food-waypoint");
        context.runOnClient(mc -> {
            mc.player.getInventory().setItem(1,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.ENDER_PEARL,16));
            var sword=new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_SWORD);
            sword.setDamageValue(sword.getMaxDamage()-20); mc.player.getInventory().setItem(2,sword);
            mc.player.getCooldowns().addCooldown(mc.player.getInventory().getItem(1),100);
            if(CleanHotbar.remainingTicks(mc.player,mc.player.getInventory().getItem(1))!=100) throw new AssertionError("Cooldown duration readout incorrect");
        });
        context.waitTicks(3); context.takeScreenshot("pvpse-clean-armor-hotbar");
        server.runCommand("setblock 0 -59 0 minecraft:air");
        server.runCommand("summon minecraft:iron_golem 2 -60 1 {NoAI:1b,Tags:[\"pvpse_general_box\"]}");
        context.runOnClient(mc -> {
            var module=PvpHudClient.getInstance().hudManager().feature(com.codex.pvphud.hud.FeatureModule.Type.MELEE_HITBOXES).orElseThrow();
            module.setEnabled(true); module.setValue(12);
        });
        context.waitTicks(10);
        context.runOnClient(mc -> {
            if(MeleeHitboxRenderer.selectedTarget(mc,12)!=null) throw new AssertionError("General-hitbox fixture must be off the crosshair");
            if(MeleeHitboxRenderer.outlineCount()==0) throw new AssertionError("Off-crosshair hitboxes disappeared while holding food");
        });
        context.takeScreenshot("pvpse-clean-general-hitboxes");
        server.runCommand("kill @e[tag=pvpse_general_box]");
        var shulker=context.computeOnClient(mc -> {
            var stack=new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.PURPLE_SHULKER_BOX);
            var items=net.minecraft.core.NonNullList.withSize(27,net.minecraft.world.item.ItemStack.EMPTY);
            items.set(0,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND,32));
            items.set(26,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.EMERALD,7));
            stack.set(net.minecraft.core.component.DataComponents.CONTAINER,net.minecraft.world.item.component.ItemContainerContents.fromItems(items));
            var data=ContainerPreview.of(stack).orElseThrow();
            if(data.items().size()!=27 || !data.items().get(1).isEmpty() || data.items().get(26).getCount()!=7)
                throw new AssertionError("Shulker preview lost empty slots or last-slot contents");
            if(!(net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent.create(stack.getTooltipImage().orElseThrow()) instanceof ContainerPreviewRenderer))
                throw new AssertionError("Tooltip preview renderer was not wired into vanilla");
            var hidden=stack.copy(); hidden.set(net.minecraft.core.component.DataComponents.TOOLTIP_DISPLAY,
                    net.minecraft.world.item.component.TooltipDisplay.DEFAULT.withHidden(net.minecraft.core.component.DataComponents.CONTAINER,true));
            if(ContainerPreview.of(hidden).isPresent()) throw new AssertionError("Hidden container tooltip was revealed");
            return stack;
        });
        previewScreenshot(context,shulker,"pvpse-clean-shulker-preview");
        var bundle=context.computeOnClient(mc -> {
            var stack=new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.BUNDLE);
            stack.set(net.minecraft.core.component.DataComponents.BUNDLE_CONTENTS,new net.minecraft.world.item.component.BundleContents(java.util.List.of(
                    new net.minecraft.world.item.ItemStackTemplate(net.minecraft.world.item.Items.APPLE,16),
                    new net.minecraft.world.item.ItemStackTemplate(net.minecraft.world.item.Items.ENDER_PEARL,4))));
            var data=ContainerPreview.of(stack).orElseThrow();
            if(!data.bundle() || data.items().size()!=2 || Math.abs(data.fullness()-.5F)>.001) throw new AssertionError("Bundle contents or capacity incorrect");
            return stack;
        });
        previewScreenshot(context,bundle,"pvpse-clean-bundle-preview");
        context.runOnClient(mc -> PvpHudClient.getInstance().openMenu(null));
        context.clickScreenButton("Settings & profiles"); context.clickScreenButton("ITEMS");
        context.clickScreenButton("Container previews: ON"); context.clickScreenButton("Held food preview: ON"); context.clickScreenButton("Clean hotbar: ON");
        context.runOnClient(mc -> {
            var saved=HudConfig.load();
            if(saved.containerPreviews || saved.foodPreview || saved.cleanHotbar || ContainerPreview.of(shulker).isPresent())
                throw new AssertionError("New controls did not persist or disable previews");
        });
        context.clickScreenButton("Container previews: OFF"); context.clickScreenButton("Held food preview: OFF"); context.clickScreenButton("Clean hotbar: OFF");
        context.setScreen(() -> null);
    }

    private static void previewScreenshot(ClientGameTestContext context, net.minecraft.world.item.ItemStack stack,String name) {
        context.runOnClient(mc -> mc.setScreen(new net.minecraft.client.gui.screens.inventory.InventoryScreen(mc.player) {
            @Override public void extractRenderState(net.minecraft.client.gui.GuiGraphicsExtractor c,int mx,int my,float delta) {
                super.extractRenderState(c,mx,my,delta);
                c.setTooltipForNextFrame(mc.font,stack,width-10,height-10);
            }
        }));
        context.waitTicks(4); context.takeScreenshot(name); context.setScreen(() -> null);
    }

    private static void testNativeHud(ClientGameTestContext context,
            net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext server) {
        server.runCommand("gamemode survival");
        server.runCommand("effect clear @p");
        server.runCommand("effect give @p minecraft:speed 60 0 false");
        server.runCommand("effect give @p minecraft:strength infinite 0 false");
        server.runCommand("effect give @p minecraft:weakness 9 0 false");
        context.runOnClient(mc -> {
            var mod = PvpHudClient.getInstance();
            if (mod.hudManager().find("food_saturation").isPresent() || mod.hudManager().find("potions").isPresent())
                throw new AssertionError("Obsolete duplicate HUD cards are still registered");
            mod.hudManager().elements().forEach(e -> e.setEnabled(false));
            mc.options.hideGui = false;
            mc.getSingleplayerServer().execute(() -> {
                for (var player : mc.getSingleplayerServer().getPlayerList().getPlayers()) {
                    player.setHealth(player.getMaxHealth());
                    player.getFoodData().setFoodLevel(20);
                    player.getFoodData().setSaturation(11);
                }
            });
            var settings = new HudConfig(); settings.ensureWidgets();
            settings.hitboxTargetOnly = true; settings.ensureWidgets();
            if (!settings.hitboxTargetOnly) throw new AssertionError("Later target-only choice overwritten");
            if (!NativeHudOverlays.duration(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.SPEED, 1201)).equals("1:01"))
                throw new AssertionError("Potion time rounding incorrect");
        });
        context.waitTicks(12);
        context.runOnClient(mc -> {
            if (Math.abs(SaturationState.value(mc) - 11) > .1F || !Float.isFinite(SaturationState.value(mc)))
                throw new AssertionError("Integrated server saturation unavailable or incorrect");
            if (!PvpHudClient.getInstance().nativePotionTimersEnabled() || !PvpHudClient.getInstance().nativeSaturationEnabled())
                throw new AssertionError("Native overlays should default on");
        });
        var on = context.takeScreenshot("pvpse-native-hud-on");
        context.runOnClient(mc -> PvpHudClient.getInstance().openMenu(null));
        context.clickScreenButton("Settings & profiles");
        context.clickScreenButton("Potion timers: ON");
        context.clickScreenButton("Yellow saturation: ON");
        context.runOnClient(mc -> {
            var saved = HudConfig.load();
            if (saved.nativePotionTimers || saved.nativeSaturation) throw new AssertionError("Native HUD choices not saved");
        });
        context.setScreen(() -> null); context.waitTicks(2);
        var off = context.takeScreenshot("pvpse-native-hud-off");
        try {
            var a = javax.imageio.ImageIO.read(on.toFile());
            var b = javax.imageio.ImageIO.read(off.toFile());
            int yellowOn = 0, yellowOff = 0;
            for (int y = a.getHeight() * 3 / 4; y < a.getHeight(); y++) {
                for (int x = a.getWidth() / 2; x < a.getWidth() * 3 / 4; x++) {
                    int av = a.getRGB(x,y), bv = b.getRGB(x,y);
                    if ((av & 0xFFFFFF) == 0xFFD34A) yellowOn++;
                    if ((bv & 0xFFFFFF) == 0xFFD34A) yellowOff++;
                }
            }
            if (yellowOn <= yellowOff + 25) throw new AssertionError("Yellow saturation is missing on the actual hunger bar");
        } catch (java.io.IOException e) { throw new AssertionError(e); }
        context.runOnClient(mc -> PvpHudClient.getInstance().openMenu(null));
        context.clickScreenButton("Settings & profiles");
        context.clickScreenButton("Potion timers: OFF");
        context.clickScreenButton("Yellow saturation: OFF");
        context.setScreen(() -> null);
        server.runCommand("effect clear @p");
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
        context.runOnClient(mc -> PvpHudClient.getInstance().openMenu(null));
        context.clickScreenButton("Settings & profiles"); context.clickScreenButton("HUD: ON");
        context.setScreen(() -> null);
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
        server.runCommand("item replace entity @p weapon.mainhand with minecraft:diamond_sword");
        server.runCommand("item replace entity @p weapon.offhand with minecraft:shield");
        context.runOnClient(mc -> {
            mc.player.getInventory().setSelectedSlot(0);
            mc.player.getInventory().setItem(0, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_SWORD));
            mc.player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.OFFHAND, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.SHIELD));
        });
        context.waitTicks(10); context.takeScreenshot("pvpse-reforged-low-shield");
        context.getInput().holdMouse(1); context.waitTicks(12);
        context.runOnClient(mc -> { if (!mc.player.isUsingItem()) throw new AssertionError("Shield blocking was not active"); });
        context.takeScreenshot("pvpse-reforged-blocking-shield"); context.getInput().releaseMouse(1);
        server.runCommand("gamemode survival");
        server.runCommand("effect give @p minecraft:fire_resistance 30 0 true");
        server.runCommand("setblock 0 -60 -4 minecraft:fire");
        context.waitTicks(15);
        context.runOnClient(mc -> { mc.player.setRemainingFireTicks(200); mc.player.setSharedFlagOnFire(true);
            if (!mc.player.isOnFire()) throw new AssertionError("Fire overlay fixture is not burning"); });
        context.waitTick(); context.takeScreenshot("pvpse-reforged-low-fire");
        server.runCommand("setblock 0 -60 -4 minecraft:air");
        context.runOnClient(mc -> { mc.player.setRemainingFireTicks(0); mc.player.setSharedFlagOnFire(false); });
        server.runCommand("effect clear @p");
        server.runCommand("time set midnight"); server.runCommand("tp @p 0 -60 -4 0 -60");
        context.waitTicks(10); context.takeScreenshot("pvpse-reforged-native-moon");
        server.runCommand("time set noon");
        context.runOnClient(mc -> PvpHudClient.getInstance().openMenu(null));
        context.clickScreenButton("Settings & profiles"); context.clickScreenButton("HUD: OFF");
        context.setScreen(() -> null);
    }

    private static void testRangeAndFullbright(ClientGameTestContext context,
            net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext server) {
        server.runCommand("fill -2 -60 -3 2 -55 31 minecraft:air");
        server.runCommand("tp @p 0 -60 -4 0 0");
        server.runCommand("summon minecraft:iron_golem 0 -60 8 {NoAI:1b,Tags:[\"pvpse_range\"]}");
        context.waitTicks(15);
        context.runOnClient(mc -> {
            if (MeleeHitboxRenderer.selectedTarget(mc, 12) == null || MeleeHitboxRenderer.selectedTarget(mc, 8) != null)
                throw new AssertionError("12-block visual selection still uses vanilla reach");
        });
        server.runCommand("fill -1 -60 2 1 -56 2 minecraft:stone"); context.waitTicks(8);
        context.runOnClient(mc -> { if (MeleeHitboxRenderer.selectedTarget(mc, 12) != null) throw new AssertionError("Hitbox selected through a wall"); });
        server.runCommand("fill -1 -60 2 1 -56 2 minecraft:air");
        server.runCommand("kill @e[tag=pvpse_range]");
        server.runCommand("summon minecraft:iron_golem 0 -60 25 {NoAI:1b,Tags:[\"pvpse_range\"]}"); context.waitTicks(12);
        context.runOnClient(mc -> { if (MeleeHitboxRenderer.selectedTarget(mc, 32) == null || MeleeHitboxRenderer.selectedTarget(mc, 12) != null) throw new AssertionError("32-block selection failed"); });
        server.runCommand("fill -4 -61 -8 4 -55 0 minecraft:stone");
        server.runCommand("fill -3 -60 -7 3 -56 -1 minecraft:air");
        server.runCommand("tp @p 0 -60 -4 0 0"); server.runCommand("time set midnight");
        double gamma = context.computeOnClient(mc -> mc.options.gamma().get());
        context.runOnClient(mc -> PvpHudClient.getInstance().hudManager().feature(com.codex.pvphud.hud.FeatureModule.Type.FULLBRIGHT).orElseThrow().setEnabled(false));
        context.waitTicks(12); var dark = context.takeScreenshot("pvpse-fullbright-off");
        context.runOnClient(mc -> {
            var feature = PvpHudClient.getInstance().hudManager().feature(com.codex.pvphud.hud.FeatureModule.Type.FULLBRIGHT).orElseThrow(); feature.setValue(1); feature.setEnabled(true);
        });
        context.waitTicks(12); var bright = context.takeScreenshot("pvpse-fullbright-on");
        context.runOnClient(mc -> {
            if (FullbrightState.strength() != 1 || mc.player.getEffect(net.minecraft.world.effect.MobEffects.NIGHT_VISION) != null || mc.options.gamma().get() != gamma)
                throw new AssertionError("Fullbright changed real effects or video settings");
            PvpHudClient.getInstance().hudManager().feature(com.codex.pvphud.hud.FeatureModule.Type.FULLBRIGHT).orElseThrow().setEnabled(false);
        });
        context.waitTicks(3);
        context.runOnClient(mc -> { if (FullbrightState.strength() != 0 || mc.player.hasEffect(net.minecraft.world.effect.MobEffects.NIGHT_VISION)) throw new AssertionError("Fullbright did not restore darkness"); });
        try {
            var a=javax.imageio.ImageIO.read(dark.toFile()); var b=javax.imageio.ImageIO.read(bright.toFile()); long off=0,on=0;
            for(int y=a.getHeight()/3;y<a.getHeight()/2;y++)for(int x=a.getWidth()/3;x<a.getWidth()*2/3;x++) { off+=(a.getRGB(x,y)>>16)&255; on+=(b.getRGB(x,y)>>16)&255; }
            if(on<=off*1.4+1000) throw new AssertionError("Fullbright did not brighten the actual dark-room pixels");
        } catch(java.io.IOException e) {throw new AssertionError(e);}
    }

    private static void testAuditFixes(ClientGameTestContext context,
            net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext server) {
        context.runOnClient(mc -> {
            var manager = new com.codex.pvphud.hud.HudManager();
            manager.elements().forEach(e -> e.setEnabled(false));
            var watermark = manager.find("watermark").orElseThrow();
            watermark.setEnabled(true); watermark.setPosition(42, 43); watermark.setScale(1.2F);
            manager.register(new com.codex.pvphud.hud.HudElement("too_large", "Oversized fixture", 75, 76) {
                public int width(net.minecraft.client.Minecraft client) { return 100000; }
                public int height(net.minecraft.client.Minecraft client) { return 100000; }
                protected void renderElement(net.minecraft.client.gui.GuiGraphicsExtractor c, net.minecraft.client.Minecraft client,
                        com.codex.pvphud.theme.Theme t, boolean editor, float alpha) {}
            });
            if (manager.tryArrange(mc) || watermark.x() != 42 || watermark.y() != 43 || watermark.scale() != 1.2F)
                throw new AssertionError("Failed auto arrange damaged the existing layout");
            if (SettingsSearch.find("hitbox afstand").stream().noneMatch(r -> r.label().equals("View range")))
                throw new AssertionError("Hitbox distance is missing from search");
            var feature = PvpHudClient.getInstance().hudManager().feature(com.codex.pvphud.hud.FeatureModule.Type.FULLBRIGHT).orElseThrow();
            feature.setValue(1); feature.setEnabled(true);
        });
        server.runCommand("effect give @p minecraft:night_vision 5 0 true"); context.waitTicks(8);
        context.runOnClient(mc -> {
            var effect = mc.player.getEffect(net.minecraft.world.effect.MobEffects.NIGHT_VISION);
            if (effect == null || effect.getDuration() > 100) throw new AssertionError("Real expiring night vision fixture missing");
            for (float delta : new float[]{0, .25F, .5F, .75F})
                if (net.minecraft.client.renderer.GameRenderer.getNightVisionScale(mc.player, delta) < .999F)
                    throw new AssertionError("Real night vision flickers through fullbright");
            PvpHudClient.getInstance().hudManager().feature(com.codex.pvphud.hud.FeatureModule.Type.FULLBRIGHT).orElseThrow().setEnabled(false);
        });
        context.waitTicks(3);
        context.runOnClient(mc -> {
            if (FullbrightState.strength() != 0 || mc.player.getEffect(net.minecraft.world.effect.MobEffects.NIGHT_VISION) == null)
                throw new AssertionError("Disabling fullbright removed the real potion effect");
        });
        server.runCommand("effect clear @p");
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

