package com.codex.pvphud;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

final class HudConfigScreen extends Screen {
    private static final int CARD_PAD = 12;
    private static final int ROW_H = 22;
    private static final int ROW_GAP = 4;
    private static final int LOGO_H = 44;
    private static final int TAB_H = 24;
    private static final int CONTENT_TOP_PAD = 10;

    // tabs
    private static final String[] TAB_KEYS = {
        "screen.pvp_hud_client.tab.widgets",
        "screen.pvp_hud_client.tab.style",
        "screen.pvp_hud_client.tab.crosshair",
        "screen.pvp_hud_client.tab.cps"
    };
    private int activeTab = 0;
    private int scrollOffset = 0;
    private int maxScroll = 0;

    // open animation
    private float animTick = 0f;
    private static final float ANIM_DURATION = 8f;

    private final HudConfig config;
    private final Screen parent;

    HudConfigScreen(HudConfig config) {
        this(config, null);
    }

    HudConfigScreen(HudConfig config, Screen parent) {
        super(Component.translatable("screen.pvp_hud_client.config"));
        this.config = config;
        this.parent = parent;
    }

    @Override
    protected void init() {
        animTick = 0f;
        scrollOffset = 0;
        buildTab(activeTab);
    }

    private void clearAndBuild() {
        clearWidgets();
        buildTab(activeTab);
    }

    private void buildTab(int tab) {
        int panelX = panelX();
        int panelW = panelW();
        int contentX = panelX + CARD_PAD;
        int contentW = panelW - CARD_PAD * 2;
        int rowX = contentX;
        int rowW = contentW;
        int startY = LOGO_H + TAB_H + CONTENT_TOP_PAD;
        int y = startY - scrollOffset;

        // Tab buttons along the top of the panel
        int tabW = panelW / TAB_KEYS.length;
        for (int i = 0; i < TAB_KEYS.length; i++) {
            final int idx = i;
            int tx = panelX + i * tabW;
            addRenderableWidget(com.codex.pvphud.gui.PvpButton.styled(Component.translatable(TAB_KEYS[i]), b -> {
                activeTab = idx;
                scrollOffset = 0;
                clearAndBuild();
            }).bounds(tx, LOGO_H, tabW, TAB_H).build());
        }

        switch (tab) {
            case 0 -> buildWidgetsTab(rowX, rowW, y);
            case 1 -> buildStyleTab(rowX, rowW, y);
            case 2 -> buildCrosshairTab(rowX, rowW, y);
            case 3 -> buildCpsTab(rowX, rowW, y);
        }

        int contentRows = switch (tab) {
            case 0 -> 5;
            case 1 -> 4;
            case 2 -> 5;
            case 3 -> 2;
            default -> 0;
        };
        int contentHeight = contentRows * (ROW_H + ROW_GAP);
        int visibleHeight = Math.max(0, height - startY - 42);
        maxScroll = Math.max(0, contentHeight - visibleHeight);

        // Bottom buttons
        int btnY = height - 28;
        addRenderableWidget(com.codex.pvphud.gui.PvpButton.styled(Component.translatable("gui.back"), b -> goBack())
                .bounds(panelX, btnY, panelW / 2 - 4, 20).build());
        addRenderableWidget(com.codex.pvphud.gui.PvpButton.styled(Component.translatable("gui.done"), b -> onClose())
                .bounds(panelX + panelW / 2 + 4, btnY, panelW / 2 - 4, 20).build());
    }

    private void buildWidgetsTab(int x, int w, int y) {
        int half = (w - 4) / 2;
        int right = x + half + 4;

        addRow2(x, right, y, half, "screen.pvp_hud_client.hud_enabled", () -> config.enabled, v -> config.enabled = v,
                "screen.pvp_hud_client.compact", () -> config.compact, v -> config.compact = v);
        y += ROW_H + ROW_GAP;
        addRow2(x, right, y, half, "screen.pvp_hud_client.coords", () -> config.showCoords, v -> config.showCoords = v,
                "screen.pvp_hud_client.armor", () -> config.showArmor, v -> config.showArmor = v);
        y += ROW_H + ROW_GAP;
        addRow2(x, right, y, half, "screen.pvp_hud_client.potions", () -> config.showPotions, v -> config.showPotions = v,
                "screen.pvp_hud_client.target", () -> config.showTarget, v -> config.showTarget = v);
        y += ROW_H + ROW_GAP;
        addRow2(x, right, y, half, "screen.pvp_hud_client.health_bar", () -> config.showHealthBar, v -> config.showHealthBar = v,
                "screen.pvp_hud_client.durability_bars", () -> config.showDurabilityBars, v -> config.showDurabilityBars = v);
        y += ROW_H + ROW_GAP;
        addRow2(x, right, y, half, "screen.pvp_hud_client.free_layout", () -> config.useFreeLayout, v -> config.useFreeLayout = v,
                "screen.pvp_hud_client.glow", () -> config.glowEffects, v -> config.glowEffects = v);
    }

