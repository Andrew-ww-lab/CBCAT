package com.cbcatfix.munitions;

import com.cbcatfix.config.CbcatFixConfig;
import net.minecraft.core.Position;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.Vec3;
import rbasamoyai.createbigcannons.CreateBigCannons;
import rbasamoyai.createbigcannons.index.CBCEntityTypes;
import rbasamoyai.createbigcannons.index.CBCMunitionPropertiesHandlers;
import rbasamoyai.createbigcannons.munitions.AbstractCannonProjectile;
import rbasamoyai.createbigcannons.munitions.big_cannon.smoke_shell.SmokeEmitterEntity;
import rbasamoyai.createbigcannons.munitions.big_cannon.smoke_shell.SmokeExplosion;
import rbasamoyai.createbigcannons.munitions.big_cannon.smoke_shell.SmokeShellProperties;

public final class CbcatSmokeCompat {
    private CbcatSmokeCompat() {
    }

    public static void detonate(AbstractCannonProjectile projectile, Position position, float scale) {
        CbcatFixConfig.ExplosionSettings settings = CbcatFixConfig.HEAVY_AUTOCANNON_SMOKE_EXPLOSION;
        SmokeExplosion explosion = new SmokeExplosion(
            projectile.level(), null, position.x(), position.y(), position.z(),
            settings.blockPower(scale), settings.entityPower(scale), Explosion.BlockInteraction.KEEP
        );
        CreateBigCannons.handleCustomExplosion(projectile.level(), explosion);

        SmokeShellProperties properties = CBCMunitionPropertiesHandlers.SMOKE_SHELL
            .getPropertiesOf(CBCEntityTypes.SMOKE_SHELL.get());
        SmokeEmitterEntity smoke = CBCEntityTypes.SMOKE_EMITTER.create(projectile.level());
        if (smoke != null) {
            smoke.setPos(new Vec3(position.x(), position.y(), position.z()));
            smoke.setDuration((int) Math.round(
                properties.smokeDuration() * CbcatFixConfig.value(CbcatFixConfig.HEAVY_AUTOCANNON_SMOKE_DURATION_SCALE)
            ));
            smoke.setSize(
                properties.smokeScale()
                    * CbcatFixConfig.value(CbcatFixConfig.HEAVY_AUTOCANNON_SMOKE_SIZE_SCALE).floatValue()
                    * scale
            );
            projectile.level().addFreshEntity(smoke);
        }
    }
}
