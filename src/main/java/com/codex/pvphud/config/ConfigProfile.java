package com.codex.pvphud.config;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ConfigProfile {
    public String name = "default";
    public String theme = "ignite";
    public Map<String, HudState> hud = new LinkedHashMap<>();

    public ConfigProfile() {}

    public ConfigProfile(String name) {
        this.name = name;
    }

    public static final class HudState {
        public boolean enabled = true;
        public float x;
        public float y;
        public float scale = 1.0F;

        public HudState() {}

        public HudState(boolean enabled, float x, float y, float scale) {
            this.enabled = enabled;
            this.x = x;
            this.y = y;
            this.scale = scale;
        }
    }
}
