package com.codex.pvphud.waypoint;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;
import java.nio.file.StandardCopyOption;

public final class WaypointManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("pvpse/waypoints.json");
    private static final WaypointManager INSTANCE = load();
    private final List<Waypoint> waypoints = new ArrayList<>();
    private int tracked = -1;
    private int mapRange = 128;
    private boolean northUp;
    private boolean labels = true;
    private boolean deathMarkers = true;
    private transient boolean wasDead;
    private transient Waypoint temporary;
    private transient long temporaryUntil;
    public static final int[] COLORS = {0xFF36D399, 0xFF60A5FA, 0xFFFBBF24, 0xFFFB7185, 0xFFBF5AF2, 0xFFFF7043};

    private WaypointManager() {}

    public static WaypointManager getInstance() { return INSTANCE; }
    public synchronized List<Waypoint> waypoints() { return List.copyOf(waypoints); }
    public int mapRange() { return Math.clamp(mapRange, 32, 512); }
    public boolean northUp() { return northUp; }
    public boolean labels() { return labels; }
    public boolean deathMarkers() { return deathMarkers; }
    public void cycleRange() { mapRange = mapRange() >= 512 ? 32 : mapRange() * 2; save(); }
    public void toggleNorth() { northUp = !northUp; save(); }
    public void toggleLabels() { labels = !labels; save(); }
    public void toggleDeaths() { deathMarkers = !deathMarkers; save(); }
    public synchronized void track(int index) { temporary = null; tracked = tracked == index ? -1 : index; save(); }
    public synchronized boolean tracked(Waypoint point) { return point.equals(temporary) || tracked >= 0 && tracked < waypoints.size() && waypoints.get(tracked).equals(point); }
    public synchronized void toggleHidden(int index) { edit(index, false); }
    public synchronized void cycleColor(int index) { edit(index, true); }
    private void edit(int index, boolean color) {
        if (index < 0 || index >= waypoints.size()) return;
        var p = waypoints.get(index);
        int next = 0;
        for (int i = 0; i < COLORS.length; i++) if (COLORS[i] == p.tint()) next = (i + 1) % COLORS.length;
        waypoints.set(index, new Waypoint(p.name(), p.x(), p.y(), p.z(), p.dimension(), p.world(), color ? COLORS[next] : p.color(), color ? p.hidden() : !p.hidden()));
        save();
    }
    private static String worldKey(Minecraft client) {
        if (client.getCurrentServer() != null) return "server:" + client.getCurrentServer().ip.toLowerCase(java.util.Locale.ROOT);
        if (client.getSingleplayerServer() != null) return "local:" + client.getSingleplayerServer().getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).toAbsolutePath().normalize();
        return "unknown";
    }
    public synchronized List<Waypoint> visible(Minecraft client) {
        if (client.player == null || client.level == null) return List.of();
        String dimension = client.level.dimension().identifier().toString(), world = worldKey(client);
        var candidates = new ArrayList<>(waypoints);
        if (temporary != null && System.nanoTime() < temporaryUntil) candidates.addFirst(temporary);
        return candidates.stream().filter(p -> !p.hidden() && dimension.equals(p.dimension()) && (p.world() == null || p.world().equals(world)))
                .sorted(Comparator.<Waypoint>comparingInt(p -> p.equals(temporary) ? -1 : tracked(p) ? 0 : 1).thenComparingDouble(p -> client.player.distanceToSqr(p.x(), p.y(), p.z())))
                .toList();
    }
    public void tick(Minecraft client) {
        if (client.player == null || client.level == null) { wasDead = false; temporary = null; return; }
        if (temporary != null && (System.nanoTime() >= temporaryUntil || !temporary.world().equals(worldKey(client))
                || !temporary.dimension().equals(client.level.dimension().identifier().toString()))) temporary = null;
        boolean dead = client.player.isDeadOrDying();
        if (dead && !wasDead && deathMarkers) {
            addCurrent(client, "Death " + java.time.LocalTime.now().withNano(0));
            com.codex.pvphud.notification.NotificationManager.getInstance().push("Death marker saved", "Your last position is on the radar");
        }
        wasDead = dead;
    }

    public synchronized Waypoint addCurrent(Minecraft client) {
        return addCurrent(client, "");
    }

    /** Shift+B marks the first visible block along a separate ray; never changes attack targeting. */
    public synchronized Waypoint addLookTarget(Minecraft client) {
        if (client.player == null || client.level == null || client.screen != null) return null;
        var hit = client.player.pick(128, 1, false);
        if (!(hit instanceof net.minecraft.world.phys.BlockHitResult block)
                || hit.getType() != net.minecraft.world.phys.HitResult.Type.BLOCK) return null;
        var pos = block.getBlockPos();
        temporary = new Waypoint("Look target", pos.getX(), pos.getY(), pos.getZ(),
                client.level.dimension().identifier().toString(), worldKey(client), COLORS[2], false);
        temporaryUntil = System.nanoTime() + 300_000_000_000L;
        return temporary;
    }

    public synchronized Waypoint addCurrent(Minecraft client, String requestedName) {
        if (client.player == null || client.level == null) return null;
        var pos = client.player.blockPosition();
        String dimension = client.level.dimension().identifier().toString();
        String cleanName = requestedName == null ? "" : requestedName.strip();
        if (cleanName.isBlank()) cleanName = "Waypoint " + (waypoints.size() + 1);
        if (cleanName.length() > 24) cleanName = cleanName.substring(0, 24);
        Waypoint waypoint = new Waypoint(cleanName, pos.getX(), pos.getY(), pos.getZ(), dimension, worldKey(client), COLORS[waypoints.size() % COLORS.length], false);
        waypoints.add(waypoint);
        save();
        return waypoint;
    }

    public synchronized void rename(int index, String requestedName) {
        if (index < 0 || index >= waypoints.size() || requestedName == null || requestedName.isBlank()) return;
        String name = requestedName.strip();
        if (name.length() > 24) name = name.substring(0, 24);
        Waypoint old = waypoints.get(index);
        waypoints.set(index, new Waypoint(name, old.x(), old.y(), old.z(), old.dimension(), old.world(), old.color(), old.hidden()));
        save();
    }

    public synchronized void remove(int index) {
        if (index >= 0 && index < waypoints.size()) {
            waypoints.remove(index);
            if (tracked == index) tracked = -1;
            else if (tracked > index) tracked--;
            save();
        }
    }

    public synchronized void clear() {
        waypoints.clear();
        temporary = null;
        tracked = -1;
        save();
    }

    public synchronized void save() {
        try {
            Files.createDirectories(PATH.getParent());
            Path temporary = PATH.resolveSibling("waypoints.json.tmp");
            try (Writer writer = Files.newBufferedWriter(temporary)) { GSON.toJson(this, writer); }
            try { Files.move(temporary, PATH, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); }
            catch (IOException unsupported) { Files.move(temporary, PATH, StandardCopyOption.REPLACE_EXISTING); }
        } catch (IOException ignored) {}
    }

    private static WaypointManager load() {
        if (!Files.exists(PATH)) return new WaypointManager();
        try (Reader reader = Files.newBufferedReader(PATH)) {
            WaypointManager manager = GSON.fromJson(reader, WaypointManager.class);
            if (manager == null || manager.waypoints == null) return new WaypointManager();
            manager.waypoints.removeIf(p -> p == null || p.name() == null || p.dimension() == null);
            return manager;
        } catch (IOException | RuntimeException ignored) {
            return new WaypointManager();
        }
    }

    public record Waypoint(String name, int x, int y, int z, String dimension, String world, int color, boolean hidden) {
        public int tint() { return color == 0 ? COLORS[0] : color | 0xFF000000; }
    }
}
