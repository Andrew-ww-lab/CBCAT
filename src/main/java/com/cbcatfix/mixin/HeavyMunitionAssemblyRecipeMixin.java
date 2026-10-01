package com.cbcatfix.mixin;

import com.dsvv.cbcat.cannon.heavy_autocannon.munitions.AbstractHeavyAutocannonProjectileItem;
import com.dsvv.cbcat.cannon.heavy_autocannon.munitions.HeavyAutocannonCartridgeItem;
import com.dsvv.cbcat.crafting.HAMunitionAssemblyRecipe;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Keep the recipe's charge selection, but insert the actual input projectile. */
@Mixin(value = HAMunitionAssemblyRecipe.class, remap = false)
public class HeavyMunitionAssemblyRecipeMixin {
    @Inject(method = "assemble", at = @At("RETURN"), cancellable = true)
    private void cbcatfix$preserveProjectile(CraftingInput input, HolderLookup.Provider registries,
        CallbackInfoReturnable<ItemStack> cir) {
        ItemStack result = cir.getReturnValue();
        if (result.isEmpty()) return;
        for (ItemStack stack : input.items()) {
            if (stack.getItem() instanceof HeavyAutocannonCartridgeItem) {
                var strong = result.get(com.dsvv.cbcat.registry.DataComponentRegistry.HA_STRONG_ROUND);
                result.applyComponents(stack.getComponentsPatch());
                result.set(com.dsvv.cbcat.registry.DataComponentRegistry.HA_STRONG_ROUND, strong);
                break;
            }
        }
        for (ItemStack stack : input.items()) {
            if (stack.getItem() instanceof AbstractHeavyAutocannonProjectileItem) {
                HeavyAutocannonCartridgeItem.writeProjectile(stack.copyWithCount(1), result);
                cir.setReturnValue(result);
                return;
            }
        }
    }
}
