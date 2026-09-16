package com.cbcatfix.mixin;

import com.cbcatfix.RocketDetonationContext;
import com.cbcatfix.config.CbcatFixConfig;
import net.minecraft.core.Position;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rbasamoyai.createbigcannons.CreateBigCannons;
import rbasamoyai.createbigcannons.config.CBCConfigs;
import rbasamoyai.createbigcannons.index.CBCEntityTypes;
import rbasamoyai.createbigcannons.index.CBCMunitionPropertiesHandlers;
import rbasamoyai.createbigcannons.munitions.AbstractCannonProjectile;
import rbasamoyai.createbigcannons.munitions.ShellExplosion;
import rbasamoyai.createbigcannons.munitions.autocannon.flak.FlakExplosion;
import rbasamoyai.createbigcannons.munitions.autocannon.flak.FlakAutocannonProjectileProperties;
import rbasamoyai.createbigcannons.munitions.fragment_burst.CBCProjectileBurst;

@Mixin(targets = {
    "com.dsvv.cbcat.cannon.autocannon.munitions.he.AutocannonHEProjectile",
    "com.dsvv.cbcat.cannon.autocannon.munitions.hei.AutocannonHEIProjectile",
    "com.dsvv.cbcat.cannon.heavy_autocannon.munitions.he_shell.HA_HEProjectile",
    "com.dsvv.cbcat.cannon.heavy_autocannon.munitions.hef_shell.HA_HEFProjectile"
}, remap = false)
public class CbcatSpecialMunitionEffectsMixin {

    @Inject(method = "detonate", at = @At("HEAD"), cancellable = true)
    private void cbcatfix$detonate(Position position, CallbackInfo ci) {
        AbstractCannonProjectile projectile = (AbstractCannonProjectile) (Object) this;
        String type = this.getClass().getSimpleName();
        float payloadScale = RocketDetonationContext.get() ? RocketDetonationContext.scale() : 1.0f;

        switch (type) {
            case "AutocannonHEProjectile" -> cbcatfix$explodeFromCbcFlak(projectile, position, false,
                CbcatFixConfig.value(CbcatFixConfig.AUTOCANNON_HE_EXPLOSION_SCALE).floatValue(), payloadScale);
            case "AutocannonHEIProjectile" -> cbcatfix$explodeFromCbcFlak(projectile, position, true, 1.0f, payloadScale);
            case "HA_HEProjectile" -> cbcatfix$explodeFromCbcFlak(projectile, position, false,
                CbcatFixConfig.value(CbcatFixConfig.HEAVY_AUTOCANNON_HE_EXPLOSION_SCALE).floatValue(), payloadScale);
            case "HA_HEFProjectile" -> cbcatfix$fragment(projectile, position, payloadScale);
            default -> throw new IllegalStateException("Unsupported CBCAT special munition: " + this.getClass().getName());
        }
        ci.cancel();
    }

    private static void cbcatfix$explodeFromCbcFlak(AbstractCannonProjectile projectile, Position position,
                                                    boolean causesFire, float cbcScale, float payloadScale) {
        FlakAutocannonProjectileProperties cbcProperties = cbcatfix$cbcFlakProperties();
        ShellExplosion explosion = new ShellExplosion(projectile.level(), projectile,
            projectile.indirectArtilleryFire(false), position.x(), position.y(), position.z(),
            cbcProperties.explosion().blockDamagePower() * cbcScale * payloadScale,
            cbcProperties.explosion().entityDamagePower() * cbcScale * payloadScale,
            causesFire, CBCConfigs.server().munitions.damageRestriction.get().explosiveInteraction());
        CreateBigCannons.handleCustomExplosion(projectile.level(), explosion);
    }

    private static void cbcatfix$fragment(AbstractCannonProjectile projectile, Position position, float scale) {
        FlakAutocannonProjectileProperties cbcProperties = cbcatfix$cbcFlakProperties();
        FlakExplosion explosion = new FlakExplosion(projectile.level(), null, projectile.indirectArtilleryFire(false),
            position.x(), position.y(), position.z(),
            cbcProperties.explosion().blockDamagePower() * CbcatFixConfig.value(CbcatFixConfig.HEAVY_AUTOCANNON_HEF_BLOCK_EXPLOSION_SCALE).floatValue() * scale,
            cbcProperties.explosion().entityDamagePower() * CbcatFixConfig.value(CbcatFixConfig.HEAVY_AUTOCANNON_HEF_ENTITY_EXPLOSION_SCALE).floatValue() * scale,
            CBCConfigs.server().munitions.damageRestriction.get().explosiveInteraction());
        CreateBigCannons.handleCustomExplosion(projectile.level(), explosion);
        CBCProjectileBurst.spawnConeBurst(projectile.level(), CBCEntityTypes.FLAK_BURST.get(),
            new Vec3(position.x(), position.y(), position.z()), projectile.getDeltaMovement(),
            Math.max(0, Math.round(cbcProperties.flakBurst().burstProjectileCount()
                * CbcatFixConfig.value(CbcatFixConfig.HEAVY_AUTOCANNON_HEF_FRAGMENT_COUNT_SCALE).floatValue())),
            cbcProperties.flakBurst().burstSpread()
                * CbcatFixConfig.value(CbcatFixConfig.HEAVY_AUTOCANNON_HEF_FRAGMENT_SPREAD_SCALE).floatValue());
    }

    private static FlakAutocannonProjectileProperties cbcatfix$cbcFlakProperties() {
        return CBCMunitionPropertiesHandlers.FLAK_AUTOCANNON.getPropertiesOf(CBCEntityTypes.FLAK_AUTOCANNON.get());
    }
}
