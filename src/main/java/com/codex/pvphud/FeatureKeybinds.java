package com.codex.pvphud;

import com.codex.pvphud.hud.FeatureModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import java.util.EnumMap;
import java.util.Map;

public final class FeatureKeybinds {
    private static final Map<FeatureModule.Type, KeyBinding> KEYS = new EnumMap<>(FeatureModule.Type.class);
    private static FeatureModule.Type listening;
    private static boolean dirty;
    private FeatureKeybinds() {}
    public static void register(FeatureModule.Type type, KeyBinding key) { KEYS.put(type, key); }
    public static KeyBinding key(FeatureModule.Type type) { return KEYS.get(type); }
    public static boolean listening(FeatureModule.Type type) { return listening == type; }
    public static void begin(FeatureModule.Type type) { if (KEYS.containsKey(type)) listening = type; }
    public static void cancel() { listening = null; }
    public static boolean capture(KeyInput input, MinecraftClient client) {
        if (listening == null) return false;
        if (input.key() != GLFW.GLFW_KEY_ESCAPE) {
            var binding = KEYS.get(listening);
            if (binding != null) {
                binding.setBoundKey(input.key() == GLFW.GLFW_KEY_BACKSPACE || input.key() == GLFW.GLFW_KEY_DELETE ? InputUtil.UNKNOWN_KEY : InputUtil.fromKeyCode(input));
                KeyBinding.updateKeysByCode();
                dirty = true;
            }
        }
        cancel(); return true;
    }
    public static void save(MinecraftClient client) { if (dirty) { client.options.write(); dirty = false; } }
    public static void tick(MinecraftClient client) { if (client.currentScreen == null) cancel(); }
}
