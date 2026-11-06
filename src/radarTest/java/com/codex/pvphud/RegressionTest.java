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
        require(EnemyInfo.hearts(20)==10 && EnemyInfo.hearts(6)==3 && EnemyInfo.hearts(Float.NaN)==0,"Enemy hearts use two HP each");
        var history=new com.codex.pvphud.hud.LayoutHistory<String>();
        history.record("a","b");history.record("b","c");
        require(history.undo("c").equals("b") && history.undo("b").equals("a"),"Undo steps are individual transactions");
        require(history.redo("a").equals("b"),"Redo lost a state");history.record("b","d");
        require(!history.canRedo(),"New edit must invalidate redo");
        double[] timing=new double[100];java.util.Arrays.fill(timing,10);timing[99]=100;
        var stats=FrameTimes.summarize(timing);
        require(stats.count()==100 && Math.abs(stats.fps()-100000.0/1090)<.001 && stats.lowOnePercent()==10 && stats.p95()==10 && stats.p99()==10,"Frame statistics or 1% low incorrect");
        var buffer=new FrameTimes();for(int i=1;i<=700;i++)buffer.add(i);
        require(buffer.size()==600 && buffer.last(600)[0]==101 && buffer.last(1)[0]==700,"Frame ring did not bound history");
        require(FrameTimes.summarize(new double[]{Double.NaN,0,-1}).count()==0,"Invalid frame values accepted");

        var overshoot = new com.codex.pvphud.animation.Animation(0, 10_000_000L, progress -> 1);
        overshoot.animateTo(1); overshoot.value();
        require(!overshoot.finished(), "Crossing the target must not end an unfinished animation");
        overshoot.snap(1); require(overshoot.finished() && overshoot.value() == 1, "Snap must settle immediately");
        for (int radius = 1; radius <= 64; radius++) {
            int row = 0;
            for (var span : com.codex.pvphud.render.RoundedGeometry.spans(radius)) {
                require(span.offset() == row && span.rows() > 0, "Rounded coverage gap");
                for (int y = span.offset(); y < span.offset() + span.rows(); y++) {
                    int expected = Math.max(0, (int)Math.ceil(radius - Math.sqrt(Math.max(0,
                            (double)radius * radius - (radius - y - .5) * (radius - y - .5)))));
                    require(span.inset() == expected, "Rounded pixels changed"); row++;
                }
            }
            require(row == radius, "Rounded corner incomplete");
        }
        require(com.codex.pvphud.render.RoundedGeometry.rectangleCount(100, 40, 6) < 13, "Rounded renderer did not reduce rectangles");
        require(com.codex.pvphud.render.RoundedGeometry.rectangleCount(0, 40, 6) == 0, "Empty geometry must not draw");
        require(com.codex.pvphud.render.RoundedGeometry.spans(6) == com.codex.pvphud.render.RoundedGeometry.spans(6), "Corner cache is not reused");
        com.codex.pvphud.render.RenderBudget.configure(PerformanceMode.BALANCED, true);
        require(com.codex.pvphud.render.RenderBudget.lightweight() && com.codex.pvphud.render.RenderBudget.particles(48) == 8, "Balanced budget");
        com.codex.pvphud.render.RenderBudget.configure(PerformanceMode.MAX, false);
        require(!com.codex.pvphud.render.RenderBudget.lightweight() && com.codex.pvphud.render.RenderBudget.particles(48) == 0, "Max particle budget / HUD opt-out");
        com.codex.pvphud.render.RenderBudget.configure(PerformanceMode.OFF, true);
        require(!com.codex.pvphud.render.RenderBudget.lightweight(), "OFF must restore full HUD rendering");
        require(SettingsSearch.find("fps verbeteren").stream().anyMatch(r -> r.tab().equals("FPS")), "Dutch FPS search");
        var gate = new ArmorAlertGate();
        require(gate.update(true, 0), "First armor warning missing");
        require(!gate.update(true, 1) && !gate.update(true, 199), "Armor sound spam");
        require(gate.update(true, 200), "Persistent armor warning not repeated");
        require(!gate.update(false, 201) && gate.update(true, 202), "Armor recovery must re-arm warning");
        gate.reset(); require(gate.update(true, 0), "Player change must reset warning timer");
        require(SettingsSearch.find("ARMOR geluid").stream().anyMatch(r -> r.label().equals("Armor sound")), "Dutch armor search");
        require(SettingsSearch.find("doorzichtig").stream().anyMatch(r -> r.tab().equals("STYLE")), "Transparency search");
        require(SettingsSearch.find("blok aankijken").stream().anyMatch(r -> r.label().equals("Block Overlay")), "Block overlay search");
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
        var fixedTime = java.nio.file.attribute.FileTime.fromMillis(123456000L);
        Files.setLastModifiedTime(file, fixedTime);
        require(JsonStorage.write(file, Map.of("value", 42)), "Unchanged save failed");
        require(Files.getLastModifiedTime(file).equals(fixedTime), "Unchanged config was rewritten");
        require(!JsonStorage.write(file, Map.of("value", Double.NaN)), "Non-finite save accepted");
        require(Files.readString(file).equals(initial), "Failed save overwrote the old config");
        require(JsonStorage.write(file, Map.of("value", 84)), "Replacement save failed");
        require(Files.readString(file).contains("84"), "Replacement save missing");
        try (var files = Files.list(folder)) { require(files.count() == 1, "Temporary files leaked"); }
        Files.delete(file); Files.delete(folder);
        System.out.println("Crosshair import and atomic config regression checks passed");
    }
}
