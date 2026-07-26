package com.codex.pvphud.animation;

public final class FadeAnimation extends Animation {
    public FadeAnimation(boolean visible) {
        super(visible ? 1.0F : 0.0F, 180L, Easing.OUT_CUBIC);
    }

    public void setVisible(boolean visible) {
        animateTo(visible ? 1.0F : 0.0F);
    }
}
