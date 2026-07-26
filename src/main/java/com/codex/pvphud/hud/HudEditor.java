package com.codex.pvphud.hud;

import com.codex.pvphud.theme.ThemeManager;
import com.codex.pvphud.render.RoundedRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public final class HudEditor extends Screen {
    private final HudManager manager;
    private final Screen parent;
    private HudElement dragging;
    private double offsetX;
    private double offsetY;
    private boolean resizing;
    private double resizeStartX;
    private float resizeStartScale;
    private HudElement selected;

    private static final int TOP_BAR_HEIGHT = 38;
    private static final int BUTTON_WIDTH = 72;

    public HudEditor(HudManager manager, Screen parent) {
        super(Text.literal("PVPSE HUD Editor"));
        this.manager = manager;
        this.parent = parent;
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        var theme = ThemeManager.getInstance().getTheme();
        context.fill(0, 0, width, height, theme.background());
        RoundedRenderer.fill(context, 8, 7, width - 16, 26, 7, theme.panelHeader());
        context.drawTextWithShadow(textRenderer, title, 18, 16, theme.accent());
        drawAction(context, width - BUTTON_WIDTH * 2 - 24, 10, "THEME", mouseX, mouseY, theme.accentSecondary());
        drawAction(context, width - BUTTON_WIDTH - 16, 10, "DONE", mouseX, mouseY, theme.accent());
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Drag elements • resize with the corner handle • right-click to toggle"),
                width / 2, TOP_BAR_HEIGHT + 2, theme.mutedText());
        if (client != null) {
            manager.render(context, client, true);
            for (HudElement element : manager.elements()) drawElementOutline(context, element, element == selected, theme);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    public boolean mouseClicked(Click click, boolean doubled) {
        if (client != null && click.button() == 0) {
            if (inside(click.x(), click.y(), width - BUTTON_WIDTH - 16, 10, BUTTON_WIDTH, 20)) {
                close();
                return true;
            }
            if (inside(click.x(), click.y(), width - BUTTON_WIDTH * 2 - 24, 10, BUTTON_WIDTH, 20)) {
                ThemeManager.getInstance().cycle();
                return true;
            }
            for (int i = manager.elements().size() - 1; i >= 0; i--) {
                HudElement element = manager.elements().get(i);
                if (overResizeHandle(click.x(), click.y(), element)) {
                    selected = element;
                    dragging = element;
                    resizing = true;
                    resizeStartX = click.x();
                    resizeStartScale = element.scale();
                    return true;
                }
                if (element.contains(click.x(), click.y(), client)) {
                    selected = element;
                    dragging = element;
                    resizing = false;
                    offsetX = click.x() - element.x();
                    offsetY = click.y() - element.y();
                    return true;
                }
            }
        }
        if (client != null && click.button() == 1) {
            manager.elements().stream().filter(element -> element.contains(click.x(), click.y(), client)).findFirst()
                    .ifPresent(element -> {
                        selected = element;
                        element.setEnabled(!element.enabled());
                    });
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    public boolean mouseDragged(Click click, double deltaX, double deltaY) {
        if (dragging != null) {
            if (resizing) {
                float delta = (float) ((click.x() - resizeStartX) / Math.max(40.0, dragging.width(client)));
                dragging.setScale(resizeStartScale + delta);
            } else {
                dragging.setPosition((float) (click.x() - offsetX), (float) (click.y() - offsetY));
            }
            return true;
        }
        return super.mouseDragged(click, deltaX, deltaY);
    }

    public boolean mouseReleased(Click click) {
        dragging = null;
        resizing = false;
        return super.mouseReleased(click);
    }

    public void close() {
        if (client != null) client.setScreen(parent);
    }

    public boolean shouldPause() { return false; }

    private void drawAction(DrawContext context, int x, int y, String label, int mouseX, int mouseY, int color) {
        boolean hover = inside(mouseX, mouseY, x, y, BUTTON_WIDTH, 20);
        RoundedRenderer.fill(context, x, y, BUTTON_WIDTH, 20, 5, hover ? color : (0xCC000000 | color & 0xFFFFFF));
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(label), x + BUTTON_WIDTH / 2, y + 6, 0xFFF8F8F8);
    }

    private void drawElementOutline(DrawContext context, HudElement element, boolean active, com.codex.pvphud.theme.Theme theme) {
        int x = Math.round(element.x());
        int y = Math.round(element.y());
        int w = Math.round(element.width(client) * element.scale());
        int h = Math.round(element.height(client) * element.scale());
        int color = active ? theme.accentSecondary() : theme.accent();
        context.fill(x, y, x + w, y + 1, color);
        context.fill(x, y + h - 1, x + w, y + h, color);
        context.fill(x, y, x + 1, y + h, color);
        context.fill(x + w - 1, y, x + w, y + h, color);
        RoundedRenderer.fill(context, x + w - 5, y + h - 5, 9, 9, 3, color);
        if (active) {
            String scale = String.format(java.util.Locale.ROOT, "%.2fx", element.scale());
            context.drawTextWithShadow(textRenderer, Text.literal(scale), x + w + 7, y + h - 4, theme.text());
        }
    }

    private boolean overResizeHandle(double mouseX, double mouseY, HudElement element) {
        int right = Math.round(element.x() + element.width(client) * element.scale());
        int bottom = Math.round(element.y() + element.height(client) * element.scale());
        return inside(mouseX, mouseY, right - 7, bottom - 7, 14, 14);
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }
}
