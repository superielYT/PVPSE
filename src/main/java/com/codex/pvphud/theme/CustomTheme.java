package com.codex.pvphud.theme;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class CustomTheme implements Theme {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("pvpse/custom-theme.json");
    private int accent = 0xFFFF6500;

    public static CustomTheme load() {
        if (!Files.exists(PATH)) return new CustomTheme();
        try (Reader reader = Files.newBufferedReader(PATH)) {
            CustomTheme theme = GSON.fromJson(reader, CustomTheme.class);
            return theme == null ? new CustomTheme() : theme;
        } catch (IOException | RuntimeException ignored) {
            return new CustomTheme();
        }
    }

    public synchronized void setAccent(int color) {
        accent = 0xFF000000 | color & 0xFFFFFF;
        save();
    }

    public synchronized void save() {
        try {
            Files.createDirectories(PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(PATH)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException ignored) {}
    }

    public String id() { return "custom"; }
    public String displayName() { return "Custom RGB"; }
    public int background() { return 0xEF090C11; }
    public int panel() { return 0xE8171C23; }
    public int panelHeader() { return 0xFF222A34; }
    public synchronized int accent() { return accent; }
    public synchronized int accentSecondary() { return lighten(accent, 0.28F); }
    public int text() { return 0xFFF4F7FA; }
    public int mutedText() { return 0xFF929EAA; }
    public int danger() { return 0xFFFF4655; }
    public synchronized int glow() { return 0x66000000 | accent & 0xFFFFFF; }

    private static int lighten(int color, float amount) {
        int r = color >> 16 & 0xFF;
        int g = color >> 8 & 0xFF;
        int b = color & 0xFF;
        r += Math.round((255 - r) * amount);
        g += Math.round((255 - g) * amount);
        b += Math.round((255 - b) * amount);
        return 0xFF000000 | r << 16 | g << 8 | b;
    }
}