    private void buildStyleTab(int x, int w, int y) {
        addRenderableWidget(CycleButton.builder(HudConfigScreen::themeLabel, config.theme)
                .withValues(HudConfig.Theme.values())
                .create(x, y, w, ROW_H, Component.translatable("screen.pvp_hud_client.theme"), (b, theme) -> {
                    config.setTheme(theme);
                }));
        y += ROW_H + ROW_GAP;
        addRenderableWidget(toggle(x, y, w, "screen.pvp_hud_client.crosshair", () -> config.showCrosshair, v -> config.showCrosshair = v));
        y += ROW_H + ROW_GAP;
        addRenderableWidget(new OpacitySlider(x, y, w, ROW_H, config));
        y += ROW_H + ROW_GAP;
        addRenderableWidget(toggle(x, y, w, "screen.pvp_hud_client.animated_themes", () -> config.animatedThemes, v -> config.animatedThemes = v));
    }

    private void buildCrosshairTab(int x, int w, int y) {
        addRenderableWidget(CycleButton.builder(HudConfigScreen::crosshairLabel, config.crosshairStyle)
                .withValues(HudConfig.CrosshairStyle.values())
                .create(x, y, w, ROW_H, Component.translatable("screen.pvp_hud_client.crosshair_style"), (b, style) -> {
                    config.crosshairStyle = style;
                    config.save();
                }));
        y += ROW_H + ROW_GAP;
        addRenderableWidget(new CrosshairSizeSlider(x, y, w, ROW_H, config));
        y += ROW_H + ROW_GAP;
        addRenderableWidget(toggle(x, y, w, "screen.pvp_hud_client.hit_marker", () -> config.showHitMarker, v -> config.showHitMarker = v));
        y += ROW_H + ROW_GAP;
        addRenderableWidget(toggle(x, y, w, "screen.pvp_hud_client.damage_flash", () -> config.showDamageFlash, v -> config.showDamageFlash = v));
        y += ROW_H + ROW_GAP;
        addRenderableWidget(toggle(x, y, w, "screen.pvp_hud_client.kill_particles", () -> config.showKillParticles, v -> config.showKillParticles = v));
    }

    private void buildCpsTab(int x, int w, int y) {
        int half = (w - 4) / 2;
        int right = x + half + 4;
        addRow2(x, right, y, half, "screen.pvp_hud_client.show_cps", () -> config.showCps, v -> config.showCps = v,
                "screen.pvp_hud_client.show_right_cps", () -> config.showRightCps, v -> config.showRightCps = v);
        y += ROW_H + ROW_GAP;
        addRow2(x, right, y, half, "screen.pvp_hud_client.show_sprint", () -> config.showSprint, v -> config.showSprint = v,
                "screen.pvp_hud_client.show_reach", () -> config.showReach, v -> config.showReach = v);
    }

    // helpers
    private void addRow2(int x1, int x2, int y, int w,
                         String key1, BooleanSupplier g1, BooleanConsumer s1,
                         String key2, BooleanSupplier g2, BooleanConsumer s2) {
        addRenderableWidget(toggle(x1, y, w, key1, g1, s1));
        addRenderableWidget(toggle(x2, y, w, key2, g2, s2));
    }

    private Button toggle(int x, int y, int w, String key, BooleanSupplier getter, BooleanConsumer setter) {
        return com.codex.pvphud.gui.PvpButton.styled(stateText(key, getter.get()), b -> {
            setter.accept(!getter.get());
            config.save();
            b.setMessage(stateText(key, getter.get()));
        }).bounds(x, y, w, ROW_H).build();
    }

    // ── animation & render ──────────────────────────────────────────────────

    @Override
    public void tick() {
        if (animTick < ANIM_DURATION) animTick++;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        float progress = Math.min(1.0f, (animTick + delta) / ANIM_DURATION);
        float ease = 1f - (1f - progress) * (1f - progress); // ease-out quad

        int panelX = panelX();
        int panelW = panelW();

        // Animated slide-down: content starts above the screen
        int slideOffset = (int) ((1f - ease) * -60);

        context.fill(0, 0, width, height, 0xBB000000);

        context.pose().pushMatrix();
        context.pose().translate(0, slideOffset);

        // ── PVPSE logo header ──
        drawLogo(context, panelX, panelW, ease);

        // ── card background for content area ──
        int contentTop = LOGO_H + TAB_H;
        int contentBot = height - 34;
        HudPanel.drawWidgetFrame(context, panelX, contentTop, panelW, contentBot - contentTop, config, false);

        // ── crosshair preview on crosshair tab ──
        if (activeTab == 2) {
            int previewCx = panelX + panelW - 60;
            int previewCy = contentTop + 60;
            context.fill(previewCx - 28, previewCy - 28, previewCx + 28, previewCy + 28, 0xFF0A0C10);
            context.fill(previewCx - 28, previewCy - 28, previewCx + 28, previewCy - 27, config.accentColor());
            context.centeredText(font,
                Component.translatable("screen.pvp_hud_client.preview"),
                previewCx, previewCy - 38, config.mutedColor());
            CrosshairRenderer.draw(context, previewCx, previewCy, config, false);
        }

        super.extractRenderState(context, mouseX, mouseY, delta);
        context.pose().popMatrix();
    }

