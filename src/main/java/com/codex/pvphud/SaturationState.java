package com.codex.pvphud;

import java.lang.ref.WeakReference;
import java.util.UUID;
import net.minecraft.client.Minecraft;

/** Uses the synchronized client food state; integrated-server sampling verifies singleplayer exactly. */
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

    /** Vanilla health/food packets synchronize saturation to the client on multiplayer. */
    public static float value(Minecraft mc) {
        var current = sample;
        if (mc.player == null) return Float.NaN;
        float exact = current != null && current.server().get() == mc.getSingleplayerServer()
                && current.player().equals(mc.player.getUUID())
                ? current.value() : Float.NaN;
        return chooseValue(exact, mc.player.getFoodData().getSaturationLevel(),
                mc.player.getFoodData().getFoodLevel());
    }

    static float chooseValue(float exact, float synchronizedValue, int foodLevel) {
        float value = Float.isFinite(exact) ? exact : synchronizedValue;
        return Math.clamp(value, 0, Math.max(0, foodLevel));
    }
}
