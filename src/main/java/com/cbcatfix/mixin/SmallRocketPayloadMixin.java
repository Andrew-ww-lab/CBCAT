package com.cbcatfix.mixin;

import com.cbcatfix.rocket.RocketBalance;
import com.dsvv.cbcat.cannon.rocketpod.munitions.AbstractRocket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = {
    "com.dsvv.cbcat.cannon.rocketpod.munitions.ap_rocket.AP_RocketItem",
    "com.dsvv.cbcat.cannon.rocketpod.munitions.flak_rocket.Flak_RocketItem",
    "com.dsvv.cbcat.cannon.rocketpod.munitions.he_rocket.HE_RocketItem",
    "com.dsvv.cbcat.cannon.rocketpod.munitions.hei_rocket.HEI_RocketItem"
}, remap = false)
public class SmallRocketPayloadMixin {
    @Inject(method = "getAutocannonProjectile", at = @At("RETURN"))
    private void cbcatfix$applyBalancedPayload(ItemStack stack, Level level, CallbackInfoReturnable<AbstractRocket<?>> cir) {
        AbstractRocket<?> projectile = cir.getReturnValue();
        if (projectile != null) {
            RocketBalance.configureProjectile(stack, projectile);
        }
    }
}
