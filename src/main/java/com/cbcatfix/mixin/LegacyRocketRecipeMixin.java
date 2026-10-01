package com.cbcatfix.mixin;

import com.cbcatfix.crafting.AmmunitionFuzes;
import com.dsvv.cbcat.crafting.MediumRocketRecipe;
import com.dsvv.cbcat.crafting.RocketRecipe;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Also enforce the rule if a data pack restores CBCA's legacy assembly recipes. */
@Mixin(value = {RocketRecipe.class, MediumRocketRecipe.class}, remap = false)
public class LegacyRocketRecipeMixin {
    @Inject(method = "matches", at = @At("HEAD"), cancellable = true)
    private void cbcatfix$cleanInputs(CraftingInput input, Level level, CallbackInfoReturnable<Boolean> cir) {
        if (input.items().stream().anyMatch(stack -> !AmmunitionFuzes.isCleanPayload(stack))) cir.setReturnValue(false);
    }

    @Inject(method = "assemble", at = @At("HEAD"), cancellable = true)
    private void cbcatfix$guardAssembly(CraftingInput input, HolderLookup.Provider registries,
        CallbackInfoReturnable<ItemStack> cir) {
        if (input.items().stream().anyMatch(stack -> !AmmunitionFuzes.isCleanPayload(stack))) cir.setReturnValue(ItemStack.EMPTY);
    }
}
