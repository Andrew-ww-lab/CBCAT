package com.cbcatfix.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.List;
import java.util.Collections;

@Mixin(targets = "com.dsvv.cbcat.crafting.jei.CBCATJEI", remap = false)
public class CBCATJEIMixin {

    @Inject(method = "getMediumRocketAssemblyRecipes", at = @At("HEAD"), cancellable = true)
    private void onGetMediumRocketAssemblyRecipes(CallbackInfoReturnable<List<?>> cir) {
        cir.setReturnValue(Collections.emptyList());
    }

    @Inject(method = "getRocketAssemblyRecipes", at = @At("HEAD"), cancellable = true)
    private void onGetRocketAssemblyRecipes(CallbackInfoReturnable<List<?>> cir) {
        cir.setReturnValue(Collections.emptyList());
    }
}
