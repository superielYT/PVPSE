package com.codex.pvphud.theme;

import com.codex.pvphud.notification.NotificationManager;
import com.codex.pvphud.render.RenderUtil;
import com.codex.pvphud.render.RoundedRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public final class ThemeEditorScreen extends Screen {
    private final Screen parent;
    private int red;
    private int green;
    private int blue;
    private int panelX;
    private int panelY;

    public ThemeEditorScreen(Screen parent) {
        super(Text.literal("PVPSE Theme Editor"));
        this.parent = parent;
        int accent = ThemeManager.getInstance().customTheme().accent();
        red = accent >> 16 & 0xFF;
        green = accent >> 8 & 0xFF;
        blue = accent & 0xFF;
    }

    protected void init() {
        panelX = width / 2 - 150;
        panelY = height / 2 - 92;
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        CustomTheme custom = ThemeManager.getInstance().customTheme();
        context.fill(0, 0, width, height, custom.background());
        RenderUtil.panel(context, panelX, panelY, 300, 184, custom);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, panelY + 12, custom.accent());
        drawSlider(context, panelY + 44, "RED", red, 0xFFFF4B4B);
        drawSlider(context, panelY + 75, "GREEN", green, 0xFF45E58B);
        drawSlider(context, panelY + 106, "BLUE", blue, 0xFF4B9DFF);
        int preview = 0xFF000000 | red << 16 | green << 8 | blue;
        RoundedRenderer.fill(context, panelX + 18, panelY + 141, 112, 28, 7, preview);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(String.format("#%02X%02X%02X", red, green, blue)),
                panelX + 74, panelY + 151, 0xFFFFFFFF);
        drawButton(context, panelX + 170, panelY + 141, 52, "APPLY", mouseX, mouseY, custom);
        drawButton(context, panelX + 228, panelY + 141, 52, "DONE", mouseX, mouseY, custom);
        super.render(context, mouseX, mouseY, delta);
    }

    public boolean mouseClicked(Click click, boolean doubled) {
        if (setSlider(click.x(), click.y(), panelY + 44, 0)) return true;
        if (setSlider(click.x(), click.y(), panelY + 75, 1)) return true;
        if (setSlider(click.x(), click.y(), panelY + 106, 2)) return true;
        if (inside(click.x(), click.y(), panelX + 170, panelY + 141, 52, 28)) {
            apply();
            return true;
        }
        if (inside(click.x(), click.y(), panelX + 228, panelY + 141, 52, 28)) {
            apply();
            close();
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    public boolean mouseDragged(Click click, double deltaX, double deltaY) {
        for (int i = 0; i < 3; i++) if (setSlider(click.x(), click.y(), panelY + 44 + i * 31, i)) return true;
        return super.mouseDragged(click, deltaX, deltaY);
    }

    public void close() {
        if (client != null) client.setScreen(parent);
    }

    public boolean shouldPause() { return false; }

    private void apply() {
        int color = 0xFF000000 | red << 16 | green << 8 | blue;
        ThemeManager manager = ThemeManager.getInstance();
        manager.customTheme().setAccent(color);
        manager.setTheme(manager.customTheme());
        NotificationManager.getInstance().push("Theme applied", String.format("#%02X%02X%02X", red, green, blue));
    }

    private void drawSlider(DrawContext context, int y, String label, int value, int color) {
        CustomTheme theme = ThemeManager.getInstance().customTheme();
        context.drawTextWithShadow(textRenderer, Text.literal(label + "  " + value), panelX + 18, y, theme.text());
        int x = panelX + 92;
        int width = 188;
        context.fill(x, y + 5, x + width, y + 9, 0xFF333B45);
        context.fill(x, y + 4, x + Math.round(width * value / 255.0F), y + 10, color);
    }

    private boolean setSlider(double mouseX, double mouseY, int y, int channel) {
        int x = panelX + 92;
        int width = 188;
        if (!inside(mouseX, mouseY, x, y - 3, width, 18)) return false;
        int value = Math.clamp(Math.round((float) ((mouseX - x) / width) * 255), 0, 255);
        if (channel == 0) red = value;
        if (channel == 1) green = value;
        if (channel == 2) blue = value;
        return true;
    }

    private void drawButton(DrawContext context, int x, int y, int width, String label, int mouseX, int mouseY, Theme theme) {
        RoundedRenderer.fill(context, x, y, width, 28, 6,
                inside(mouseX, mouseY, x, y, width, 28) ? theme.accentSecondary() : theme.panelHeader());
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(label), x + width / 2, y + 10, theme.text());
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }
}
