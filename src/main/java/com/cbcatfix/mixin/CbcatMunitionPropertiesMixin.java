package com.cbcatfix.mixin;

import com.cbcatfix.config.CbcatFixConfig;
import com.cbcatfix.config.CbcatFixConfig.MunitionSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rbasamoyai.createbigcannons.munitions.big_cannon.config.BigCannonProjectilePropertiesComponent;
import rbasamoyai.createbigcannons.munitions.config.components.BallisticPropertiesComponent;
import rbasamoyai.createbigcannons.munitions.config.components.EntityDamagePropertiesComponent;

@Mixin(targets = {
    "com.dsvv.cbcat.cannon.autocannon.munitions.apds.AutocannonAPDSProjectile",
    "com.dsvv.cbcat.cannon.autocannon.munitions.apdsfs.AutocannonAPDSFSProjectile",
    "com.dsvv.cbcat.cannon.autocannon.munitions.he.AutocannonHEProjectile",
    "com.dsvv.cbcat.cannon.autocannon.munitions.hei.AutocannonHEIProjectile",
    "com.dsvv.cbcat.cannon.heavy_autocannon.munitions.ap_shot.HA_APProjectile",
    "com.dsvv.cbcat.cannon.heavy_autocannon.munitions.apds_shot.HA_APDSProjectile",
    "com.dsvv.cbcat.cannon.heavy_autocannon.munitions.apdsfs.HA_APDSFSProjectile",
    "com.dsvv.cbcat.cannon.heavy_autocannon.munitions.he_shell.HA_HEProjectile",
    "com.dsvv.cbcat.cannon.heavy_autocannon.munitions.hef_shell.HA_HEFProjectile",
    "com.dsvv.cbcat.cannon.heavy_autocannon.munitions.heat_shell.HA_HEATProjectile",
    "com.dsvv.cbcat.cannon.heavy_autocannon.munitions.smoke_shell.HA_SmokeProjectile",
    "com.dsvv.cbcat.cluster_munition.FuzedClusterProjectile"
}, remap = false)
public class CbcatMunitionPropertiesMixin {

    @Inject(method = "getDamageProperties", at = @At("HEAD"), cancellable = true)
    private void cbcatfix$getDamageProperties(CallbackInfoReturnable<EntityDamagePropertiesComponent> cir) {
        cir.setReturnValue(cbcatfix$settings().damageProperties());
    }

    @Inject(method = "getBallisticProperties", at = @At("HEAD"), cancellable = true)
    private void cbcatfix$getBallisticProperties(CallbackInfoReturnable<BallisticPropertiesComponent> cir) {
        cir.setReturnValue(cbcatfix$settings().ballisticProperties());
    }

    @Inject(method = "getBigCannonProjectileProperties", at = @At("HEAD"), cancellable = true, require = 0)
    private void cbcatfix$getBigCannonProjectileProperties(CallbackInfoReturnable<BigCannonProjectilePropertiesComponent> cir) {
        if (this.getClass().getSimpleName().equals("FuzedClusterProjectile")) {
            cir.setReturnValue(CbcatFixConfig.CLUSTER_SHELL.bigCannonProperties());
        }
    }

    private MunitionSettings cbcatfix$settings() {
        return switch (this.getClass().getSimpleName()) {
            case "AutocannonAPDSProjectile" -> CbcatFixConfig.AUTOCANNON_APDS;
            case "AutocannonAPDSFSProjectile" -> CbcatFixConfig.AUTOCANNON_APDSFS;
            case "AutocannonHEProjectile" -> CbcatFixConfig.AUTOCANNON_HE;
            case "AutocannonHEIProjectile" -> CbcatFixConfig.AUTOCANNON_HEI;
            case "HA_APProjectile" -> CbcatFixConfig.HEAVY_AUTOCANNON_AP;
            case "HA_APDSProjectile" -> CbcatFixConfig.HEAVY_AUTOCANNON_APDS;
            case "HA_APDSFSProjectile" -> CbcatFixConfig.HEAVY_AUTOCANNON_APDSFS;
            case "HA_HEProjectile" -> CbcatFixConfig.HEAVY_AUTOCANNON_HE;
            case "HA_HEFProjectile" -> CbcatFixConfig.HEAVY_AUTOCANNON_HEF;
            case "HA_HEATProjectile" -> CbcatFixConfig.HEAVY_AUTOCANNON_HEAT_PROJECTILE;
            case "HA_SmokeProjectile" -> CbcatFixConfig.HEAVY_AUTOCANNON_SMOKE;
            case "FuzedClusterProjectile" -> CbcatFixConfig.CLUSTER_SHELL;
            default -> throw new IllegalStateException("Unsupported CBCAT munition: " + this.getClass().getName());
        };
    }
}
