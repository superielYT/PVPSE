package com.codex.pvphud;

import com.codex.pvphud.config.ConfigManager;
import com.codex.pvphud.gui.ClickGuiScreen;
import com.codex.pvphud.hud.HudManager;
import com.codex.pvphud.hud.FeatureModule;
import com.codex.pvphud.notification.NotificationManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public final class PvpHudClient implements ClientModInitializer {
    private static final Identifier HUD_ID = Identifier.of("pvp_hud_client", "hud");
    private static final KeyBinding.Category CATEGORY = KeyBinding.Category.create(Identifier.of("pvp_hud_client", "controls"));

    private final CpsTracker cps = new CpsTracker();
    private final CombatFeedback combatFeedback = new CombatFeedback();
    private final HudManager hudManager = new HudManager();
    private final ConfigManager profileManager = new ConfigManager();
    private HudConfig config;
    private KeyBinding toggleHudKey;
    private KeyBinding openConfigKey;
    private KeyBinding zoomKey;
    private KeyBinding waypointKey;
    private KeyBinding fullbrightKey;
    private int profileSaveTicks;
    private boolean damageTiltDisabled;
    private Integer previousFov;
    private Double previousGamma;

    @Override
    public void onInitializeClient() {
        config = HudConfig.load();
        profileManager.load(hudManager);
        toggleHudKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.pvp_hud_client.toggle_hud",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                CATEGORY
        ));
        openConfigKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.pvp_hud_client.open_config",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                CATEGORY
        ));
        zoomKey = registerKey("key.pvp_hud_client.zoom", GLFW.GLFW_KEY_Z);
        waypointKey = registerKey("key.pvp_hud_client.waypoint", GLFW.GLFW_KEY_B);
        fullbrightKey = registerKey("key.pvp_hud_client.fullbright", GLFW.GLFW_KEY_V);

        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
        registerHudElements();
    }

    private void registerHudElements() {
        HudElementRegistry.replaceElement(VanillaHudElements.CROSSHAIR, original -> (context, tickCounter) -> {
            if (CrosshairRenderer.usesCustomCrosshair(config)) {
                CrosshairRenderer.drawCentered(context, config);
                return;
            }
            original.render(context, tickCounter);
        });
        HudElementRegistry.replaceElement(VanillaHudElements.MISC_OVERLAYS, original -> (context, tickCounter) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            boolean hidePumpkin = hudManager.feature(FeatureModule.Type.NO_PUMPKIN)
                    .map(FeatureModule::enabled).orElse(false)
                    && client.player != null
                    && client.player.getEquippedStack(net.minecraft.entity.EquipmentSlot.HEAD)
                    .isOf(net.minecraft.item.Items.CARVED_PUMPKIN);
            if (!hidePumpkin) original.render(context, tickCounter);
        });

        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, HUD_ID, this::renderHud);
    }

    private void tick(MinecraftClient client) {
        cps.tick(client);
        combatFeedback.tick(client, config);
        PvpStats.getInstance().tick(client);
        if (!damageTiltDisabled) {
            client.options.getDamageTiltStrength().setValue(0.0D);
            damageTiltDisabled = true;
        }
        updateUtilityFeatures(client);
        if (++profileSaveTicks >= 100) {
            profileSaveTicks = 0;
            profileManager.save(hudManager);
        }

        while (toggleHudKey.wasPressed()) {
            config.enabled = !config.enabled;
            config.save();
        }
        while (openConfigKey.wasPressed()) {
            if (client.player != null && client.currentScreen == null) {
                client.setScreen(new ClickGuiScreen(hudManager));
            }
        }
    }

    private KeyBinding registerKey(String translation, int key) {
        return KeyBindingHelper.registerKeyBinding(new KeyBinding(
                translation, InputUtil.Type.KEYSYM, key, CATEGORY
        ));
    }

    private void updateUtilityFeatures(MinecraftClient client) {
        FeatureModule zoom = hudManager.feature(FeatureModule.Type.ZOOM).orElse(null);
        boolean zooming = zoom != null && zoom.enabled() && zoomKey.isPressed();
        if (zooming && previousFov == null) {
            previousFov = client.options.getFov().getValue();
            client.options.getFov().setValue((int) Math.round(zoom.value()));
        } else if (!zooming && previousFov != null) {
            client.options.getFov().setValue(previousFov);
            previousFov = null;
        }

        while (waypointKey.wasPressed()) {
            hudManager.feature(FeatureModule.Type.WAYPOINT).filter(FeatureModule::enabled).ifPresent(feature -> {
                if (client.player != null) {
                    feature.setWaypoint(client.player.getBlockPos());
                    NotificationManager.getInstance().push("Waypoint set", client.player.getBlockPos().toShortString());
                }
            });
        }
        while (fullbrightKey.wasPressed()) {
            hudManager.feature(FeatureModule.Type.FULLBRIGHT).ifPresent(feature -> feature.setEnabled(!feature.enabled()));
        }
        FeatureModule fullbright = hudManager.feature(FeatureModule.Type.FULLBRIGHT).orElse(null);
        if (fullbright != null && fullbright.enabled()) {
            if (previousGamma == null) previousGamma = client.options.getGamma().getValue();
            client.options.getGamma().setValue(fullbright.value());
        } else if (previousGamma != null) {
            client.options.getGamma().setValue(previousGamma);
            previousGamma = null;
        }
    }

    private void renderHud(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.currentScreen instanceof com.codex.pvphud.hud.HudEditor) {
            return;
        }
        if (config.enabled) {
            hudManager.render(context, client, false);
        }
        combatFeedback.render(context, config);
        NotificationManager.getInstance().render(context, client);
    }
}
