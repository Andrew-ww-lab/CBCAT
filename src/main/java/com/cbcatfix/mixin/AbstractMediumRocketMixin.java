package com.cbcatfix.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.cbcatfix.rocket.RocketPayloadAccess;
import com.cbcatfix.rocket.RocketPenetrationBalance;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import rbasamoyai.createbigcannons.munitions.AbstractCannonProjectile;
import rbasamoyai.createbigcannons.munitions.ProjectileContext;
import org.spongepowered.asm.mixin.Unique;

@Mixin(targets = "com.dsvv.cbcat.cannon.medium_rocketpod.munitions.AbstractMediumRocket", remap = false)
public class AbstractMediumRocketMixin {
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
    private void cbcatfix$applyArmorPiercingDurability(
        ProjectileContext context,
        BlockState state,
        BlockHitResult hitResult,
        CallbackInfoReturnable<AbstractCannonProjectile.ImpactResult> cir
    ) {
        RocketPayloadAccess rocket = (RocketPayloadAccess) this;
        if (!rocket.cbcatfix$isArmorPiercing()) {
            return;
        }
        rocket.cbcatfix$breakGuidance();
        AbstractCannonProjectile projectile = (AbstractCannonProjectile) (Object) this;
        AbstractCannonProjectile.ImpactResult result = cir.getReturnValue();
        if (result == null || result.kinematics() == AbstractCannonProjectile.ImpactResult.KinematicOutcome.STOP) {
            return;
        }
        boolean bounced = result.kinematics() == AbstractCannonProjectile.ImpactResult.KinematicOutcome.BOUNCE;
        float collisionDamage = RocketPenetrationBalance.durabilityDamage(
            projectile, state, hitResult, cbcatfix$impactEnergy, bounced
        );
        boolean remainsIntact = rocket.cbcatfix$damageDurability(collisionDamage);
        cir.setReturnValue(new AbstractCannonProjectile.ImpactResult(
            result.kinematics(),
            !remainsIntact
        ));
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lrbasamoyai/createbigcannons/munitions/AbstractCannonProjectile;tick()V", shift = At.Shift.AFTER))
    private void afterSuperTick(CallbackInfo ci) {
        rbasamoyai.createbigcannons.munitions.AbstractCannonProjectile projectile = (rbasamoyai.createbigcannons.munitions.AbstractCannonProjectile) (Object) this;
        net.minecraft.world.phys.Vec3 vec3 = projectile.getDeltaMovement();
        if (vec3.lengthSqr() > 0.000001D) { // Only update if moving
            double d0 = vec3.horizontalDistance();
            projectile.setYRot((float)(net.minecraft.util.Mth.atan2(vec3.x, vec3.z) * (double)(180F / (float)Math.PI)));
            projectile.setXRot((float)(net.minecraft.util.Mth.atan2(vec3.y, d0) * (double)(180F / (float)Math.PI)));
        }
    }
}
