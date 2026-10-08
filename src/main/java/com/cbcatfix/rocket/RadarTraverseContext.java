package com.cbcatfix.rocket;

/** Scoped only around a native Radars CBC step; never persists or calculates motion. */
public final class RadarTraverseContext {
    public static final ThreadLocal<Double> MULTIPLIER = ThreadLocal.withInitial(() -> 1.0);
    private RadarTraverseContext() {}
}
