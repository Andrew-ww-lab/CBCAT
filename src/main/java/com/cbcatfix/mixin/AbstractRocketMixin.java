package com.cbcatfix.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.dsvv.cbcat.cannon.rocketpod.munitions.AbstractRocket;
import rbasamoyai.createbigcannons.munitions.AbstractCannonProjectile;
import rbasamoyai.createbigcannons.munitions.ProjectileContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

@Mixin(AbstractRocket.class)
public class AbstractRocketMixin {

    @Inject(method = "calculateBlockPenetration", at = @At("RETURN"), cancellable = true, remap = false)
    private void cbcatfix$noRicochet(ProjectileContext context, BlockState state, BlockHitResult hitResult, CallbackInfoReturnable<AbstractCannonProjectile.ImpactResult> cir) {
        AbstractCannonProjectile.ImpactResult res = cir.getReturnValue();
        if (res != null && res.kinematics() == AbstractCannonProjectile.ImpactResult.KinematicOutcome.BOUNCE) {
            cir.setReturnValue(new AbstractCannonProjectile.ImpactResult(AbstractCannonProjectile.ImpactResult.KinematicOutcome.STOP, true));
        }
    }
}
