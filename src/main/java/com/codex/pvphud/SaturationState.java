package com.codex.pvphud;

import java.lang.ref.WeakReference;
import java.util.UUID;
import net.minecraft.client.Minecraft;

/** Vanilla does not synchronize saturation to multiplayer clients. */
public final class SaturationState {
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
        var current = sample;
        if (mc.player == null || current == null || current.server().get() != mc.getSingleplayerServer()
                || !current.player().equals(mc.player.getUUID())) return Float.NaN;
        return Math.clamp(current.value(), 0, mc.player.getFoodData().getFoodLevel());
    }
}
