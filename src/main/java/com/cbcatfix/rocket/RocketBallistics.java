package com.cbcatfix.rocket;

import com.cbcatfix.config.CbcatFixConfig;
import com.cbcatfix.munitions.BigAPRocketProjectile;
import com.dsvv.cbcat.cannon.rocketpod.munitions.ap_rocket.AP_Rocket;
import com.dsvv.cbcat.cannon.medium_rocketpod.munitions.medium_ap_rocket.MediumAPRocket;
import rbasamoyai.createbigcannons.munitions.AbstractCannonProjectile;
import rbasamoyai.createbigcannons.munitions.config.components.BallisticPropertiesComponent;

public final class RocketBallistics {
    private RocketBallistics() {}

    public static boolean isApProjectile(AbstractCannonProjectile rocket) {
        return rocket instanceof AP_Rocket || rocket instanceof MediumAPRocket || rocket instanceof BigAPRocketProjectile;
    }

    public static BallisticPropertiesComponent properties(AbstractCannonProjectile rocket,
                                                          BallisticPropertiesComponent original) {
        // Called during construction, before payload flags have been configured.
        var configured = !isApProjectile(rocket) ? original : switch (RocketBalance.tierForProjectile(rocket)) {
            case SMALL -> CbcatFixConfig.SMALL_ROCKET_AP.apply(original);
            case MEDIUM -> CbcatFixConfig.MEDIUM_ROCKET_AP.apply(original);
            case BIG -> CbcatFixConfig.BIG_ROCKET_AP.apply(original);
        };
        // Carrier aerodynamics belong to the rocket size, not the stored autocannon warhead.
        var airframe = switch (RocketBalance.tierForProjectile(rocket)) {
            case SMALL -> com.cbcatfix.balance.BalanceDefaults.SMALL_AIRFRAME;
            case MEDIUM -> com.cbcatfix.balance.BalanceDefaults.MEDIUM_AIRFRAME;
            case BIG -> com.cbcatfix.balance.BalanceDefaults.BIG_AIRFRAME;
        };
        return new BallisticPropertiesComponent(airframe.gravity() * CbcatFixConfig.value(CbcatFixConfig.GRAVITY_MULTIPLIER),
            airframe.drag(), false, configured.durabilityMass(),
            configured.penetration() * CbcatFixConfig.value(CbcatFixConfig.PENETRATION_MULTIPLIER).floatValue(),
            configured.toughness(), configured.deflection());
    }
}
