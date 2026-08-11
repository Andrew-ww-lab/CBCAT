package com.cbcatfix.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.core.Position;
import com.cbcatfix.RocketDetonationContext;

@Mixin(targets = "com.dsvv.cbcat.cannon.medium_rocketpod.munitions.AbstractMediumFuzedRocket", remap = false)
public class AbstractMediumFuzedRocketMixin {

    @Inject(method = "detonate", at = @At("HEAD"))
    private void onDetonateHead(Position pos, CallbackInfo ci) {
        RocketDetonationContext.set(true);
    }

    @Inject(method = "detonate", at = @At("TAIL"))
    private void onDetonateTail(Position pos, CallbackInfo ci) {
        RocketDetonationContext.set(false);
    }
}
