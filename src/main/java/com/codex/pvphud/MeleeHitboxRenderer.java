package com.codex.pvphud;

import com.codex.pvphud.hud.FeatureModule;
import com.codex.pvphud.hud.HudManager;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.MaceItem;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShapes;

/**
 * Read-only melee practice overlay. Wireframes are displayed only while a sword
 * or mace is held; it never changes server packets, reach, aim, or attacks.
 */
public final class MeleeHitboxRenderer {
    private static boolean registered;
    private static HudManager hudManager;

    private MeleeHitboxRenderer() { }

    public static void register(HudManager manager) {
        hudManager = manager;
        if (registered) return;
        registered = true;
        WorldRenderEvents.AFTER_ENTITIES.register(MeleeHitboxRenderer::render);
    }

    private static void render(net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (hudManager == null || client.player == null || client.world == null || !enabled() || !hasMeleeWeapon(client)) return;

        // Targeted-only is intentional: it keeps the overlay useful for practice
        // without revealing entities that are not already under the crosshair.
        if (!(client.crosshairTarget instanceof EntityHitResult hit)) return;
        Entity target = hit.getEntity();
        if (!(target instanceof LivingEntity) || target == client.player || !target.isAlive()) return;
        if (!client.player.canSee(target)) return;

        double range = range();
        if (client.player.squaredDistanceTo(target) > range * range) return;

        Vec3d camera = client.gameRenderer.getCamera().getCameraPos();
        Box box = target.getBoundingBox().expand(0.015D);
        context.matrices().push();
        context.matrices().translate(-camera.x, -camera.y, -camera.z);
        VertexRendering.drawOutline(
                context.matrices(),
                context.consumers().getBuffer(RenderLayers.lines()),
                VoxelShapes.cuboid(box), 0.0D, 0.0D, 0.0D,
                0xFF46E0A0, 1.5F
        );
        context.matrices().pop();
    }

    private static boolean enabled() {
        return hudManager.feature(FeatureModule.Type.MELEE_HITBOXES)
                .map(FeatureModule::enabled).orElse(false);
    }

    private static double range() {
        return hudManager.feature(FeatureModule.Type.MELEE_HITBOXES)
                .map(FeatureModule::value).orElse(5.0D);
    }

    private static boolean hasMeleeWeapon(MinecraftClient client) {
        return client.player.getMainHandStack().isIn(ItemTags.SWORDS)
                || client.player.getMainHandStack().getItem() instanceof MaceItem;
    }
}
