package com.cbcatfix.mixin;

import com.cbcatfix.rocket.RocketPayloadAccess;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rbasamoyai.createbigcannons.munitions.AbstractCannonProjectile;

@Mixin(targets = "com.happysg.radar.item.GuidedFuzeItem", remap = false)
public class GuidedFuzeItemMixin {
    @Inject(method = "onProjectileTick", at = @At("HEAD"), cancellable = true)
    private void cbcatfix$suppressDelayedRadarGuidance(
        ItemStack fuzeStack,
        AbstractCannonProjectile projectile,
        CallbackInfoReturnable<Boolean> cir
    ) {
        if (projectile instanceof RocketPayloadAccess) {
            // Rocket guidance is applied once, at the beginning of the rocket
            // entity tick. Suppress Create: Radar's later duplicate pass.
            cir.setReturnValue(false);
        }
    }

}
