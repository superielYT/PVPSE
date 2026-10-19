package com.codex.pvphud;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;

public final class PvpStats {
    private static final PvpStats INSTANCE = new PvpStats();
    private LivingEntity pendingTarget;
    private int pendingHurtTime;
    private int pendingTicks;
    private boolean attackWasDown;
    private int combo;
    private double lastReach;
    private long lastHitAt;

    private PvpStats() {}

    public static PvpStats getInstance() {
        return INSTANCE;
    }

    public void tick(Minecraft client) {
        if (client.player == null || client.level == null) {
            reset();
            return;
        }
        boolean attackDown = client.screen == null && client.options.keyAttack.isDown();
        if (attackDown && !attackWasDown && client.hitResult instanceof EntityHitResult hit
                && hit.getEntity() instanceof LivingEntity living && living != client.player) {
            pendingTarget = living;
            pendingHurtTime = living.hurtTime;
            pendingTicks = 6;
            lastReach = client.player.getEyePosition().distanceTo(hit.getLocation());
        }
        attackWasDown = attackDown;

        if (pendingTarget != null) {
            if (pendingTarget.hurtTime > pendingHurtTime) {
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

    private void reset() {
        pendingTarget = null;
        pendingTicks = 0;
        attackWasDown = false;
        combo = 0;
        lastReach = 0;
    }
}
