package com.cbcatfix.munitions;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import com.dsvv.cbcat.cannon.medium_rocketpod.munitions.AbstractMediumRocket;
import com.dsvv.cbcat.cannon.heavy_autocannon.munitions.ap_shot.HA_APProjectile;
import com.dsvv.cbcat.registry.EntityRegister;
import com.cbcatfix.CbcatFixHelper;

public class BigAPRocketProjectile extends AbstractMediumRocket<HA_APProjectile> {
    public BigAPRocketProjectile(EntityType<? extends BigAPRocketProjectile> type, Level level) {
        super(type, level, 1.0d, 9.0d, EntityRegister.HA_AP_PROJECTILE);
    }

    @Override
    public void tick() {
        super.tick();
        CbcatFixHelper.spawnBigRocketParticles(this);
    }

    @Override
    protected double getDefaultGravity() {
        return -0.015d;
    }
}
