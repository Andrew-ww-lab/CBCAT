package com.cbcatfix;

import java.util.ArrayDeque;
import java.util.Deque;
import com.cbcatfix.rocket.RocketBalance;
import net.minecraft.world.phys.Vec3;

public class RocketDetonationContext {
    private static final ThreadLocal<Deque<DetonationData>> DETONATIONS = ThreadLocal.withInitial(ArrayDeque::new);

    public static void enter(int payloadCount, Vec3 direction, RocketBalance.Tier tier) {
        float payloadScale = com.cbcatfix.rocket.RocketBalance.payloadScale(payloadCount);
        Vec3 safeDirection = direction.lengthSqr() > 1.0e-8 ? direction.normalize() : Vec3.ZERO;
        DETONATIONS.get().push(new DetonationData(payloadScale, safeDirection, tier));
    }

    public static void exit() {
        Deque<DetonationData> detonations = DETONATIONS.get();
        if (!detonations.isEmpty()) {
            detonations.pop();
        }
        if (detonations.isEmpty()) {
            DETONATIONS.remove();
        }
    }

    public static boolean get() {
        return !DETONATIONS.get().isEmpty();
    }

    public static float scale() {
        Deque<DetonationData> detonations = DETONATIONS.get();
        return detonations.isEmpty() ? 1.0f : detonations.peek().scale();
    }

    public static Vec3 direction() {
        Deque<DetonationData> detonations = DETONATIONS.get();
        return detonations.isEmpty() ? Vec3.ZERO : detonations.peek().direction();
    }

    public static RocketBalance.Tier tier() {
        Deque<DetonationData> detonations = DETONATIONS.get();
        return detonations.isEmpty() ? null : detonations.peek().tier();
    }

    private record DetonationData(float scale, Vec3 direction, RocketBalance.Tier tier) {
    }
}
