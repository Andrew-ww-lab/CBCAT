package com.cbcatfix.munitions;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.core.Position;
import com.dsvv.cbcat.cannon.medium_rocketpod.munitions.AbstractMediumFuzedRocket;
import com.dsvv.cbcat.cannon.heavy_autocannon.munitions.he_shell.HA_HEProjectile;
import com.dsvv.cbcat.registry.EntityRegister;
import rbasamoyai.createbigcannons.config.CBCConfigs;
import rbasamoyai.createbigcannons.CreateBigCannons;
import rbasamoyai.createbigcannons.munitions.ShellExplosion;
import rbasamoyai.createbigcannons.index.CBCEntityTypes;
import rbasamoyai.createbigcannons.index.CBCMunitionPropertiesHandlers;
import rbasamoyai.createbigcannons.munitions.big_cannon.config.BigCannonCommonShellProperties;
import com.cbcatfix.rocket.RocketPayloadAccess;

public class BigHERocketProjectile extends AbstractMediumFuzedRocket<HA_HEProjectile> {
    public BigHERocketProjectile(EntityType<? extends BigHERocketProjectile> type, Level level) {
        super(type, level, 1.0d, 9.0d, EntityRegister.HA_HE_PROJECTILE);
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
