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
