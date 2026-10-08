package com.cbcatfix.rocket;

import com.cbcatfix.balance.BalanceDefaults;

/** One assembled session owns readiness; the entity only transports client snapshots. */
public interface LauncherReadiness {
    int cbcatfix$armingTicksRemaining();
    void cbcatfix$receiveArmingSnapshot(int ticks);
    boolean cbcatfix$hasRockets();

    static int duration(RocketBalance.Tier tier) {
        return switch (tier) {
            case SMALL -> BalanceDefaults.SMALL_ARMING_TICKS;
            case MEDIUM -> BalanceDefaults.MEDIUM_ARMING_TICKS;
            case BIG -> BalanceDefaults.BIG_ARMING_TICKS;
        };
    }

    interface Sync {
        void cbcatfix$syncArming(int ticks);
    }
}
