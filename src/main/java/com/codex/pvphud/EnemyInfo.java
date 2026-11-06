package com.codex.pvphud;

public final class EnemyInfo {
    private EnemyInfo() {}
    public static float hearts(float hp) { return Float.isFinite(hp) ? Math.max(0,hp)/2 : 0; }
}
