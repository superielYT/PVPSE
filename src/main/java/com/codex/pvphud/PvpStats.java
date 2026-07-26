package com.codex.pvphud;

import com.codex.pvphud.notification.NotificationManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.EntityHitResult;

public final class PvpStats {
    private static final PvpStats INSTANCE = new PvpStats();
    private LivingEntity pendingTarget;
    private int pendingHurtTime;
    private int pendingTicks;
    private boolean attackWasDown;
    private boolean lowHealthWarning;
    private int combo;
    private double lastReach;
    private long lastHitAt;

    private PvpStats() {}

    public static PvpStats getInstance() {
        return INSTANCE;
    }

    public void tick(MinecraftClient client) {
        if (client.player == null || client.world == null) {
            reset();
            return;
        }
        boolean attackDown = client.options.attackKey.isPressed();
        if (attackDown && !attackWasDown && client.crosshairTarget instanceof EntityHitResult hit
                && hit.getEntity() instanceof LivingEntity living && living != client.player) {
            pendingTarget = living;
            pendingHurtTime = living.hurtTime;
            pendingTicks = 6;
            lastReach = client.player.getEyePos().distanceTo(hit.getPos());
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

        float health = client.player.getHealth() + client.player.getAbsorptionAmount();
        if (health <= 6.0F && !lowHealthWarning) {
            lowHealthWarning = true;
            NotificationManager.getInstance().push("Low health", String.format("%.1f hearts left", health / 2.0F));
        } else if (health > 8.0F) {
            lowHealthWarning = false;
        }
    }

    public int combo() { return combo; }
    public double lastReach() { return lastReach; }

    private void reset() {
        pendingTarget = null;
        pendingTicks = 0;
        attackWasDown = false;
        lowHealthWarning = false;
        combo = 0;
        lastReach = 0;
    }
}
