package com.cbcatfix.mixin;

import com.cbcatfix.crafting.AmmunitionFuzes;
import com.dsvv.cbcat.cannon.heavy_autocannon.munitions.HeavyAutocannonCartridgeItem;
import com.dsvv.cbcat.crafting.HAMunitionUnfuzingRecipe;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rbasamoyai.createbigcannons.index.CBCDataComponents;

@Mixin(value = HAMunitionUnfuzingRecipe.class, remap = false)
public class HeavyMunitionUnfuzingRecipeMixin {
    @Inject(method = "matches", at = @At("RETURN"), cancellable = true)
    private void cbcatfix$requireInstalledFuze(CraftingInput input, Level level, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) return;
        for (ItemStack stack : input.items()) {
            if (stack.isEmpty()) continue;
            ItemStack projectile = stack.getItem() instanceof HeavyAutocannonCartridgeItem
                ? HeavyAutocannonCartridgeItem.getProjectileStack(stack) : stack;
            if (!AmmunitionFuzes.hasFuze(projectile)) cir.setReturnValue(false);
        }
    }

    @Inject(method = "getRemainingItems", at = @At("RETURN"))
    private void cbcatfix$preserveCartridge(CraftingInput input, CallbackInfoReturnable<NonNullList<ItemStack>> cir) {
        for (int slot = 0; slot < input.size(); slot++) {
            ItemStack remainder = cir.getReturnValue().get(slot);
            if (remainder.isEmpty()) continue;
            if (input.getItem(slot).getItem() instanceof HeavyAutocannonCartridgeItem) {
                ItemStack projectile = HeavyAutocannonCartridgeItem.getProjectileStack(remainder);
                projectile.remove(CBCDataComponents.FUZE);
                ItemStack cartridge = input.getItem(slot).copyWithCount(1);
                HeavyAutocannonCartridgeItem.writeProjectile(projectile, cartridge);
                cir.getReturnValue().set(slot, cartridge);
            } else {
                remainder.remove(CBCDataComponents.FUZE);
            }
        }
    }
}
