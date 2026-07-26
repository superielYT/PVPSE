package com.codex.pvphud.animation;

@FunctionalInterface
public interface Easing {
    Easing LINEAR = t -> t;
    Easing OUT_CUBIC = t -> 1.0F - (float) Math.pow(1.0F - t, 3.0D);
    Easing IN_OUT_QUAD = t -> t < 0.5F ? 2.0F * t * t : 1.0F - (float) Math.pow(-2.0F * t + 2.0F, 2.0D) / 2.0F;
    Easing OUT_BACK = t -> {
        float c1 = 1.70158F;
        float c3 = c1 + 1.0F;
        return 1.0F + c3 * (float) Math.pow(t - 1.0F, 3.0D) + c1 * (float) Math.pow(t - 1.0F, 2.0D);
    };

    float apply(float progress);
}
