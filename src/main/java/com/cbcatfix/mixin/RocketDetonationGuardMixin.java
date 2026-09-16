package com.cbcatfix.mixin;

import net.minecraft.core.Position;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = {
    "com.dsvv.cbcat.cannon.rocketpod.munitions.AbstractFuzedRocket",
    "com.dsvv.cbcat.cannon.medium_rocketpod.munitions.AbstractMediumFuzedRocket",
    "com.cbcatfix.munitions.BigHERocketProjectile",
    "com.cbcatfix.munitions.BigHEATRocketProjectile"
}, remap = false)
public class RocketDetonationGuardMixin {
    @Inject(method = "detonate", at = @At("HEAD"), cancellable = true)
    private void cbcatfix$detonateOnce(Position position, CallbackInfo ci) {
        AbstractCannonProjectileAccess projectile = (AbstractCannonProjectileAccess) this;
        if (projectile.cbcatfix$isPendingRemoval()) ci.cancel();
        else projectile.cbcatfix$setPendingRemoval(true);
    }
}
