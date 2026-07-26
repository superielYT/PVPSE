package com.codex.pvphud.hud;

import com.codex.pvphud.theme.ThemeManager;
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

    public HudEditor(HudManager manager, Screen parent) {
        super(Text.literal("PVPSE HUD Editor"));
        this.manager = manager;
        this.parent = parent;
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0x9905080D);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 8, ThemeManager.getInstance().getTheme().accent());
        if (client != null) manager.render(context, client, true);
        super.render(context, mouseX, mouseY, delta);
    }

    public boolean mouseClicked(Click click, boolean doubled) {
        if (client != null && click.button() == 0) {
            for (int i = manager.elements().size() - 1; i >= 0; i--) {
                HudElement element = manager.elements().get(i);
                if (element.contains(click.x(), click.y(), client)) {
                    dragging = element;
                    offsetX = click.x() - element.x();
                    offsetY = click.y() - element.y();
                    return true;
                }
            }
        }
        if (client != null && click.button() == 1) {
            manager.elements().stream().filter(element -> element.contains(click.x(), click.y(), client)).findFirst()
                    .ifPresent(element -> element.setEnabled(!element.enabled()));
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    public boolean mouseDragged(Click click, double deltaX, double deltaY) {
        if (dragging != null) {
            dragging.setPosition((float) (click.x() - offsetX), (float) (click.y() - offsetY));
            return true;
        }
        return super.mouseDragged(click, deltaX, deltaY);
    }

    public boolean mouseReleased(Click click) {
        dragging = null;
        return super.mouseReleased(click);
    }

    public void close() {
        if (client != null) client.setScreen(parent);
    }

    public boolean shouldPause() { return false; }
}
