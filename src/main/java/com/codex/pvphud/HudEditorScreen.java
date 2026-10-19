package com.codex.pvphud;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

final class HudEditorScreen extends Screen {
    private final HudConfig config;
    private final HudRenderer renderer = new HudRenderer();
    private final CpsTracker cps;

    private HudWidgetId dragging;
    private int dragOffsetX;
    private int dragOffsetY;

    HudEditorScreen(HudConfig config, CpsTracker cps) {
        super(Component.translatable("screen.pvp_hud_client.editor"));
        this.config = config;
        this.cps = cps;
    }

    @Override
    protected void init() {
        int barY = height - 28;
        addRenderableWidget(Button.builder(Component.translatable("screen.pvp_hud_client.settings"), button -> {
            if (minecraft != null) {
                minecraft.setScreen(new HudConfigScreen(config, this));
            }
        }).bounds(12, barY, 90, 20).build());

        addRenderableWidget(Button.builder(Component.translatable("screen.pvp_hud_client.reset_layout"), button -> {
            config.resetAllWidgets();
        }).bounds(108, barY, 100, 20).build());

        addRenderableWidget(Button.builder(Component.translatable("screen.pvp_hud_client.show_all"), button -> {
            for (HudWidgetId id : HudWidgetId.values()) {
                config.layout(id).visible = true;
            }
            config.save();
        }).bounds(214, barY, 90, 20).build());

        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(width - 102, barY, 90, 20).build());

        super.init();
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0x55000000);
        context.drawCenteredString(font, title, width / 2, 8, config.textColor());
        context.drawCenteredString(font, Component.translatable("screen.pvp_hud_client.editor_hint"),
                width / 2, 20, config.mutedColor());

        if (minecraft != null) {
            renderer.render(context, minecraft, config, cps, true);
            for (WidgetBounds bounds : renderer.lastBounds()) {
                boolean isHovered = bounds.contains(mouseX, mouseY);
                HudPanel.drawEditorOutline(context, bounds.x(), bounds.y(), bounds.width(), bounds.height(), config, isHovered || bounds.id() == dragging);
                HudPanel.drawCloseButton(context, bounds.closeX(), bounds.closeY(), bounds.closeContains(mouseX, mouseY));
            }
        }

        context.fill(0, height - 36, width, height, 0xCC0A0C10);
        context.fill(0, height - 36, width, height - 35, config.accentColor());
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (minecraft == null) {
            return super.mouseClicked(click, doubled);
        }

        double mouseX = click.x();
        double mouseY = click.y();

        for (int i = renderer.lastBounds().size() - 1; i >= 0; i--) {
            WidgetBounds bounds = renderer.lastBounds().get(i);
            if (bounds.closeContains(mouseX, mouseY)) {
                config.layout(bounds.id()).visible = false;
                config.save();
                return true;
            }
            if (bounds.titleContains(mouseX, mouseY)) {
                dragging = bounds.id();
                dragOffsetX = (int) mouseX - bounds.x();
                dragOffsetY = (int) mouseY - bounds.y();
                return true;
            }
        }

        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
        if (dragging != null) {
            dragging = null;
            config.save();
            return true;
        }
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent click, double deltaX, double deltaY) {
        if (dragging != null) {
            WidgetLayout layout = config.layout(dragging);
            int newX = (int) click.x() - dragOffsetX;
            int newY = (int) click.y() - dragOffsetY;
            layout.x = HudRenderer.toPercentX(newX, width);
            layout.y = HudRenderer.toPercentY(newY, height);
            return true;
        }
        return super.mouseDragged(click, deltaX, deltaY);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        config.save();
        if (minecraft != null) {
            minecraft.setScreen(null);
        }
    }
}
