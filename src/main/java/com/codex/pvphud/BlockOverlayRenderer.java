package com.codex.pvphud;

import com.codex.pvphud.hud.BlockOverlayHUD;
import com.codex.pvphud.hud.HudManager;
import com.codex.pvphud.theme.ThemeManager;
import java.util.List;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;

/** Extract the selected shape once, then draw only immutable frame data with normal depth testing. */
final class BlockOverlayRenderer {
    private record Frame(Vec3 camera, List<AABB> boxes, int color) {}
    private static final Frame EMPTY = new Frame(Vec3.ZERO, List.of(), 0);
    private static volatile Frame frame = EMPTY;

    static void register(HudManager manager, HudConfig settings) {
        LevelRenderEvents.END_EXTRACTION.register(context -> {
            frame = EMPTY;
            Minecraft mc = Minecraft.getInstance();
            if (!settings.enabled || mc.player == null || mc.level == null || mc.options.hideGui
                    || mc.screen != null || mc.player.isSpectator()
                    || (settings.hideOnDebug && mc.getDebugOverlay().showDebugScreen())
                    || !settings.blockOverlay) return;
            var target = BlockOverlayHUD.target(mc);
            if (target == null) return;
            var pos = target.position();
            var shape = target.state().getShape(mc.level, pos);
            var boxes = shape.toAabbs().stream().map(box -> box.move(pos).inflate(0.002)).toList();
            frame = new Frame(mc.gameRenderer.getMainCamera().position(), boxes,
                    (ThemeManager.getInstance().getTheme().accent() & 0xFFFFFF) | 0xFF000000);
        });
        LevelRenderEvents.END_MAIN.register(BlockOverlayRenderer::render);
    }

    private static void render(LevelRenderContext context) {
        Frame current = frame;
        if (current.boxes().isEmpty()) return;
        context.poseStack().pushPose();
        try {
            Vec3 camera = current.camera();
            context.poseStack().translate(-camera.x, -camera.y, -camera.z);
            var faces = context.bufferSource().getBuffer(RenderTypes.debugQuads());
            for (AABB box : current.boxes()) fill(context, faces, box, (current.color() & 0xFFFFFF) | 0x28000000);
            context.bufferSource().endBatch(RenderTypes.debugQuads());
            var lines = context.bufferSource().getBuffer(RenderTypes.lines());
            for (AABB box : current.boxes()) ShapeRenderer.renderShape(context.poseStack(), lines,
                    Shapes.create(box), 0, 0, 0, 0xDD101217, 5);
            for (AABB box : current.boxes()) ShapeRenderer.renderShape(context.poseStack(), lines,
                    Shapes.create(box), 0, 0, 0, current.color(), 3);
            context.bufferSource().endBatch(RenderTypes.lines());
        } finally { context.poseStack().popPose(); }
    }

    private static void fill(LevelRenderContext context, com.mojang.blaze3d.vertex.VertexConsumer vertices, AABB box, int color) {
        float x0=(float)box.minX, x1=(float)box.maxX, y0=(float)box.minY, y1=(float)box.maxY, z0=(float)box.minZ, z1=(float)box.maxZ;
        float[][] corners = {{x0,y0,z0},{x1,y0,z0},{x1,y1,z0},{x0,y1,z0},
                {x0,y0,z1},{x1,y0,z1},{x1,y1,z1},{x0,y1,z1}};
        int[][] faces={{0,3,2,1},{4,5,6,7},{0,4,7,3},{1,2,6,5},{0,1,5,4},{3,7,6,2}};
        for(var face:faces) for(int index:face) {
            var p=corners[index];
            vertices.addVertex(context.poseStack().last(),p[0],p[1],p[2]).setColor(color);
        }
    }
}
