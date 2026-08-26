package com.cbcatfix;

public class RocketDetonationContext {
    private static final ThreadLocal<Integer> DETONATION_DEPTH = ThreadLocal.withInitial(() -> 0);

    public static void enter() {
        DETONATION_DEPTH.set(DETONATION_DEPTH.get() + 1);
    }

    public static void exit() {
        int depth = DETONATION_DEPTH.get() - 1;
        if (depth > 0) {
            DETONATION_DEPTH.set(depth);
        } else {
            DETONATION_DEPTH.remove();
        }
    }

    public static boolean get() {
        return DETONATION_DEPTH.get() > 0;
    }
}
