package com.codex.pvphud;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

final class HudConfig {
    static final int MIN_CROSSHAIR_SIZE = 3;
    static final int MAX_CROSSHAIR_SIZE = 15;
    static final int MIN_OPACITY = 40;
    static final int MAX_OPACITY = 100;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("pvp-hud-client.json");

    boolean enabled = true;
    boolean showCoords = true;
    boolean showArmor = true;
    boolean showPotions = true;
    boolean showTarget = true;
    boolean showCrosshair = true;
    boolean showHealthBar = true;
    boolean showDurabilityBars = true;
    boolean glowEffects = true;
    boolean animatedThemes = true;
    boolean showHitMarker = true;
    boolean showDamageFlash = true;
    boolean showKillParticles = true;
    boolean showCps = true;
    boolean showRightCps = true;
    boolean showSprint = true;
    boolean showReach = false;
    int particleAmount = 18;
    boolean compact = false;
    boolean useFreeLayout = true;
    int panelOpacity = 92;
    Theme theme = Theme.CYBER;
    CrosshairStyle crosshairStyle = CrosshairStyle.DIAMOND;
    int crosshairSize = 7;

    private transient Theme transitionFrom;
    private transient long transitionStartedNanos;
    private static final long THEME_TRANSITION_NANOS = 320_000_000L;

    WidgetLayout combat = new WidgetLayout(0.02f, 0.03f);
    WidgetLayout player = new WidgetLayout(0.02f, 0.12f);
    WidgetLayout location = new WidgetLayout(0.74f, 0.03f);
    WidgetLayout held = new WidgetLayout(0.74f, 0.12f);
    WidgetLayout target = new WidgetLayout(0.36f, 0.03f);
    WidgetLayout effects = new WidgetLayout(0.02f, 0.72f);
    WidgetLayout armor = new WidgetLayout(0.68f, 0.68f);
    WidgetLayout stack = new WidgetLayout(0.02f, 0.03f);

    // Legacy fields kept for older config files.
    @SuppressWarnings("unused")
    Corner corner = Corner.TOP_LEFT;
    @SuppressWarnings("unused")
    boolean spreadLayout = true;

    static HudConfig load() {
        if (!Files.exists(PATH)) {
            HudConfig config = new HudConfig();
            config.save();
            return config;
        }

        try (Reader reader = Files.newBufferedReader(PATH)) {
            HudConfig config = GSON.fromJson(reader, HudConfig.class);
            if (config == null) {
                return new HudConfig();
            }
            config.ensureWidgets();
            return config;
        } catch (IOException ignored) {
            return new HudConfig();
        }
    }

    void ensureWidgets() {
        if (combat == null) combat = new WidgetLayout(0.02f, 0.03f);
        if (player == null) player = new WidgetLayout(0.02f, 0.12f);
        if (location == null) location = new WidgetLayout(0.74f, 0.03f);
        if (held == null) held = new WidgetLayout(0.74f, 0.12f);
        if (target == null) target = new WidgetLayout(0.36f, 0.03f);
        if (effects == null) effects = new WidgetLayout(0.02f, 0.72f);
        if (armor == null) armor = new WidgetLayout(0.68f, 0.68f);
        if (stack == null) stack = new WidgetLayout(0.02f, 0.03f);
    }

    void save() {
        try {
            Files.createDirectories(PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(PATH)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException ignored) {
        }
    }

    WidgetLayout layout(HudWidgetId id) {
        return switch (id) {
            case COMBAT -> combat;
            case PLAYER -> player;
            case LOCATION -> location;
            case HELD -> held;
            case TARGET -> target;
            case EFFECTS -> effects;
            case ARMOR -> armor;
            case STACK -> stack;
        };
    }

    void resetAllWidgets() {
        for (HudWidgetId id : HudWidgetId.values()) {
            layout(id).reset(id);
        }
        save();
    }

    void cycleTheme() {
        Theme[] values = Theme.values();
        setTheme(values[(theme.ordinal() + 1) % values.length]);
    }

    void setTheme(Theme next) {
        if (next == null || next == theme) return;
        transitionFrom = theme;
        transitionStartedNanos = System.nanoTime();
        theme = next;
        save();
    }

    int accentColor() { return animatedColor(ColorSlot.ACCENT); }
    int textColor() { return animatedColor(ColorSlot.TEXT); }
    int mutedColor() { return animatedColor(ColorSlot.MUTED); }
    int dangerColor() { return animatedColor(ColorSlot.DANGER); }
    int glowColor() { return animatedColor(ColorSlot.GLOW); }

    private int animatedColor(ColorSlot slot) {
        int target = slot.get(theme);
        if (!animatedThemes || transitionFrom == null) return target;
        float progress = (System.nanoTime() - transitionStartedNanos) / (float) THEME_TRANSITION_NANOS;
        if (progress >= 1.0F) {
            transitionFrom = null;
            return target;
        }
        float eased = 1.0F - (1.0F - Math.max(0.0F, progress)) * (1.0F - Math.max(0.0F, progress));
        return HudPanel.lerpColor(slot.get(transitionFrom), target, eased);
    }

    private enum ColorSlot {
        ACCENT { int get(Theme t) { return t.accent; } },
        TEXT { int get(Theme t) { return t.text; } },
        MUTED { int get(Theme t) { return t.muted; } },
        DANGER { int get(Theme t) { return t.danger; } },
        GLOW { int get(Theme t) { return t.glow; } };
        abstract int get(Theme theme);
    }

    void cycleCrosshair() {
        CrosshairStyle[] values = CrosshairStyle.values();
        crosshairStyle = values[(crosshairStyle.ordinal() + 1) % values.length];
        save();
    }

    int backgroundColor() {
        int alpha = Math.clamp(panelOpacity, MIN_OPACITY, MAX_OPACITY);
        return (alpha << 24) | 0x121418;
    }

    enum Corner {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT
    }

    enum Theme {
        CYBER(0xFF36D399, 0xFFE8EEF5, 0xFF9AA7B2, 0xFFFF647C, 0xFF1A2332),
        FROST(0xFF60A5FA, 0xFFF8FBFF, 0xFFA7B8CC, 0xFFFF6B8A, 0xFF152033),
        GOLD(0xFFFBBF24, 0xFFFFF7E6, 0xFFC7B58A, 0xFFFF5C5C, 0xFF231C12),
        CHERRY(0xFFFB7185, 0xFFFFF1F4, 0xFFD7A7B0, 0xFFFFC857, 0xFF2A1418),
        MATRIX(0xFF22C55E, 0xFFE7FFE9, 0xFF7BCB88, 0xFFFF4D6D, 0xFF0D1A10),
        NEON(0xFFBF5AF2, 0xFFF5EEFF, 0xFFB794D3, 0xFFFF375F, 0xFF1A1028);

        final int accent;
        final int text;
        final int muted;
        final int danger;
        final int glow;

        Theme(int accent, int text, int muted, int danger, int glow) {
            this.accent = accent;
            this.text = text;
            this.muted = muted;
            this.danger = danger;
            this.glow = glow;
        }
    }

    enum CrosshairStyle {
        OFF,
        DOT,
        PLUS,
        GAP,
        DIAMOND,
        BOX
    }
}
