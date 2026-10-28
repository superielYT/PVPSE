package com.codex.pvphud;

import com.codex.pvphud.config.ConfigManager;
import com.codex.pvphud.hud.HudManager;
import com.codex.pvphud.theme.ThemeManager;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

final class ClientSettingsScreen extends Screen {
    private enum Tab { GENERAL, COMBAT, HITBOX, EFFECTS, STYLE, PROFILES, FPS, AIM }
    private final Screen parent;
    private final HudConfig config;
    private final ConfigManager profiles;
    private final HudManager hud;
    private Tab tab = Tab.GENERAL;
    private EditBox profileName;
    private String draftName = "";
    private String status = "";
    private EditBox search;
    private String query = "";
    private int searchPage;
    private String focusLabel = "";
    private boolean restoreSearchFocus;

    ClientSettingsScreen(Screen parent, HudConfig config, ConfigManager profiles, HudManager hud) {
        super(Component.literal("SETTINGS")); this.parent = parent; this.config = config; this.profiles = profiles; this.hud = hud;
    }

    private int x() { return (width - w()) / 2; }
    private int w() { return Math.min(420, width - 24); }
    private int top() { return Math.max(88, (height - 240) / 2 + 88); }

    protected void init() {
        profileName = null;
        boolean typing = restoreSearchFocus || (search != null && search.isFocused());
        restoreSearchFocus = false;
        search = addRenderableWidget(new EditBox(font, x(), top() - 55, w(), 20, Component.literal("Search settings")));
        search.setHint(Component.literal("Find settings: armor, geluid, HUD..."));
        search.setMaxLength(80); search.setValue(query);
        search.setResponder(value -> {
            if (query.equals(value)) return;
            rememberDraft(); query = value; searchPage = 0; status = ""; restoreSearchFocus = true; rebuildWidgets();
        });
        if (typing) setFocused(search);
        int gap = 4, tw = (w() - gap * (Tab.values().length - 1)) / Tab.values().length;
        for (Tab next : Tab.values()) {
            var button = addRenderableWidget(com.codex.pvphud.gui.PvpButton.styled(Component.literal(next == Tab.PROFILES ? "PROFILE" : next.name()), b -> {
                rememberDraft(); tab = next; query = ""; status = ""; rebuildWidgets();
            }).bounds(x() + next.ordinal() * (tw + gap), top() - 25, tw, 20).build());
            button.active = next != tab || !query.isBlank();
        }
        int half = (w() - 6) / 2, y = top();
        if (!query.isBlank()) {
            var matches = SettingsSearch.find(query);
            int pages = Math.max(1, (matches.size() + 3) / 4);
            searchPage = Math.clamp(searchPage, 0, pages - 1);
            for (int i = searchPage * 4; i < Math.min(matches.size(), searchPage * 4 + 4); i++) {
                var result = matches.get(i);
                action(x(), y + (i % 4) * 25, w(), result.label() + "  > " + result.tab(), () -> {
                    query = ""; tab = Tab.valueOf(result.tab()); focusLabel = result.label(); rebuildWidgets();
                });
            }
            if (pages > 1) {
                action(x(), y + 100, half, "Previous", () -> { searchPage = (searchPage + pages - 1) % pages; rebuildWidgets(); });
                action(x() + half + 6, y + 100, half, "Next", () -> { searchPage = (searchPage + 1) % pages; rebuildWidgets(); });
            }
            action(x(), height - 28, w(), "Done", this::onClose);
            return;
        }
        switch (tab) {
            case GENERAL -> {
                toggle(x(), y + 100, half, "Potion timers", () -> config.nativePotionTimers, v -> config.nativePotionTimers = v);
                toggle(x() + half + 6, y + 100, half, "Yellow saturation", () -> config.nativeSaturation, v -> config.nativeSaturation = v);
                toggle(x(), y, half, "HUD", () -> config.enabled, v -> config.enabled = v);
                toggle(x() + half + 6, y, half, "Notifications", () -> config.notifications, v -> config.notifications = v);
                toggle(x(), y + 25, half, "Hide on F3", () -> config.hideOnDebug, v -> config.hideOnDebug = v);
                toggle(x() + half + 6, y + 25, half, "No damage tilt", () -> config.suppressDamageTilt, v -> config.suppressDamageTilt = v);
                toggle(x(), y + 50, half, "Custom crosshair", () -> config.showCrosshair, v -> config.showCrosshair = v);
                toggle(x() + half + 6, y + 50, half, "Block Overlay", () -> hud.find("block_overlay").map(e -> e.enabled()).orElse(false),
                        v -> { hud.find("block_overlay").ifPresent(e -> e.setEnabled(v)); profiles.save(hud); });
                int presetWidth = (w() - 8) / 3;
                for (var preset : com.codex.pvphud.hud.PvpPreset.values()) {
                    String label = preset.name().substring(0, 1) + preset.name().substring(1).toLowerCase(java.util.Locale.ROOT) + " preset";
                    action(x() + preset.ordinal() * (presetWidth + 4), y + 75, presetWidth, label, () -> {
                        if (minecraft.player == null) { status = "Join a world first"; return; }
                        preset.apply(hud); config.enabled = true; minecraft.options.hideGui = false;
                        hud.arrange(minecraft); config.save(); profiles.save(hud);
                        status = label + " applied to this profile";
                    });
                }
            }
            case COMBAT -> {
                toggle(x(), y, w(), "Attack cooldown bar", () -> config.showAttackCooldown, v -> config.showAttackCooldown = v);
                toggle(x(), y + 25, half, "Low health alert", () -> config.showLowHealthWarning, v -> config.showLowHealthWarning = v);
                number(x() + half + 6, y + 25, half, "HP threshold", () -> config.lowHealthThreshold, v -> config.lowHealthThreshold = v, 2, 16, 2);
                toggle(x(), y + 50, half, "Armor alert", () -> config.showArmorWarning, v -> config.showArmorWarning = v);
                number(x() + half + 6, y + 50, half, "Durability %", () -> config.armorWarningPercent, v -> config.armorWarningPercent = v, 5, 50, 5);
                toggle(x(), y + 75, half, "Armor sound", () -> config.armorWarningSound, v -> config.armorWarningSound = v);
                number(x() + half + 6, y + 75, half, "Sound volume %", () -> config.armorSoundVolume, v -> config.armorSoundVolume = v, 0, 100, 10);
            }
            case AIM -> {
                toggle(x(), y, w(), "Cooldown circle", () -> config.cooldownRing, v -> config.cooldownRing = v);
                toggle(x(), y + 25, w(), "Crosshair reactions", () -> config.crosshairReactions, v -> config.crosshairReactions = v);
                number(x(), y + 50, w(), "Circle radius", () -> config.cooldownRingRadius, v -> config.cooldownRingRadius = v, 10, 24, 2);
                toggle(x(), y + 75, w(), "Health indicator", () -> hud.find("health_indicator").map(e -> e.enabled()).orElse(false), v -> {
                    hud.find("health_indicator").ifPresent(e -> e.setEnabled(v));
                    if (v && minecraft.player != null && hud.find("health_indicator").map(e -> e.needsInitialPlacement()).orElse(false))
                        {
                        if (!hud.placeNewCards(minecraft, java.util.List.of("health_indicator"))) {
                            hud.find("health_indicator").ifPresent(e -> e.setEnabled(false));
                            status = "No free space: enable Health Indicator in the HUD picker.";
                            rebuildWidgets();
                        }
                    }
                    profiles.save(hud);
                });
            }
            case HITBOX -> {
                toggle(x(), y, half, "Melee hitboxes", () -> hud.feature(com.codex.pvphud.hud.FeatureModule.Type.MELEE_HITBOXES).map(e -> e.enabled()).orElse(false),
                        v -> hud.feature(com.codex.pvphud.hud.FeatureModule.Type.MELEE_HITBOXES).ifPresent(e -> e.setEnabled(v)));
                number(x() + half + 6, y, half, "View range", () -> hud.feature(com.codex.pvphud.hud.FeatureModule.Type.MELEE_HITBOXES).map(e -> (int)e.value()).orElse(12), v -> {
                    hud.feature(com.codex.pvphud.hud.FeatureModule.Type.MELEE_HITBOXES).ifPresent(e -> e.setValue(v)); profiles.save(hud);
                }, 2, 32, 1);
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
                    config.showHitMarker = true; config.save(); rebuildWidgets();
                });
            }
            case STYLE -> {
                var style = ThemeManager.getInstance().appearance();
                action(x(), y, w(), "Theme: " + ThemeManager.getInstance().getTheme().displayName() + " >", () -> {
                    ThemeManager.getInstance().cycle(); profiles.save(hud); rebuildWidgets();
                });
                number(x(), y + 25, half, "HUD opacity %", () -> style.opacity, v -> style.opacity = v, 20, 100, 10);
                toggle(x() + half + 6, y + 25, half, "Accent lines", () -> style.accents, v -> style.accents = v);
                toggle(x(), y + 50, half, "Panel shadows", () -> style.shadows, v -> style.shadows = v);
                toggle(x() + half + 6, y + 50, half, "Text shadow", () -> style.textShadow, v -> style.textShadow = v);
                action(x(), y + 75, half, "MINIMAL HUD", () -> {
                    style.opacity = 60; style.accents = false; style.shadows = false;
                    style.textShadow = true; profiles.save(hud); rebuildWidgets();
                });
                action(x() + half + 6, y + 75, half, "RESET STYLE", () -> {
                    ThemeManager.getInstance().setAppearance(null); profiles.save(hud); rebuildWidgets();
                });
            }
            case FPS -> {
                var performance = PvpHudClient.getInstance().performance();
                action(x(), y, w(), "FPS mode: " + config.performanceMode.label + " >", () -> {
                    performance.setMode(minecraft, config.performanceMode.next()); rebuildWidgets();
                });
                action(x(), y + 25, half, "Balanced", () -> { performance.setMode(minecraft, PerformanceMode.BALANCED); rebuildWidgets(); });
                action(x() + half + 6, y + 25, half, "Max FPS", () -> { performance.setMode(minecraft, PerformanceMode.MAX); rebuildWidgets(); });
                action(x(), y + 50, w(), "Restore previous settings", () -> { performance.setMode(minecraft, PerformanceMode.OFF); rebuildWidgets(); });
                toggle(x(), y + 75, w(), "Light HUD", () -> config.performanceLightHud, v -> {
                    config.performanceLightHud = v; performance.refreshBudget();
                });
            }
            case PROFILES -> {
                action(x(), y, w(), "Profile: " + profiles.activeProfile() + " >", () -> {
                    rememberDraft();
                    var names = profiles.profiles();
                    int next = (names.indexOf(profiles.activeProfile()) + 1) % names.size();
                    status = profiles.switchProfile(names.get(next), hud) ? "Profile loaded" : "Could not load profile";
                    rebuildWidgets();
                });
                profileName = addRenderableWidget(new EditBox(font, x(), y + 25, half, 20, Component.literal("New profile name")));
                profileName.setHint(Component.literal("New profile name..."));
                profileName.setMaxLength(32); profileName.setValue(draftName);
                action(x() + half + 6, y + 25, half, "COPY CURRENT", () -> {
                    rememberDraft();
                    boolean created = profiles.createProfile(draftName, hud);
                    status = created ? "Current HUD and theme copied" : "Use a new name (letters/numbers)";
                    if (created) draftName = "";
                    rebuildWidgets();
                });
                action(x(), y + 50, w(), "Reset HUD positions & scales", () -> {
                    rememberDraft();
                    minecraft.setScreen(new ConfirmScreen(yes -> {
                        if (yes) {
                            hud.elements().forEach(e -> e.resetLayout());
                            if (minecraft.player != null) hud.arrange(minecraft);
                            profiles.save(hud); status = "HUD layout restored";
                        }
                        minecraft.setScreen(this);
                    }, Component.literal("Reset HUD layout?"), Component.literal("Your module switches and other profiles are kept.")));
                });
                action(x(), y + 75, w(), "AUTO ARRANGE", () -> {
                    if (minecraft.player == null) { status = "Join a world first"; return; }
                    hud.arrange(minecraft); profiles.save(hud);
                    status = "HUD arranged; module switches kept";
                });
            }
        }
        action(x(), height - 28, w(), "Done", this::onClose);
        if (!focusLabel.isBlank()) {
            children().stream().filter(child -> child instanceof Button button && button.getMessage().getString().startsWith(focusLabel))
                    .findFirst().ifPresent(this::setFocused);
            focusLabel = "";
        }
    }

    private void action(int x, int y, int width, String label, Runnable action) {
        addRenderableWidget(com.codex.pvphud.gui.PvpButton.styled(Component.literal(label), b -> action.run()).bounds(x, y, width, 20).build());
    }
    private void toggle(int x, int y, int width, String label, BooleanSupplier get, Consumer<Boolean> set) {
        addRenderableWidget(com.codex.pvphud.gui.PvpButton.styled(Component.literal(label + ": " + (get.getAsBoolean() ? "ON" : "OFF")), b -> {
            set.accept(!get.getAsBoolean()); config.save();
            b.setMessage(Component.literal(label + ": " + (get.getAsBoolean() ? "ON" : "OFF")));
        }).bounds(x, y, width, 20).build());
    }
    private void number(int x, int y, int width, String label, IntSupplier get, IntConsumer set, int min, int max, int step) {
        addRenderableWidget(com.codex.pvphud.gui.PvpButton.styled(Component.literal(label + ": " + get.getAsInt() + " >"), b -> {
            int next = get.getAsInt() + step; set.accept(next > max ? min : next); config.save();
            b.setMessage(Component.literal(label + ": " + get.getAsInt() + " >"));
        }).bounds(x, y, width, 20).build());
    }
    private void rememberDraft() { if (profileName != null) draftName = profileName.getValue(); }

    public void extractRenderState(GuiGraphicsExtractor c, int mx, int my, float delta) {
        var theme = ThemeManager.getInstance().getTheme();
        c.fill(0, 0, width, height, theme.background());
        c.fill(x() - 8, top() - 32, x() + w() + 8,
                Math.min(height - 34, top() + (tab == Tab.GENERAL ? 147 : 122)), theme.panel());
        c.text(font, Component.literal("PVPSE / SETTINGS"), x(), top() - 80, theme.accent());
        String helper = switch (tab) {
            case AIM -> "Subtle attack readiness ring; hidden when fully charged.";
            case GENERAL -> "Yellow hunger outline = saturation (singleplayer).";
            case COMBAT -> "HP uses health points: 2 HP = 1 heart";
            case HITBOX -> "Green: ready | Amber: cooldown | Purple: falling mace";
            case EFFECTS -> "Click a value to increase it; maximum wraps to minimum";
            case STYLE -> "HUD appearance is saved per profile. Clear Glass is optional.";
            case PROFILES -> "Profiles store HUD, utilities and theme. Alerts are global.";
            case FPS -> switch (config.performanceMode) {
                case OFF -> "OFF: previous settings. Presets reduce view distance and effects.";
                case BALANCED -> "View <=12, simulation <=8, fewer particles, clouds/shadows off.";
                case MAX -> "View <=8, simulation <=5, minimal particles, clouds/shadows off.";
            };
        };
        if (!query.isBlank() && SettingsSearch.find(query).isEmpty()) {
            c.text(font, Component.literal("No settings found. Try armor, HUD or geluid."), x(), top() + 8, theme.mutedText());
        }
        int footer = top() + (tab == Tab.GENERAL ? 128 : 103);
        if (query.isBlank() && footer < height - 39) {
            String message = status.isBlank() ? helper : status;
            c.text(font, Component.literal(font.plainSubstrByWidth(message, w())), x(), footer, theme.mutedText());
        }
        super.extractRenderState(c, mx, my, delta);
    }
    public void removed() { rememberDraft(); config.save(); profiles.save(hud); super.removed(); }
    public void onClose() { if (minecraft != null) minecraft.setScreen(parent); }
    public boolean isPauseScreen() { return false; }
}
