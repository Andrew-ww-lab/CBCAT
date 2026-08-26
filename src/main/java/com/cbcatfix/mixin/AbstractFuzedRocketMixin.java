package com.cbcatfix.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import net.minecraft.core.Position;
import com.cbcatfix.RocketDetonationContext;
import com.dsvv.cbcat.base.IAbstractAutocannonProjectileMixin;

@Mixin(targets = "com.dsvv.cbcat.cannon.rocketpod.munitions.AbstractFuzedRocket", remap = false)
public class AbstractFuzedRocketMixin {

    @Redirect(
        method = "detonate",
        at = @At(
            value = "INVOKE",
            target = "Lcom/dsvv/cbcat/base/IAbstractAutocannonProjectileMixin;detonateProjectile(Lnet/minecraft/core/Position;)V"
        )
    )
    private void detonateWithRocketContext(IAbstractAutocannonProjectileMixin projectile, Position pos) {
        RocketDetonationContext.enter();
        try {
            projectile.detonateProjectile(pos);
        } finally {
            RocketDetonationContext.exit();
        }
    }
}
