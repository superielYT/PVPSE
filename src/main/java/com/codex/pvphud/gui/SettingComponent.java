package com.codex.pvphud.gui;

import com.codex.pvphud.hud.HudElement;
import com.codex.pvphud.hud.FeatureModule;
import com.codex.pvphud.FeatureKeybinds;
import com.codex.pvphud.render.RoundedRenderer;
import com.codex.pvphud.theme.Theme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public abstract class SettingComponent {
    protected final HudElement element;
    protected int x;
    protected int y;
    protected int width;
    protected int height = 20;

    protected SettingComponent(HudElement element) {
        this.element = element;
    }

    public SettingComponent bounds(int x, int y, int width) {
        this.x = x;
        this.y = y;
        this.width = width;
        return this;
    }

    public abstract void render(GuiGraphicsExtractor context, Minecraft client, Theme theme, int mouseX, int mouseY);
    public abstract boolean click(double mouseX, double mouseY, int button);
    public boolean draggable() { return this instanceof Scale || this instanceof FeatureValue || this instanceof Position; }
    public void drag(double mouseX) { click(Math.clamp(mouseX, x, x + width - 1), y + height / 2.0, 0); }
    public int height() { return height; }

    public static final class Toggle extends SettingComponent {
        public Toggle(HudElement element) { super(element); }
        public void render(GuiGraphicsExtractor c, Minecraft mc, Theme t, int mx, int my) {
            c.text(mc.font, Component.literal("Visible"), x, y + 6, t.text(), false);
            int track = element.enabled() ? t.accent() : 0xFF3A414B;
            RoundedRenderer.fill(c, x + width - 34, y + 3, 30, 14, 7, track);
            RoundedRenderer.fill(c, x + width - (element.enabled() ? 18 : 31), y + 5, 10, 10, 5, 0xFFF8F8F8);
        }
        public boolean click(double mx, double my, int button) {
            if (button == 0 && inside(mx, my)) {
                element.setEnabled(!element.enabled());
                return true;
            }
            return false;
        }
    }

    public static final class Scale extends SettingComponent {
        public Scale(HudElement element) { super(element); }
        public void render(GuiGraphicsExtractor c, Minecraft mc, Theme t, int mx, int my) {
            c.text(mc.font, Component.literal(mc.font.plainSubstrByWidth(String.format(java.util.Locale.ROOT, "Scale %.2f", element.scale()), width / 2 - 3)), x, y + 4, t.text(), false);
            int sx = x + width / 2;
            int sw = width - width / 2 - 4;
            c.fill(sx, y + 9, sx + sw, y + 11, 0xFF343B45);
            int fill = Math.round((element.scale() - 0.5F) / 2.0F * sw);
            c.fill(sx, y + 8, sx + fill, y + 12, t.accent());
        }
        public boolean click(double mx, double my, int button) {
            if (button == 0 && inside(mx, my)) {
                float normalized = (float) Math.clamp((mx - (x + width / 2)) / Math.max(1.0, width - width / 2 - 4.0), 0.0, 1.0);
                element.setScale(0.5F + normalized * 2.0F);
                return true;
            }
            return false;
        }
    }

    public static final class FeatureValue extends SettingComponent {
        private final FeatureModule feature;
        public FeatureValue(FeatureModule feature) { super(feature); this.feature = feature; }
        public void render(GuiGraphicsExtractor c, Minecraft mc, Theme t, int mx, int my) {
            c.text(mc.font, Component.literal(mc.font.plainSubstrByWidth(feature.valueName() + " " + (feature.type() == FeatureModule.Type.FULLBRIGHT ? Math.round(feature.value() * 100) + "%" : Math.round(feature.value()) + ""), width / 2 - 3)), x, y + 4, t.text(), false);
            int sx = x + width / 2;
            int sw = Math.max(1, width - width / 2 - 4);
            c.fill(sx, y + 9, sx + sw, y + 11, 0xFF343B45);
            double range = feature.maximum() - feature.minimum();
            int fill = (int) Math.round((feature.value() - feature.minimum()) / range * sw);
            c.fill(sx, y + 8, sx + fill, y + 12, t.accent());
        }
        public boolean click(double mx, double my, int button) {
            if (button != 0 || !inside(mx, my)) return false;
            double normalized = Math.clamp((mx - (x + width / 2)) / Math.max(1.0, width - width / 2 - 4.0), 0.0, 1.0);
            feature.setValue(feature.minimum() + normalized * (feature.maximum() - feature.minimum()));
            return true;
        }
    }

    public static final class Keybind extends SettingComponent {
        private final FeatureModule feature;
        public Keybind(FeatureModule feature) { super(feature); this.feature = feature; }
        public void render(GuiGraphicsExtractor c, Minecraft mc, Theme t, int mx, int my) {
            c.text(mc.font, Component.literal("Keybind"), x, y + 6, t.text(), false);
            var binding = FeatureKeybinds.key(feature.type());
            String label = FeatureKeybinds.listening(feature.type()) ? "LISTENING..."
                    : binding == null ? "NONE" : binding.getTranslatedKeyMessage().getString();
            int buttonWidth = Math.max(68, mc.font.width(label) + 12);
            RoundedRenderer.fill(c, x + width - buttonWidth, y + 2, buttonWidth, 16, 4,
                    FeatureKeybinds.listening(feature.type()) ? t.accent() : t.panelHeader());
            c.centeredText(mc.font, Component.literal(label),
                    x + width - buttonWidth / 2, y + 6, t.text());
        }
        public boolean click(double mx, double my, int button) {
            if (button == 0 && inside(mx, my)) {
                FeatureKeybinds.begin(feature.type());
                return true;
            }
            return false;
        }
    }

    public static final class Position extends SettingComponent {
        private final boolean horizontal;
        public Position(HudElement element, boolean horizontal) { super(element); this.horizontal = horizontal; }
        public void render(GuiGraphicsExtractor c, Minecraft mc, Theme t, int mx, int my) {
            float position = horizontal ? element.x() : element.y();
            c.text(mc.font, Component.literal((horizontal ? "X " : "Y ") + Math.round(position)), x, y + 6, t.text(), false);
            float limit = Math.max(1, (horizontal ? mc.getWindow().getGuiScaledWidth() : mc.getWindow().getGuiScaledHeight()) - (horizontal ? element.width(mc) : element.height(mc)) * element.scale());
            int start = x + width / 2, length = Math.max(1, width - width / 2 - 4);
            c.fill(start, y + 9, start + length, y + 11, t.panelHeader());
            c.fill(start, y + 8, start + Math.round(Math.clamp(position / Math.max(1, limit), 0, 1) * length), y + 12, t.accent());
        }
        public boolean click(double mx, double my, int button) {
            if (button != 0 || !inside(mx, my)) return false;
            var mc = Minecraft.getInstance();
            float limit = horizontal ? mc.getWindow().getGuiScaledWidth() : mc.getWindow().getGuiScaledHeight();
            float size = horizontal ? element.width(mc) : element.height(mc);
            float value = (float) Math.clamp((mx - x - width / 2) / Math.max(1, width - width / 2 - 4), 0, 1) * Math.max(0, limit - size * element.scale());
            element.setPosition(horizontal ? value : element.x(), horizontal ? element.y() : value);
            return true;
        }
    }

    public static final class ResetLayout extends SettingComponent {
        public ResetLayout(HudElement element) { super(element); }
        public void render(GuiGraphicsExtractor c, Minecraft mc, Theme t, int mx, int my) {
            RoundedRenderer.fill(c, x, y + 2, width, 16, 4, inside(mx, my) ? t.accentSecondary() : t.panelHeader());
            c.centeredText(mc.font, Component.literal("Reset position & scale"), x + width / 2, y + 6, t.text());
        }
        public boolean click(double mx, double my, int button) {
            if (button != 0 || !inside(mx, my)) return false;
            element.resetLayout();
            return true;
        }
    }

    protected boolean inside(double mx, double my) {
        return mx >= x && my >= y && mx < x + width && my < y + height;
    }
}
