package com.cbcatfix.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.dsvv.cbcat.cannon.rocketpod.munitions.AbstractRocket;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

@Mixin(value = AbstractRocket.class, remap = false)
public class AbstractRocketFlightMixin {

    @Inject(method = "onTickRotate", at = @At("HEAD"), cancellable = true, remap = false)
    private void cbcatfix$smoothRocketRotation(CallbackInfo ci) {
        AbstractRocket rocket = (AbstractRocket) (Object) this;
        rocket.yRotO = rocket.getYRot();
        rocket.xRotO = rocket.getXRot();

        if (!rocket.isInGround()) {
            Vec3 vel = rocket.getDeltaMovement();
            double horizSq = vel.x * vel.x + vel.z * vel.z;
            if (horizSq > 0.001) {
                float targetYRot = (float) (Mth.atan2(vel.x, vel.z) * (180.0 / Math.PI));
                float targetXRot = (float) (Mth.atan2(vel.y, Math.sqrt(horizSq)) * (180.0 / Math.PI));

                float currentYRot = rocket.getYRot();
                float currentXRot = rocket.getXRot();

                float deltaY = Mth.wrapDegrees(targetYRot - currentYRot);
                float deltaX = Mth.wrapDegrees(targetXRot - currentXRot);

                rocket.setYRot(currentYRot + deltaY * 0.35f);
                rocket.setXRot(currentXRot + deltaX * 0.35f);
            }
        }
        ci.cancel();
    }
}
