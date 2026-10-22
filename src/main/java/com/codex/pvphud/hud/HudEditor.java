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


    private boolean controlsVisible = true;
    private final java.util.List<net.minecraft.client.gui.components.Button> controls = new java.util.ArrayList<>();
    private net.minecraft.client.gui.components.Button hideButton, smallerButton, largerButton;

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
        controls.clear();
        if (selected != null && !selected.enabled()) selected = null;
        int large = (Math.min(360, width - 16) - 82) / 4;
        int x = (width - (large * 4 + 70)) / 2;
        control(x, large, "HUDs", () -> minecraft.setScreen(new HudModulesScreen(manager, this, save))); x += large + 4;
        hideButton = control(x, large, "Hide", this::hideSelected); x += large + 4;
        smallerButton = control(x, 25, "-", () -> {
            if (selected != null) resizeSelected(selected.scale() - 0.1F);
        }); x += 29;
        largerButton = control(x, 25, "+", () -> {
            if (selected != null) resizeSelected(selected.scale() + 0.1F);
        }); x += 29;
        control(x, large, "Theme", () -> ThemeManager.getInstance().cycle()); x += large + 4;
        control(x, large, "Done", this::onClose);
        updateControls();
    }

    private net.minecraft.client.gui.components.Button control(int x, int w, String label, Runnable action) {
        var button = addRenderableWidget(com.codex.pvphud.gui.PvpButton.styled(Component.literal(label), b -> action.run())
                .bounds(x, 8, w, 20).build());
        controls.add(button); return button;
    }

    private void updateControls() {
        controls.forEach(b -> b.visible = controlsVisible);
        if (hideButton != null) hideButton.active = smallerButton.active = largerButton.active = selected != null && selected.enabled();
    }

    private void hideSelected() {
        if (selected == null) return;
        selected.setEnabled(false); selected = null; dragging = null; resizing = false;
        guideX = null; guideY = null; updateControls(); save.run();
    }

    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        var theme = ThemeManager.getInstance().getTheme();
        context.fill(0, 0, width, height, theme.background());
        if (minecraft != null) {
            manager.render(context, minecraft, true);
            if (guideX != null) context.fill(guideX, 0, guideX + 1, height, theme.accentSecondary());
            if (guideY != null) context.fill(0, guideY, width, guideY + 1, theme.accentSecondary());
            for (HudElement element : manager.layoutElements()) if (element.enabled()) drawElementOutline(context, element, element == selected, theme);
        }
        if (controlsVisible) {
            RoundedRenderer.fill(context, (width - Math.min(360, width - 16)) / 2, 4, Math.min(360, width - 16), 28, 6, theme.panelHeader());
            context.centeredText(font, Component.literal("Drag / resize | X or Hide: remove | HUDs: choose | Tab: toolbar"), width / 2, 35, theme.mutedText());
        }
        super.extractRenderState(context, mouseX, mouseY, delta);
    }

    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (super.mouseClicked(click, doubled)) return true;
        if (minecraft != null && click.button() == 0) {
            for (int i = manager.layoutElements().size() - 1; i >= 0; i--) {
                HudElement element = manager.layoutElements().get(i);
                if (!element.enabled()) continue;
                if (overCloseButton(click.x(), click.y(), element)) {
                    selected = element; hideSelected(); return true;
                }
                if (overResizeHandle(click.x(), click.y(), element)) {
                    selected = element;
                    updateControls();
                    dragging = element;
                    resizing = true;
                    resizeStartX = click.x();
                    resizeStartScale = element.scale();
                    return true;
                }
                if (element.contains(click.x(), click.y(), minecraft)) {
                    selected = element;
                    updateControls();
                    dragging = element;
                    resizing = false;
                    offsetX = click.x() - element.x();
                    offsetY = click.y() - element.y();
                    return true;
                }
            }
        }
        if (minecraft != null && click.button() == 1) {
            manager.layoutElements().reversed().stream().filter(element -> element.enabled() && element.contains(click.x(), click.y(), minecraft)).findFirst()
                    .ifPresent(element -> {
                        selected = element;
                        hideSelected();
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
            if (element.enabled() && element.contains(mouseX, mouseY, minecraft)) {
                selected = element;
                updateControls();
                resizeSelected(element.scale() + (float) vertical * 0.05F);
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
    }

    private void resizeSelected(float next) {
        if (selected == null || minecraft == null) return;
        float maximum = Math.min(2.5F, Math.min(width / (float) Math.max(1, selected.width(minecraft)),
                height / (float) Math.max(1, selected.height(minecraft))));
        selected.setScale(Math.min(next, Math.max(0.5F, maximum)));
        selected.keepOnScreen(minecraft);
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
        if (key == org.lwjgl.glfw.GLFW.GLFW_KEY_TAB) {
            controlsVisible = !controlsVisible; setFocused(null); updateControls(); return true;
        }
        if (key == org.lwjgl.glfw.GLFW.GLFW_KEY_Z && (input.modifiers() & org.lwjgl.glfw.GLFW.GLFW_MOD_CONTROL) != 0) {
            original.forEach((e, l) -> { e.setPosition(l.x(), l.y()); e.setScale(l.scale()); e.setEnabled(l.enabled()); });
            if (selected != null && !selected.enabled()) selected = null;
            updateControls();
            return true;
        }
        if (selected != null) {
            if (key == org.lwjgl.glfw.GLFW.GLFW_KEY_DELETE || key == org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSPACE) {
                hideSelected(); return true;
            }
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
        RoundedRenderer.fill(context, x + w - 12, y + 2, 10, 10, 2, theme.panelHeader());
        context.centeredText(font, Component.literal("x"), x + w - 7, y + 3, theme.text());
        if (active) {
            String scale = String.format(java.util.Locale.ROOT, "%.2fx", element.scale());
            context.text(font, Component.literal(scale), Math.clamp(x + w - font.width(scale) - 3, 0, Math.max(0, width - font.width(scale))), Math.max(0, y + h - 12), theme.text());
        }
    }

    private boolean overResizeHandle(double mouseX, double mouseY, HudElement element) {
        int right = Math.round(element.x() + element.width(minecraft) * element.scale());
        int bottom = Math.round(element.y() + element.height(minecraft) * element.scale());
        return inside(mouseX, mouseY, right - 7, bottom - 7, 14, 14);
    }

    private boolean overCloseButton(double mouseX, double mouseY, HudElement element) {
        int right = Math.round(element.x() + element.width(minecraft) * element.scale());
        return inside(mouseX, mouseY, right - 12, Math.round(element.y()) + 2, 10, 10);
    }

    private void moveWithSnapping(float proposedX, float proposedY) {
        float elementWidth = dragging.width(minecraft) * dragging.scale();
        float elementHeight = dragging.height(minecraft) * dragging.scale();
        float[] xTargets = new float[3 + manager.layoutElements().size() * 3];
        float[] yTargets = new float[3 + manager.layoutElements().size() * 3];
        int xi = 0;
        int yi = 0;
        xTargets[xi++] = 0;
        xTargets[xi++] = width / 2.0F;
        xTargets[xi++] = width;
        yTargets[yi++] = 0;
        yTargets[yi++] = height / 2.0F;
        yTargets[yi++] = height;
        for (HudElement other : manager.layoutElements()) {
            if (other == dragging || !other.enabled()) continue;
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
                Math.clamp(sy.position(), 0, Math.max(0, height - elementHeight)));
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
