package com.cbcatfix.mixin;

import com.cbcatfix.rocket.LauncherShotView;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import rbasamoyai.createbigcannons.cannon_control.contraption.AbstractMountedCannonContraption;

/** Radars 5 EA's normal-world fallback rejects CBCAT rockets even with a DIRECT adapter. */
@Mixin(targets = "com.happysg.radar.block.behavior.networks.WeaponFiringControl", remap = false)
public class RadarDirectLauncherAimMixin {
    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target =
        "Lcom/happysg/radar/compat/cbc/CannonUtil;isPoweredRocket(Lrbasamoyai/createbigcannons/cannon_control/contraption/AbstractMountedCannonContraption;)Z"))
    private boolean cbcatfix$allowExplicitDirectAdapter(AbstractMountedCannonContraption cannon, Operation<Boolean> original) {
        // This affects only fallback rejection in tick(), never rocket identity or projectile properties.
        return !(cannon instanceof LauncherShotView) && original.call(cannon);
    }
}
