package com.cbcatfix;

public class RocketDetonationContext {
    private static final ThreadLocal<Boolean> ROCKET_DETONATING = ThreadLocal.withInitial(() -> false);

    public static void set(boolean value) {
        ROCKET_DETONATING.set(value);
    }

    public static boolean get() {
        return ROCKET_DETONATING.get();
    }
}
