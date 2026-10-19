package com.codex.pvphud.hud;

import com.codex.pvphud.animation.FadeAnimation;
import com.codex.pvphud.theme.Theme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public abstract class HudElement {
    private final String id;
    private final String name;
    private final FadeAnimation visibility = new FadeAnimation(true);
    private boolean enabled = true;
    private float x;
    private float y;
    private float scale = 1.0F;
    private final float defaultX;
    private final float defaultY;

    protected HudElement(String id, String name, float x, float y) {
        this.id = id;
        this.name = name;
        this.x = x;
        this.y = y;
        this.defaultX = x;
        this.defaultY = y;
    }

    public final void render(GuiGraphicsExtractor context, Minecraft client, Theme theme, boolean editorMode) {
        visibility.setVisible(enabled || editorMode);
        float alpha = visibility.value();
        if (alpha <= 0.01F) return;
        context.pose().pushMatrix();
        context.pose().translate(x, y);
        context.pose().scale(scale, scale);
        try {
            renderElement(context, client, theme, editorMode, alpha);
        } finally {
            context.pose().popMatrix();
        }
    }

    protected abstract void renderElement(GuiGraphicsExtractor context, Minecraft client, Theme theme, boolean editorMode, float alpha);
    public abstract int width(Minecraft client);
    public abstract int height(Minecraft client);

    public String id() { return id; }
    public String name() { return name; }
    public boolean enabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public float x() { return x; }
    public float y() { return y; }
    public void setPosition(float x, float y) { this.x = Float.isFinite(x) ? Math.max(0, x) : defaultX; this.y = Float.isFinite(y) ? Math.max(0, y) : defaultY; }
    public float scale() { return scale; }
    public void setScale(float scale) { this.scale = Float.isFinite(scale) ? Math.clamp(scale, 0.5F, 2.5F) : 1.0F; }
    public void resetLayout() { x = defaultX; y = defaultY; scale = 1.0F; }
    public void resetSettings() { resetLayout(); enabled = true; }
    public void keepOnScreen(Minecraft client) {
        setPosition(HudGeometry.coordinate(x, client.getWindow().getGuiScaledWidth(), width(client) * scale),
                HudGeometry.coordinate(y, client.getWindow().getGuiScaledHeight(), height(client) * scale));
    }

    public boolean contains(double mouseX, double mouseY, Minecraft client) {
        return mouseX >= x && mouseY >= y && mouseX <= x + width(client) * scale && mouseY <= y + height(client) * scale;
    }
}
