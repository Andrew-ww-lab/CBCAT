package com.cbcatfix.mixin;

import com.cbcatfix.rocket.RocketBalance;
import com.dsvv.cbcat.cannon.medium_rocketpod.munitions.AbstractMediumRocket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = {
    "com.cbcatfix.munitions.BigAPRocketItem",
    "com.cbcatfix.munitions.BigHERocketItem",
    "com.cbcatfix.munitions.BigHEATRocketItem"
}, remap = false)
public class BigRocketPayloadMixin {
    @Inject(method = "getAutocannonProjectile", at = @At("RETURN"))
    private void cbcatfix$applyBalancedPayload(ItemStack stack, Level level, CallbackInfoReturnable<AbstractMediumRocket<?>> cir) {
        AbstractMediumRocket<?> projectile = cir.getReturnValue();
        if (projectile != null) {
            RocketBalance.configureProjectile(stack, projectile);
        }
    }
}
