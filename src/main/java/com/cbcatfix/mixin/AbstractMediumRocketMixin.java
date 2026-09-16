package com.cbcatfix.mixin;

import com.cbcatfix.rocket.RocketBallistics;
import com.cbcatfix.rocket.RocketPenetrationBalance;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rbasamoyai.createbigcannons.munitions.AbstractCannonProjectile;
import rbasamoyai.createbigcannons.munitions.ProjectileContext;
import rbasamoyai.createbigcannons.munitions.config.components.BallisticPropertiesComponent;

@Mixin(targets = "com.dsvv.cbcat.cannon.medium_rocketpod.munitions.AbstractMediumRocket", remap = false)
public abstract class AbstractMediumRocketMixin {
    @Inject(method = "getBallisticProperties", at = @At("RETURN"), cancellable = true)
    private void cbcatfix$apBallistics(CallbackInfoReturnable<BallisticPropertiesComponent> cir) {
        cir.setReturnValue(RocketBallistics.properties((AbstractCannonProjectile) (Object) this, cir.getReturnValue()));
    }

    @Inject(method = "calculateBlockPenetration", at = @At("HEAD"), cancellable = true)
    private void cbcatfix$permeableBlocks(ProjectileContext context, BlockState block, BlockHitResult hit,
                                        CallbackInfoReturnable<AbstractCannonProjectile.ImpactResult> cir) {
        var result = RocketPenetrationBalance.tryPermeable((AbstractCannonProjectile) (Object) this, context, block, hit);
        if (result != null) cir.setReturnValue(result);
    }

}
