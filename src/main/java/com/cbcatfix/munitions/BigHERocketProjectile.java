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
import com.cbcatfix.CbcatFixHelper;

public class BigHERocketProjectile extends AbstractMediumFuzedRocket<HA_HEProjectile> {
    public BigHERocketProjectile(EntityType<? extends BigHERocketProjectile> type, Level level) {
        super(type, level, 1.0d, 9.0d, EntityRegister.HA_HE_PROJECTILE);
    }

    @Override
    public void tick() {
        super.tick();
        CbcatFixHelper.spawnBigRocketParticles(this);
    }

    @Override
    protected void detonate(Position pos) {
        if (!this.level().isClientSide()) {
            ShellExplosion explosion = new ShellExplosion(
                this.level(), this, this.indirectArtilleryFire(false),
                pos.x(), pos.y(), pos.z(),
                12.0f, 16.0f, true, // Slightly decreased explosion size (was 18/24)
                CBCConfigs.server().munitions.damageRestriction.get().explosiveInteraction()
            );
            CreateBigCannons.handleCustomExplosion(this.level(), explosion);
        }
    }

    @Override
    protected double getDefaultGravity() {
        return -0.015d;
    }
}
