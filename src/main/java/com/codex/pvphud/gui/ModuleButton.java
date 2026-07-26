package com.codex.pvphud.gui;

import com.codex.pvphud.animation.Animation;
import com.codex.pvphud.animation.Easing;
import com.codex.pvphud.hud.HudElement;
import com.codex.pvphud.hud.FeatureModule;
import com.codex.pvphud.render.RenderUtil;
import com.codex.pvphud.render.RoundedRenderer;
import com.codex.pvphud.theme.Theme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.List;

public final class ModuleButton {
    private final HudElement element;
    private final List<SettingComponent> settings;
    private final Animation expansion = new Animation(0, 180, Easing.OUT_CUBIC);
    private int x;
    private int y;
    private int width;
    private boolean expanded;

    public ModuleButton(HudElement element) {
        this.element = element;
        if (element instanceof FeatureModule feature) {
            this.settings = feature.adjustable()
                    ? List.of(new SettingComponent.Toggle(element), new SettingComponent.FeatureValue(feature))
                    : feature.type() == FeatureModule.Type.WAYPOINT
                    ? List.of(new SettingComponent.Toggle(element), new SettingComponent.Scale(element))
                    : List.of(new SettingComponent.Toggle(element));
        } else {
            this.settings = List.of(new SettingComponent.Toggle(element), new SettingComponent.Scale(element));
        }
    }

    public void render(DrawContext context, MinecraftClient client, Theme theme, int x, int y, int width, int mouseX, int mouseY) {
        this.x = x; this.y = y; this.width = width;
        expansion.animateTo(expanded ? 1 : 0);
        boolean hover = RenderUtil.hovered(mouseX, mouseY, x, y, width, 24);
        RoundedRenderer.fill(context, x, y, width, totalHeight(), 5, hover ? 0xEE252C35 : 0xDD1A2028);
        context.drawText(client.textRenderer, Text.literal(element.name()), x + 8, y + 8,
                element.enabled() ? theme.text() : theme.mutedText(), false);
        RoundedRenderer.fill(context, x + width - 26, y + 7, 18, 10, 5, element.enabled() ? theme.accent() : 0xFF3A414B);
        context.drawText(client.textRenderer, Text.literal(expanded ? "–" : "+"), x + width - 40, y + 7, theme.mutedText(), false);
        int visible = Math.round(settings.size() * 22 * expansion.value());
        if (visible > 0) {
            context.enableScissor(x, y + 24, x + width, y + 24 + visible);
            int sy = y + 26;
            for (SettingComponent setting : settings) {
                setting.bounds(x + 8, sy, width - 16).render(context, client, theme, mouseX, mouseY);
                sy += 22;
            }
            context.disableScissor();
        }
    }

    public boolean click(double mouseX, double mouseY, int button) {
        if (RenderUtil.hovered(mouseX, mouseY, x, y, width, 24)) {
            if (button == 0) element.setEnabled(!element.enabled());
            if (button == 1) expanded = !expanded;
            return true;
        }
        if (expanded) {
            for (SettingComponent setting : settings) if (setting.click(mouseX, mouseY, button)) return true;
        }
        return false;
    }

    public int totalHeight() {
        return 24 + Math.round(settings.size() * 22 * expansion.value());
    }

    public HudElement element() { return element; }
}
