package com.codex.pvphud;

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
    private final HudRenderer renderer = new HudRenderer();
    private final CombatFeedback combatFeedback = new CombatFeedback();
    private HudConfig config;
    private KeyBinding toggleHudKey;
    private KeyBinding openConfigKey;

    @Override
    public void onInitializeClient() {
        config = HudConfig.load();
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

        while (toggleHudKey.wasPressed()) {
            config.enabled = !config.enabled;
            config.save();
        }
        while (openConfigKey.wasPressed()) {
            if (client.player != null && client.currentScreen == null) {
                client.setScreen(new HudEditorScreen(config, cps));
            }
        }
    }

    private void renderHud(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.currentScreen instanceof HudEditorScreen) {
            return;
        }
        renderer.render(context, client, config, cps, false);
        combatFeedback.render(context, config);
    }
}
