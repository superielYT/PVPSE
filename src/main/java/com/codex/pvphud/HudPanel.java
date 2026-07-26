package com.codex.pvphud;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.List;

final class HudPanel {
    private static final int GAP = 5;

    private HudPanel() {
    }

    static int draw(DrawContext context, TextRenderer text, int x, int y, Text title, List<Line> lines, HudConfig config, boolean editorOverlay) {
        if (lines.isEmpty()) {
            return 0;
        }

        int[] size = measurePanel(text, title, lines, config.compact);
        drawCoolFrame(context, x, y, size[0], size[1], config, editorOverlay);
        drawTitle(context, text, x, y, title, size[0], config, editorOverlay);
        drawLines(context, text, x, y, lines, size[0], config);
        return size[1] + GAP;
    }

    static int drawChips(DrawContext context, TextRenderer text, int x, int y, Text title, List<Chip> chips, HudConfig config, boolean editorOverlay) {
        if (chips.isEmpty()) {
            return 0;
        }

        int[] size = measureChipPanel(text, title, chips, config.compact);
        drawCoolFrame(context, x, y, size[0], size[1], config, editorOverlay);
        drawTitle(context, text, x, y, title, size[0], config, editorOverlay);
        drawChipsContent(context, text, x, y, chips, size[0], config);
        return size[1] + GAP;
    }

    static int panelWidth(TextRenderer text, Text title, List<Line> lines, boolean compact) {
        return measurePanel(text, title, lines, compact)[0];
    }

    static int chipPanelWidth(TextRenderer text, Text title, List<Chip> chips, boolean compact) {
        return measureChipPanel(text, title, chips, compact)[0];
    }

    static int panelHeight(TextRenderer text, Text title, List<Line> lines, boolean compact) {
        return measurePanel(text, title, lines, compact)[1];
    }

    static int chipPanelHeight(TextRenderer text, Text title, List<Chip> chips, boolean compact) {
        return measureChipPanel(text, title, chips, compact)[1];
    }

    static void drawProgressBar(DrawContext context, int x, int y, int width, int height, float percent, HudConfig config, boolean danger) {
        int fill = Math.min(width, Math.max(0, Math.round(width * percent)));
        context.fill(x, y, x + width, y + height, 0x66000000);
        int color = danger ? config.dangerColor() : lerpColor(config.accentColor(), config.dangerColor(), 1.0F - percent);
        if (config.glowEffects && percent < 0.25F) {
            context.fill(x - 1, y - 1, x + fill + 1, y + height + 1, withAlpha(config.dangerColor(), 0x44));
        }
        context.fill(x, y, x + fill, y + height, color);
        context.fill(x, y, x + fill, y + 1, 0x88FFFFFF);
    }

    static void drawCloseButton(DrawContext context, int x, int y, boolean hovered) {
        int bg = hovered ? 0xFFFF4757 : 0xAAFF4757;
        context.fill(x, y, x + 8, y + 8, bg);
        context.fill(x + 2, y + 3, x + 6, y + 4, 0xFFFFFFFF);
    }

    static void drawEditorOutline(DrawContext context, int x, int y, int width, int height, HudConfig config, boolean hovered) {
        int color = hovered ? config.accentColor() : withAlpha(config.accentColor(), 0x88);
        context.fill(x - 1, y - 1, x + width + 1, y, color);
        context.fill(x - 1, y + height, x + width + 1, y + height + 1, color);
        context.fill(x - 1, y, x, y + height, color);
        context.fill(x + width, y, x + width + 1, y + height, color);
    }

    static void drawWidgetFrame(DrawContext context, int x, int y, int width, int height, HudConfig config, boolean editorOverlay) {
        drawCoolFrame(context, x, y, width, height, config, editorOverlay);
    }

    private static void drawCoolFrame(DrawContext context, int x, int y, int width, int height, HudConfig config, boolean editorOverlay) {
        int background = config.backgroundColor();
        context.fill(x + 3, y + 3, x + width + 3, y + height + 3, 0x44000000);
        context.fill(x, y, x + width, y + height, background);
        context.fill(x, y, x + width, y + 2, config.accentColor());
        context.fill(x, y + 2, x + width, y + 3, withAlpha(config.accentColor(), 0x55));
        context.fill(x, y + height - 1, x + width, y + height, 0x55000000);
        context.fill(x, y, x + 1, y + height, withAlpha(config.accentColor(), 0x33));
        context.fill(x + width - 1, y, x + width, y + height, 0x22000000);
        if (config.glowEffects) {
            context.fill(x + 2, y + 2, x + 5, y + 5, withAlpha(config.glowColor(), editorOverlay ? 0xAA : 0x66));
        }
    }

    private static void drawTitle(DrawContext context, TextRenderer text, int x, int y, Text title, int width, HudConfig config, boolean editorOverlay) {
        context.drawText(text, title, x + 6, y + 4, config.accentColor(), false);
        if (editorOverlay) {
            drawCloseButton(context, x + width - 10, y + 3, false);
        }
        context.fill(x + 6, y + 14, x + width - 6, y + 15, 0x33FFFFFF);
    }

