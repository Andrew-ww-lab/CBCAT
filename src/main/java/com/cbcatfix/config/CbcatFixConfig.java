package com.cbcatfix.config;

import com.cbcatfix.rocket.RocketBalance;
import com.cbcatfix.balance.BalanceDefaults;
import net.neoforged.neoforge.common.ModConfigSpec;
import rbasamoyai.createbigcannons.munitions.big_cannon.config.BigCannonProjectilePropertiesComponent;
import rbasamoyai.createbigcannons.munitions.config.components.BallisticPropertiesComponent;
import rbasamoyai.createbigcannons.munitions.config.components.EntityDamagePropertiesComponent;

public final class CbcatFixConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final RocketSettings SMALL_ROCKET = rocketSettings("small", BalanceDefaults.SMALL_ROCKET);
    public static final RocketSettings MEDIUM_ROCKET = rocketSettings("medium", BalanceDefaults.MEDIUM_ROCKET);
    public static final RocketSettings BIG_ROCKET = rocketSettings("big", BalanceDefaults.BIG_ROCKET);
    public static final ModConfigSpec.DoubleValue GUIDANCE_SPEED_LOSS_PER_DEGREE = guidanceDoubleSetting("speedLossPerTurnDegree", BalanceDefaults.GUIDANCE_SPEED_LOSS_PER_DEGREE, 0.0, 0.1, "Fraction of speed lost for each degree actually turned in one tick.");
    public static final ModConfigSpec.DoubleValue GUIDANCE_MAX_SPEED_LOSS = guidanceDoubleSetting("maximumSpeedLossPerTick", BalanceDefaults.GUIDANCE_MAX_SPEED_LOSS, 0.0, 0.9, "Maximum fraction of speed a rocket can lose to steering in one tick.");
    public static final ModConfigSpec.DoubleValue LIGHTWEIGHT_MASS_MULTIPLIER = guidanceDoubleSetting("lightweightMassMultiplier", BalanceDefaults.LIGHTWEIGHT_MASS_MULTIPLIER, 0.1, 1.0, "Mass multiplier for the one-warhead, one-fuel lightweight recipe.");
    public static final ModConfigSpec.DoubleValue LIGHTWEIGHT_FUEL_MULTIPLIER = guidanceDoubleSetting("lightweightFuelMultiplier", BalanceDefaults.LIGHTWEIGHT_FUEL_MULTIPLIER, 0.05, 1.0, "Powered-flight-time multiplier for the one-warhead, one-fuel lightweight recipe.");
    public static final HeatSettings HEAVY_AUTOCANNON_HEAT = heatSettings("heavyAutocannon", BalanceDefaults.HEAVY_AUTOCANNON_HEAT);
    public static final HeatSettings MEDIUM_ROCKET_HEAT = heatSettings("mediumRocket", BalanceDefaults.MEDIUM_ROCKET_HEAT);
    public static final HeatSettings BIG_CANNON_HEAT = heatSettings("bigCannon", BalanceDefaults.BIG_CANNON_HEAT);
    public static final HeatSettings BIG_ROCKET_HEAT = heatSettings("bigRocket", BalanceDefaults.BIG_ROCKET_HEAT);

    public static final MunitionSettings AUTOCANNON_APDS = munitionSettings("cbcAt.autocannon.apds", BalanceDefaults.AUTOCANNON_APDS);
    public static final MunitionSettings AUTOCANNON_APDSFS = munitionSettings("cbcAt.autocannon.apdsfs", BalanceDefaults.AUTOCANNON_APDSFS);
    public static final MunitionSettings AUTOCANNON_HE = munitionSettings("cbcAt.autocannon.he", BalanceDefaults.AUTOCANNON_HE);
    public static final MunitionSettings AUTOCANNON_HEI = munitionSettings("cbcAt.autocannon.hei", BalanceDefaults.AUTOCANNON_HEI);

    public static final MunitionSettings HEAVY_AUTOCANNON_AP = munitionSettings("cbcAt.heavyAutocannon.ap", BalanceDefaults.HEAVY_AUTOCANNON_AP);
    public static final MunitionSettings HEAVY_AUTOCANNON_APDS = munitionSettings("cbcAt.heavyAutocannon.apds", BalanceDefaults.HEAVY_AUTOCANNON_APDS);
    public static final MunitionSettings HEAVY_AUTOCANNON_APDSFS = munitionSettings("cbcAt.heavyAutocannon.apdsfs", BalanceDefaults.HEAVY_AUTOCANNON_APDSFS);
    public static final MunitionSettings HEAVY_AUTOCANNON_HE = munitionSettings("cbcAt.heavyAutocannon.he", BalanceDefaults.HEAVY_AUTOCANNON_HE);
    public static final MunitionSettings HEAVY_AUTOCANNON_HEF = munitionSettings("cbcAt.heavyAutocannon.hef", BalanceDefaults.HEAVY_AUTOCANNON_HEF);
    public static final MunitionSettings HEAVY_AUTOCANNON_HEAT_PROJECTILE = munitionSettings("cbcAt.heavyAutocannon.heat", BalanceDefaults.HEAVY_AUTOCANNON_HEAT_PROJECTILE);
    public static final MunitionSettings HEAVY_AUTOCANNON_SMOKE = munitionSettings("cbcAt.heavyAutocannon.smoke", BalanceDefaults.HEAVY_AUTOCANNON_SMOKE);

    public static final MunitionSettings CLUSTER_SHELL = bigMunitionSettings("cbcAt.bigCannon.cluster", BalanceDefaults.CLUSTER_SHELL);
    public static final MunitionSettings FLAK_SHELL = bigMunitionSettings("fix.bigCannon.flak", BalanceDefaults.FLAK_SHELL);
    public static final MunitionSettings HEAVY_HE_SHELL = bigMunitionSettings("fix.bigCannon.heavyHe", BalanceDefaults.HEAVY_HE_SHELL);
    public static final MunitionSettings HEAT_SHELL = bigMunitionSettings("fix.bigCannon.heat", BalanceDefaults.HEAT_SHELL);

    public static final ModConfigSpec.DoubleValue HEAVY_HE_BLOCK_POWER = effectSetting("fix.bigCannon.heavyHe", "blockExplosionPower", BalanceDefaults.HEAVY_HE_BLOCK_POWER, 0, 32);
    public static final ModConfigSpec.DoubleValue HEAVY_HE_ENTITY_POWER = effectSetting("fix.bigCannon.heavyHe", "entityExplosionPower", BalanceDefaults.HEAVY_HE_ENTITY_POWER, 0, 32);
    public static final ModConfigSpec.DoubleValue FLAK_BLOCK_POWER = effectSetting("fix.bigCannon.flak", "blockExplosionPower", BalanceDefaults.FLAK_BLOCK_POWER, 0, 32);
    public static final ModConfigSpec.DoubleValue FLAK_ENTITY_POWER = effectSetting("fix.bigCannon.flak", "entityExplosionPower", BalanceDefaults.FLAK_ENTITY_POWER, 0, 32);
    public static final ModConfigSpec.IntValue FLAK_SHRAPNEL_COUNT = intEffectSetting("fix.bigCannon.flak", "shrapnelCount", BalanceDefaults.FLAK_SHRAPNEL_COUNT, 0, 256);
    public static final ModConfigSpec.IntValue FLAK_BURST_COUNT = intEffectSetting("fix.bigCannon.flak", "burstParticleProjectileCount", BalanceDefaults.FLAK_BURST_COUNT, 0, 256);
    public static final ModConfigSpec.DoubleValue HEAVY_HE_MAX_SPEED = effectSetting("fix.bigCannon.heavyHe", "maximumSpeedBlocksPerTick", BalanceDefaults.HEAVY_HE_MAX_SPEED, 0.1, 20);
    public static final ModConfigSpec.DoubleValue AUTOCANNON_HE_EXPLOSION_SCALE = effectSetting("cbcAt.autocannon.he", "cbcFlakExplosionScale", BalanceDefaults.AUTOCANNON_HE_EXPLOSION_SCALE, 0, 10);
    public static final ModConfigSpec.DoubleValue HEAVY_AUTOCANNON_HE_EXPLOSION_SCALE = effectSetting("cbcAt.heavyAutocannon.he", "cbcFlakExplosionScale", BalanceDefaults.HEAVY_AUTOCANNON_HE_EXPLOSION_SCALE, 0, 10);
    public static final ModConfigSpec.DoubleValue HEAVY_AUTOCANNON_HEF_BLOCK_EXPLOSION_SCALE = effectSetting("cbcAt.heavyAutocannon.hef", "cbcFlakBlockExplosionScale", BalanceDefaults.HEAVY_AUTOCANNON_HEF_BLOCK_EXPLOSION_SCALE, 0, 10);
    public static final ModConfigSpec.DoubleValue HEAVY_AUTOCANNON_HEF_ENTITY_EXPLOSION_SCALE = effectSetting("cbcAt.heavyAutocannon.hef", "cbcFlakEntityExplosionScale", BalanceDefaults.HEAVY_AUTOCANNON_HEF_ENTITY_EXPLOSION_SCALE, 0, 10);
    public static final ModConfigSpec.DoubleValue HEAVY_AUTOCANNON_HEF_FRAGMENT_COUNT_SCALE = effectSetting("cbcAt.heavyAutocannon.hef", "cbcFlakFragmentCountScale", BalanceDefaults.HEAVY_AUTOCANNON_HEF_FRAGMENT_COUNT_SCALE, 0, 10);
    public static final ModConfigSpec.DoubleValue HEAVY_AUTOCANNON_HEF_FRAGMENT_SPREAD_SCALE = effectSetting("cbcAt.heavyAutocannon.hef", "cbcFlakFragmentSpreadScale", BalanceDefaults.HEAVY_AUTOCANNON_HEF_FRAGMENT_SPREAD_SCALE, 0, 10);
    public static final ExplosionSettings HEAVY_AUTOCANNON_SMOKE_EXPLOSION = explosionSettings("cbcAt.heavyAutocannon.smoke", BalanceDefaults.HEAVY_AUTOCANNON_SMOKE_EXPLOSION);
    public static final ModConfigSpec.DoubleValue HEAVY_AUTOCANNON_SMOKE_DURATION_SCALE = effectSetting("cbcAt.heavyAutocannon.smoke", "smokeDurationScale", BalanceDefaults.HEAVY_AUTOCANNON_SMOKE_DURATION_SCALE, 0, 10);
    public static final ModConfigSpec.DoubleValue HEAVY_AUTOCANNON_SMOKE_SIZE_SCALE = effectSetting("cbcAt.heavyAutocannon.smoke", "smokeSizeScale", BalanceDefaults.HEAVY_AUTOCANNON_SMOKE_SIZE_SCALE, 0, 10);
    public static final ModConfigSpec.IntValue CLUSTER_SUBMUNITION_LIFETIME = intEffectSetting("cbcAt.bigCannon.cluster", "submunitionLifetimeTicks", BalanceDefaults.CLUSTER_SUBMUNITION_LIFETIME, 1, 12000);
    public static final ModConfigSpec.DoubleValue CLUSTER_SUBMUNITION_SPEED_SCALE = effectSetting("cbcAt.bigCannon.cluster", "submunitionSpeedScale", BalanceDefaults.CLUSTER_SUBMUNITION_SPEED_SCALE, 0, 10);
    public static final ModConfigSpec.DoubleValue CLUSTER_SUBMUNITION_INACCURACY = effectSetting("cbcAt.bigCannon.cluster", "submunitionInaccuracyDegrees", BalanceDefaults.CLUSTER_SUBMUNITION_INACCURACY, 0, 180);

    public static final RocketApSettings SMALL_ROCKET_AP = rocketApSettings("small", BalanceDefaults.SMALL_ROCKET_AP);
    public static final RocketApSettings MEDIUM_ROCKET_AP = rocketApSettings("medium", BalanceDefaults.MEDIUM_ROCKET_AP);
    public static final RocketApSettings BIG_ROCKET_AP = rocketApSettings("big", BalanceDefaults.BIG_ROCKET_AP);

    public static final ModConfigSpec.DoubleValue VELOCITY_MULTIPLIER = gameplay("rockets.physics", "velocityMultiplier", BalanceDefaults.NEUTRAL_MULTIPLIER, 0.1, 4, "Powered speed multiplier; effective speed is capped at 20 blocks/tick.");
    public static final ModConfigSpec.DoubleValue ACCELERATION_MULTIPLIER = gameplay("rockets.physics", "accelerationMultiplier", BalanceDefaults.NEUTRAL_MULTIPLIER, 0.1, 4, "Motor response multiplier; variants retain their mass penalty.");
    public static final ModConfigSpec.DoubleValue FUEL_MULTIPLIER = gameplay("rockets.physics", "fuelMultiplier", BalanceDefaults.NEUTRAL_MULTIPLIER, 0.1, 4, "Configured fuel duration multiplier; total powered time is capped at 120 seconds.");
    public static final ModConfigSpec.DoubleValue GRAVITY_MULTIPLIER = gameplay("rockets.physics", "gravityMultiplier", BalanceDefaults.NEUTRAL_MULTIPLIER, 0, 4, "Ballistic rocket gravity multiplier. Does not add gravity during powered flight.");
    public static final ModConfigSpec.DoubleValue ROCKET_DAMAGE_MULTIPLIER = gameplay("rockets.payload", "directDamageMultiplier", BalanceDefaults.NEUTRAL_MULTIPLIER, 0, 10, "Direct rocket impact damage multiplier; explosion and jet power have separate munition settings.");
    public static final ModConfigSpec.DoubleValue PENETRATION_MULTIPLIER = gameplay("rockets.payload", "penetrationMultiplier", BalanceDefaults.NEUTRAL_MULTIPLIER, 0, 4, "Rocket penetration coefficient multiplier; preserves CBC armor calculations.");
    public static final ModConfigSpec.DoubleValue DOUBLE_PAYLOAD_MULTIPLIER = gameplay("rockets.payload", "doublePayloadMultiplier", BalanceDefaults.DOUBLE_PAYLOAD_SCALE, 1, 2, "Double-warhead damage/effect and penetrator mass scale; fuel remains halved.");
    public static final ModConfigSpec.DoubleValue LIGHTWEIGHT_DURABILITY_MULTIPLIER = gameplay("rockets.payload", "lightweightDurabilityMultiplier", BalanceDefaults.LIGHTWEIGHT_HEALTH, 0.1, 1, "Lightweight hull health multiplier; does not weaken the AP penetrator.");
    public static final ModConfigSpec.DoubleValue LAUNCHER_SPREAD_MULTIPLIER = gameplay("launchers", "spreadMultiplier", BalanceDefaults.NEUTRAL_MULTIPLIER, 0, 4, "Multiplier after native CBCAT material/barrel-length spread. Native RNG is preserved.");
    public static final ModConfigSpec.DoubleValue LAUNCHER_FIRE_RATE_MULTIPLIER = gameplay("launchers", "fireRateMultiplier", BalanceDefaults.NEUTRAL_MULTIPLIER, 0.1, 4, "Firing-rate multiplier; minimum cooldown is one tick. Large rails retain their slower rate.");
    public static final ModConfigSpec.DoubleValue MULTI_LAUNCHER_BONUS = gameplay("launchers", "extraLaneFireRateBonus", BalanceDefaults.GROUP_FIRE_RATE_BONUS, 0, 1, "Additional firing-rate fraction per attached lane.");
    public static final ModConfigSpec.BooleanValue GUIDANCE_ENABLED = gameplayFlag("rockets.guidance", "enabled", "Allow rocket steering and launch-time Radar designation.");
    public static final ModConfigSpec.DoubleValue SEEKER_HALF_ANGLE = gameplay("rockets.guidance", "seekerHalfAngleDegrees", BalanceDefaults.SEEKER_HALF_ANGLE, 1, 85, "Half-angle of the seeker's forward detection cone.");
    public static final ModConfigSpec.DoubleValue TURN_RATE_MULTIPLIER = gameplay("rockets.guidance", "turnRateMultiplier", BalanceDefaults.NEUTRAL_MULTIPLIER, 0, 4, "Multiplier before speed/mass steering inertia; effective cap 45 degrees/tick.");
    public static final ModConfigSpec.DoubleValue INHERITED_VELOCITY_MULTIPLIER = gameplay("compat.sable", "inheritedVelocityMultiplier", BalanceDefaults.NEUTRAL_MULTIPLIER, 0, 2, "Fraction of native Sable velocity inherited at rocket launch; Sable remains optional.");
    public static final ModConfigSpec.IntValue CLUSTER_SUBMUNITION_COUNT = intEffectSetting("cbcAt.bigCannon.cluster", "submunitionCount", BalanceDefaults.CLUSTER_RECIPE_COUNT, 1, 64);

    public static final ModConfigSpec.DoubleValue LIGHTWEIGHT_SPEED_MULTIPLIER = gameplay("rockets.physics", "lightweightSpeedMultiplier", BalanceDefaults.LIGHTWEIGHT_SPEED_MULTIPLIER, 1, 1.5, "Lightweight powered speed advantage; same 20 blocks/tick powered ceiling applies.");
    public static final ModConfigSpec SPEC = BUILDER.build();

    private static ModConfigSpec.DoubleValue gameplay(String category, String name, double initial, double min, double max, String comment) {
        for (String part : category.split("\\.")) BUILDER.push(part);
        var result = BUILDER.comment(comment).defineInRange(name, initial, min, max);
        BUILDER.pop(category.split("\\.").length);
        return result;
    }

    private static ModConfigSpec.BooleanValue gameplayFlag(String category, String name, String comment) {
        for (String part : category.split("\\.")) BUILDER.push(part);
        var result = BUILDER.comment(comment).define(name, BalanceDefaults.GUIDANCE_ENABLED);
        BUILDER.pop(category.split("\\.").length);
        return result;
    }

    public static <T> T value(ModConfigSpec.ConfigValue<T> setting) {
        return SPEC.isLoaded() ? setting.get() : setting.getDefault();
    }

    private static RocketApSettings rocketApSettings(String tier, double mass, double penetration,
                                                    double toughness, double deflection) {
        BUILDER.push("rockets").push(tier).push("apWarhead");
        var settings = new RocketApSettings(ranged("durabilityMass", mass, 0.01, 100),
            ranged("penetration", penetration, 0, 100), ranged("toughness", toughness, 0, 100),
            ranged("deflection", deflection, 0, 1));
        BUILDER.pop(3);
        return settings;
    }

    public record RocketApSettings(ModConfigSpec.DoubleValue mass, ModConfigSpec.DoubleValue penetration,
                                   ModConfigSpec.DoubleValue toughness, ModConfigSpec.DoubleValue deflection) {
        public BallisticPropertiesComponent apply(BallisticPropertiesComponent original) {
            return new BallisticPropertiesComponent(original.gravity(), original.drag(), original.isQuadraticDrag(),
                value(mass).floatValue(), value(penetration).floatValue(), value(toughness).floatValue(),
                value(deflection).floatValue());
        }
    }

    private static RocketSettings rocketSettings(String path, BalanceDefaults.Rocket d) {
        return rocketSettings(path, d.maxSpeed(), d.turnRate(), d.fuelSeconds(), d.health(), d.seekerRange());
    }

    private static HeatSettings heatSettings(String path, BalanceDefaults.Heat d) {
        return heatSettings(path, d.blockPower(), d.entityPower(), d.jetLength(), d.jetEnergy(), d.jetPenetration(), d.jetDamage());
    }

    private static MunitionSettings munitionSettings(String path, BalanceDefaults.Munition d) {
        return munitionSettings(path, d.damage(), d.knockback(), d.gravity(), d.drag(), d.quadratic(), d.mass(), d.penetration(), d.toughness(), d.deflection());
    }

    private static MunitionSettings bigMunitionSettings(String path, BalanceDefaults.BigMunition d) {
        return bigMunitionSettings(path, d.damage(), d.knockback(), d.gravity(), d.drag(), d.quadratic(), d.mass(), d.penetration(), d.toughness(), d.deflection(), d.addedCharge(), d.minimumCharge(), d.canSquib(), d.recoil());
    }

    private static RocketApSettings rocketApSettings(String path, BalanceDefaults.Ap d) {
        return rocketApSettings(path, d.mass(), d.penetration(), d.toughness(), d.deflection());
    }

    private static ExplosionSettings explosionSettings(String path, BalanceDefaults.Explosion d) {
        return explosionSettings(path, d.blockPower(), d.entityPower());
    }

    private CbcatFixConfig() {
    }

    public static RocketSettings settings(RocketBalance.Tier tier) {
        return switch (tier) {
            case SMALL -> SMALL_ROCKET;
            case MEDIUM -> MEDIUM_ROCKET;
            case BIG -> BIG_ROCKET;
        };
    }

    private static RocketSettings rocketSettings(
        String name,
        double defaultMaxSpeed,
        double defaultGuidanceTurnRate,
        double defaultPoweredFlightSeconds,
        double defaultDurability,
        double defaultGuidanceRange
    ) {
        BUILDER.push("rockets").push(name);
        ModConfigSpec.DoubleValue maxSpeed = BUILDER
            .comment("Maximum powered speed in blocks per tick.")
            .defineInRange("maxSpeedBlocksPerTick", defaultMaxSpeed, 0.1, 20.0);
        ModConfigSpec.DoubleValue guidanceTurnRate = BUILDER
            .comment("Maximum guided turn rate in degrees per tick.")
            .defineInRange("guidedTurnRateDegreesPerTick", defaultGuidanceTurnRate, 0.0, 45.0);
        ModConfigSpec.DoubleValue poweredFlightSeconds = BUILDER
            .comment(
                "Powered flight time in seconds for a long-range rocket.",
                "A double-payload rocket receives half of this duration."
            )
            .defineInRange("poweredFlightSeconds", defaultPoweredFlightSeconds, 0.05, 120.0);
        ModConfigSpec.DoubleValue durability = BUILDER
            .comment("Damage required to shoot this rocket down.")
            .defineInRange("durability", defaultDurability, 1.0, 1000.0);
        ModConfigSpec.DoubleValue guidanceRange = BUILDER
            .comment("Maximum distance in blocks at which a guided rocket can retain its target.")
            .defineInRange("maximumGuidanceRangeBlocks", defaultGuidanceRange, 1.0, 4096.0);
        BUILDER.pop(2);
        return new RocketSettings(maxSpeed, guidanceTurnRate, poweredFlightSeconds, durability,
            guidanceRange);
    }

    private static ModConfigSpec.DoubleValue guidanceDoubleSetting(String name, double value, double min, double max, String comment) {
        BUILDER.push("rockets").push("guidance");
        ModConfigSpec.DoubleValue result = BUILDER.comment(comment).defineInRange(name, value, min, max);
        BUILDER.pop(2);
        return result;
    }

    private static HeatSettings heatSettings(
        String name,
        double defaultBlockExplosionPower,
        double defaultEntityExplosionPower,
        double defaultJetLength,
        double defaultJetEnergy,
        double defaultJetPenetration,
        double defaultJetEntityDamage
    ) {
        BUILDER.push("munitions").push("heat").push(name);
        ModConfigSpec.DoubleValue blockExplosionPower = BUILDER
            .comment("HEAT explosion power against blocks.")
            .defineInRange("blockExplosionPower", defaultBlockExplosionPower, 0.0, 32.0);
        ModConfigSpec.DoubleValue entityExplosionPower = BUILDER
            .comment("HEAT explosion power against entities.")
            .defineInRange("entityExplosionPower", defaultEntityExplosionPower, 0.0, 32.0);
        ModConfigSpec.DoubleValue jetLength = BUILDER
            .comment("Maximum HEAT jet length through air, in blocks.")
            .defineInRange("jetLengthBlocks", defaultJetLength, 0.25, 64.0);
        ModConfigSpec.DoubleValue jetEnergy = BUILDER
            .comment("Energy available for penetrating CBC block armor.")
            .defineInRange("jetEnergy", defaultJetEnergy, 0.0, 1000.0);
        ModConfigSpec.DoubleValue jetPenetration = BUILDER
            .comment("Resistance to CBC armor hardness; higher values preserve more jet energy.")
            .defineInRange("jetPenetration", defaultJetPenetration, 0.01, 100.0);
        ModConfigSpec.DoubleValue jetEntityDamage = BUILDER
            .comment("Damage dealt once to each entity intersected by the HEAT jet.")
            .defineInRange("jetEntityDamage", defaultJetEntityDamage, 0.0, 1000.0);
        BUILDER.pop(3);
        return new HeatSettings(
            blockExplosionPower,
            entityExplosionPower,
            jetLength,
            jetEnergy,
            jetPenetration,
            jetEntityDamage
        );
    }

    private static MunitionSettings munitionSettings(String path, double damage, double knockback, double gravity,
                                                     double drag, boolean quadraticDrag, double mass,
                                                     double penetration, double toughness, double deflection) {
        BUILDER.push("munitions");
        for (String part : path.split("\\.")) BUILDER.push(part);
        ModConfigSpec.DoubleValue damageValue = ranged("entityDamage", damage, 0, 1000);
        ModConfigSpec.DoubleValue knockbackValue = ranged("knockback", knockback, 0, 1000);
        ModConfigSpec.DoubleValue gravityValue = ranged("gravity", gravity, -1, 0);
        ModConfigSpec.DoubleValue dragValue = ranged("drag", drag, 0, 1);
        ModConfigSpec.BooleanValue quadraticValue = BUILDER.define("quadraticDrag", quadraticDrag);
        ModConfigSpec.DoubleValue massValue = ranged("durabilityMass", mass, 0, 100);
        ModConfigSpec.DoubleValue penetrationValue = ranged("penetration", penetration, 0, 100);
        ModConfigSpec.DoubleValue toughnessValue = ranged("toughness", toughness, 0, 100);
        ModConfigSpec.DoubleValue deflectionValue = ranged("deflection", deflection, 0, 1);
        BUILDER.pop(path.split("\\.").length + 1);
        return new MunitionSettings(damageValue, knockbackValue, gravityValue, dragValue, quadraticValue, massValue,
            penetrationValue, toughnessValue, deflectionValue, null, null, null, null);
    }

    private static MunitionSettings bigMunitionSettings(String path, double damage, double knockback, double gravity,
                                                        double drag, boolean quadraticDrag, double mass,
                                                        double penetration, double toughness, double deflection,
                                                        double addedCharge, double minimumCharge, boolean canSquib,
                                                        double addedRecoil) {
        BUILDER.push("munitions");
        for (String part : path.split("\\.")) BUILDER.push(part);
        ModConfigSpec.DoubleValue damageValue = ranged("entityDamage", damage, 0, 1000);
        ModConfigSpec.DoubleValue knockbackValue = ranged("knockback", knockback, 0, 1000);
        ModConfigSpec.DoubleValue gravityValue = ranged("gravity", gravity, -1, 0);
        ModConfigSpec.DoubleValue dragValue = ranged("drag", drag, 0, 1);
        ModConfigSpec.BooleanValue quadraticValue = BUILDER.define("quadraticDrag", quadraticDrag);
        ModConfigSpec.DoubleValue massValue = ranged("durabilityMass", mass, 0, 100);
        ModConfigSpec.DoubleValue penetrationValue = ranged("penetration", penetration, 0, 100);
        ModConfigSpec.DoubleValue toughnessValue = ranged("toughness", toughness, 0, 100);
        ModConfigSpec.DoubleValue deflectionValue = ranged("deflection", deflection, 0, 1);
        ModConfigSpec.DoubleValue addedChargeValue = ranged("addedChargePower", addedCharge, 0, 100);
        ModConfigSpec.DoubleValue minimumChargeValue = ranged("minimumChargePower", minimumCharge, 0, 1000);
        ModConfigSpec.BooleanValue canSquibValue = BUILDER.define("canSquib", canSquib);
        ModConfigSpec.DoubleValue addedRecoilValue = ranged("addedRecoil", addedRecoil, 0, 100);
        BUILDER.pop(path.split("\\.").length + 1);
        return new MunitionSettings(damageValue, knockbackValue, gravityValue, dragValue, quadraticValue, massValue,
            penetrationValue, toughnessValue, deflectionValue, addedChargeValue, minimumChargeValue,
            canSquibValue, addedRecoilValue);
    }

    private static ModConfigSpec.DoubleValue effectSetting(String path, String name, double value, double min, double max) {
        BUILDER.push("munitions");
        for (String part : path.split("\\.")) BUILDER.push(part);
        ModConfigSpec.DoubleValue result = ranged(name, value, min, max);
        BUILDER.pop(path.split("\\.").length + 1);
        return result;
    }

    private static ExplosionSettings explosionSettings(String path, double blockPower, double entityPower) {
        return new ExplosionSettings(
            effectSetting(path, "blockExplosionPower", blockPower, 0, 32),
            effectSetting(path, "entityExplosionPower", entityPower, 0, 32)
        );
    }

    private static ModConfigSpec.IntValue intEffectSetting(String path, String name, int value, int min, int max) {
        BUILDER.push("munitions");
        for (String part : path.split("\\.")) BUILDER.push(part);
        ModConfigSpec.IntValue result = BUILDER.comment(description(name)).defineInRange(name, value, min, max);
        BUILDER.pop(path.split("\\.").length + 1);
        return result;
    }

    private static ModConfigSpec.DoubleValue ranged(String name, double value, double min, double max) {
        return BUILDER.comment(description(name)).defineInRange(name, value, min, max);
    }

    private static String description(String name) {
        return switch (name) {
            case "entityDamage" -> "Direct hit damage in health points, before armor and other native damage rules.";
            case "knockback" -> "Native projectile knockback strength.";
            case "gravity" -> "Vertical ballistic acceleration in blocks/tick squared; zero disables gravity.";
            case "drag" -> "Native linear or quadratic drag coefficient.";
            case "durabilityMass" -> "CBC penetrator mass consumed by impacts; distinct from rocket hull health.";
            case "penetration" -> "CBC armor penetration coefficient; not a fixed number of blocks.";
            case "toughness" -> "CBC resistance to projectile shatter.";
            case "deflection" -> "CBC angle coefficient used by the native ricochet calculation.";
            case "addedChargePower" -> "Additional native big-cannon charge power.";
            case "minimumChargePower" -> "Minimum charge power used by native squib checks.";
            case "addedRecoil" -> "Additional native big-cannon recoil.";
            case "blockExplosionPower" -> "Explosion strength against terrain, subject to CBC damage restrictions.";
            case "entityExplosionPower" -> "Explosion strength against entities.";
            case "shrapnelCount", "burstParticleProjectileCount" -> "Number of native fragment projectiles per detonation.";
            case "maximumSpeedBlocksPerTick" -> "Projectile speed cap in blocks per tick.";
            case "cbcFlakExplosionScale", "cbcFlakBlockExplosionScale", "cbcFlakEntityExplosionScale" -> "Multiplier applied to the corresponding CBC flak explosion property.";
            case "cbcFlakFragmentCountScale" -> "Multiplier applied to the CBC flak fragment count.";
            case "cbcFlakFragmentSpreadScale" -> "Multiplier applied to the CBC flak fragment spread.";
            case "smokeDurationScale" -> "Multiplier applied to native smoke lifetime.";
            case "smokeSizeScale" -> "Multiplier applied to native smoke size.";
            case "submunitionCount" -> "Emitted cluster submunitions; crafting still consumes eight parts. Fuzes repeat in assembly order.";
            case "submunitionLifetimeTicks" -> "Maximum emitted submunition lifetime in ticks (20 ticks per second).";
            case "submunitionSpeedScale" -> "Multiplier applied to native cluster release speed.";
            case "submunitionInaccuracyDegrees" -> "Native shoot inaccuracy parameter for cluster release; larger values widen the spread.";
            default -> name;
        };
    }

    public record RocketSettings(
        ModConfigSpec.DoubleValue maxSpeedBlocksPerTick,
        ModConfigSpec.DoubleValue guidedTurnRateDegreesPerTick,
        ModConfigSpec.DoubleValue poweredFlightSeconds,
        ModConfigSpec.DoubleValue durability,
        ModConfigSpec.DoubleValue maximumGuidanceRangeBlocks
    ) {
        public double maxSpeed() {
            return Math.min(BalanceDefaults.MAX_POWERED_SPEED, value(maxSpeedBlocksPerTick) * value(VELOCITY_MULTIPLIER));
        }

        public float guidanceTurnRate() {
            return (float) Math.min(45, value(guidedTurnRateDegreesPerTick) * value(TURN_RATE_MULTIPLIER));
        }

        public int poweredFlightTicks() {
            return Math.clamp((int) Math.round(value(poweredFlightSeconds) * 20.0 * value(FUEL_MULTIPLIER)), 1, BalanceDefaults.MAX_FUEL_TICKS);
        }

        public float maximumDurability() {
            return value(durability).floatValue();
        }

        public double maximumGuidanceRange() {
            return value(maximumGuidanceRangeBlocks);
        }
    }

    public record HeatSettings(
        ModConfigSpec.DoubleValue blockExplosionPowerValue,
        ModConfigSpec.DoubleValue entityExplosionPowerValue,
        ModConfigSpec.DoubleValue jetLengthValue,
        ModConfigSpec.DoubleValue jetEnergyValue,
        ModConfigSpec.DoubleValue jetPenetrationValue,
        ModConfigSpec.DoubleValue jetEntityDamageValue
    ) {
        public float blockExplosionPower() {
            return value(blockExplosionPowerValue).floatValue();
        }

        public float entityExplosionPower() {
            return value(entityExplosionPowerValue).floatValue();
        }

        public double jetLength() {
            return value(jetLengthValue);
        }

        public double jetEnergy() {
            return value(jetEnergyValue);
        }

        public double jetPenetration() {
            return value(jetPenetrationValue);
        }

        public float jetEntityDamage() {
            return value(jetEntityDamageValue).floatValue();
        }
    }

    public record MunitionSettings(
        ModConfigSpec.DoubleValue entityDamageValue,
        ModConfigSpec.DoubleValue knockbackValue,
        ModConfigSpec.DoubleValue gravityValue,
        ModConfigSpec.DoubleValue dragValue,
        ModConfigSpec.BooleanValue quadraticDragValue,
        ModConfigSpec.DoubleValue durabilityMassValue,
        ModConfigSpec.DoubleValue penetrationValue,
        ModConfigSpec.DoubleValue toughnessValue,
        ModConfigSpec.DoubleValue deflectionValue,
        ModConfigSpec.DoubleValue addedChargePowerValue,
        ModConfigSpec.DoubleValue minimumChargePowerValue,
        ModConfigSpec.BooleanValue canSquibValue,
        ModConfigSpec.DoubleValue addedRecoilValue
    ) {
        public EntityDamagePropertiesComponent damageProperties() {
            return new EntityDamagePropertiesComponent(value(entityDamageValue).floatValue(), false, true, false,
                value(knockbackValue).floatValue());
        }

        public BallisticPropertiesComponent ballisticProperties() {
            return new BallisticPropertiesComponent(value(gravityValue), value(dragValue), value(quadraticDragValue),
                value(durabilityMassValue).floatValue(), value(penetrationValue).floatValue(),
                value(toughnessValue).floatValue(), value(deflectionValue).floatValue());
        }

        public BigCannonProjectilePropertiesComponent bigCannonProperties() {
            if (addedChargePowerValue == null || minimumChargePowerValue == null || canSquibValue == null || addedRecoilValue == null) {
                throw new IllegalStateException("Big-cannon properties requested for a non-big-cannon munition");
            }
            return new BigCannonProjectilePropertiesComponent(value(addedChargePowerValue).floatValue(),
                value(minimumChargePowerValue).floatValue(), value(canSquibValue), value(addedRecoilValue).floatValue());
        }
    }

    public record ExplosionSettings(ModConfigSpec.DoubleValue blockPowerValue, ModConfigSpec.DoubleValue entityPowerValue) {
        public float blockPower(float scale) {
            return value(blockPowerValue).floatValue() * scale;
        }

        public float entityPower(float scale) {
            return value(entityPowerValue).floatValue() * scale;
        }
    }

}
