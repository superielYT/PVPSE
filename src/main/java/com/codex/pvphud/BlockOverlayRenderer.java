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
                    || !manager.find("block_overlay").map(e -> e.enabled()).orElse(false)) return;
            var target = BlockOverlayHUD.target(mc);
            if (target == null) return;
            var pos = target.position();
            var shape = target.state().getShape(mc.level, pos);
            var boxes = shape.toAabbs().stream().map(box -> box.move(pos).inflate(0.002)).toList();
            frame = new Frame(mc.gameRenderer.getMainCamera().position(), boxes,
                    (ThemeManager.getInstance().getTheme().accent() & 0xFFFFFF) | 0xDD000000);
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
            var lines = context.bufferSource().getBuffer(RenderTypes.lines());
            for (AABB box : current.boxes()) ShapeRenderer.renderShape(context.poseStack(), lines,
                    Shapes.create(box), 0, 0, 0, current.color(), 2);
            context.bufferSource().endBatch(RenderTypes.lines());
        } finally { context.poseStack().popPose(); }
    }
}
