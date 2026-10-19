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
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

final class ClientSettingsScreen extends Screen {
    private enum Tab { GENERAL, COMBAT, HITBOX, EFFECTS, PROFILES }
    private final Screen parent;
    private final HudConfig config;
    private final ConfigManager profiles;
    private final HudManager hud;
    private Tab tab = Tab.GENERAL;
    private TextFieldWidget profileName;
    private String draftName = "";
    private String status = "";

    ClientSettingsScreen(Screen parent, HudConfig config, ConfigManager profiles, HudManager hud) {
        super(Text.literal("SETTINGS")); this.parent = parent; this.config = config; this.profiles = profiles; this.hud = hud;
    }

    private int x() { return (width - w()) / 2; }
    private int w() { return Math.min(420, width - 24); }
    private int top() { return Math.max(55, (height - 210) / 2 + 48); }

    protected void init() {
        profileName = null;
        int gap = 4, tw = (w() - gap * (Tab.values().length - 1)) / Tab.values().length;
        for (Tab next : Tab.values()) {
            var button = addDrawableChild(ButtonWidget.builder(Text.literal(next.name()), b -> {
                rememberDraft(); tab = next; status = ""; clearAndInit();
            }).dimensions(x() + next.ordinal() * (tw + gap), top() - 25, tw, 20).build());
            button.active = next != tab;
        }
        int half = (w() - 6) / 2, y = top();
        switch (tab) {
            case GENERAL -> {
                toggle(x(), y, half, "HUD", () -> config.enabled, v -> config.enabled = v);
                toggle(x() + half + 6, y, half, "Notifications", () -> config.notifications, v -> config.notifications = v);
                toggle(x(), y + 25, half, "Hide on F3", () -> config.hideOnDebug, v -> config.hideOnDebug = v);
                toggle(x() + half + 6, y + 25, half, "No damage tilt", () -> config.suppressDamageTilt, v -> config.suppressDamageTilt = v);
                toggle(x(), y + 50, w(), "Custom crosshair", () -> config.showCrosshair, v -> config.showCrosshair = v);
            }
            case COMBAT -> {
                toggle(x(), y, w(), "Attack cooldown bar", () -> config.showAttackCooldown, v -> config.showAttackCooldown = v);
                toggle(x(), y + 25, half, "Low health alert", () -> config.showLowHealthWarning, v -> config.showLowHealthWarning = v);
                number(x() + half + 6, y + 25, half, "HP threshold", () -> config.lowHealthThreshold, v -> config.lowHealthThreshold = v, 2, 16, 2);
                toggle(x(), y + 50, half, "Armor alert", () -> config.showArmorWarning, v -> config.showArmorWarning = v);
                number(x() + half + 6, y + 50, half, "Durability %", () -> config.armorWarningPercent, v -> config.armorWarningPercent = v, 5, 50, 5);
            }
            case HITBOX -> {
                toggle(x(), y, w(), "Melee hitboxes", () -> hud.feature(com.codex.pvphud.hud.FeatureModule.Type.MELEE_HITBOXES).map(e -> e.enabled()).orElse(false),
                        v -> hud.feature(com.codex.pvphud.hud.FeatureModule.Type.MELEE_HITBOXES).ifPresent(e -> e.setEnabled(v)));
                toggle(x(), y + 25, half, "Swords", () -> config.hitboxSwords, v -> config.hitboxSwords = v);
                toggle(x() + half + 6, y + 25, half, "Mace", () -> config.hitboxMace, v -> config.hitboxMace = v);
                toggle(x(), y + 50, half, "Target only", () -> config.hitboxTargetOnly, v -> config.hitboxTargetOnly = v);
                toggle(x() + half + 6, y + 50, half, "Eye-level ring", () -> config.hitboxEyeLine, v -> config.hitboxEyeLine = v);
                number(x(), y + 75, half, "Line width", () -> config.hitboxLineWidth, v -> config.hitboxLineWidth = v, 1, 4, 1);
                toggle(x() + half + 6, y + 75, half, "State colors", () -> config.hitboxStateColors, v -> config.hitboxStateColors = v);
            }
            case EFFECTS -> {
                toggle(x(), y, half, "Hit marker", () -> config.showHitMarker, v -> config.showHitMarker = v);
                number(x() + half + 6, y, half, "Marker size", () -> config.hitMarkerSize, v -> config.hitMarkerSize = v, 3, 10, 1);
                toggle(x(), y + 25, half, "Damage flash", () -> config.showDamageFlash, v -> config.showDamageFlash = v);
                number(x() + half + 6, y + 25, half, "Flash %", () -> config.damageFlashOpacity, v -> config.damageFlashOpacity = v, 10, 100, 10);
                toggle(x(), y + 50, half, "Kill particles", () -> config.showKillParticles, v -> config.showKillParticles = v);
                number(x() + half + 6, y + 50, half, "Particles", () -> config.particleAmount, v -> config.particleAmount = v, 4, 48, 4);
                action(x(), y + 75, w(), "MINIMAL EFFECTS", () -> {
                    config.showDamageFlash = false; config.showKillParticles = false;
                    config.showHitMarker = true; config.save(); clearAndInit();
                });
            }
            case PROFILES -> {
                action(x(), y, w(), "Profile: " + profiles.activeProfile() + " >", () -> {
                    rememberDraft();
                    var names = profiles.profiles();
                    int next = (names.indexOf(profiles.activeProfile()) + 1) % names.size();
                    status = profiles.switchProfile(names.get(next), hud) ? "Profile loaded" : "Could not load profile";
                    clearAndInit();
                });
                profileName = addDrawableChild(new TextFieldWidget(textRenderer, x(), y + 25, half, 20, Text.literal("New profile name")));
                profileName.setPlaceholder(Text.literal("New profile name..."));
                profileName.setMaxLength(32); profileName.setText(draftName);
                action(x() + half + 6, y + 25, half, "COPY CURRENT", () -> {
                    rememberDraft();
                    boolean created = profiles.createProfile(draftName, hud);
                    status = created ? "Current HUD and theme copied" : "Use a new name (letters/numbers)";
                    if (created) draftName = "";
                    clearAndInit();
                });
                action(x(), y + 50, w(), "Reset HUD positions & scales", () -> {
                    rememberDraft();
                    client.setScreen(new ConfirmScreen(yes -> {
                        if (yes) { hud.elements().forEach(e -> e.resetLayout()); profiles.save(hud); status = "HUD layout restored"; }
                        client.setScreen(this);
                    }, Text.literal("Reset HUD layout?"), Text.literal("Your module switches and other profiles are kept.")));
                });
            }
        }
        action(x(), height - 28, w(), "Done", this::close);
    }

