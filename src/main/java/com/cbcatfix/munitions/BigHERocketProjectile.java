package com.cbcatfix.munitions;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.core.Position;
import com.dsvv.cbcat.cannon.medium_rocketpod.munitions.AbstractMediumFuzedRocket;
import com.dsvv.cbcat.cannon.heavy_autocannon.munitions.AbstractHeavyAutocannonProjectile;
import com.cbcatfix.config.CbcatFixConfig;
import com.cbcatfix.rocket.RocketBallistics;
import rbasamoyai.createbigcannons.munitions.config.components.BallisticPropertiesComponent;
import rbasamoyai.createbigcannons.munitions.config.components.EntityDamagePropertiesComponent;
import rbasamoyai.createbigcannons.config.CBCConfigs;
import rbasamoyai.createbigcannons.CreateBigCannons;
import rbasamoyai.createbigcannons.munitions.ShellExplosion;
import rbasamoyai.createbigcannons.index.CBCEntityTypes;
import rbasamoyai.createbigcannons.index.CBCMunitionPropertiesHandlers;
import rbasamoyai.createbigcannons.munitions.big_cannon.config.BigCannonCommonShellProperties;
import com.cbcatfix.rocket.RocketPayloadAccess;

public class BigHERocketProjectile extends AbstractMediumFuzedRocket<AbstractHeavyAutocannonProjectile> {
    public BigHERocketProjectile(EntityType<? extends BigHERocketProjectile> type, Level level) {
        super(type, level, 1.0d, 9.0d, null);
    }

    @Override
    public EntityDamagePropertiesComponent getDamageProperties() {
        return CBCMunitionPropertiesHandlers.COMMON_SHELL_BIG_CANNON_PROJECTILE
            .getPropertiesOf(CBCEntityTypes.HE_SHELL.get()).damage();
    }

    @Override
    protected BallisticPropertiesComponent getBallisticProperties() {
        // Retain the existing carrier mass/ballistics so changing the warhead does not change flight.
        // No heavy projectile is constructed, and neither its damage nor explosion is used.
        return RocketBallistics.properties(this, CbcatFixConfig.HEAVY_AUTOCANNON_HE.ballisticProperties());
    }

    @Override
    protected void detonate(Position pos) {
        if (!this.level().isClientSide()) {
            int payloadCount = ((RocketPayloadAccess) this).cbcatfix$getPayloadCount();
            float payloadScale = com.cbcatfix.rocket.RocketBalance.payloadScale(payloadCount);
            BigCannonCommonShellProperties heProperties =
                CBCMunitionPropertiesHandlers.COMMON_SHELL_BIG_CANNON_PROJECTILE
                    .getPropertiesOf(CBCEntityTypes.HE_SHELL.get());
            ShellExplosion explosion = new ShellExplosion(
                this.level(), this, this.indirectArtilleryFire(false),
                pos.x(), pos.y(), pos.z(),
                heProperties.explosion().blockDamagePower() * payloadScale,
                heProperties.explosion().entityDamagePower() * payloadScale,
                false,
                CBCConfigs.server().munitions.damageRestriction.get().explosiveInteraction()
            );
            CreateBigCannons.handleCustomExplosion(this.level(), explosion);
        }
    }

}
