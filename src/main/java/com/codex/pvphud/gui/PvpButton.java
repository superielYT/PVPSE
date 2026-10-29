package com.codex.pvphud.gui;

import com.codex.pvphud.animation.Animation;
import com.codex.pvphud.animation.Easing;
import com.codex.pvphud.render.ColorUtil;
import com.codex.pvphud.render.RoundedRenderer;
import com.codex.pvphud.theme.ThemeManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/** Keeps vanilla button input, sounds, focus and narration with themed visuals. */
public final class PvpButton extends Button {
    private final Animation hover = new Animation(0, 110, Easing.OUT_CUBIC);

    private PvpButton(int x, int y, int width, int height, Component label, OnPress action) {
        super(x, y, width, height, label, action, DEFAULT_NARRATION);
    }

    public static Layout styled(Component label, OnPress action) { return new Layout(label, action); }

    @Override
    protected void extractContents(GuiGraphicsExtractor c, int mx, int my, float delta) {
        var theme = ThemeManager.getInstance().getTheme();
        hover.animateTo(active && (isHovered() || isFocused()) ? 1 : 0);
        float t = hover.value();
        int x = getX(), y = getY(), w = getWidth(), h = getHeight();
        int base = ColorUtil.lerp(theme.panelHeader(), theme.accent(), t * 0.15F);
        RoundedRenderer.fill(c, x, y + 1, w, h, 5, 0x33000000);
        RoundedRenderer.fill(c, x, y, w, h, 5, active ? base : ColorUtil.withAlpha(base, 130));
        if (w > 12) {
            c.fill(x+5,y,x+w-5,y+1,ColorUtil.withAlpha(0xFFFFFF,active?18:8));
            if(active)c.fill(x+5,y,x+Math.min(w-5,18+Math.round(t*12)),y+1,ColorUtil.withAlpha(theme.accent(),Math.round(90+t*120)));
            if(t>.05F)c.fill(x+1,y+5,x+2,y+h-5,ColorUtil.withAlpha(theme.accent(),Math.round(t*170)));
            if (isFocused()) c.fill(x + 5, y + h - 1, x + w - 5, y + h, theme.accent());
        }
        var font = Minecraft.getInstance().font;
        String label = font.plainSubstrByWidth(getMessage().getString(), Math.max(1, w - 12));
        c.centeredText(font, Component.literal(label), x + w / 2, y + (h - 8) / 2,
                active ? theme.text() : theme.mutedText());
    }

    public static final class Layout {
        private final Component label;
        private final OnPress action;
        private int x, y, width = 150, height = 20;
        private Layout(Component label, OnPress action) { this.label = label; this.action = action; }
        public Layout bounds(int x, int y, int width, int height) {
            this.x = x; this.y = y; this.width = width; this.height = height; return this;
        }
        public PvpButton build() { return new PvpButton(x, y, width, height, label, action); }
    }
}
