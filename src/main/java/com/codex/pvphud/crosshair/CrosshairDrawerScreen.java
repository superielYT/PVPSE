package com.codex.pvphud.crosshair;

import com.codex.pvphud.render.RenderUtil;
import com.codex.pvphud.render.RoundedRenderer;
import com.codex.pvphud.notification.NotificationManager;
import com.codex.pvphud.theme.Theme;
import com.codex.pvphud.theme.ThemeManager;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public final class CrosshairDrawerScreen extends Screen {
    private static final int GRID = CustomCrosshair.GRID_SIZE;
    private static final int CELL = 13;
    private static final int[] PALETTE = {
            0xFFFFFFFF, 0xFFFF6500, 0xFF24F1FF, 0xFFFF3BC8,
            0xFFF4FF27, 0xFF68D8FF, 0xFFFF4655
    };

    private final Screen parent;
    private final CustomCrosshair crosshair = CustomCrosshair.getInstance();
    private int gridX;
    private int gridY;
    private int scrollOffset;
    private static final int CONTENT_TOP = 54;
    private static final int CONTENT_HEIGHT = GRID * CELL + 85;

    public CrosshairDrawerScreen(Screen parent) {
        super(Text.literal("PVPSE Crosshair Drawer"));
        this.parent = parent;
    }

    protected void init() {
        gridX = Math.max(20, width / 2 - GRID * CELL / 2);
        gridY = CONTENT_TOP - scrollOffset;
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        gridY = CONTENT_TOP - scrollOffset;
        Theme theme = ThemeManager.getInstance().getTheme();
        context.fill(0, 0, width, height, theme.background());
        RenderUtil.panel(context, gridX - 12, gridY - 35, GRID * CELL + 24, GRID * CELL + 120, theme);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, gridY - 25, theme.accent());

        for (int y = 0; y < GRID; y++) {
            for (int x = 0; x < GRID; x++) {
                int px = gridX + x * CELL;
                int py = gridY + y * CELL;
                int background = (x + y) % 2 == 0 ? 0xFF20262E : 0xFF181D23;
                context.fill(px, py, px + CELL - 1, py + CELL - 1, background);
                if (crosshair.pixel(x, y)) {
                    int color = crosshair.useThemeColor() ? theme.accent() : crosshair.color();
                    RoundedRenderer.fill(context, px + 2, py + 2, CELL - 5, CELL - 5, 2, color);
                }
            }
        }

        int controlsY = gridY + GRID * CELL + 10;
        drawToggle(context, gridX, controlsY, 92, "USE CUSTOM", crosshair.enabled(), theme, mouseX, mouseY);
        drawToggle(context, gridX + 98, controlsY, 92, "MIRROR X", crosshair.mirrorHorizontal(), theme, mouseX, mouseY);
        drawToggle(context, gridX, controlsY + 24, 92, "MIRROR Y", crosshair.mirrorVertical(), theme, mouseX, mouseY);
        drawButton(context, gridX + 98, controlsY + 24, 44, "CLEAR", theme, mouseX, mouseY);
        drawButton(context, gridX + 146, controlsY + 24, 44, "RESET", theme, mouseX, mouseY);
        drawButton(context, gridX, controlsY + 50, 92, "COPY CODE", theme, mouseX, mouseY);
        drawButton(context, gridX + 98, controlsY + 50, 92, "PASTE CODE", theme, mouseX, mouseY);

        int paletteX = gridX + GRID * CELL + 22;
        context.drawTextWithShadow(textRenderer, Text.literal("COLOR"), paletteX, gridY, theme.text());
        drawColor(context, paletteX, gridY + 18, 0, theme.accent(), crosshair.useThemeColor(), theme);
        for (int i = 0; i < PALETTE.length; i++) {
            drawColor(context, paletteX, gridY + 42 + i * 24, i + 1, PALETTE[i],
                    !crosshair.useThemeColor() && crosshair.color() == PALETTE[i], theme);
        }
        drawButton(context, width - 76, 14, 60, "DONE", theme, mouseX, mouseY);
        drawScrollbar(context, theme);
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Left click draws • right click erases • drag to paint"),
                width / 2, height - 18, theme.mutedText());
        super.render(context, mouseX, mouseY, delta);
    }

    public boolean mouseClicked(Click click, boolean doubled) {
        if (insideGrid(click.x(), click.y())) {
            paint(click.x(), click.y(), click.button() != 1);
            return true;
        }
        int controlsY = gridY + GRID * CELL + 10;
        if (inside(click.x(), click.y(), gridX, controlsY, 92, 20)) crosshair.setEnabled(!crosshair.enabled());
        else if (inside(click.x(), click.y(), gridX + 98, controlsY, 92, 20)) crosshair.setMirrorHorizontal(!crosshair.mirrorHorizontal());
        else if (inside(click.x(), click.y(), gridX, controlsY + 24, 92, 20)) crosshair.setMirrorVertical(!crosshair.mirrorVertical());
        else if (inside(click.x(), click.y(), gridX + 98, controlsY + 24, 44, 20)) crosshair.clear();
        else if (inside(click.x(), click.y(), gridX + 146, controlsY + 24, 44, 20)) crosshair.reset();
        else if (inside(click.x(), click.y(), gridX, controlsY + 50, 92, 20)) {
            if (client != null) client.keyboard.setClipboard(crosshair.exportCode());
            NotificationManager.getInstance().push("Crosshair copied", "Share code copied");
        }
        else if (inside(click.x(), click.y(), gridX + 98, controlsY + 50, 92, 20)) {
            boolean imported = client != null && crosshair.importCode(client.keyboard.getClipboard());
            NotificationManager.getInstance().push(imported ? "Crosshair imported" : "Invalid code",
                    imported ? "Clipboard design loaded" : "Copy a PVPSE1 code first");
        }
        else if (inside(click.x(), click.y(), width - 76, 14, 60, 20)) close();
        else {
            int paletteX = gridX + GRID * CELL + 22;
            if (inside(click.x(), click.y(), paletteX, gridY + 18, 20, 20)) crosshair.setColor(0xFFFFFFFF, true);
            for (int i = 0; i < PALETTE.length; i++) {
                if (inside(click.x(), click.y(), paletteX, gridY + 42 + i * 24, 20, 20)) {
                    crosshair.setColor(PALETTE[i], false);
                    break;
                }
            }
        }
        crosshair.save();
        return super.mouseClicked(click, doubled);
    }

    public boolean mouseDragged(Click click, double deltaX, double deltaY) {
        if (insideGrid(click.x(), click.y())) {
            paint(click.x(), click.y(), click.button() != 1);
            return true;
        }
        return super.mouseDragged(click, deltaX, deltaY);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scrollOffset -= (int) Math.round(verticalAmount * 28.0D);
        scrollOffset = Math.clamp(scrollOffset, 0, maximumScroll());
        gridY = CONTENT_TOP - scrollOffset;
        return true;
    }

    public void close() {
        crosshair.save();
        if (client != null) client.setScreen(parent);
    }

    public boolean shouldPause() { return false; }

    private void paint(double mouseX, double mouseY, boolean active) {
        int x = (int) ((mouseX - gridX) / CELL);
        int y = (int) ((mouseY - gridY) / CELL);
        crosshair.setPixel(x, y, active);
    }

    private boolean insideGrid(double mouseX, double mouseY) {
        return inside(mouseX, mouseY, gridX, gridY, GRID * CELL, GRID * CELL);
    }

    private void drawToggle(DrawContext context, int x, int y, int width, String label, boolean active,
                            Theme theme, int mouseX, int mouseY) {
        int color = active ? theme.accent() : (inside(mouseX, mouseY, x, y, width, 20) ? theme.panelHeader() : theme.panel());
        RoundedRenderer.fill(context, x, y, width, 20, 5, color);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(label), x + width / 2, y + 6,
                active ? 0xFF101216 : theme.text());
    }

    private void drawButton(DrawContext context, int x, int y, int width, String label, Theme theme, int mouseX, int mouseY) {
        RoundedRenderer.fill(context, x, y, width, 20, 5,
                inside(mouseX, mouseY, x, y, width, 20) ? theme.accentSecondary() : theme.panelHeader());
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(label), x + width / 2, y + 6, theme.text());
    }

    private void drawColor(DrawContext context, int x, int y, int index, int color, boolean selected, Theme theme) {
        RoundedRenderer.fill(context, x, y, 20, 20, 5, color);
        if (selected) {
            context.fill(x - 2, y - 2, x + 22, y, theme.text());
            context.fill(x - 2, y + 20, x + 22, y + 22, theme.text());
        }
        if (index == 0) context.drawTextWithShadow(textRenderer, Text.literal("T"), x + 7, y + 6, 0xFF101216);
    }

    private void drawScrollbar(DrawContext context, Theme theme) {
        int maximum = maximumScroll();
        if (maximum <= 0) return;
        int top = 46;
        int bottom = height - 24;
        int available = Math.max(30, bottom - top);
        int fullHeight = available + maximum;
        int thumbHeight = Math.max(20, Math.round(available * (available / (float) fullHeight)));
        int travel = available - thumbHeight;
        int thumbY = top + Math.round(travel * (scrollOffset / (float) maximum));
        context.fill(width - 8, top, width - 4, bottom, 0x66343B45);
        context.fill(width - 9, thumbY, width - 3, thumbY + thumbHeight, theme.accent());
    }

    private int maximumScroll() {
        return Math.max(0, CONTENT_TOP + CONTENT_HEIGHT - (height - 24));
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }
}