    private static void drawLines(DrawContext context, TextRenderer text, int x, int y, List<Line> lines, int width, HudConfig config) {
        int pad = config.compact ? 5 : 6;
        int lineHeight = config.compact ? text.fontHeight + 2 : text.fontHeight + 3;
        int drawY = y + 17;
        for (Line line : lines) {
            context.drawText(text, line.text(), x + pad, drawY, line.color(config), false);
            drawY += lineHeight;
        }
    }

    private static void drawChipsContent(DrawContext context, TextRenderer text, int x, int y, List<Chip> chips, int width, HudConfig config) {
        int pad = config.compact ? 5 : 6;
        int chipPadX = 5;
        int chipPadY = 3;
        int chipGap = 4;
        int chipHeight = text.fontHeight + chipPadY * 2;
        int titleHeight = 17;
        int chipX = x + pad;
        int chipY = y + titleHeight;
        int rowWidth = 0;

        for (Chip chip : chips) {
            int labelWidth = text.getWidth(chip.label());
            int valueWidth = text.getWidth(chip.value());
            int chipWidth = labelWidth + valueWidth + chipPadX * 2 + 4;

            if (rowWidth > 0 && rowWidth + chipGap + chipWidth > width - pad * 2) {
                chipX = x + pad;
                chipY += chipHeight + chipGap;
                rowWidth = 0;
            }

            int bg = chip.danger() ? 0x55FF647C : 0x44000000;
            context.fill(chipX, chipY, chipX + chipWidth, chipY + chipHeight, bg);
            context.fill(chipX, chipY, chipX + chipWidth, chipY + 1, chip.danger() ? config.dangerColor() : config.accentColor());
            context.drawText(text, chip.label(), chipX + chipPadX, chipY + chipPadY, config.mutedColor(), false);
            context.drawText(text, chip.value(), chipX + chipPadX + labelWidth + 4, chipY + chipPadY, chip.danger() ? config.dangerColor() : config.textColor(), false);

            chipX += chipWidth + chipGap;
            rowWidth += chipWidth + chipGap;
        }
    }

    private static int[] measurePanel(TextRenderer text, Text title, List<Line> lines, boolean compact) {
        int pad = compact ? 5 : 6;
        int lineHeight = compact ? text.fontHeight + 2 : text.fontHeight + 3;
        int titleHeight = 17;
        int contentWidth = lines.stream().mapToInt(line -> text.getWidth(line.text())).max().orElse(40);
        int titleWidth = text.getWidth(title) + 14;
        int width = Math.max(contentWidth, titleWidth) + pad * 2;
        int height = titleHeight + lines.size() * lineHeight + pad;
        return new int[]{width, height};
    }

    private static int[] measureChipPanel(TextRenderer text, Text title, List<Chip> chips, boolean compact) {
        int pad = compact ? 5 : 6;
        int chipPadX = 5;
        int chipGap = 4;
        int chipHeight = text.fontHeight + 6;
        int titleHeight = 17;
        int rowWidth = 0;
        int rowCount = 1;
        int maxRowWidth = 0;

        for (Chip chip : chips) {
            int chipWidth = text.getWidth(chip.label()) + text.getWidth(chip.value()) + chipPadX * 2 + 4;
            if (rowWidth > 0 && rowWidth + chipGap + chipWidth > 180) {
                maxRowWidth = Math.max(maxRowWidth, rowWidth);
                rowWidth = chipWidth;
                rowCount++;
            } else {
                rowWidth = rowWidth == 0 ? chipWidth : rowWidth + chipGap + chipWidth;
            }
        }
        maxRowWidth = Math.max(maxRowWidth, rowWidth);
        int width = Math.max(maxRowWidth, text.getWidth(title) + 14) + pad * 2;
        int height = titleHeight + rowCount * (chipHeight + chipGap) - chipGap + pad;
        return new int[]{width, height};
    }

    static int lerpColor(int from, int to, float t) {
        t = Math.max(0.0F, Math.min(1.0F, t));
        int fa = (from >> 24) & 0xFF;
        int fr = (from >> 16) & 0xFF;
        int fg = (from >> 8) & 0xFF;
        int fb = from & 0xFF;
        int ta = (to >> 24) & 0xFF;
        int tr = (to >> 16) & 0xFF;
        int tg = (to >> 8) & 0xFF;
        int tb = to & 0xFF;
        int a = fa + Math.round((ta - fa) * t);
        int r = fr + Math.round((tr - fr) * t);
        int g = fg + Math.round((tg - fg) * t);
        int b = fb + Math.round((tb - fb) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    static int withAlpha(int color, int alpha) {
        return (alpha << 24) | (color & 0xFFFFFF);
    }

    record Line(Text text, Style style) {
        static Line normal(Text text) {
            return new Line(text, Style.NORMAL);
        }

        static Line muted(Text text) {
            return new Line(text, Style.MUTED);
        }

        static Line danger(Text text) {
            return new Line(text, Style.DANGER);
        }

        int color(HudConfig config) {
            return switch (style) {
                case NORMAL -> config.textColor();
                case MUTED -> config.mutedColor();
                case DANGER -> config.dangerColor();
            };
        }

        enum Style {
            NORMAL, MUTED, DANGER
        }
    }

    record Chip(Text label, Text value, boolean danger) {
        static Chip of(Text label, Text value) {
            return new Chip(label, value, false);
        }

        static Chip danger(Text label, Text value) {
            return new Chip(label, value, true);
        }

    }
}
