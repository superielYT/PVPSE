package com.codex.pvphud.crosshair;

import java.util.Base64;

/** Parse completely before the editor mutates any saved crosshair state. */
public final class CrosshairCode {
    public record Decoded(boolean[][] pixels, int color, int flags) {}
    private CrosshairCode() {}
    public static Decoded decode(String code) {
        if (code == null || code.length() > 80) throw new IllegalArgumentException("Invalid crosshair code");
        String[] parts = code.strip().split(":", -1);
        if (parts.length != 4 || !parts[0].equals("PVPSE1") || !parts[2].matches("[0-9a-fA-F]{1,8}"))
            throw new IllegalArgumentException("Invalid crosshair code");
        int flags = Integer.parseInt(parts[3]);
        if (flags < 0 || flags > 7) throw new IllegalArgumentException("Invalid flags");
        int color = 0xFF000000 | Integer.parseUnsignedInt(parts[2], 16) & 0xFFFFFF;
        byte[] data = Base64.getUrlDecoder().decode(parts[1]);
        if (data.length != 29) throw new IllegalArgumentException("Invalid pixel data");
        boolean[][] pixels = new boolean[15][15];
        for (int bit = 0; bit < 225; bit++) pixels[bit / 15][bit % 15] = (data[bit / 8] & (1 << bit % 8)) != 0;
        return new Decoded(pixels, color, flags);
    }
}
