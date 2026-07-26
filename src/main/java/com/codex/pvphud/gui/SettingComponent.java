package com.codex.pvphud.gui;

import com.codex.pvphud.hud.HudElement;
import com.codex.pvphud.render.RoundedRenderer;
import com.codex.pvphud.theme.Theme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

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

    public abstract void render(DrawContext context, MinecraftClient client, Theme theme, int mouseX, int mouseY);
    public abstract boolean click(double mouseX, double mouseY, int button);
    public int height() { return height; }

    public static final class Toggle extends SettingComponent {
        public Toggle(HudElement element) { super(element); }
        public void render(DrawContext c, MinecraftClient mc, Theme t, int mx, int my) {
            c.drawText(mc.textRenderer, Text.literal("Visible"), x, y + 6, t.text(), false);
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
        public void render(DrawContext c, MinecraftClient mc, Theme t, int mx, int my) {
            c.drawText(mc.textRenderer, Text.literal(String.format("Scale %.2f", element.scale())), x, y + 4, t.text(), false);
            int sx = x + 76;
            int sw = width - 82;
            c.fill(sx, y + 9, sx + sw, y + 11, 0xFF343B45);
            int fill = Math.round((element.scale() - 0.5F) / 2.0F * sw);
            c.fill(sx, y + 8, sx + fill, y + 12, t.accent());
        }
        public boolean click(double mx, double my, int button) {
            if (button == 0 && inside(mx, my)) {
                float normalized = (float) Math.clamp((mx - (x + 76)) / Math.max(1.0, width - 82.0), 0.0, 1.0);
                element.setScale(0.5F + normalized * 2.0F);
                return true;
            }
            return false;
        }
    }

    protected boolean inside(double mx, double my) {
        return mx >= x && my >= y && mx < x + width && my < y + height;
    }
}
