package com.codex.pvphud.hud;

import com.codex.pvphud.animation.FadeAnimation;
import com.codex.pvphud.theme.Theme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public abstract class HudElement {
    private final String id;
    private final String name;
    private final FadeAnimation visibility = new FadeAnimation(true);
    private boolean enabled = true;
    private float x;
    private float y;
    private float scale = 1.0F;

    protected HudElement(String id, String name, float x, float y) {
        this.id = id;
        this.name = name;
        this.x = x;
        this.y = y;
    }

    public final void render(DrawContext context, MinecraftClient client, Theme theme, boolean editorMode) {
        visibility.setVisible(enabled || editorMode);
        float alpha = visibility.value();
        if (alpha <= 0.01F) return;
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(x, y);
        context.getMatrices().scale(scale, scale);
        renderElement(context, client, theme, editorMode, alpha);
        context.getMatrices().popMatrix();
    }

    protected abstract void renderElement(DrawContext context, MinecraftClient client, Theme theme, boolean editorMode, float alpha);
    public abstract int width(MinecraftClient client);
    public abstract int height(MinecraftClient client);

    public String id() { return id; }
    public String name() { return name; }
    public boolean enabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public float x() { return x; }
    public float y() { return y; }
    public void setPosition(float x, float y) { this.x = Math.max(0, x); this.y = Math.max(0, y); }
    public float scale() { return scale; }
    public void setScale(float scale) { this.scale = Math.clamp(scale, 0.5F, 2.5F); }

    public boolean contains(double mouseX, double mouseY, MinecraftClient client) {
        return mouseX >= x && mouseY >= y && mouseX <= x + width(client) * scale && mouseY <= y + height(client) * scale;
    }
}
