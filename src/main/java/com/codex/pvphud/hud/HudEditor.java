package com.codex.pvphud.hud;

import com.codex.pvphud.theme.ThemeManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import com.codex.pvphud.render.RoundedRenderer;

public final class HudEditor extends Screen {
    private final HudManager manager;
    private final Screen parent;
    private final Runnable save;
    private HudElement dragging;
    private double offsetX;
    private double offsetY;
    private boolean resizing;
    private double resizeStartX;
    private float resizeStartScale;
    private HudElement selected;
    private Integer guideX;
    private Integer guideY;
    private final java.util.Map<HudElement, Layout> original = new java.util.LinkedHashMap<>();
    private record Layout(float x, float y, float scale, boolean enabled) {}


    private static final int TOP_BAR_HEIGHT = 38;
    private static final int BUTTON_WIDTH = 72;

    public HudEditor(HudManager manager, Screen parent) {
        this(manager, parent, () -> {});
    }

    public HudEditor(HudManager manager, Screen parent, Runnable save) {
        super(Component.literal("PVPSE HUD Editor"));
        this.manager = manager;
        this.parent = parent;
        this.save = save;
        manager.layoutElements().forEach(e -> original.put(e, new Layout(e.x(), e.y(), e.scale(), e.enabled())));
    }

