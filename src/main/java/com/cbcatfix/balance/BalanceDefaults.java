package com.cbcatfix.balance;

/** Immutable developer defaults. Supported server overrides live in CbcatFixConfig. */
public final class BalanceDefaults {
    private BalanceDefaults() {}
    public static final double NEUTRAL_MULTIPLIER = 1.0;
    public static final boolean GUIDANCE_ENABLED = true;

    public static final Rocket SMALL_ROCKET = new Rocket(9, 36.0, 1.5, 10.0, 256.0);
    public static final Rocket MEDIUM_ROCKET = new Rocket(6, 18.0, 6.0, 20.0, 768.0);
    public static final Rocket BIG_ROCKET = new Rocket(4.5, 4.0, 18.0, 40.0, 1536.0);
    public static final double GUIDANCE_SPEED_LOSS_PER_DEGREE = 0.003;
    public static final double GUIDANCE_MAX_SPEED_LOSS = 0.15;
    public static final double LIGHTWEIGHT_MASS_MULTIPLIER = 0.6;
    public static final double LIGHTWEIGHT_FUEL_MULTIPLIER = 0.5;
    public static final Heat HEAVY_AUTOCANNON_HEAT = new Heat(1.5, 3.5, 6.0, 20.0, 5.0, 50.0);
    public static final Heat MEDIUM_ROCKET_HEAT = new Heat(1.5, 3.5, 8.0, 20.0, 5.0, 50.0);
    public static final Heat BIG_CANNON_HEAT = new Heat(2.5, 2.5, 10.0, 32.0, 8.0, 80.0);
    public static final Heat BIG_ROCKET_HEAT = new Heat(3.0, 4.0, 12.0, 40.0, 8.0, 90.0);
    public static final Munition AUTOCANNON_APDS = new Munition(12, 0.3, -0.02, 0.01, false, 1.75, 2.5, 1.25, 0.7);
    public static final Munition AUTOCANNON_APDSFS = new Munition(12, 0.3, -0.016, 0.00825, false, 1.66, 2.77, 1.3, 0.7);
    public static final Munition AUTOCANNON_HE = new Munition(9, 0.5, -0.025, 0.01, false, 1, 0.9, 0.4, 0.7);
    public static final Munition AUTOCANNON_HEI = new Munition(9, 1, -0.025, 0.01, false, 1, 1, 0.4, 0.7);
    public static final Munition HEAVY_AUTOCANNON_AP = new Munition(30, 1.825, -0.03, 0.013, true, 3.25, 2.75, 1.33, 0.66);
    public static final Munition HEAVY_AUTOCANNON_APDS = new Munition(25, 1.3, -0.02, 0.011, true, 3, 3.1, 1.45, 0.66);
    public static final Munition HEAVY_AUTOCANNON_APDSFS = new Munition(24.5, 1.2, -0.0175, 0.008, true, 2.67, 3.33, 1.5, 0.66);
    public static final Munition HEAVY_AUTOCANNON_HE = new Munition(27, 1.825, -0.03, 0.013, true, 1.6, 2.3, 0.8, 0.66);
    public static final Munition HEAVY_AUTOCANNON_HEF = new Munition(28, 1.8, -0.033, 0.013, true, 1.6, 2.125, 0.875, 0.66);
    public static final Munition HEAVY_AUTOCANNON_HEAT_PROJECTILE = new Munition(28, 1.75, -0.035, 0.012, true, 1.55, 2.2, 0.85, 0.66);
    public static final Munition HEAVY_AUTOCANNON_SMOKE = new Munition(25, 1, -0.035, 0.005, true, 0.75, 0.5, 0.8, 0.7);
    public static final BigMunition CLUSTER_SHELL = new BigMunition(28, 2.5, -0.05, 0.02, false, 1.75, 1, 1, 0.7, 0, 1, true, 1);
    public static final BigMunition FLAK_SHELL = new BigMunition(10, 1, -0.05, 0.01, false, 2, 1, 1, 0.7, 0, 1, true, 1);
    public static final BigMunition HEAVY_HE_SHELL = new BigMunition(30, 3, -0.07, 0.015, false, 4, 1, 1, 0.7, 0, 1, true, 2);
    public static final BigMunition HEAT_SHELL = new BigMunition(25, 1, -0.05, 0.01, false, 2, 2, 2, 0.5, 0, 1, true, 1.2);
    public static final double HEAVY_HE_BLOCK_POWER = 8;
    public static final double HEAVY_HE_ENTITY_POWER = 10;
    public static final double FLAK_BLOCK_POWER = 2;
    public static final double FLAK_ENTITY_POWER = 2;
    public static final int FLAK_SHRAPNEL_COUNT = 45;
    public static final int FLAK_BURST_COUNT = 30;
    public static final double HEAVY_HE_MAX_SPEED = 4;
    public static final double AUTOCANNON_HE_EXPLOSION_SCALE = 1.05;
    public static final double HEAVY_AUTOCANNON_HE_EXPLOSION_SCALE = 1.66;
    public static final double HEAVY_AUTOCANNON_HEF_BLOCK_EXPLOSION_SCALE = 1.3;
    public static final double HEAVY_AUTOCANNON_HEF_ENTITY_EXPLOSION_SCALE = 1.4;
    public static final double HEAVY_AUTOCANNON_HEF_FRAGMENT_COUNT_SCALE = 8;
    public static final double HEAVY_AUTOCANNON_HEF_FRAGMENT_SPREAD_SCALE = 4;
    public static final Explosion HEAVY_AUTOCANNON_SMOKE_EXPLOSION = new Explosion(1.66, 2);
    public static final double HEAVY_AUTOCANNON_SMOKE_DURATION_SCALE = 0.75;
    public static final double HEAVY_AUTOCANNON_SMOKE_SIZE_SCALE = 0.3;
    public static final int CLUSTER_SUBMUNITION_LIFETIME = 50;
    public static final double CLUSTER_SUBMUNITION_SPEED_SCALE = 1;
    public static final double CLUSTER_SUBMUNITION_INACCURACY = 25;
    public static final Ap SMALL_ROCKET_AP = new Ap(2, 2, 1, 0.70);
    public static final Ap MEDIUM_ROCKET_AP = new Ap(3.25, 2.75, 1.33, 0.66);
    public static final Ap BIG_ROCKET_AP = new Ap(8, 2, 1, 0.70);

