package com.cbcatfix.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import net.minecraft.core.Position;
import com.cbcatfix.RocketDetonationContext;
import com.cbcatfix.rocket.RocketPayloadAccess;
import com.dsvv.cbcat.base.IAbstractAutocannonProjectileMixin;
import net.minecraft.world.entity.Entity;
import com.cbcatfix.rocket.RocketBalance;
import rbasamoyai.createbigcannons.munitions.AbstractCannonProjectile;

@Mixin(targets = "com.dsvv.cbcat.cannon.medium_rocketpod.munitions.AbstractMediumFuzedRocket", remap = false)
public class AbstractMediumFuzedRocketMixin {

    @Redirect(
        method = "detonate",
        at = @At(
            value = "INVOKE",
            target = "Lcom/dsvv/cbcat/base/IAbstractAutocannonProjectileMixin;detonateProjectile(Lnet/minecraft/core/Position;)V"
        )
    )
    private void detonateWithRocketContext(IAbstractAutocannonProjectileMixin projectile, Position pos) {
        int payloadCount = ((RocketPayloadAccess) this).cbcatfix$getPayloadCount();
        RocketDetonationContext.enter(
            payloadCount,
            ((Entity) (Object) this).getDeltaMovement(),
            RocketBalance.tierForProjectile((AbstractCannonProjectile) (Object) this)
        );
        try {
            projectile.detonateProjectile(pos);
        } finally {
            RocketDetonationContext.exit();
        }
    }
}
