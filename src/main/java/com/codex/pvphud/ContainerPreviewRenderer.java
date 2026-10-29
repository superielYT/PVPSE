package com.codex.pvphud;

import com.codex.pvphud.render.RenderUtil;
import com.codex.pvphud.theme.ThemeManager;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

public final class ContainerPreviewRenderer implements ClientTooltipComponent {
    private final ContainerPreview data;
    public ContainerPreviewRenderer(ContainerPreview data) { this.data = data; }
    private int rows() { return data.bundle() ? Math.max(1, (data.items().size() + 8) / 9) : 3; }
    public int getWidth(Font font) { return 170; }
    public int getHeight(Font font) { return 21 + rows() * 18 + (data.bundle() ? 7 : 0); }
    public void extractImage(Font font, int x, int y, int width, int height, GuiGraphicsExtractor c) {
        var theme = ThemeManager.getInstance().getTheme();
        RenderUtil.panel(c, x, y, getWidth(font), getHeight(font) - 3, theme, theme.panel(), true, false);
        long occupied = data.items().stream().filter(s -> !s.isEmpty()).count();
        String title = data.bundle() ? "BUNDLE  " + occupied + " stacks / " + Math.round(data.fullness() * 100) + "%"
                : "SHULKER  " + occupied + " / 27 slots";
        c.text(font, title, x + 5, y + 5, theme.mutedText(), false);
        for (int i = 0; i < rows() * 9; i++) {
            int sx = x + 4 + i % 9 * 18, sy = y + 17 + i / 9 * 18;
            c.fill(sx, sy, sx + 17, sy + 17, data.bundle() && i == data.selected() ? 0x886B482D : 0x36000000);
            if (i < data.items().size() && !data.items().get(i).isEmpty()) {
                var stack = data.items().get(i);
                c.item(stack, sx, sy);
                c.itemDecorations(font, stack, sx, sy);
            }
        }
        if (occupied == 0) c.text(font, "Empty", x + 9, y + 21, theme.mutedText(), true);
        if (data.bundle()) {
            int by = y + 18 + rows() * 18;
            c.fill(x + 5, by, x + 165, by + 3, 0x55000000);
            c.fill(x + 5, by, x + 5 + Math.round(160 * Math.clamp(data.fullness(), 0, 1)), by + 3, theme.accent());
        }
    }
}
