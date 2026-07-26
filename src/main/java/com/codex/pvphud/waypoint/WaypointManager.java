package com.codex.pvphud.waypoint;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class WaypointManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("pvpse/waypoints.json");
    private static final WaypointManager INSTANCE = load();
    private final List<Waypoint> waypoints = new ArrayList<>();

    private WaypointManager() {}

    public static WaypointManager getInstance() { return INSTANCE; }
    public synchronized List<Waypoint> waypoints() { return List.copyOf(waypoints); }

    public synchronized Waypoint addCurrent(MinecraftClient client) {
        return addCurrent(client, "");
    }

    public synchronized Waypoint addCurrent(MinecraftClient client, String requestedName) {
        if (client.player == null || client.world == null) return null;
        var pos = client.player.getBlockPos();
        String dimension = client.world.getRegistryKey().getValue().toString();
        String cleanName = requestedName == null ? "" : requestedName.strip();
        if (cleanName.isBlank()) cleanName = "Waypoint " + (waypoints.size() + 1);
        if (cleanName.length() > 24) cleanName = cleanName.substring(0, 24);
        Waypoint waypoint = new Waypoint(cleanName, pos.getX(), pos.getY(), pos.getZ(), dimension);
        waypoints.add(waypoint);
        save();
        return waypoint;
    }

    public synchronized void rename(int index, String requestedName) {
        if (index < 0 || index >= waypoints.size() || requestedName == null || requestedName.isBlank()) return;
        String name = requestedName.strip();
        if (name.length() > 24) name = name.substring(0, 24);
        Waypoint old = waypoints.get(index);
        waypoints.set(index, new Waypoint(name, old.x(), old.y(), old.z(), old.dimension()));
        save();
    }

    public synchronized void remove(int index) {
        if (index >= 0 && index < waypoints.size()) {
            waypoints.remove(index);
            save();
        }
    }

    public synchronized void clear() {
        waypoints.clear();
        save();
    }

    public synchronized void save() {
        try {
            Files.createDirectories(PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(PATH)) { GSON.toJson(this, writer); }
        } catch (IOException ignored) {}
    }

    private static WaypointManager load() {
        if (!Files.exists(PATH)) return new WaypointManager();
        try (Reader reader = Files.newBufferedReader(PATH)) {
            WaypointManager manager = GSON.fromJson(reader, WaypointManager.class);
            return manager == null || manager.waypoints == null ? new WaypointManager() : manager;
        } catch (IOException | RuntimeException ignored) {
            return new WaypointManager();
        }
    }

    public record Waypoint(String name, int x, int y, int z, String dimension) {}
}
