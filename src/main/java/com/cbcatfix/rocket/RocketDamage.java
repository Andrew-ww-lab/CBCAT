package com.cbcatfix.rocket;

import com.cbcatfix.mixin.RocketDetonationInvoker;
import com.cbcatfix.mixin.AbstractCannonProjectileAccess;
import net.minecraft.world.phys.Vec3;
import rbasamoyai.createbigcannons.munitions.AbstractCannonProjectile;

public final class RocketDamage {
    private RocketDamage() {}

    public static void disableEngine(AbstractCannonProjectile rocket) {
        RocketPayloadAccess payload = (RocketPayloadAccess) rocket;
        ((RocketEngineAccess) rocket).cbcatfix$setEngineDisabled(true);
        payload.cbcatfix$setPoweredFlightTicks(0);
        payload.cbcatfix$breakGuidance();
        if (rocket instanceof com.dsvv.cbcat.cannon.rocketpod.munitions.AbstractRocket<?> small) {
            small.setFuel(0);
        } else if (rocket instanceof com.dsvv.cbcat.cannon.medium_rocketpod.munitions.AbstractMediumRocket<?> medium) {
            medium.setFuel(0);
        }
    }

    public static void damageHull(AbstractCannonProjectile rocket, float damage, Vec3 position) {
        if (rocket.level().isClientSide() || rocket.isRemoved()) return;
        RocketPayloadAccess payload = (RocketPayloadAccess) rocket;
        if (payload.cbcatfix$damageDurability(damage)) return;
        disableEngine(rocket);
        if (!payload.cbcatfix$isArmorPiercing() && rocket instanceof RocketDetonationInvoker explosive
            && !((AbstractCannonProjectileAccess) rocket).cbcatfix$isPendingRemoval()) {
            // The detonation guard marks before invoking the explosion, preventing recursive damage.
            explosive.cbcatfix$detonate(position);
            rocket.discard();
        }
        // An AP penetrator keeps its remaining CBC mass and velocity, even at zero hull HP.
    }
}
