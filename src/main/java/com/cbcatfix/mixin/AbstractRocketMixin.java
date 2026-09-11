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
import com.cbcatfix.rocket.RocketPayloadAccess;
import com.cbcatfix.rocket.RocketPenetrationBalance;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AbstractRocket.class)
public class AbstractRocketMixin {
    @Unique
    private double cbcatfix$impactEnergy;

    @Inject(method = "calculateBlockPenetration", at = @At("HEAD"), remap = false)
    private void cbcatfix$captureImpactEnergy(
        ProjectileContext context,
        BlockState state,
        BlockHitResult hitResult,
        CallbackInfoReturnable<AbstractCannonProjectile.ImpactResult> cir
    ) {
        RocketPayloadAccess rocket = (RocketPayloadAccess) this;
        if (rocket.cbcatfix$isArmorPiercing()) {
            AbstractCannonProjectile projectile = (AbstractCannonProjectile) (Object) this;
            cbcatfix$impactEnergy = RocketPenetrationBalance.impactEnergy(projectile, hitResult);
        }
    }

    @Inject(method = "calculateBlockPenetration", at = @At("RETURN"), cancellable = true, remap = false)
    private void cbcatfix$noRicochet(ProjectileContext context, BlockState state, BlockHitResult hitResult, CallbackInfoReturnable<AbstractCannonProjectile.ImpactResult> cir) {
        AbstractCannonProjectile.ImpactResult res = cir.getReturnValue();
        RocketPayloadAccess rocket = (RocketPayloadAccess) this;
        if (rocket.cbcatfix$isArmorPiercing()) {
            rocket.cbcatfix$breakGuidance();
            AbstractRocket<?> projectile = (AbstractRocket<?>) (Object) this;
            if (res == null || res.kinematics() == AbstractCannonProjectile.ImpactResult.KinematicOutcome.STOP) {
                return;
            }
            boolean bounced = res.kinematics() == AbstractCannonProjectile.ImpactResult.KinematicOutcome.BOUNCE;
            float collisionDamage = RocketPenetrationBalance.durabilityDamage(
                projectile, state, hitResult, cbcatfix$impactEnergy, bounced
            );
            boolean remainsIntact = rocket.cbcatfix$damageDurability(collisionDamage);
            cir.setReturnValue(new AbstractCannonProjectile.ImpactResult(
                res.kinematics(),
                !remainsIntact
            ));
            return;
        }
        if (res != null && res.kinematics() == AbstractCannonProjectile.ImpactResult.KinematicOutcome.BOUNCE) {
            cir.setReturnValue(new AbstractCannonProjectile.ImpactResult(AbstractCannonProjectile.ImpactResult.KinematicOutcome.STOP, true));
        }
    }
}
