package com.cbcatfix.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.dsvv.cbcat.cannon.medium_rocketpod.munitions.AbstractMediumRocket", remap = false)
public class AbstractMediumRocketMixin {

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
