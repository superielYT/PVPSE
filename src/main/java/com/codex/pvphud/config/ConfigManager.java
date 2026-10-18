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
    private final Path base = FabricLoader.getInstance().getConfigDir().resolve("pvpse");
    private final Path directory = base.resolve("profiles");
    private String activeProfile = "default";

    public ConfigManager() {
        try {
            Files.createDirectories(directory);
            if (Files.exists(base.resolve("default.json")) && !Files.exists(path("default")))
                Files.copy(base.resolve("default.json"), path("default"));
            Path active = base.resolve("active-profile.txt");
            if (Files.exists(active)) {
                String name = sanitize(Files.readString(active).strip());
                if (!name.isBlank() && Files.exists(path(name))) activeProfile = name;
            }
        } catch (IOException ignored) {}
    }

    public synchronized void load(HudManager hudManager) {
        ConfigProfile profile = read(activeProfile);
        if (profile == null) {
            save(hudManager);
            return;
        }
        if (profile.theme != null) ThemeManager.getInstance().setTheme(profile.theme);
        if (profile.hud == null) return;
        for (HudElement element : hudManager.elements()) {
            element.resetSettings();
            ConfigProfile.HudState state = profile.hud.get(element.id());
            if (state == null) continue;
            element.setEnabled(state.enabled);
            element.setPosition(state.x, state.y);
            element.setScale(state.scale);
            if (element instanceof FeatureModule feature && state.value > 0) feature.setValue(state.value);
        }
    }

    public synchronized boolean save(HudManager hudManager) {
        ConfigProfile profile = new ConfigProfile(activeProfile);
        profile.theme = ThemeManager.getInstance().getTheme().id();
        for (HudElement element : hudManager.elements()) {
            ConfigProfile.HudState state = new ConfigProfile.HudState(
                    element.enabled(), element.x(), element.y(), element.scale());
            if (element instanceof FeatureModule feature) state.value = feature.value();
            profile.hud.put(element.id(), state);
        }
        return write(profile);
    }

    public synchronized boolean switchProfile(String name, HudManager hudManager) {
        String safe = sanitize(name);
        if (safe.isBlank() || read(safe) == null) return false;
        if (!save(hudManager)) return false;
        activeProfile = safe;
        load(hudManager);
        rememberActive();
        return true;
    }

    public synchronized boolean createProfile(String name, HudManager hudManager) {
        String safe = sanitize(name);
        if (safe.isBlank() || Files.exists(path(safe))) return false;
        if (!save(hudManager)) return false;
        String previous = activeProfile;
        activeProfile = safe;
        if (!save(hudManager)) { activeProfile = previous; return false; }
        rememberActive();
        return true;
    }

    private void rememberActive() {
        try { Files.writeString(base.resolve("active-profile.txt"), activeProfile); }
        catch (IOException ignored) {}
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
            try { Files.copy(path, path.resolveSibling(path.getFileName() + ".broken-" + System.currentTimeMillis())); }
            catch (IOException ignoredBackup) {}
            return null;
        }
    }

    private boolean write(ConfigProfile profile) {
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
            return true;
        } catch (IOException ignored) { return false; }
    }

    private Path path(String name) {
        return directory.resolve(sanitize(name) + ".json");
    }

    private static String sanitize(String name) {
        return name == null ? "" : name.toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9_-]", "");
    }
}
