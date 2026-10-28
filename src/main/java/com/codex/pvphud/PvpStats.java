package com.codex.pvphud;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PvpStats {
    private static final PvpStats INSTANCE = new PvpStats();
    private LivingEntity pendingTarget;
    private LivingEntity comboTarget;
    private LivingEntity observedPlayer;
    private double pendingReach;
    private int pendingHurtTime;
    private int pendingTicks;
    private boolean attackWasDown;
    private int combo;
    private double lastReach;
    private long lastHitAt;
    private final Map<UUID, Integer> totemPops = new HashMap<>();

    private PvpStats() {}

    public static PvpStats getInstance() {
        return INSTANCE;
    }

    public void tick(Minecraft client) {
        if (client.player == null || client.level == null) {
            reset();
            return;
        }
        if (observedPlayer != client.player) { reset(); observedPlayer = client.player; }
        boolean attackDown = client.screen == null && client.options.keyAttack.isDown();
        if (attackDown && !attackWasDown && client.hitResult instanceof EntityHitResult hit
                && hit.getEntity() instanceof LivingEntity living && living != client.player) {
            pendingTarget = living;
            pendingHurtTime = living.hurtTime;
            pendingTicks = 6;
            pendingReach = client.player.getEyePosition().distanceTo(hit.getLocation());
        }
        attackWasDown = attackDown;

        if (pendingTarget != null) {
            if (pendingTarget.hurtTime > pendingHurtTime) {
                if (comboTarget != pendingTarget) combo = 0;
                comboTarget = pendingTarget;
                lastReach = pendingReach;
                combo++;
                lastHitAt = System.currentTimeMillis();
                pendingTarget = null;
            } else if (--pendingTicks <= 0 || pendingTarget.isRemoved()) {
                combo = 0;
                pendingTarget = null;
            }
        }
        if (combo > 0 && System.currentTimeMillis() - lastHitAt > 3_000L) combo = 0;

    }

    public int combo() { return combo; }
    public double lastReach() { return lastReach; }
    public int totemPops(UUID player) { return totemPops.getOrDefault(player, 0); }
    public void recordTotemPop(Entity entity) {
        if (entity instanceof Player player && Minecraft.getInstance().player != player)
            totemPops.merge(player.getUUID(), 1, Integer::sum);
    }

    private void reset() {
        pendingTarget = null;
        comboTarget = null;
        observedPlayer = null;
        pendingReach = 0;
        pendingTicks = 0;
        attackWasDown = false;
        combo = 0;
        lastReach = 0;
        totemPops.clear();
    }
}