    @Override protected void init() {
        addRenderableWidget(com.codex.pvphud.gui.PvpButton.styled(Component.literal("-"), b -> {
            if (selected != null) resizeSelected(selected.scale() - 0.1F);
        }).bounds(width - 228, height - 32, 25, 20).build());
        addRenderableWidget(com.codex.pvphud.gui.PvpButton.styled(Component.literal("+"), b -> {
            if (selected != null) resizeSelected(selected.scale() + 0.1F);
        }).bounds(width - 199, height - 32, 25, 20).build());
    }

    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        var theme = ThemeManager.getInstance().getTheme();
        context.fill(0, 0, width, height, theme.background());
        RoundedRenderer.fill(context, 8, height - 37, width - 16, 30, 7, theme.panelHeader());
        context.text(font, Component.literal("HUD EDITOR"), 18, height - 25, theme.accent());
        drawAction(context, width - BUTTON_WIDTH * 2 - 24, height - 32, "THEME", mouseX, mouseY, theme.accentSecondary());
        drawAction(context, width - BUTTON_WIDTH - 16, height - 32, "DONE", mouseX, mouseY, theme.accent());
        context.centeredText(font,
                Component.literal("Drag: move | Corner / wheel / +/-: size | Ctrl+Z: undo"),
                width / 2, height - 49, theme.mutedText());
        if (minecraft != null) {
            manager.render(context, minecraft, true);
            if (guideX != null) context.fill(guideX, TOP_BAR_HEIGHT + 12, guideX + 1, height, theme.accentSecondary());
            if (guideY != null) context.fill(0, guideY, width, guideY + 1, theme.accentSecondary());
            for (HudElement element : manager.layoutElements()) drawElementOutline(context, element, element == selected, theme);
        }
        super.extractRenderState(context, mouseX, mouseY, delta);
    }

    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (super.mouseClicked(click, doubled)) return true;
        if (minecraft != null && click.button() == 0) {
            if (inside(click.x(), click.y(), width - BUTTON_WIDTH - 16, height - 32, BUTTON_WIDTH, 20)) {
                onClose();
                return true;
            }
            if (inside(click.x(), click.y(), width - BUTTON_WIDTH * 2 - 24, height - 32, BUTTON_WIDTH, 20)) {
                ThemeManager.getInstance().cycle();
                return true;
            }
            for (int i = manager.layoutElements().size() - 1; i >= 0; i--) {
                HudElement element = manager.layoutElements().get(i);
                if (overResizeHandle(click.x(), click.y(), element)) {
                    selected = element;
                    dragging = element;
                    resizing = true;
                    resizeStartX = click.x();
                    resizeStartScale = element.scale();
                    return true;
                }
                if (element.contains(click.x(), click.y(), minecraft)) {
                    selected = element;
                    dragging = element;
                    resizing = false;
                    offsetX = click.x() - element.x();
                    offsetY = click.y() - element.y();
                    return true;
                }
            }
        }
        if (minecraft != null && click.button() == 1) {
            manager.layoutElements().reversed().stream().filter(element -> element.contains(click.x(), click.y(), minecraft)).findFirst()
                    .ifPresent(element -> {
                        selected = element;
                        element.setEnabled(!element.enabled());
                    });
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    public boolean mouseDragged(MouseButtonEvent click, double deltaX, double deltaY) {
        if (dragging != null) {
            if (resizing) {
                float delta = (float) ((click.x() - resizeStartX) / Math.max(40.0, dragging.width(minecraft)));
                resizeSelected(resizeStartScale + delta);
            } else {
                moveWithSnapping((float) (click.x() - offsetX), (float) (click.y() - offsetY));
            }
            return true;
        }
        return super.mouseDragged(click, deltaX, deltaY);
    }

    @Override public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        for (HudElement element : manager.layoutElements().reversed()) {
            if (element.contains(mouseX, mouseY, minecraft)) {
                selected = element;
                resizeSelected(element.scale() + (float) vertical * 0.05F);
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
    }

    private void resizeSelected(float next) {
        if (selected == null || minecraft == null) return;
        float maximum = Math.min(2.5F, Math.min(width / (float) Math.max(1, selected.width(minecraft)),
                (height - 58) / (float) Math.max(1, selected.height(minecraft))));
        selected.setScale(Math.min(next, Math.max(0.5F, maximum)));
        selected.keepOnScreen(minecraft);
        selected.setPosition(selected.x(), Math.min(selected.y(), Math.max(0, height - 58 - selected.height(minecraft) * selected.scale())));
    }

    public boolean mouseReleased(MouseButtonEvent click) {
        dragging = null;
        resizing = false;
        guideX = null;
        guideY = null;
        return super.mouseReleased(click);
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyEvent input) {
        int key = input.key();
        if (key == org.lwjgl.glfw.GLFW.GLFW_KEY_Z && (input.modifiers() & org.lwjgl.glfw.GLFW.GLFW_MOD_CONTROL) != 0) {
            original.forEach((e, l) -> { e.setPosition(l.x(), l.y()); e.setScale(l.scale()); e.setEnabled(l.enabled()); });
            return true;
        }
        if (selected != null) {
            if (key == org.lwjgl.glfw.GLFW.GLFW_KEY_EQUAL || key == org.lwjgl.glfw.GLFW.GLFW_KEY_KP_ADD) {
                resizeSelected(selected.scale() + 0.1F); return true;
            }
            if (key == org.lwjgl.glfw.GLFW.GLFW_KEY_MINUS || key == org.lwjgl.glfw.GLFW.GLFW_KEY_KP_SUBTRACT) {
                resizeSelected(selected.scale() - 0.1F); return true;
            }
            if (key == org.lwjgl.glfw.GLFW.GLFW_KEY_R) { selected.resetLayout(); selected.keepOnScreen(minecraft); return true; }
            int step = (input.modifiers() & org.lwjgl.glfw.GLFW.GLFW_MOD_SHIFT) != 0 ? 10 : 1;
            int dx = key == org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT ? -step : key == org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT ? step : 0;
            int dy = key == org.lwjgl.glfw.GLFW.GLFW_KEY_UP ? -step : key == org.lwjgl.glfw.GLFW.GLFW_KEY_DOWN ? step : 0;
            if (dx != 0 || dy != 0) { selected.setPosition(selected.x() + dx, selected.y() + dy); selected.keepOnScreen(minecraft); return true; }
        }
        return super.keyPressed(input);
    }
    public void onClose() {
        if (minecraft != null) minecraft.setScreen(parent);
    }

    @Override public void removed() { save.run(); super.removed(); }

    public boolean isPauseScreen() { return false; }

    private void drawAction(GuiGraphicsExtractor context, int x, int y, String label, int mouseX, int mouseY, int color) {
        boolean hover = inside(mouseX, mouseY, x, y, BUTTON_WIDTH, 20);
        RoundedRenderer.fill(context, x, y, BUTTON_WIDTH, 20, 5, hover ? color : (0xCC000000 | color & 0xFFFFFF));
        context.centeredText(font, Component.literal(label), x + BUTTON_WIDTH / 2, y + 6, 0xFFF8F8F8);
    }

    private void drawElementOutline(GuiGraphicsExtractor context, HudElement element, boolean active, com.codex.pvphud.theme.Theme theme) {
        int x = Math.round(element.x());
        int y = Math.round(element.y());
        int w = Math.round(element.width(minecraft) * element.scale());
        int h = Math.round(element.height(minecraft) * element.scale());
        int color = active ? theme.accentSecondary() : theme.accent();
        context.fill(x, y, x + w, y + 1, color);
        context.fill(x, y + h - 1, x + w, y + h, color);
        context.fill(x, y, x + 1, y + h, color);
        context.fill(x + w - 1, y, x + w, y + h, color);
        RoundedRenderer.fill(context, x + w - 5, y + h - 5, 9, 9, 3, color);
        if (active) {
            String scale = String.format(java.util.Locale.ROOT, "%.2fx", element.scale());
            context.text(font, Component.literal(scale), x + w + 7, y + h - 4, theme.text());
        }
    }

    private boolean overResizeHandle(double mouseX, double mouseY, HudElement element) {
        int right = Math.round(element.x() + element.width(minecraft) * element.scale());
        int bottom = Math.round(element.y() + element.height(minecraft) * element.scale());
        return inside(mouseX, mouseY, right - 7, bottom - 7, 14, 14);
    }

    private void moveWithSnapping(float proposedX, float proposedY) {
        float elementWidth = dragging.width(minecraft) * dragging.scale();
        float elementHeight = dragging.height(minecraft) * dragging.scale();
        float[] xTargets = new float[3 + manager.layoutElements().size() * 3];
        float[] yTargets = new float[3 + manager.layoutElements().size() * 3];
        int xi = 0;
        int yi = 0;
        xTargets[xi++] = 8;
        xTargets[xi++] = width / 2.0F;
        xTargets[xi++] = width - 8;
        yTargets[yi++] = 8;
        yTargets[yi++] = height / 2.0F;
        yTargets[yi++] = height - 8;
        for (HudElement other : manager.layoutElements()) {
            if (other == dragging) continue;
            float ow = other.width(minecraft) * other.scale();
            float oh = other.height(minecraft) * other.scale();
            xTargets[xi++] = other.x();
            xTargets[xi++] = other.x() + ow / 2.0F;
            xTargets[xi++] = other.x() + ow;
            yTargets[yi++] = other.y();
            yTargets[yi++] = other.y() + oh / 2.0F;
            yTargets[yi++] = other.y() + oh;
        }
        Snap sx = snap(proposedX, elementWidth, xTargets, xi);
        Snap sy = snap(proposedY, elementHeight, yTargets, yi);
        guideX = sx.guide();
        guideY = sy.guide();
        dragging.setPosition(Math.clamp(sx.position(), 0, Math.max(0, width - elementWidth)),
                Math.clamp(sy.position(), 0, Math.max(0, height - 58 - elementHeight)));
    }

    private static Snap snap(float position, float size, float[] targets, int count) {
        float bestPosition = position;
        float bestDistance = 6.1F;
        Integer guide = null;
        for (int i = 0; i < count; i++) {
            for (float anchor : new float[]{0, size / 2.0F, size}) {
                float candidate = targets[i] - anchor;
                float distance = Math.abs(candidate - position);
                if (distance < bestDistance) {
                    bestDistance = distance;
                    bestPosition = candidate;
                    guide = Math.round(targets[i]);
                }
            }
        }
        return new Snap(bestPosition, guide);
    }

    private record Snap(float position, Integer guide) {}

    private static boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }
}
