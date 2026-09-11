package com.cbcatfix.mixin;

import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(targets = "com.dsvv.cbcat.crafting.jei.CBCATJEI", remap = false)
public class CBCATJEIMixin {
    @Inject(method = "getClusterMunitionAssemblyRecipes", at = @At("HEAD"), cancellable = true)
    private void cbcatfix$useRealClusterRecipes(CallbackInfoReturnable<List<RecipeHolder<CraftingRecipe>>> cir) {
        cir.setReturnValue(List.of());
    }
    @Inject(method = "getRocketAssemblyRecipes", at = @At("HEAD"), cancellable = true)
    private void cbcatfix$getSmallRocketRecipes(CallbackInfoReturnable<List<RecipeHolder<CraftingRecipe>>> cir) {
        // Create registers the real mechanical recipes in its own JEI category.
        cir.setReturnValue(List.of());
    }

    @Inject(method = "getMediumRocketAssemblyRecipes", at = @At("HEAD"), cancellable = true)
    private void cbcatfix$getMediumAndBigRocketRecipes(CallbackInfoReturnable<List<RecipeHolder<CraftingRecipe>>> cir) {
        cir.setReturnValue(List.of());
    }
}
