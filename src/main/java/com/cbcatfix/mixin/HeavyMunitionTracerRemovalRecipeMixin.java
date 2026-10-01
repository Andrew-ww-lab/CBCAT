package com.cbcatfix.mixin;

import com.dsvv.cbcat.cannon.heavy_autocannon.munitions.HeavyAutocannonCartridgeItem;
import com.dsvv.cbcat.crafting.HAMunitionTracerRemovalRecipe;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = HAMunitionTracerRemovalRecipe.class, remap = false)
public class HeavyMunitionTracerRemovalRecipeMixin {
    @Inject(method = "getRemainingItems", at = @At("RETURN"))
    private void cbcatfix$preserveCartridge(CraftingInput input, CallbackInfoReturnable<NonNullList<ItemStack>> cir) {
        for (int slot = 0; slot < input.size(); slot++) {
            ItemStack remainder = cir.getReturnValue().get(slot);
            if (remainder.isEmpty() || !(input.getItem(slot).getItem() instanceof HeavyAutocannonCartridgeItem)) continue;
            ItemStack cartridge = input.getItem(slot).copyWithCount(1);
            HeavyAutocannonCartridgeItem.writeProjectile(HeavyAutocannonCartridgeItem.getProjectileStack(remainder), cartridge);
            cir.getReturnValue().set(slot, cartridge);
        }
    }
}
