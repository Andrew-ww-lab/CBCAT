package com.cbcatfix.rocket;

/** Access to CBCAT's existing persisted per-breech cooldown, not another timer. */
public interface LauncherCooldown {
    int cbcatfix$getCooldown();
    void cbcatfix$setCooldown(int ticks);
}
