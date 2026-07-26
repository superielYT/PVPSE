package com.codex.pvphud;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

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
        super(Text.translatable("screen.pvp_hud_client.config"));
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
        clearChildren();
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
            addDrawableChild(ButtonWidget.builder(Text.translatable(TAB_KEYS[i]), b -> {
                activeTab = idx;
                scrollOffset = 0;
                clearAndBuild();
            }).dimensions(tx, LOGO_H, tabW, TAB_H).build());
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
        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.back"), b -> goBack())
                .dimensions(panelX, btnY, panelW / 2 - 4, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.done"), b -> close())
                .dimensions(panelX + panelW / 2 + 4, btnY, panelW / 2 - 4, 20).build());
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
        addDrawableChild(CyclingButtonWidget.builder(HudConfigScreen::themeLabel, config.theme)
                .values(HudConfig.Theme.values())
                .build(x, y, w, ROW_H, Text.translatable("screen.pvp_hud_client.theme"), (b, theme) -> {
                    config.setTheme(theme);
                }));
        y += ROW_H + ROW_GAP;
        addDrawableChild(toggle(x, y, w, "screen.pvp_hud_client.crosshair", () -> config.showCrosshair, v -> config.showCrosshair = v));
        y += ROW_H + ROW_GAP;
        addDrawableChild(new OpacitySlider(x, y, w, ROW_H, config));
        y += ROW_H + ROW_GAP;
        addDrawableChild(toggle(x, y, w, "screen.pvp_hud_client.animated_themes", () -> config.animatedThemes, v -> config.animatedThemes = v));
    }

    private void buildCrosshairTab(int x, int w, int y) {
        addDrawableChild(CyclingButtonWidget.builder(HudConfigScreen::crosshairLabel, config.crosshairStyle)
                .values(HudConfig.CrosshairStyle.values())
                .build(x, y, w, ROW_H, Text.translatable("screen.pvp_hud_client.crosshair_style"), (b, style) -> {
                    config.crosshairStyle = style;
                    config.save();
                }));
        y += ROW_H + ROW_GAP;
        addDrawableChild(new CrosshairSizeSlider(x, y, w, ROW_H, config));
        y += ROW_H + ROW_GAP;
        addDrawableChild(toggle(x, y, w, "screen.pvp_hud_client.hit_marker", () -> config.showHitMarker, v -> config.showHitMarker = v));
        y += ROW_H + ROW_GAP;
        addDrawableChild(toggle(x, y, w, "screen.pvp_hud_client.damage_flash", () -> config.showDamageFlash, v -> config.showDamageFlash = v));
        y += ROW_H + ROW_GAP;
        addDrawableChild(toggle(x, y, w, "screen.pvp_hud_client.kill_particles", () -> config.showKillParticles, v -> config.showKillParticles = v));
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
        addDrawableChild(toggle(x1, y, w, key1, g1, s1));
        addDrawableChild(toggle(x2, y, w, key2, g2, s2));
    }

    private ButtonWidget toggle(int x, int y, int w, String key, BooleanSupplier getter, BooleanConsumer setter) {
        return ButtonWidget.builder(stateText(key, getter.get()), b -> {
            setter.accept(!getter.get());
            config.save();
            b.setMessage(stateText(key, getter.get()));
        }).dimensions(x, y, w, ROW_H).build();
    }

    // ── animation & render ──────────────────────────────────────────────────

    @Override
    public void tick() {
        if (animTick < ANIM_DURATION) animTick++;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        float progress = Math.min(1.0f, (animTick + delta) / ANIM_DURATION);
        float ease = 1f - (1f - progress) * (1f - progress); // ease-out quad

        int panelX = panelX();
        int panelW = panelW();

        // Animated slide-down: content starts above the screen
        int slideOffset = (int) ((1f - ease) * -60);

        context.fill(0, 0, width, height, 0xBB000000);

        context.getMatrices().pushMatrix();
        context.getMatrices().translate(0, slideOffset);

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
            context.drawCenteredTextWithShadow(textRenderer,
                Text.translatable("screen.pvp_hud_client.preview"),
                previewCx, previewCy - 38, config.mutedColor());
            CrosshairRenderer.draw(context, previewCx, previewCy, config, false);
        }

        super.render(context, mouseX, mouseY, delta);
        context.getMatrices().popMatrix();
    }

    private void drawLogo(DrawContext context, int panelX, int panelW, float ease) {
        context.fill(panelX, 0, panelX + panelW, LOGO_H, 0xFF0A0C12);
        int lineW = (int) (panelW * ease);
        context.fill(panelX, 0, panelX + lineW, 2, config.accentColor());

        context.getMatrices().pushMatrix();
        context.getMatrices().translate(panelX + panelW / 2.0F, 7);
        context.getMatrices().scale(1.65F, 1.65F);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("PVPSE"), 0, 0, config.accentColor());
        context.getMatrices().popMatrix();

        context.drawCenteredTextWithShadow(textRenderer,
            Text.literal("PVP CLIENT  •  1.21"), panelX + panelW / 2, LOGO_H - 12, config.mutedColor());
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int old = scrollOffset;
        scrollOffset = MathHelper.clamp(scrollOffset - (int) Math.round(verticalAmount * 18.0D), 0, maxScroll);
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
    public boolean shouldPause() {
        return false;
    }

    private void goBack() {
        config.save();
        if (client != null) client.setScreen(parent);
    }

    @Override
    public void close() {
        config.save();
        if (client != null) client.setScreen(parent != null ? parent : null);
    }

    private static Text stateText(String key, boolean on) {
        return Text.translatable(key).append(": ")
                .append(Text.translatable(on ? "options.on" : "options.off"));
    }

    private static Text themeLabel(HudConfig.Theme theme) {
        return Text.translatable("screen.pvp_hud_client.theme." + theme.name().toLowerCase());
    }

    private static Text crosshairLabel(HudConfig.CrosshairStyle style) {
        return Text.translatable("screen.pvp_hud_client.crosshair." + style.name().toLowerCase());
    }

    @FunctionalInterface
    private interface BooleanSupplier { boolean get(); }

    @FunctionalInterface
    private interface BooleanConsumer { void accept(boolean value); }

    // ── sliders ──────────────────────────────────────────────────────────────

    private static final class CrosshairSizeSlider extends SliderWidget {
        private final HudConfig config;
        CrosshairSizeSlider(int x, int y, int w, int h, HudConfig config) {
            super(x, y, w, h, Text.empty(), sizeToValue(config.crosshairSize));
            this.config = config;
            updateMessage();
        }
        @Override protected void updateMessage() {
            setMessage(Text.translatable("screen.pvp_hud_client.crosshair_size", config.crosshairSize));
        }
        @Override protected void applyValue() {
            config.crosshairSize = MathHelper.clamp(
                HudConfig.MIN_CROSSHAIR_SIZE + MathHelper.floor(value * (HudConfig.MAX_CROSSHAIR_SIZE - HudConfig.MIN_CROSSHAIR_SIZE)),
                HudConfig.MIN_CROSSHAIR_SIZE, HudConfig.MAX_CROSSHAIR_SIZE);
            config.save(); updateMessage();
        }
        private static double sizeToValue(int s) {
            return (s - HudConfig.MIN_CROSSHAIR_SIZE) / (double)(HudConfig.MAX_CROSSHAIR_SIZE - HudConfig.MIN_CROSSHAIR_SIZE);
        }
    }

    private static final class OpacitySlider extends SliderWidget {
        private final HudConfig config;
        OpacitySlider(int x, int y, int w, int h, HudConfig config) {
            super(x, y, w, h, Text.empty(), opacityToValue(config.panelOpacity));
            this.config = config;
            updateMessage();
        }
        @Override protected void updateMessage() {
            setMessage(Text.translatable("screen.pvp_hud_client.opacity", config.panelOpacity));
        }
        @Override protected void applyValue() {
            config.panelOpacity = MathHelper.clamp(
                HudConfig.MIN_OPACITY + MathHelper.floor(value * (HudConfig.MAX_OPACITY - HudConfig.MIN_OPACITY)),
                HudConfig.MIN_OPACITY, HudConfig.MAX_OPACITY);
            config.save(); updateMessage();
        }
        private static double opacityToValue(int o) {
            return (o - HudConfig.MIN_OPACITY) / (double)(HudConfig.MAX_OPACITY - HudConfig.MIN_OPACITY);
        }
    }
}