    public static final double SMALL_RESPONSE_TICKS = 3.0;
    public static final double MEDIUM_RESPONSE_TICKS = 6.0;
    public static final double BIG_RESPONSE_TICKS = 10.0;
    public static final double SMALL_FIRE_RATE = 0.9;
    public static final double MEDIUM_FIRE_RATE = 1.0;
    public static final double BIG_FIRE_RATE = 0.5;
    public static final float EJECTION_SPEED = 0.15f;
    public static final float DOUBLE_PAYLOAD_SCALE = 1.75f;
    public static final double DOUBLE_PAYLOAD_FUEL = 0.5;
    public static final double LIGHTWEIGHT_HEALTH = 0.75;
    public static final double GROUP_FIRE_RATE_BONUS = 0.25;
    public static final double CARRIER_OVERSPEED_DRAG = 0.035;
    public static final double ANGULAR_RESPONSE = 0.25;
    public static final double SEEKER_HALF_ANGLE = 30.0;
    public static final double SEEKER_LEAD_TICKS = 20.0;
    public static final int CLUSTER_RECIPE_COUNT = 8;
    public static final double MAX_POWERED_SPEED = 20.0;
    public static final int MAX_FUEL_TICKS = 2400;
    public static final double FOLIAGE_SPEED_LOSS = 0.02;
    public static final double FRAGILE_SPEED_LOSS = 0.08;
    public static final double SOFT_SPEED_LOSS = 0.12;
    public static final float FRAGILE_HULL_DAMAGE = 1;
    public static final float SOFT_HULL_DAMAGE = 2;
    public static final double LIGHTWEIGHT_SPEED_MULTIPLIER = 1.2;
    public static final double LAUNCH_CLEARANCE_MARGIN = 1.0 / 16.0;
    public static final Airframe SMALL_AIRFRAME = new Airframe(-0.035, 0.035);
    public static final Airframe MEDIUM_AIRFRAME = new Airframe(-0.04, 0.015);
    public static final Airframe BIG_AIRFRAME = new Airframe(-0.045, 0.006);
    public record Airframe(double gravity, double drag) {}

    public record Rocket(double maxSpeed, double turnRate, double fuelSeconds, double health, double seekerRange) {}
    public record Heat(double blockPower, double entityPower, double jetLength, double jetEnergy, double jetPenetration, double jetDamage) {}
    public record Munition(double damage, double knockback, double gravity, double drag, boolean quadratic, double mass, double penetration, double toughness, double deflection) {}
    public record BigMunition(double damage, double knockback, double gravity, double drag, boolean quadratic, double mass, double penetration, double toughness, double deflection, double addedCharge, double minimumCharge, boolean canSquib, double recoil) {}
    public record Ap(double mass, double penetration, double toughness, double deflection) {}
    public record Explosion(double blockPower, double entityPower) {}
}
