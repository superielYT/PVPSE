package com.codex.pvphud;

import com.codex.pvphud.config.ConfigManager;
import com.codex.pvphud.hud.HudManager;
import com.codex.pvphud.theme.ThemeManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

final class ClientSettingsScreen extends Screen {
    private final Screen parent;
    private final HudConfig config;
    private final ConfigManager profiles;
    private final HudManager hud;
    private TextFieldWidget profileName;
    private String status = "";
    ClientSettingsScreen(Screen parent, HudConfig config, ConfigManager profiles, HudManager hud) {
        super(Text.literal("CLIENT SETTINGS")); this.parent = parent; this.config = config; this.profiles = profiles; this.hud = hud;
    }
    private int x() { return (width - w()) / 2; }
    private int w() { return Math.min(360, width - 20); }
    protected void init() {
        int half = (w() - 4) / 2;
        toggle(x(), 34, half, "HUD", () -> config.enabled, v -> config.enabled = v);
        toggle(x() + half + 4, 34, half, "Notifications", () -> config.notifications, v -> config.notifications = v);
        toggle(x(), 58, half, "Hide on F3", () -> config.hideOnDebug, v -> config.hideOnDebug = v);
        toggle(x() + half + 4, 58, half, "No damage tilt", () -> config.suppressDamageTilt, v -> config.suppressDamageTilt = v);
        toggle(x(), 82, half, "Hit marker", () -> config.showHitMarker, v -> config.showHitMarker = v);
        toggle(x() + half + 4, 82, half, "Damage flash", () -> config.showDamageFlash, v -> config.showDamageFlash = v);
        toggle(x(), 106, half, "Kill particles", () -> config.showKillParticles, v -> config.showKillParticles = v);
        toggle(x() + half + 4, 106, half, "Crosshair", () -> config.showCrosshair, v -> config.showCrosshair = v);
        addDrawableChild(ButtonWidget.builder(Text.literal("Profile: " + profiles.activeProfile() + " >"), b -> {
            var names = profiles.profiles(); int next = (names.indexOf(profiles.activeProfile()) + 1) % names.size();
            status = profiles.switchProfile(names.get(next), hud) ? "Profile loaded" : "Could not load profile";
            clearAndInit();
        }).dimensions(x(), 136, w(), 20).build());
        profileName = addDrawableChild(new TextFieldWidget(textRenderer, x(), 160, half, 20, Text.literal("New profile name")));
        profileName.setPlaceholder(Text.literal("New profile name...")); profileName.setMaxLength(32);
        addDrawableChild(ButtonWidget.builder(Text.literal("COPY TO NEW"), b -> {
            status = profiles.createProfile(profileName.getText(), hud) ? "Copied current HUD and theme" : "Use a new name (letters/numbers)";
            clearAndInit();
        }).dimensions(x() + half + 4, 160, half, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Reset HUD positions & scales"), b -> client.setScreen(new ConfirmScreen(yes -> {
            if (yes) { hud.elements().forEach(e -> e.resetLayout()); profiles.save(hud); status = "HUD layout restored"; } client.setScreen(this);
        }, Text.literal("Reset HUD layout?"), Text.literal("Your module switches and other profiles are kept.")))).dimensions(x(), 184, w(), 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> close()).dimensions(x(), height - 26, w(), 20).build());
    }
    private void toggle(int x, int y, int width, String label, BooleanSupplier get, Consumer<Boolean> set) {
        addDrawableChild(ButtonWidget.builder(Text.literal(label + ": " + (get.getAsBoolean() ? "ON" : "OFF")), b -> {
            set.accept(!get.getAsBoolean()); config.save(); b.setMessage(Text.literal(label + ": " + (get.getAsBoolean() ? "ON" : "OFF")));
        }).dimensions(x, y, width, 20).build());
    }
    public void render(DrawContext c, int mx, int my, float delta) {
        var theme = ThemeManager.getInstance().getTheme(); c.fill(0, 0, width, height, theme.background());
        c.drawCenteredTextWithShadow(textRenderer, title, width / 2, 14, theme.accent());
        if (height > 250) c.drawCenteredTextWithShadow(textRenderer, Text.literal(status), width / 2, 214, theme.mutedText());
        super.render(c, mx, my, delta);
    }
    public void removed() { config.save(); profiles.save(hud); super.removed(); }
    public void close() { if (client != null) client.setScreen(parent); }
    public boolean shouldPause() { return false; }
}
