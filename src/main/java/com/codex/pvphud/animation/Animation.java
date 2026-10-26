package com.codex.pvphud.animation;

public class Animation {
    private final long durationNanos;
    private final Easing easing;
    private float from;
    private float target;
    private float value;
    private long startedAt;
    private boolean complete = true;

    public Animation(float initialValue, long durationMillis, Easing easing) {
        this.value = initialValue;
        this.from = initialValue;
        this.target = initialValue;
        this.durationNanos = Math.max(1L, durationMillis) * 1_000_000L;
        this.easing = easing;
        this.startedAt = System.nanoTime();
    }

    public synchronized void animateTo(float next) {
        update();
        if (Float.compare(target, next) == 0) return;
        from = value;
        target = next;
        complete = Float.compare(from, next) == 0;
        startedAt = System.nanoTime();
    }

    public synchronized float value() {
        update();
        return value;
    }

    public synchronized boolean finished() {
        update();
        return complete;
    }

    public synchronized void snap(float next) {
        from = target = value = next;
        complete = true;
        startedAt = System.nanoTime();
    }

    private void update() {
        if (complete) return;
        float progress = Math.clamp((System.nanoTime() - startedAt) / (float) durationNanos, 0.0F, 1.0F);
        value = from + (target - from) * easing.apply(progress);
        if (progress >= 1.0F) { value = target; complete = true; }
    }
}
