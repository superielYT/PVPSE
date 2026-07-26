package com.codex.pvphud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

final class CombatFeedback {
    private static final Random RANDOM = new Random();
    private final List<Particle> particles = new ArrayList<>();
    private PlayerEntity pendingTarget;
    private int pendingHurtTime;
    private int pendingTicks;
    private boolean attackWasDown;
    private float previousHealth = -1.0F;
    private float hitMarkerLife;
    private float damageFlashLife;

    void tick(MinecraftClient client, HudConfig config) {
        if (client.player == null || client.world == null) {
            reset();
            return;
        }

        float health = client.player.getHealth() + client.player.getAbsorptionAmount();
        if (previousHealth >= 0.0F && health < previousHealth - 0.01F && config.showDamageFlash) {
            damageFlashLife = 1.0F;
        }
        previousHealth = health;

        boolean attackDown = client.options.attackKey.isPressed();
        if (attackDown && !attackWasDown && client.crosshairTarget instanceof EntityHitResult hit
                && hit.getEntity() instanceof PlayerEntity player && player != client.player) {
            pendingTarget = player;
            pendingHurtTime = player.hurtTime;
            pendingTicks = 6;
        }
        attackWasDown = attackDown;

        if (pendingTarget != null) {
            if (pendingTarget.hurtTime > pendingHurtTime) {
                if (config.showHitMarker) hitMarkerLife = 1.0F;
                if ((!pendingTarget.isAlive() || pendingTarget.getHealth() <= 0.0F) && config.showKillParticles) {
                    spawnParticles(config.particleAmount);
                }
                pendingTarget = null;
            } else if (--pendingTicks <= 0 || pendingTarget.isRemoved()) {
                pendingTarget = null;
            }
        }

        hitMarkerLife = Math.max(0.0F, hitMarkerLife - 0.16F);
        damageFlashLife = Math.max(0.0F, damageFlashLife - 0.12F);
        Iterator<Particle> it = particles.iterator();
        while (it.hasNext()) {
            Particle p = it.next();
            p.x += p.vx;
            p.y += p.vy;
            p.vy += 0.025F;
            p.life -= 0.055F;
            if (p.life <= 0.0F) it.remove();
        }
    }

    void render(DrawContext context, HudConfig config) {
        int w = context.getScaledWindowWidth();
        int h = context.getScaledWindowHeight();
        int cx = w / 2;
        int cy = h / 2;

        if (damageFlashLife > 0.0F && config.showDamageFlash) {
            int alpha = Math.min(100, Math.round(90 * damageFlashLife));
            int color = (alpha << 24) | 0x00FF2438;
            int edge = Math.max(8, Math.min(w, h) / 14);
            context.fill(0, 0, w, edge, color);
            context.fill(0, h - edge, w, h, color);
            context.fill(0, edge, edge, h - edge, color);
            context.fill(w - edge, edge, w, h - edge, color);
        }

        if (hitMarkerLife > 0.0F && config.showHitMarker) {
            int alpha = Math.min(255, Math.round(255 * hitMarkerLife));
            int color = (alpha << 24) | 0xFFFFFF;
            int size = 5 + Math.round((1.0F - hitMarkerLife) * 2.0F);
            line(context, cx - size, cy - size, cx - 2, cy - 2, color);
            line(context, cx + 2, cy + 2, cx + size + 1, cy + size + 1, color);
            line(context, cx + 2, cy - 2, cx + size + 1, cy - size - 1, color);
            line(context, cx - size, cy + size, cx - 2, cy + 2, color);
        }

        for (Particle p : particles) {
            int alpha = Math.min(255, Math.max(0, Math.round(255 * p.life)));
            int color = (alpha << 24) | (config.accentColor() & 0xFFFFFF);
            int x = cx + Math.round(p.x);
            int y = cy + Math.round(p.y);
            context.fill(x, y, x + p.size, y + p.size, color);
        }
    }

    private void spawnParticles(int requested) {
        int count = Math.max(4, Math.min(48, requested));
        for (int i = 0; i < count; i++) {
            double angle = RANDOM.nextDouble() * Math.PI * 2.0D;
            float speed = 0.7F + RANDOM.nextFloat() * 1.8F;
            particles.add(new Particle(0, 0, (float) Math.cos(angle) * speed,
                    (float) Math.sin(angle) * speed - 0.35F, 0.75F + RANDOM.nextFloat() * 0.25F,
                    RANDOM.nextBoolean() ? 1 : 2));
        }
    }

    private static void line(DrawContext context, int x1, int y1, int x2, int y2, int color) {
        int minX = Math.min(x1, x2);
        int maxX = Math.max(x1, x2);
        int minY = Math.min(y1, y2);
        int maxY = Math.max(y1, y2);
        int length = Math.max(maxX - minX, maxY - minY);
        for (int i = 0; i <= length; i++) {
            float t = length == 0 ? 0 : i / (float) length;
            int x = Math.round(x1 + (x2 - x1) * t);
            int y = Math.round(y1 + (y2 - y1) * t);
            context.fill(x, y, x + 1, y + 1, color);
        }
    }

    private void reset() {
        previousHealth = -1.0F;
        pendingTarget = null;
        pendingTicks = 0;
        hitMarkerLife = 0;
        damageFlashLife = 0;
        particles.clear();
    }

    private static final class Particle {
        float x, y, vx, vy, life;
        final int size;
        Particle(float x, float y, float vx, float vy, float life, int size) {
            this.x=x; this.y=y; this.vx=vx; this.vy=vy; this.life=life; this.size=size;
        }
    }
}
