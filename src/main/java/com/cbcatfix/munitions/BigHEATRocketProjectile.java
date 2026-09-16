package com.cbcatfix.munitions;

import net.minecraft.core.Position;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import com.dsvv.cbcat.cannon.medium_rocketpod.munitions.AbstractMediumFuzedRocket;
import com.dsvv.cbcat.cannon.heavy_autocannon.munitions.he_shell.HA_HEProjectile;
import com.dsvv.cbcat.registry.EntityRegister;
import com.cbcatfix.rocket.RocketPayloadAccess;
import com.cbcatfix.config.CbcatFixConfig;

public class BigHEATRocketProjectile extends AbstractMediumFuzedRocket<HA_HEProjectile> {
    public BigHEATRocketProjectile(EntityType<? extends BigHEATRocketProjectile> type, Level level) {
        super(type, level, 1.0d, 9.0d, EntityRegister.HA_HE_PROJECTILE);
    }

    @Override
    protected void detonate(Position pos) {
        int payloadCount = ((RocketPayloadAccess) this).cbcatfix$getPayloadCount();
        float payloadScale = com.cbcatfix.rocket.RocketBalance.payloadScale(payloadCount);
        HeatEffect.detonate(this, pos, this.getDeltaMovement(), payloadScale, CbcatFixConfig.BIG_ROCKET_HEAT);
    }

}