    private void action(int x, int y, int width, String label, Runnable action) {
        addDrawableChild(ButtonWidget.builder(Text.literal(label), b -> action.run()).dimensions(x, y, width, 20).build());
    }
    private void toggle(int x, int y, int width, String label, BooleanSupplier get, Consumer<Boolean> set) {
        addDrawableChild(ButtonWidget.builder(Text.literal(label + ": " + (get.getAsBoolean() ? "ON" : "OFF")), b -> {
            set.accept(!get.getAsBoolean()); config.save();
            b.setMessage(Text.literal(label + ": " + (get.getAsBoolean() ? "ON" : "OFF")));
        }).dimensions(x, y, width, 20).build());
    }
    private void number(int x, int y, int width, String label, IntSupplier get, IntConsumer set, int min, int max, int step) {
        addDrawableChild(ButtonWidget.builder(Text.literal(label + ": " + get.getAsInt() + " >"), b -> {
            int next = get.getAsInt() + step; set.accept(next > max ? min : next); config.save();
            b.setMessage(Text.literal(label + ": " + get.getAsInt() + " >"));
        }).dimensions(x, y, width, 20).build());
    }
    private void rememberDraft() { if (profileName != null) draftName = profileName.getText(); }

    public void render(DrawContext c, int mx, int my, float delta) {
        var theme = ThemeManager.getInstance().getTheme();
        c.fill(0, 0, width, height, theme.background());
        c.fill(x() - 8, top() - 32, x() + w() + 8, Math.min(height - 34, top() + 122), theme.panel());
        c.drawTextWithShadow(textRenderer, Text.literal("PVPSE / SETTINGS"), x(), top() - 44, theme.accent());
        String helper = switch (tab) {
            case GENERAL -> "Your display and visibility preferences";
            case COMBAT -> "HP uses health points: 2 HP = 1 heart";
            case HITBOX -> "Green: ready | Amber: cooldown | Purple: falling mace";
            case EFFECTS -> "Click a value to increase it; maximum wraps to minimum";
            case PROFILES -> "Profiles store HUD, utilities and theme. Alerts are global.";
        };
        int footer = top() + ((tab == Tab.EFFECTS || tab == Tab.HITBOX) ? 103 : 82);
        if (footer < height - 39) {
            String message = status.isBlank() ? helper : status;
            c.drawTextWithShadow(textRenderer, Text.literal(textRenderer.trimToWidth(message, w())), x(), footer, theme.mutedText());
        }
        super.render(c, mx, my, delta);
    }
    public void removed() { rememberDraft(); config.save(); profiles.save(hud); super.removed(); }
    public void close() { if (client != null) client.setScreen(parent); }
    public boolean shouldPause() { return false; }
}
