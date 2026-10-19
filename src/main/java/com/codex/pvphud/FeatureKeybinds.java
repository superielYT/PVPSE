package com.codex.pvphud;

import com.codex.pvphud.hud.FeatureModule;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;

public final class FeatureKeybinds {
    private static final Map<FeatureModule.Type, KeyMapping> KEYS = new EnumMap<>(FeatureModule.Type.class);
    private static FeatureModule.Type listening;
    private static boolean dirty;
    private FeatureKeybinds() {}
    public static void register(FeatureModule.Type type, KeyMapping key) { KEYS.put(type, key); }
    public static KeyMapping key(FeatureModule.Type type) { return KEYS.get(type); }
    public static boolean listening(FeatureModule.Type type) { return listening == type; }
    public static void begin(FeatureModule.Type type) { if (KEYS.containsKey(type)) listening = type; }
    public static void cancel() { listening = null; }
    public static boolean capture(KeyEvent input, Minecraft client) {
        if (listening == null) return false;
        if (input.key() != GLFW.GLFW_KEY_ESCAPE) {
            var binding = KEYS.get(listening);
            if (binding != null) {
                binding.setKey(input.key() == GLFW.GLFW_KEY_BACKSPACE || input.key() == GLFW.GLFW_KEY_DELETE ? InputConstants.UNKNOWN : InputConstants.getKey(input));
                KeyMapping.resetMapping();
                dirty = true;
            }
        }
        cancel(); return true;
    }
    public static void save(Minecraft client) { if (dirty) { client.options.save(); dirty = false; } }
    public static void tick(Minecraft client) { if (client.screen == null) cancel(); }
}
