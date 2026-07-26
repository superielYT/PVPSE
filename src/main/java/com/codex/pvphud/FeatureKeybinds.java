package com.codex.pvphud;

import com.codex.pvphud.hud.FeatureModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.util.EnumMap;
import java.util.Map;

public final class FeatureKeybinds {
    private static final Map<FeatureModule.Type, KeyBinding> KEYS = new EnumMap<>(FeatureModule.Type.class);
    private static FeatureModule.Type listening;
    private static boolean waitForRelease;

    private FeatureKeybinds() {}

    public static void register(FeatureModule.Type type, KeyBinding key) { KEYS.put(type, key); }
    public static KeyBinding key(FeatureModule.Type type) { return KEYS.get(type); }
    public static boolean listening(FeatureModule.Type type) { return listening == type; }

    public static void begin(FeatureModule.Type type) {
        listening = type;
        waitForRelease = true;
    }

    public static void tick(MinecraftClient client) {
        if (listening == null || client.currentScreen == null) return;
        long window = client.getWindow().getHandle();
        boolean anyDown = false;
        for (int key = GLFW.GLFW_KEY_SPACE; key <= GLFW.GLFW_KEY_LAST; key++) {
            if (!InputUtil.isKeyPressed(window, key)) continue;
            anyDown = true;
            if (waitForRelease) continue;
            if (key == GLFW.GLFW_KEY_ESCAPE) {
                listening = null;
                return;
            }
            KeyBinding binding = KEYS.get(listening);
            if (binding != null) {
                binding.setBoundKey(InputUtil.fromKeyCode(key, 0));
                KeyBinding.updateKeysByCode();
            }
            listening = null;
            return;
        }
        if (!anyDown) waitForRelease = false;
    }
}
