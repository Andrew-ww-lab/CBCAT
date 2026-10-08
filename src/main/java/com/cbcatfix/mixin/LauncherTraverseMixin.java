package com.cbcatfix.mixin;

import com.cbcatfix.balance.BalanceDefaults;
import com.cbcatfix.rocket.LauncherModuleBlock;
import com.cbcatfix.rocket.SmallLauncherStructure;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import rbasamoyai.createbigcannons.cannon_control.cannon_mount.CannonMountBlockEntity;
import rbasamoyai.createbigcannons.cannon_control.contraption.AbstractMountedCannonContraption;
import rbasamoyai.createbigcannons.cannon_control.contraption.PitchOrientedContraptionEntity;

/** Scale both native drive inputs once, before CBC's angle limits and client correction. */
@Mixin(value = CannonMountBlockEntity.class, remap = false)
public abstract class LauncherTraverseMixin {
    @Shadow protected PitchOrientedContraptionEntity mountedContraption;

    @ModifyExpressionValue(method = "tick", at = @At(value = "INVOKE", target =
        "Lrbasamoyai/createbigcannons/cannon_control/cannon_mount/CannonMountInterfaceBlockEntity;getSpeed()F"))
    private float cbcatfix$traverseDrive(float speed) {
        return mountedContraption != null && mountedContraption.getContraption() instanceof AbstractMountedCannonContraption cannon
            && SmallLauncherStructure.module(cannon) == LauncherModuleBlock.Module.TRAVERSE
            ? (float) (speed * BalanceDefaults.TRAVERSE_GEAR_SPEED) : speed;
    }
}
