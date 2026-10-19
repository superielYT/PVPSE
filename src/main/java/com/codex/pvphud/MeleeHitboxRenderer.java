package com.codex.pvphud;

import com.codex.pvphud.hud.FeatureModule;
import com.codex.pvphud.hud.HudManager;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.MaceItem;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShapes;
import java.util.Comparator;
import java.util.List;

/** Visualizes real entity bounds without changing collision or attack behavior. */
public final class MeleeHitboxRenderer {
    private static boolean registered;
    private static HudManager hudManager;
    private static HudConfig config;
    private MeleeHitboxRenderer() {}

    public static void register(HudManager manager, HudConfig settings) {
        hudManager = manager; config = settings;
        if (registered) return;
        registered = true;
        WorldRenderEvents.AFTER_ENTITIES.register(MeleeHitboxRenderer::render);
    }

    private static void render(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (hudManager == null || config == null || client.player == null || client.world == null
                || !config.enabled || client.options.hudHidden || client.currentScreen != null
                || client.player.isSpectator()
                || (config.hideOnDebug && client.getDebugHud().shouldShowDebugHud())) return;
        FeatureModule module = hudManager.feature(FeatureModule.Type.MELEE_HITBOXES).orElse(null);
        if (module == null || !module.enabled()) return;
        var held = client.player.getMainHandStack();
        boolean mace = held.getItem() instanceof MaceItem;
        if (!(mace && config.hitboxMace) && !(held.isIn(ItemTags.SWORDS) && config.hitboxSwords)) return;

        Entity selected = client.crosshairTarget instanceof EntityHitResult hit ? hit.getEntity() : null;
        double range = module.value();
        Vec3d eye = client.player.getEyePos();
        List<Entity> targets = config.hitboxTargetOnly
                ? (selected == null ? List.of() : List.of(selected))
                : client.world.getOtherEntities(client.player, client.player.getBoundingBox().expand(range),
                    entity -> eligible(entity, client, eye, range)).stream()
                    .sorted(Comparator.comparingDouble(entity -> distanceSquared(eye, entity.getBoundingBox())))
                    .limit(16).toList();
        float tick = client.getRenderTickCounter().getTickProgress(false);
        Vec3d camera = client.gameRenderer.getCamera().getCameraPos();
        context.matrices().push();
        try {
            context.matrices().translate(-camera.x, -camera.y, -camera.z);
            for (Entity target : targets) {
                if (!eligible(target, client, eye, range)) continue;
                // Match rendered movement between ticks, including airborne mace fights.
                Vec3d smooth = target.getLerpedPos(tick);
                Box box = target.getBoundingBox().offset(smooth.x - target.getX(),
                        smooth.y - target.getY(), smooth.z - target.getZ());
                int color = 0xAA91A6BA;
                if (target == selected) {
                    color = 0xFF46E0A0;
                    if (config.hitboxStateColors) {
                        boolean ready = client.player.getAttackCooldownProgress(tick) >= 0.95F;
                        color = !ready ? 0xFFFFB454
                                : mace && client.player.fallDistance > 1.5 ? 0xFFD89CFF : 0xFF46E0A0;
                    }
                }
                outline(context, box, color, config.hitboxLineWidth);
                if (config.hitboxEyeLine) {
                    double eyeY = smooth.y + target.getEyeY() - target.getY();
                    // A thin ring marks the eye level; entity bounds stay unchanged.
                    outline(context, new Box(box.minX, eyeY - 0.002, box.minZ,
                            box.maxX, eyeY + 0.002, box.maxZ), color, 1.0F);
                }
            }
        } finally {
            context.matrices().pop();
        }
    }

    private static boolean eligible(Entity entity, MinecraftClient client, Vec3d eye, double range) {
        return entity instanceof LivingEntity && entity != client.player && entity.isAlive()
                && !entity.isSpectator() && !entity.isInvisibleTo(client.player)
                && distanceSquared(eye, entity.getBoundingBox()) <= range * range
                && client.player.canSee(entity);
    }

    private static double distanceSquared(Vec3d point, Box box) {
        double dx = point.x - Math.clamp(point.x, box.minX, box.maxX);
        double dy = point.y - Math.clamp(point.y, box.minY, box.maxY);
        double dz = point.z - Math.clamp(point.z, box.minZ, box.maxZ);
        return dx * dx + dy * dy + dz * dz;
    }

    private static void outline(WorldRenderContext context, Box box, int color, float width) {
        VertexRendering.drawOutline(context.matrices(),
                context.consumers().getBuffer(RenderLayers.lines()), VoxelShapes.cuboid(box),
                0, 0, 0, color, width);
    }
}
