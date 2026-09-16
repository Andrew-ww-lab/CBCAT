package com.cbcatfix.crafting;

import com.dsvv.cbcat.cannon.medium_rocketpod.munitions.AbstractMediumRocketItem;
import com.dsvv.cbcat.cannon.rocketpod.munitions.AbstractRocketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import rbasamoyai.createbigcannons.munitions.fuzes.FuzeItem;

public final class RocketFuzingSupport {
    private RocketFuzingSupport() {
    }

    public static Parts findParts(CraftingInput input) {
        ItemStack rocket = ItemStack.EMPTY;
        ItemStack fuze = ItemStack.EMPTY;
        for (int index = 0; index < input.size(); index++) {
            ItemStack stack = input.getItem(index);
            if (stack.isEmpty()) {
                continue;
            }
            if (isRocket(stack)) {
                if (!rocket.isEmpty()) {
                    return null;
                }
                rocket = stack;
            } else if (stack.getItem() instanceof FuzeItem) {
                if (!fuze.isEmpty()) {
                    return null;
                }
                fuze = stack;
            } else {
                return null;
            }
        }
        return rocket.isEmpty() || fuze.isEmpty() ? null : new Parts(rocket, fuze);
    }

    private static boolean isRocket(ItemStack stack) {
        return stack.getItem() instanceof AbstractRocketItem
            || stack.getItem() instanceof AbstractMediumRocketItem;
    }

    public record Parts(ItemStack rocket, ItemStack fuze) {
    }
}
