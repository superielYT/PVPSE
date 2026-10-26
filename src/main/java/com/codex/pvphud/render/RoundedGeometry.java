package com.codex.pvphud.render;

import java.util.ArrayList;
import java.util.List;

/** Cache exact corner coverage; merge equal adjacent rows into single rectangles. */
public final class RoundedGeometry {
    public record Span(int offset, int rows, int inset) {}
    private static final List<List<Span>> CACHE;
    static {
        var cache = new ArrayList<List<Span>>();
        for (int radius = 0; radius <= 64; radius++) cache.add(build(radius));
        CACHE = List.copyOf(cache);
    }
    private RoundedGeometry() {}
    public static List<Span> spans(int radius) { return radius <= 64 ? CACHE.get(Math.max(0, radius)) : build(radius); }
    private static List<Span> build(int radius) {
        var spans = new ArrayList<Span>();
        for (int y = 0; y < radius; y++) {
            int inset = Math.max(0, (int)Math.ceil(radius - Math.sqrt(Math.max(0,
                    (double)radius * radius - (radius - y - .5) * (radius - y - .5)))));
            if (!spans.isEmpty() && spans.getLast().inset() == inset) {
                Span previous = spans.removeLast(); spans.add(new Span(previous.offset(), previous.rows() + 1, inset));
            } else spans.add(new Span(y, 1, inset));
        }
        return List.copyOf(spans);
    }
    public static int rectangleCount(int width, int height, int radius) {
        if (width <= 0 || height <= 0) return 0;
        int r = Math.clamp(radius, 0, Math.min(width, height) / 2);
        return r == 0 ? 1 : (height > 2 * r ? 1 : 0) + spans(r).size() * 2;
    }
}