    private void drawLogo(GuiGraphicsExtractor context, int panelX, int panelW, float ease) {
        context.fill(panelX, 0, panelX + panelW, LOGO_H, 0xFF0A0C12);
        int lineW = (int) (panelW * ease);
        context.fill(panelX, 0, panelX + lineW, 2, config.accentColor());

        context.pose().pushMatrix();
        context.pose().translate(panelX + panelW / 2.0F, 7);
        context.pose().scale(1.65F, 1.65F);
        context.centeredText(font, Component.literal("PVPSE"), 0, 0, config.accentColor());
        context.pose().popMatrix();

        context.centeredText(font,
            Component.literal("PVP CLIENT  •  1.21"), panelX + panelW / 2, LOGO_H - 12, config.mutedColor());
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int old = scrollOffset;
        scrollOffset = Mth.clamp(scrollOffset - (int) Math.round(verticalAmount * 18.0D), 0, maxScroll);
        if (old != scrollOffset) clearAndBuild();
        return old != scrollOffset || super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    private int panelX() {
        return Math.max(10, (width - 320) / 2);
    }

    private int panelW() {
        return Math.min(320, width - 20);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void goBack() {
        config.save();
        if (minecraft != null) minecraft.setScreen(parent);
    }

    @Override
    public void onClose() {
        config.save();
        if (minecraft != null) minecraft.setScreen(parent != null ? parent : null);
    }

    private static Component stateText(String key, boolean on) {
        return Component.translatable(key).append(": ")
                .append(Component.translatable(on ? "options.on" : "options.off"));
    }

    private static Component themeLabel(HudConfig.Theme theme) {
        return Component.translatable("screen.pvp_hud_client.theme." + theme.name().toLowerCase());
    }

    private static Component crosshairLabel(HudConfig.CrosshairStyle style) {
        return Component.translatable("screen.pvp_hud_client.crosshair." + style.name().toLowerCase());
    }

    @FunctionalInterface
    private interface BooleanSupplier { boolean get(); }

    @FunctionalInterface
    private interface BooleanConsumer { void accept(boolean value); }

    // ── sliders ──────────────────────────────────────────────────────────────

    private static final class CrosshairSizeSlider extends AbstractSliderButton {
        private final HudConfig config;
        CrosshairSizeSlider(int x, int y, int w, int h, HudConfig config) {
            super(x, y, w, h, Component.empty(), sizeToValue(config.crosshairSize));
            this.config = config;
            updateMessage();
        }
        @Override protected void updateMessage() {
            setMessage(Component.translatable("screen.pvp_hud_client.crosshair_size", config.crosshairSize));
        }
        @Override protected void applyValue() {
            config.crosshairSize = Mth.clamp(
                HudConfig.MIN_CROSSHAIR_SIZE + Mth.floor(value * (HudConfig.MAX_CROSSHAIR_SIZE - HudConfig.MIN_CROSSHAIR_SIZE)),
                HudConfig.MIN_CROSSHAIR_SIZE, HudConfig.MAX_CROSSHAIR_SIZE);
            config.save(); updateMessage();
        }
        private static double sizeToValue(int s) {
            return (s - HudConfig.MIN_CROSSHAIR_SIZE) / (double)(HudConfig.MAX_CROSSHAIR_SIZE - HudConfig.MIN_CROSSHAIR_SIZE);
        }
    }

    private static final class OpacitySlider extends AbstractSliderButton {
        private final HudConfig config;
        OpacitySlider(int x, int y, int w, int h, HudConfig config) {
            super(x, y, w, h, Component.empty(), opacityToValue(config.panelOpacity));
            this.config = config;
            updateMessage();
        }
        @Override protected void updateMessage() {
            setMessage(Component.translatable("screen.pvp_hud_client.opacity", config.panelOpacity));
        }
        @Override protected void applyValue() {
            config.panelOpacity = Mth.clamp(
                HudConfig.MIN_OPACITY + Mth.floor(value * (HudConfig.MAX_OPACITY - HudConfig.MIN_OPACITY)),
                HudConfig.MIN_OPACITY, HudConfig.MAX_OPACITY);
            config.save(); updateMessage();
        }
        private static double opacityToValue(int o) {
            return (o - HudConfig.MIN_OPACITY) / (double)(HudConfig.MAX_OPACITY - HudConfig.MIN_OPACITY);
        }
    }
}
