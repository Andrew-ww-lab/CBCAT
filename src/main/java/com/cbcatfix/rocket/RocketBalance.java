package com.cbcatfix.rocket;

import com.cbcatfix.config.CbcatFixConfig;
import com.cbcatfix.mixin.AbstractCannonProjectileAccess;
import com.cbcatfix.munitions.BigAPRocketProjectile;
import com.cbcatfix.munitions.BigHERocketProjectile;
import com.cbcatfix.munitions.BigHEATRocketProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import rbasamoyai.createbigcannons.index.CBCDataComponents;
import rbasamoyai.createbigcannons.munitions.AbstractCannonProjectile;

public final class RocketBalance {
    private RocketBalance() {
    }

    public static void configureProjectile(ItemStack rocketStack, AbstractCannonProjectile projectile) {
        if (!(projectile instanceof RocketPayloadAccess payloadAccess)) {
            return;
        }

        ItemContainerContents payload = rocketStack.getOrDefault(CBCDataComponents.PROJECTILE, ItemContainerContents.EMPTY);
        ItemStack warhead = payload.copyOne();
        int payloadCount = warhead.isEmpty() ? 1 : Math.clamp(warhead.getCount(), 1, 2);
        ItemStack fuze = rocketStack.getOrDefault(CBCDataComponents.FUZE, ItemContainerContents.EMPTY).copyOne();
        SableGuidanceCompat.initializeAtLaunch(fuze, projectile);
        boolean armorPiercing = isArmorPiercing(warhead);
        payloadAccess.cbcatfix$configurePayload(payloadCount, fuze, armorPiercing);

        Tier tier = tierForProjectile(projectile);
        payloadAccess.cbcatfix$configureFlight(maxSpeed(rocketStack), guidanceTurnRate(tier));
        payloadAccess.cbcatfix$configureDurability(maximumDurability(rocketStack));

        int fuel = RocketStackFactory.getFuelTicks(rocketStack);
        payloadAccess.cbcatfix$setPoweredFlightTicks(fuel);
        if (projectile instanceof com.dsvv.cbcat.cannon.rocketpod.munitions.AbstractRocket<?> smallRocket) {
            smallRocket.setFuel(Math.min(fuel, 127));
        } else if (projectile instanceof com.dsvv.cbcat.cannon.medium_rocketpod.munitions.AbstractMediumRocket<?> mediumRocket) {
            mediumRocket.setFuel(Math.min(fuel, 127));
        }

        float payloadScale = payloadScale(payloadCount);
        float weightScale = RocketStackFactory.isLightweight(rocketStack)
            ? CbcatFixConfig.value(CbcatFixConfig.LIGHTWEIGHT_MASS_MULTIPLIER).floatValue() : 1.0f;
        // Lightweight construction changes the carrier, not the AP penetrator.
        projectile.setProjectileMass(projectile.getProjectileMass() * payloadScale);
        payloadAccess.cbcatfix$flightState().launchMass = projectile.getProjectileMass() * weightScale;
        payloadAccess.cbcatfix$flightState().motorMassScale = payloadScale * weightScale;
        AbstractCannonProjectileAccess damageAccess = (AbstractCannonProjectileAccess) projectile;
        damageAccess.cbcatfix$setDamage(damageAccess.cbcatfix$getDamage() * payloadScale
            * CbcatFixConfig.value(CbcatFixConfig.ROCKET_DAMAGE_MULTIPLIER).floatValue());
    }

    public static float payloadScale(int payloadCount) {
        return payloadCount > 1 ? CbcatFixConfig.value(CbcatFixConfig.DOUBLE_PAYLOAD_MULTIPLIER).floatValue() : 1.0f;
    }

    public static float maximumDurability(ItemStack stack) {
        return Math.max(1.0f, maximumDurability(tierForRocketItem(stack.getItem())) * (RocketStackFactory.isLightweight(stack)
            ? CbcatFixConfig.value(CbcatFixConfig.LIGHTWEIGHT_DURABILITY_MULTIPLIER).floatValue() : 1.0f));
    }

    public static double responseTicks(Tier tier) {
        return switch (tier) {
            case SMALL -> com.cbcatfix.balance.BalanceDefaults.SMALL_RESPONSE_TICKS;
            case MEDIUM -> com.cbcatfix.balance.BalanceDefaults.MEDIUM_RESPONSE_TICKS;
            case BIG -> com.cbcatfix.balance.BalanceDefaults.BIG_RESPONSE_TICKS;
        };
    }

    public static double launcherRate(Tier tier, int lanes) {
        double base = switch (tier) {
            case SMALL -> com.cbcatfix.balance.BalanceDefaults.SMALL_FIRE_RATE;
            case MEDIUM -> com.cbcatfix.balance.BalanceDefaults.MEDIUM_FIRE_RATE;
            case BIG -> com.cbcatfix.balance.BalanceDefaults.BIG_FIRE_RATE;
        };
        return base * CbcatFixConfig.value(CbcatFixConfig.LAUNCHER_FIRE_RATE_MULTIPLIER)
            * (1 + CbcatFixConfig.value(CbcatFixConfig.MULTI_LAUNCHER_BONUS) * Math.max(0, lanes - 1));
    }

    public static double maxSpeed(ItemStack stack) {
        double speed = maxSpeed(tierForRocketItem(stack.getItem()));
        return Math.min(com.cbcatfix.balance.BalanceDefaults.MAX_POWERED_SPEED, speed * (RocketStackFactory.isLightweight(stack)
            ? CbcatFixConfig.value(CbcatFixConfig.LIGHTWEIGHT_SPEED_MULTIPLIER) : 1.0));
    }

    public static double maxSpeed(Tier tier) {
        return CbcatFixConfig.settings(tier).maxSpeed();
    }

    public static float guidanceTurnRate(Tier tier) {
        return CbcatFixConfig.settings(tier).guidanceTurnRate();
    }

    public static int fullFlightTicks(Tier tier) {
        return CbcatFixConfig.settings(tier).poweredFlightTicks();
    }

    public static float maximumDurability(Tier tier) {
        return CbcatFixConfig.settings(tier).maximumDurability();
    }

    public static boolean isArmorPiercing(ItemStack warhead) {
        return warhead.is(rbasamoyai.createbigcannons.index.CBCItems.AP_AUTOCANNON_ROUND.get())
            || warhead.is(com.dsvv.cbcat.registry.ItemRegister.HA_AP_ITEM.get())
            || warhead.is(rbasamoyai.createbigcannons.index.CBCBlocks.AP_SHOT.asItem());
    }

    public static Tier tierForProjectile(AbstractCannonProjectile projectile) {
        if (projectile instanceof BigAPRocketProjectile
            || projectile instanceof BigHERocketProjectile
            || projectile instanceof BigHEATRocketProjectile) {
            return Tier.BIG;
        }
        if (projectile instanceof com.dsvv.cbcat.cannon.medium_rocketpod.munitions.AbstractMediumRocket<?>) {
            return Tier.MEDIUM;
        }
        return Tier.SMALL;
    }

    public static Tier tierForRocketItem(Item item) {
        if (item instanceof com.cbcatfix.munitions.BigAPRocketItem
            || item instanceof com.cbcatfix.munitions.BigHERocketItem
            || item instanceof com.cbcatfix.munitions.BigHEATRocketItem) {
            return Tier.BIG;
        }
        if (item instanceof com.dsvv.cbcat.cannon.rocketpod.munitions.AbstractRocketItem) {
            return Tier.SMALL;
        }
        return Tier.MEDIUM;
    }

    public enum Tier {
        SMALL,
        MEDIUM,
        BIG
    }
}
