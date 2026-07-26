package com.codex.pvphud.crosshair;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

public final class CustomCrosshair {
    public static final int GRID_SIZE = 15;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("pvpse/custom-crosshair.json");
    private static final CustomCrosshair INSTANCE = load();

    private boolean enabled;
    private boolean mirrorHorizontal = true;
    private boolean mirrorVertical = true;
    private boolean useThemeColor = true;
    private int color = 0xFFFFFFFF;
    private boolean[][] pixels = defaultPixels();

    private CustomCrosshair() {}

    public static CustomCrosshair getInstance() {
        return INSTANCE;
    }

    public synchronized boolean pixel(int x, int y) {
        return valid(x, y) && pixels[y][x];
    }

    public synchronized void setPixel(int x, int y, boolean active) {
        paint(x, y, active);
        if (mirrorHorizontal) paint(GRID_SIZE - 1 - x, y, active);
        if (mirrorVertical) paint(x, GRID_SIZE - 1 - y, active);
        if (mirrorHorizontal && mirrorVertical) paint(GRID_SIZE - 1 - x, GRID_SIZE - 1 - y, active);
    }

    public synchronized void clear() {
        pixels = new boolean[GRID_SIZE][GRID_SIZE];
        save();
    }

    public synchronized void reset() {
        pixels = defaultPixels();
        save();
    }

    public synchronized boolean enabled() { return enabled; }
    public synchronized void setEnabled(boolean enabled) { this.enabled = enabled; save(); }
    public synchronized boolean mirrorHorizontal() { return mirrorHorizontal; }
    public synchronized void setMirrorHorizontal(boolean value) { mirrorHorizontal = value; save(); }
    public synchronized boolean mirrorVertical() { return mirrorVertical; }
    public synchronized void setMirrorVertical(boolean value) { mirrorVertical = value; save(); }
    public synchronized boolean useThemeColor() { return useThemeColor; }
    public synchronized int color() { return color; }
    public synchronized void setColor(int color, boolean useThemeColor) {
        this.color = color;
        this.useThemeColor = useThemeColor;
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

    public synchronized String exportCode() {
        byte[] data = new byte[(GRID_SIZE * GRID_SIZE + 7) / 8];
        for (int y = 0; y < GRID_SIZE; y++) {
            for (int x = 0; x < GRID_SIZE; x++) {
                int bit = y * GRID_SIZE + x;
                if (pixels[y][x]) data[bit / 8] |= (byte) (1 << bit % 8);
            }
        }
        int flags = (mirrorHorizontal ? 1 : 0) | (mirrorVertical ? 2 : 0) | (useThemeColor ? 4 : 0);
        return "PVPSE1:" + Base64.getUrlEncoder().withoutPadding().encodeToString(data)
                + ":" + Integer.toHexString(color) + ":" + flags;
    }

    public synchronized boolean importCode(String code) {
        try {
            String[] parts = code == null ? new String[0] : code.trim().split(":");
            if (parts.length != 4 || !"PVPSE1".equals(parts[0])) return false;
            byte[] data = Base64.getUrlDecoder().decode(parts[1]);
            if (data.length != (GRID_SIZE * GRID_SIZE + 7) / 8) return false;
            boolean[][] imported = new boolean[GRID_SIZE][GRID_SIZE];
            for (int bit = 0; bit < GRID_SIZE * GRID_SIZE; bit++) {
                imported[bit / GRID_SIZE][bit % GRID_SIZE] = (data[bit / 8] & 1 << bit % 8) != 0;
            }
            color = (int) Long.parseLong(parts[2], 16);
            int flags = Integer.parseInt(parts[3]);
            mirrorHorizontal = (flags & 1) != 0;
            mirrorVertical = (flags & 2) != 0;
            useThemeColor = (flags & 4) != 0;
            pixels = imported;
            enabled = true;
            save();
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private void paint(int x, int y, boolean active) {
        if (valid(x, y)) pixels[y][x] = active;
    }

    private static boolean valid(int x, int y) {
        return x >= 0 && y >= 0 && x < GRID_SIZE && y < GRID_SIZE;
    }

    private static CustomCrosshair load() {
        if (!Files.exists(PATH)) return new CustomCrosshair();
        try (Reader reader = Files.newBufferedReader(PATH)) {
            CustomCrosshair loaded = GSON.fromJson(reader, CustomCrosshair.class);
            if (loaded == null || loaded.pixels == null || loaded.pixels.length != GRID_SIZE) return new CustomCrosshair();
            for (boolean[] row : loaded.pixels) if (row == null || row.length != GRID_SIZE) return new CustomCrosshair();
            return loaded;
        } catch (IOException | RuntimeException ignored) {
            return new CustomCrosshair();
        }
    }

    private static boolean[][] defaultPixels() {
        boolean[][] grid = new boolean[GRID_SIZE][GRID_SIZE];
        int center = GRID_SIZE / 2;
        grid[center][center] = true;
        for (int distance = 3; distance <= 5; distance++) {
            grid[center][center - distance] = true;
            grid[center][center + distance] = true;
            grid[center - distance][center] = true;
            grid[center + distance][center] = true;
        }
        return grid;
    }
}
