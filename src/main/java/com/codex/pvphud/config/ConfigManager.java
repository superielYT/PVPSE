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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Path base = FabricLoader.getInstance().getConfigDir().resolve("pvpse");
    private final Path directory = base.resolve("profiles");
    private String activeProfile = "default";
    private boolean needsInitialLayout;
    private final List<String> newCards = new ArrayList<>();

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
        needsInitialLayout = !Files.exists(path(activeProfile));
    }

    public synchronized void initializeLayout(net.minecraft.client.Minecraft client, HudManager hud) {
        if (!needsInitialLayout) {
            if (!newCards.isEmpty()) { hud.placeNewCards(client, newCards); newCards.clear(); save(hud); }
            return;
        }
        needsInitialLayout = false;
        newCards.clear();
        hud.arrange(client);
        save(hud);
    }

    public synchronized void load(HudManager hudManager) {
        newCards.clear();
        ConfigProfile profile = read(activeProfile);
        if (profile == null) {
            save(hudManager);
            return;
        }
        if (profile.theme == null || !ThemeManager.getInstance().setTheme(profile.theme)) ThemeManager.getInstance().setTheme("ignite");
        ThemeManager.getInstance().setAppearance(profile.appearance);
        hudManager.elements().forEach(HudElement::resetSettings);
        if (profile.hud == null) return;
        for (HudElement element : hudManager.elements()) {
            element.resetSettings();
            ConfigProfile.HudState state = profile.hud.get(element.id());
            if (state == null) { newCards.add(element.id()); continue; }
            element.setEnabled(state.enabled);
            element.setPosition(state.x, state.y);
            element.setScale(state.scale);
            if (element instanceof FeatureModule feature && feature.adjustable() && Double.isFinite(state.value) && state.value >= feature.minimum()) feature.setValue(state.value);
        }
    }

    public synchronized boolean save(HudManager hudManager) {
        ConfigProfile profile = new ConfigProfile(activeProfile);
        profile.theme = ThemeManager.getInstance().getTheme().id();
        profile.appearance = ThemeManager.getInstance().appearance();
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
        return JsonStorage.write(path(profile.name), profile);
    }

    private Path path(String name) {
        return directory.resolve(sanitize(name) + ".json");
    }

    private static String sanitize(String name) {
        return name == null ? "" : name.toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9_-]", "");
    }
}
