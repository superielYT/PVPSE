package com.codex.pvphud;

import java.lang.ref.WeakReference;
import java.util.UUID;
import net.minecraft.client.Minecraft;

/** Integrated/server sync is exact; ordinary vanilla health snapshots may be stale. */
public final class SaturationState {
    private static volatile NetworkSample network;
    public static boolean applyingVanilla;
    private record NetworkSample(WeakReference<Object> connection, WeakReference<Object> player, float value, boolean exact) {}
    public static void accept(float value, boolean exact) {
        var mc=Minecraft.getInstance();
        if(mc.player==null || mc.getConnection()==null || !Float.isFinite(value) || value<0 || value>20)return;
        var old=network;
        if(!exact && old!=null && old.exact() && old.connection().get()==mc.getConnection() && old.player().get()==mc.player)return;
        network=new NetworkSample(new WeakReference<>(mc.getConnection()),new WeakReference<>(mc.player),value,exact);
    }
    public static void clearNetwork() { network=null; }
    static float networkValue(Minecraft mc) {
        var n=network;
        return mc.player!=null && n!=null && n.connection().get()==mc.getConnection() && n.player().get()==mc.player
                ? Math.clamp(n.value(),0,mc.player.getFoodData().getFoodLevel()):Float.NaN;
    }
    static boolean networkExact() { return network!=null && network.exact(); }
    public static boolean approximate(Minecraft mc) {
        var n=network;
        return mc.getSingleplayerServer()==null && n!=null && n.connection().get()==mc.getConnection()
                && n.player().get()==mc.player && !n.exact();
    }
    private static volatile Sample sample;
    private static WeakReference<Object> lastWorld = new WeakReference<>(null);
    private static int lastTick = -1;
    private record Sample(WeakReference<Object> server, UUID player, float value) {}

    private SaturationState() {}

    static void tick(Minecraft mc) {
        if (mc.player == null || mc.getSingleplayerServer() == null) {
            sample = null;
            lastWorld.clear();
            lastTick = -1;
            return;
        }
        if (lastWorld.get() == mc.level && lastTick == mc.player.tickCount) return;
        lastWorld = new WeakReference<>(mc.level);
        lastTick = mc.player.tickCount;
        var server = mc.getSingleplayerServer();
        var id = mc.player.getUUID();
        server.execute(() -> {
            var player = server.getPlayerList().getPlayer(id);
            if (player != null) sample = new Sample(new WeakReference<>(server), id,
                    player.getFoodData().getSaturationLevel());
        });
    }

    /** NaN means unavailable; never use the client's unsynchronized default. */
    public static float value(Minecraft mc) {
        if(mc.getSingleplayerServer()==null){
            return networkValue(mc);
        }
        var current = sample;
        if (mc.player == null || current == null || current.server().get() != mc.getSingleplayerServer()
                || !current.player().equals(mc.player.getUUID())) return Float.NaN;
        return Math.clamp(current.value(), 0, mc.player.getFoodData().getFoodLevel());
    }
}
