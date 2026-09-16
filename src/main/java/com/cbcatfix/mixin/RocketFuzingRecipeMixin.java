package com.cbcatfix.mixin;

import com.cbcatfix.crafting.RocketFuzingSupport;
import com.dsvv.cbcat.crafting.RocketFuzingRecipe;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import rbasamoyai.createbigcannons.index.CBCDataComponents;
import java.util.List;

@Mixin(value = RocketFuzingRecipe.class, remap = false)
public class RocketFuzingRecipeMixin {
    /**
     * Allows every small, medium, and large rocket to receive any CBC-compatible fuze.
     */
    @Overwrite
    public boolean matches(CraftingInput input, Level level) {
        return RocketFuzingSupport.findParts(input) != null;
    }

    /**
     * Preserves the assembled rocket's payload/fuel and stores the selected fuze on it.
     */
    @Overwrite
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        RocketFuzingSupport.Parts parts = RocketFuzingSupport.findParts(input);
        if (parts == null) {
            return ItemStack.EMPTY;
        }

        ItemStack result = parts.rocket().copyWithCount(1);
        result.set(CBCDataComponents.FUZE,
            ItemContainerContents.fromItems(List.of(parts.fuze().copyWithCount(1))));
        return result;
    }

}
