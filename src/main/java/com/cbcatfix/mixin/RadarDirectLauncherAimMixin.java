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
    @WrapOperation(method = "estimateSlewTicks", at = @At(value = "INVOKE", target =
        "Lcom/happysg/radar/compat/cbc/DirectCbcMountMotion;estimateTicks(DD)D"))
    private double cbcatfix$estimateTraverse(double angle, double rpm, Operation<Double> original,
        @com.llamalad7.mixinextras.sugar.Local(argsOnly = true) AbstractMountedCannonContraption cannon) {
        double previous = com.cbcatfix.rocket.RadarTraverseContext.MULTIPLIER.get();
        try {
            com.cbcatfix.rocket.RadarTraverseContext.MULTIPLIER.set(
                com.cbcatfix.rocket.SmallLauncherStructure.module(cannon) == com.cbcatfix.rocket.LauncherModuleBlock.Module.TRAVERSE
                    ? com.cbcatfix.balance.BalanceDefaults.TRAVERSE_GEAR_SPEED : 1.0);
            return original.call(angle, rpm);
        } finally {
            com.cbcatfix.rocket.RadarTraverseContext.MULTIPLIER.set(previous);
        }
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target =
        "Lcom/happysg/radar/compat/cbc/CannonUtil;isPoweredRocket(Lrbasamoyai/createbigcannons/cannon_control/contraption/AbstractMountedCannonContraption;)Z"))
    private boolean cbcatfix$allowExplicitDirectAdapter(AbstractMountedCannonContraption cannon, Operation<Boolean> original) {
        // This affects only fallback rejection in tick(), never rocket identity or projectile properties.
        return !(cannon instanceof LauncherShotView) && original.call(cannon);
    }
}
