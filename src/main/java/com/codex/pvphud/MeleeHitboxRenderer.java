package com.codex.pvphud;

import com.codex.pvphud.hud.FeatureModule;
import com.codex.pvphud.hud.HudManager;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Snapshots actual bounds during extraction; drawing never reads live entities. */
public final class MeleeHitboxRenderer {
    private static boolean registered;
    private static HudManager hudManager;
    private static HudConfig config;
    private static final Frame EMPTY = new Frame(Vec3.ZERO, List.of());
    private static volatile Frame frame = EMPTY;
    private record Outline(AABB box, int color, float width) {}
    private record Frame(Vec3 camera, List<Outline> outlines) {}
    private MeleeHitboxRenderer() {}

    public static void register(HudManager manager, HudConfig settings) {
        hudManager = manager; config = settings;
        if (registered) return;
        registered = true;
        LevelRenderEvents.END_EXTRACTION.register(context -> extract());
        LevelRenderEvents.END_MAIN.register(MeleeHitboxRenderer::render);
    }

    private static void extract() {
        frame = EMPTY;
        Minecraft client = Minecraft.getInstance();
        if (hudManager == null || config == null || client.player == null || client.level == null
                || !config.enabled || client.options.hideGui || client.screen != null
                || client.player.isSpectator()
                || (config.hideOnDebug && client.getDebugOverlay().showDebugScreen())) return;
        FeatureModule module = hudManager.feature(FeatureModule.Type.MELEE_HITBOXES).orElse(null);
        if (module == null || !module.enabled()) return;
        var held = client.player.getMainHandItem();
        boolean mace = held.getItem() instanceof MaceItem;
        if (!(mace && config.hitboxMace) && !(held.is(ItemTags.SWORDS) && config.hitboxSwords)) return;
        Entity selected = client.hitResult instanceof EntityHitResult hit ? hit.getEntity() : null;
        double range = module.value();
        Vec3 eye = client.player.getEyePosition();
        List<Entity> targets = config.hitboxTargetOnly
                ? (selected == null ? List.of() : List.of(selected))
                : client.level.getEntities(client.player, client.player.getBoundingBox().inflate(range),
                    entity -> eligible(entity, client, eye, range)).stream()
                    .sorted(Comparator.comparingDouble(entity -> distanceSquared(eye, entity.getBoundingBox())))
                    .limit(16).toList();
        float tick = client.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        var outlines = new ArrayList<Outline>();
        for (Entity target : targets) {
            if (!eligible(target, client, eye, range)) continue;
            Vec3 smooth = target.getPosition(tick);
            AABB box = target.getBoundingBox().move(smooth.x - target.getX(),
                    smooth.y - target.getY(), smooth.z - target.getZ());
            int color = 0x6691A6BA;
            if (target == selected) {
                color = 0xFF46E0A0;
                if (config.hitboxStateColors) {
                    boolean ready = client.player.getAttackStrengthScale(tick) >= 0.95F;
                    color = !ready ? 0xFFFFB454
                            : mace && client.player.fallDistance > 1.5 ? 0xFFD89CFF : 0xFF46E0A0;
                }
            }
            outlines.add(new Outline(box, color, target == selected ? config.hitboxLineWidth : 1.0F));
            if (config.hitboxEyeLine && target == selected) {
                double eyeY = smooth.y + target.getEyeY() - target.getY();
                outlines.add(new Outline(new AABB(box.minX, eyeY - 0.002, box.minZ,
                        box.maxX, eyeY + 0.002, box.maxZ), color, 1.0F));
            }
        }
        frame = new Frame(client.gameRenderer.getMainCamera().position(), List.copyOf(outlines));
    }

    private static void render(LevelRenderContext context) {
        Frame current = frame;
        if (current.outlines().isEmpty()) return;
        context.poseStack().pushPose();
        try {
            Vec3 camera = current.camera();
            context.poseStack().translate(-camera.x, -camera.y, -camera.z);
            var buffer = context.bufferSource().getBuffer(RenderTypes.lines());
            for (Outline outline : current.outlines()) {
                ShapeRenderer.renderShape(context.poseStack(), buffer, Shapes.create(outline.box()),
                        0, 0, 0, outline.color(), outline.width());
            }
            context.bufferSource().endBatch(RenderTypes.lines());
        } finally {
            context.poseStack().popPose();
        }
    }

    private static boolean eligible(Entity entity, Minecraft client, Vec3 eye, double range) {
        return entity instanceof LivingEntity && entity != client.player && entity.isAlive()
                && !entity.isSpectator() && !entity.isInvisibleTo(client.player)
                && distanceSquared(eye, entity.getBoundingBox()) <= range * range
                && client.player.hasLineOfSight(entity);
    }

    private static double distanceSquared(Vec3 point, AABB box) {
        double dx = point.x - Math.clamp(point.x, box.minX, box.maxX);
        double dy = point.y - Math.clamp(point.y, box.minY, box.maxY);
        double dz = point.z - Math.clamp(point.z, box.minZ, box.maxZ);
        return dx * dx + dy * dy + dz * dz;
    }
}
