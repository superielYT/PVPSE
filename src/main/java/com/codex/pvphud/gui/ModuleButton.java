package com.codex.pvphud.gui;

import com.codex.pvphud.animation.Animation;
import com.codex.pvphud.animation.Easing;
import com.codex.pvphud.hud.HudElement;
import com.codex.pvphud.hud.FeatureModule;
import com.codex.pvphud.render.RenderUtil;
import com.codex.pvphud.render.RoundedRenderer;
import com.codex.pvphud.theme.Theme;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public final class ModuleButton {
    private final HudElement element;
    private final List<SettingComponent> settings;
    private final Animation expansion = new Animation(0, 180, Easing.OUT_CUBIC);
    private int x;
    private int y;
    private int width;
    private boolean expanded;
    private SettingComponent dragging;

    public ModuleButton(HudElement element) {
        this.element = element;
        var controls = new java.util.ArrayList<SettingComponent>();
        controls.add(new SettingComponent.Toggle(element));
        if (element instanceof FeatureModule feature) {
            if (feature.adjustable()) controls.add(new SettingComponent.FeatureValue(feature));
            if (feature.type() == FeatureModule.Type.WAYPOINT) {
                controls.add(new SettingComponent.Scale(element));
                controls.add(new SettingComponent.Position(element, true));
                controls.add(new SettingComponent.Position(element, false));
                controls.add(new SettingComponent.ResetLayout(element));
            }
            if (com.codex.pvphud.FeatureKeybinds.key(feature.type()) != null) controls.add(new SettingComponent.Keybind(feature));
        } else {
            controls.add(new SettingComponent.Scale(element));
            controls.add(new SettingComponent.Position(element, true));
            controls.add(new SettingComponent.Position(element, false));
            controls.add(new SettingComponent.ResetLayout(element));
        }
        this.settings = List.copyOf(controls);
    }
    public void render(GuiGraphics context, Minecraft client, Theme theme, int x, int y, int width, int mouseX, int mouseY) {
        this.x = x; this.y = y; this.width = width;
        expansion.animateTo(expanded ? 1 : 0);
        boolean hover = RenderUtil.hovered(mouseX, mouseY, x, y, width, 24);
        RoundedRenderer.fill(context, x, y, width, totalHeight(), 5, hover ? 0xEE252C35 : 0xDD1A2028);
        context.drawString(client.font, Component.literal(client.font.plainSubstrByWidth(element.name(), Math.max(1, width - 56))), x + 8, y + 8,
                element.enabled() ? theme.text() : theme.mutedText(), false);
        RoundedRenderer.fill(context, x + width - 26, y + 7, 18, 10, 5, element.enabled() ? theme.accent() : 0xFF3A414B);
        context.drawString(client.font, Component.literal(expanded ? "–" : "+"), x + width - 40, y + 7, theme.mutedText(), false);
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
            if (button == 0 && mouseX >= x + width - 44 && mouseX < x + width - 28) expanded = !expanded;
            else if (button == 0) element.setEnabled(!element.enabled());
            if (button == 1) expanded = !expanded;
            return true;
        }
        if (expanded && mouseY >= y + 26 && mouseY < y + totalHeight()) {
            for (SettingComponent setting : settings) if (setting.click(mouseX, mouseY, button)) {
                if (setting.draggable()) dragging = setting;
                return true;
            }
        }
        return false;
    }

    public int totalHeight() {
        return 24 + Math.round(settings.size() * 22 * expansion.value());
    }

    public HudElement element() { return element; }
    public boolean drag(double mouseX) { if (dragging == null) return false; dragging.drag(mouseX); return true; }
    public void release() { dragging = null; }
}
