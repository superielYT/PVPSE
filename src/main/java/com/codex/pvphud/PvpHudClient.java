package com.codex.pvphud;

import com.codex.pvphud.config.ConfigManager;
import com.codex.pvphud.gui.ClickGuiScreen;
import com.codex.pvphud.hud.HudManager;
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
    private int profileSaveTicks;

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

        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, HUD_ID, this::renderHud);
    }

    private void tick(MinecraftClient client) {
        cps.tick(client);
        combatFeedback.tick(client, config);
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

    private void renderHud(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.currentScreen instanceof com.codex.pvphud.hud.HudEditor) {
            return;
        }
        if (config.enabled) {
            hudManager.render(context, client, false);
        }
        combatFeedback.render(context, config);
    }
}
