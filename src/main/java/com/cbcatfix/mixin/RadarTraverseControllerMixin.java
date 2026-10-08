package com.cbcatfix.mixin;

import com.cbcatfix.balance.BalanceDefaults;
import com.cbcatfix.rocket.LauncherModuleBlock;
import com.cbcatfix.rocket.RadarTraverseContext;
import com.cbcatfix.rocket.SmallLauncherStructure;
import com.happysg.radar.compat.cbc.CannonMountContext;
import com.happysg.radar.compat.cbc.DirectCbcMountMotion;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import rbasamoyai.createbigcannons.cannon_control.contraption.AbstractMountedCannonContraption;

@Pseudo
@Mixin(targets = {"com.happysg.radar.block.controller.pitch.CannonMountPitch",
    "com.happysg.radar.block.controller.yaw.CannonMountYaw"}, remap = false)
public abstract class RadarTraverseControllerMixin {
    @WrapOperation(method = "rotateCBC", at = @At(value = "INVOKE", target =
        "Lcom/happysg/radar/compat/cbc/DirectCbcMountMotion$State;nextStep(DD)D"))
    private double cbcatfix$traverseStep(DirectCbcMountMotion.State state, double error, double rpm,
        Operation<Double> original, @Local(argsOnly = true) CannonMountContext mount) {
        var entity = mount.getContraption();
        double previous = RadarTraverseContext.MULTIPLIER.get();
        double multiplier = entity != null && entity.getContraption() instanceof AbstractMountedCannonContraption cannon
            && SmallLauncherStructure.module(cannon) == LauncherModuleBlock.Module.TRAVERSE
            ? BalanceDefaults.TRAVERSE_GEAR_SPEED : 1;
        try {
            RadarTraverseContext.MULTIPLIER.set(multiplier);
            return original.call(state, error, rpm);
        } finally {
            RadarTraverseContext.MULTIPLIER.set(previous);
        }
    }
}
