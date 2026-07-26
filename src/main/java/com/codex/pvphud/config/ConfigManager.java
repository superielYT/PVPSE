package com.codex.pvphud.config;

import com.codex.pvphud.hud.HudElement;
import com.codex.pvphud.hud.HudManager;
import com.codex.pvphud.hud.FeatureModule;
import com.codex.pvphud.theme.ThemeManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public final class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Path directory = FabricLoader.getInstance().getConfigDir().resolve("pvpse");
    private String activeProfile = "default";

    public synchronized void load(HudManager hudManager) {
        ConfigProfile profile = read(activeProfile);
        if (profile == null) {
            save(hudManager);
            return;
        }
        ThemeManager.getInstance().setTheme(profile.theme);
        for (HudElement element : hudManager.elements()) {
            ConfigProfile.HudState state = profile.hud.get(element.id());
            if (state == null) continue;
            element.setEnabled(state.enabled);
            element.setPosition(state.x, state.y);
            element.setScale(state.scale);
            if (element instanceof FeatureModule feature && state.value > 0) feature.setValue(state.value);
        }
    }

    public synchronized void save(HudManager hudManager) {
        ConfigProfile profile = new ConfigProfile(activeProfile);
        profile.theme = ThemeManager.getInstance().getTheme().id();
        for (HudElement element : hudManager.elements()) {
            ConfigProfile.HudState state = new ConfigProfile.HudState(
                    element.enabled(), element.x(), element.y(), element.scale());
            if (element instanceof FeatureModule feature) state.value = feature.value();
            profile.hud.put(element.id(), state);
        }
        write(profile);
    }

    public synchronized boolean switchProfile(String name, HudManager hudManager) {
        String safe = sanitize(name);
        if (safe.isBlank()) return false;
        save(hudManager);
        activeProfile = safe;
        load(hudManager);
        return true;
    }

    public synchronized List<String> profiles() {
        if (!Files.isDirectory(directory)) return List.of("default");
        List<String> names = new ArrayList<>();
        try (var stream = Files.list(directory)) {
            stream.filter(path -> path.getFileName().toString().endsWith(".json"))
                    .map(path -> path.getFileName().toString().replaceFirst("\\.json$", ""))
                    .sorted().forEach(names::add);
        } catch (IOException ignored) {}
        if (!names.contains("default")) names.addFirst("default");
        return List.copyOf(names);
    }

    public String activeProfile() {
        return activeProfile;
    }

    private ConfigProfile read(String name) {
        Path path = path(name);
        if (!Files.exists(path)) return null;
        try (Reader reader = Files.newBufferedReader(path)) {
            return GSON.fromJson(reader, ConfigProfile.class);
        } catch (IOException | RuntimeException ignored) {
            return null;
        }
    }

    private void write(ConfigProfile profile) {
        try {
            Files.createDirectories(directory);
            Path target = path(profile.name);
            Path temporary = target.resolveSibling(target.getFileName() + ".tmp");
            try (Writer writer = Files.newBufferedWriter(temporary)) {
                GSON.toJson(profile, writer);
            }
            try {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException unsupportedAtomicMove) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ignored) {}
    }

    private Path path(String name) {
        return directory.resolve(sanitize(name) + ".json");
    }

    private static String sanitize(String name) {
        return name == null ? "" : name.toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9_-]", "");
    }
}
