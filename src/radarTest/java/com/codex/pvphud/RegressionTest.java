package com.codex.pvphud;

import com.codex.pvphud.config.JsonStorage;
import com.codex.pvphud.crosshair.CrosshairCode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Map;

public final class RegressionTest {
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
    private static void reject(String code) {
        try { CrosshairCode.decode(code); throw new AssertionError("Accepted malformed import"); }
        catch (IllegalArgumentException expected) {}
    }
    public static void main(String[] args) throws Exception {
        var gate = new ArmorAlertGate();
        require(gate.update(true, 0), "First armor warning missing");
        require(!gate.update(true, 1) && !gate.update(true, 199), "Armor sound spam");
        require(gate.update(true, 200), "Persistent armor warning not repeated");
        require(!gate.update(false, 201) && gate.update(true, 202), "Armor recovery must re-arm warning");
        gate.reset(); require(gate.update(true, 0), "Player change must reset warning timer");
        require(SettingsSearch.find("ARMOR geluid").stream().anyMatch(r -> r.label().equals("Armor sound")), "Dutch armor search");
        require(SettingsSearch.find("doorzichtig").stream().anyMatch(r -> r.tab().equals("STYLE")), "Transparency search");
        require(SettingsSearch.find("unknown-setting-xyz").isEmpty(), "Unexpected search result");
        require(com.codex.pvphud.hud.PvpPreset.SWORD.includes("reach"), "Sword preset reach");
        require(!com.codex.pvphud.hud.PvpPreset.CRYSTAL.includes("reach"), "Crystal preset must differ");
        require(com.codex.pvphud.hud.PvpPreset.MACE.includes("keystrokes"), "Mace preset keystrokes");
        for (var preset : com.codex.pvphud.hud.PvpPreset.values())
            require(preset.includes("armor") && preset.includes("inventory"), "Core PvP cards missing");
        require(com.codex.pvphud.hud.ArmorHUD.durabilityPercent(0, 100) == 100, "Fresh armor percent");
        require(com.codex.pvphud.hud.ArmorHUD.durabilityPercent(90, 100) == 10, "Damaged armor percent");
        require(com.codex.pvphud.hud.ArmorHUD.durabilityPercent(100, 100) == 0, "Broken armor percent");
        require(com.codex.pvphud.hud.ArmorHUD.durabilityPercent(0, 0) == 0, "Zero durability safety");
        require(com.codex.pvphud.hud.ArmorHUD.durabilityPercent(-100, 100) == 100, "Armor upper clamp");
        var style = new com.codex.pvphud.theme.HudAppearance();
        require(style.panelColor(0x48121A24, 1) == 0x48121A24, "Glass alpha must be preserved");
        style.opacity = 50;
        require(style.panelColor(0x48121A24, 1) == 0x24121A24, "Opacity must multiply theme alpha");
        require(style.panelColor(0x48121A24, 0) == 0x00121A24, "Hidden HUD must have no panel alpha");
        style.opacity = -500; style.normalize();
        require(style.opacity == 20, "Invalid opacity lower bound");
        style.opacity = 500; style.normalize();
        require(style.opacity == 100, "Invalid opacity upper bound");
        var gson = new com.google.gson.Gson();
        var profile = new com.codex.pvphud.config.ConfigProfile("glass-pvp");
        profile.theme = "glass"; profile.appearance.opacity = 40; profile.appearance.accents = false;
        profile.hud.put("inventory", new com.codex.pvphud.config.ConfigProfile.HudState(true, 45, 65, 1.4F));
        var restored = gson.fromJson(gson.toJson(profile), com.codex.pvphud.config.ConfigProfile.class);
        require(restored.theme.equals("glass") && restored.appearance.opacity == 40
                && !restored.appearance.accents, "Profile style did not survive JSON round trip");
        require(restored.hud.get("inventory").x == 45 && restored.hud.get("inventory").scale == 1.4F,
                "HUD position/scale did not survive saving");
        var legacy = gson.fromJson("{\"theme\":\"ignite\"}", com.codex.pvphud.config.ConfigProfile.class);
        require(legacy.appearance != null && legacy.appearance.opacity == 100, "Old profiles need default style");
        byte[] pixels = new byte[29]; pixels[14] = 1;
        String data = Base64.getUrlEncoder().withoutPadding().encodeToString(pixels);
        String prefix = "PVPSE1:" + data + ":";
        var decoded = CrosshairCode.decode(prefix + "80ff33aa:7");
        require(decoded.pixels()[7][7], "Pixel bit order changed");
        require(decoded.color() == 0xFFFF33AA, "Invisible imported colors must be opaque");
        require(decoded.flags() == 7, "Mirror flags were lost");
        reject(prefix + "ff33aa:8"); reject(prefix + "100000000:7");
        reject(prefix + "ffffffff:-1"); reject(prefix + "ffffffff:bad");
        reject("PVPSE1:AA:ffffffff:7"); reject(null); reject("x".repeat(1000));

        Path folder = Files.createTempDirectory("pvpse-regression-");
        Path file = folder.resolve("settings.json");
        require(JsonStorage.write(file, Map.of("value", 42)), "Initial save failed");
        String initial = Files.readString(file);
        require(!JsonStorage.write(file, Map.of("value", Double.NaN)), "Non-finite save accepted");
        require(Files.readString(file).equals(initial), "Failed save overwrote the old config");
        require(JsonStorage.write(file, Map.of("value", 84)), "Replacement save failed");
        require(Files.readString(file).contains("84"), "Replacement save missing");
        try (var files = Files.list(folder)) { require(files.count() == 1, "Temporary files leaked"); }
        Files.delete(file); Files.delete(folder);
        System.out.println("Crosshair import and atomic config regression checks passed");
    }
}
