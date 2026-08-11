package com.cbcatfix.mixin;

import com.dsvv.cbcat.base.CustomPropellantContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rbasamoyai.createbigcannons.config.CBCConfigs;

@Mixin(CustomPropellantContext.class)
public class CustomPropellantContextMixin {
    @Inject(method = "getDoomedToFail", at = @At("HEAD"), remap = false, cancellable = true)
    private void cbcatfix$disableExcessiveStressFailure(CallbackInfoReturnable<Boolean> cir) {
        if (CBCConfigs.server().failure.disableAllFailure.get()) {
            cir.setReturnValue(false);
        }
    }
}
