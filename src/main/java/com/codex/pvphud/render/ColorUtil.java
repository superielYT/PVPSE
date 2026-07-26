package com.codex.pvphud.render;

public final class ColorUtil {
    private ColorUtil() {}

    public static int withAlpha(int color, int alpha) {
        return (Math.clamp(alpha, 0, 255) << 24) | (color & 0xFFFFFF);
    }

    public static int lerp(int from, int to, float progress) {
        float t = Math.clamp(progress, 0.0F, 1.0F);
        int a = channel(from, 24) + Math.round((channel(to, 24) - channel(from, 24)) * t);
        int r = channel(from, 16) + Math.round((channel(to, 16) - channel(from, 16)) * t);
        int g = channel(from, 8) + Math.round((channel(to, 8) - channel(from, 8)) * t);
        int b = channel(from, 0) + Math.round((channel(to, 0) - channel(from, 0)) * t);
        return a << 24 | r << 16 | g << 8 | b;
    }

    public static int rainbow(long millis, float saturation, float brightness) {
        float hue = (System.currentTimeMillis() % Math.max(1L, millis)) / (float) Math.max(1L, millis);
        return 0xFF000000 | java.awt.Color.HSBtoRGB(hue, saturation, brightness) & 0xFFFFFF;
    }

    private static int channel(int color, int shift) {
        return color >> shift & 0xFF;
    }
}
