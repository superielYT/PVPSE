package com.codex.pvphud;

import com.codex.pvphud.config.ConfigManager;
import com.codex.pvphud.gui.ClickGuiScreen;
import com.codex.pvphud.hud.HudManager;
import com.codex.pvphud.hud.FeatureModule;
import com.codex.pvphud.MeleeHitboxRenderer;
import com.codex.pvphud.notification.NotificationManager;
import com.codex.pvphud.waypoint.WaypointManager;
import com.codex.pvphud.waypoint.WaypointOverlay;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public final class PvpHudClient implements ClientModInitializer {
    private static PvpHudClient instance;
    public static PvpHudClient getInstance() { return instance; }
    HudManager hudManager() { return hudManager; }
    private boolean nativeHudVisible() {
        var mc = Minecraft.getInstance();
        return config != null && config.enabled && !mc.options.hideGui
                && !(config.hideOnDebug && mc.getDebugOverlay().showDebugScreen());
    }
    public boolean nativePotionTimersEnabled() { return nativeHudVisible() && config.nativePotionTimers; }
    public boolean nativeSaturationEnabled() { return nativeHudVisible() && config.nativeSaturation; }
    public boolean foodPreviewEnabled() { return nativeHudVisible() && config.foodPreview; }
    public boolean containerPreviewsEnabled() { return config != null && config.containerPreviews; }
    public boolean cleanHotbarEnabled() { return nativeHudVisible() && config.cleanHotbar; }
    public int armorWarningThreshold() { return config == null ? 15 : config.armorWarningPercent; }

    private static final Identifier HUD_ID = Identifier.fromNamespaceAndPath("pvp_hud_client", "hud");
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("pvp_hud_client", "controls"));

    private final CpsTracker cps = new CpsTracker();
    private final CombatFeedback combatFeedback = new CombatFeedback();
    private final ArmorWarningSound armorSound = new ArmorWarningSound();
    private final HudManager hudManager = new HudManager();
    private final ConfigManager profileManager = new ConfigManager();
    private HudConfig config;
    private PerformanceController performance;
    PerformanceController performance() { return performance; }
    private KeyMapping toggleHudKey;
    private KeyMapping openConfigKey;
    private KeyMapping fallbackMenuKey;
    private boolean joinHintShown;
    private KeyMapping zoomKey;
    private KeyMapping waypointKey;
    private KeyMapping fullbrightKey;
    private int profileSaveTicks;
    private Double previousDamageTilt;
    private Integer previousFov;
    private Double previousGamma;

    @Override
    public void onInitializeClient() {
        instance = this;
        config = HudConfig.load();
        performance = new PerformanceController(config);
        profileManager.load(hudManager);
        toggleHudKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.pvp_hud_client.toggle_hud",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                CATEGORY
        ));
        openConfigKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.pvp_hud_client.open_config",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                CATEGORY
        ));
        fallbackMenuKey = registerKey("key.pvp_hud_client.open_menu_fallback", GLFW.GLFW_KEY_F8);
        net.fabricmc.fabric.api.client.screen.v1.ScreenEvents.AFTER_INIT.register((client, screen, w, h) -> {
            if (screen instanceof net.minecraft.client.gui.screens.TitleScreen
                    || screen instanceof net.minecraft.client.gui.screens.PauseScreen) {
                net.fabricmc.fabric.api.client.screen.v1.Screens.getWidgets(screen).add(
                        com.codex.pvphud.gui.PvpButton.styled(net.minecraft.network.chat.Component.literal("PVPSE"),
                                button -> openMenu(screen)).bounds(8, 8, 86, 20).build());
            }
        });
        net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback.EVENT.register((dispatcher, access) -> {
            dispatcher.register(net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal("pvpse")
                    .executes(ctx -> { Minecraft.getInstance().schedule(() -> openMenu(null)); return 1; })
                    .then(net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal("restore")
                            .executes(ctx -> { restoreHud(); return 1; })));
        });
        zoomKey = registerKey("key.pvp_hud_client.zoom", GLFW.GLFW_KEY_Z);
        waypointKey = registerKey("key.pvp_hud_client.waypoint", GLFW.GLFW_KEY_B);
        fullbrightKey = registerKey("key.pvp_hud_client.fullbright", GLFW.GLFW_KEY_V);
        FeatureKeybinds.register(FeatureModule.Type.ZOOM, zoomKey);
        FeatureKeybinds.register(FeatureModule.Type.WAYPOINT, waypointKey);
        FeatureKeybinds.register(FeatureModule.Type.FULLBRIGHT, fullbrightKey);

        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            config.save();
            profileManager.save(hudManager);
            if (previousFov != null) client.options.fov().set(previousFov);
            if (previousGamma != null) client.options.gamma().set(previousGamma);
            if (previousDamageTilt != null) client.options.damageTiltStrength().set(previousDamageTilt);
            FeatureKeybinds.save(client);
        });
        MeleeHitboxRenderer.register(hudManager, config);
        BlockOverlayRenderer.register(hudManager, config);
        registerHudElements();
    }

    private void registerHudElements() {
        HudElementRegistry.replaceElement(VanillaHudElements.CROSSHAIR, original -> (context, tickCounter) -> {
            if (CrosshairRenderer.usesCustomCrosshair(config)) {
                CrosshairRenderer.drawCentered(context, config);
                return;
            }
            original.extractRenderState(context, tickCounter);
        });
        HudElementRegistry.replaceElement(VanillaHudElements.MISC_OVERLAYS, original -> (context, tickCounter) -> {
            Minecraft client = Minecraft.getInstance();
            boolean hidePumpkin = hudManager.feature(FeatureModule.Type.NO_PUMPKIN)
                    .map(FeatureModule::enabled).orElse(false)
                    && client.player != null
                    && client.player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD)
                    .is(net.minecraft.world.item.Items.CARVED_PUMPKIN);
            if (!hidePumpkin) original.extractRenderState(context, tickCounter);
        });

        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, HUD_ID, this::renderHud);
    }

    private void tick(Minecraft client) {
        SaturationState.tick(client);
        performance.initialize(client);
        cps.tick(client);
        WaypointManager.getInstance().tick(client);
        combatFeedback.tick(client, config);
        armorSound.tick(client, config);
        PvpStats.getInstance().tick(client);
        if (config.suppressDamageTilt && previousDamageTilt == null) {
            previousDamageTilt = client.options.damageTiltStrength().get();
            client.options.damageTiltStrength().set(0.0D);
        } else if (!config.suppressDamageTilt && previousDamageTilt != null) {
            client.options.damageTiltStrength().set(previousDamageTilt);
            previousDamageTilt = null;
        }
        updateUtilityFeatures(client);
        FeatureKeybinds.tick(client);
        if (++profileSaveTicks >= 100) {
            profileSaveTicks = 0;
            profileManager.save(hudManager);
        }

        if (client.player == null) joinHintShown = false;
        else if (!joinHintShown) {
            joinHintShown = true;
            profileManager.initializeLayout(client, hudManager);
            client.player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "PVPSE loaded | Right Shift / F8 / /pvpse opens the menu | HUD: "
                            + (config.enabled ? "ON" : "OFF") + " | Use Restore HUD if hidden"));
        }
        while (toggleHudKey.consumeClick()) {
            config.enabled = !config.enabled;
            config.save();
            if (client.player != null) client.player.sendOverlayMessage(
                    net.minecraft.network.chat.Component.literal("PVPSE HUD " + (config.enabled ? "ON" : "OFF")
                            + " | F8 opens settings"));
        }
        while (openConfigKey.consumeClick()) {
            if (client.screen == null) openMenu(null);
        }
        while (fallbackMenuKey.consumeClick()) {
            if (client.screen == null) openMenu(null);
        }
    }

    public void openMenu(net.minecraft.client.gui.screens.Screen parent) {
        Minecraft.getInstance().setScreen(new ClickGuiScreen(hudManager, () -> profileManager.save(hudManager),
                screen -> new ClientSettingsScreen(screen, config, profileManager, hudManager), parent,
                () -> config.enabled, () -> { config.enabled = !config.enabled; config.save(); }, this::restoreHud));
    }

    public void restoreHud() {
        config.enabled = true;
        config.hideOnDebug = false;
        Minecraft.getInstance().options.hideGui = false;
        hudManager.layoutElements().forEach(com.codex.pvphud.hud.HudElement::resetLayout);
        hudManager.arrange(Minecraft.getInstance());
        config.save();
        profileManager.save(hudManager);
    }

    private KeyMapping registerKey(String translation, int key) {
        return KeyMappingHelper.registerKeyMapping(new KeyMapping(
                translation, InputConstants.Type.KEYSYM, key, CATEGORY
        ));
    }

    private void updateUtilityFeatures(Minecraft client) {
        FeatureModule zoom = hudManager.feature(FeatureModule.Type.ZOOM).orElse(null);
        boolean zooming = zoom != null && zoom.enabled() && zoomKey.isDown() && client.player != null && client.screen == null;
        if (zooming && previousFov == null) {
            previousFov = client.options.fov().get();
            client.options.fov().set((int) Math.round(zoom.value()));
        } else if (!zooming && previousFov != null) {
            client.options.fov().set(previousFov);
            previousFov = null;
        }

        while (waypointKey.consumeClick()) {
            hudManager.feature(FeatureModule.Type.WAYPOINT).filter(FeatureModule::enabled).ifPresent(feature -> {
                if (client.player != null && client.screen == null) {
                        var waypoint = client.options.keyShift.isDown()
                                ? WaypointManager.getInstance().addLookTarget(client)
                                : WaypointManager.getInstance().addCurrent(client);
                    if (waypoint != null) NotificationManager.getInstance().push("Waypoint added", waypoint.name());
                }
            });
        }
        while (fullbrightKey.consumeClick()) {
            hudManager.feature(FeatureModule.Type.FULLBRIGHT).ifPresent(feature -> feature.setEnabled(!feature.enabled()));
        }
        FeatureModule fullbright = hudManager.feature(FeatureModule.Type.FULLBRIGHT).orElse(null);
        FullbrightState.set(config.enabled && client.player != null && fullbright != null && fullbright.enabled()
                ? fullbright.value() : 0);
    }

    private void renderHud(GuiGraphicsExtractor context, DeltaTracker tickCounter) {
        Minecraft client = Minecraft.getInstance();
        if (client.options.hideGui || (config.hideOnDebug && client.getDebugOverlay().showDebugScreen())
                || client.screen instanceof com.codex.pvphud.hud.HudEditor
                || client.screen instanceof ClickGuiScreen || client.screen instanceof ClientSettingsScreen
                || client.screen instanceof com.codex.pvphud.hud.HudModulesScreen) {
            return;
        }
        if (config.enabled) {
            hudManager.render(context, client, false);
            PvpIndicators.render(context, client, config);
            if (hudManager.feature(FeatureModule.Type.WAYPOINT).map(FeatureModule::enabled).orElse(false)) {
                WaypointOverlay.render(context, client, com.codex.pvphud.theme.ThemeManager.getInstance().getTheme());
            }
        }
        if (config.enabled && client.player != null && client.level != null && client.screen == null) {
            combatFeedback.render(context, config);
        }
        if (config.notifications) NotificationManager.getInstance().render(context, client);
    }
}
