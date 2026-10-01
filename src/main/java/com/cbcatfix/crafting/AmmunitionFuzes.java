package com.cbcatfix.crafting;

import com.dsvv.cbcat.registry.DataComponentRegistry;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import rbasamoyai.createbigcannons.index.CBCDataComponents;

/** Reads the native fuze containers, including ammunition nested inside a cartridge. */
public final class AmmunitionFuzes {
    private AmmunitionFuzes() {}

    public static boolean hasFuze(ItemStack stack) {
        return stack.getOrDefault(CBCDataComponents.FUZE, ItemContainerContents.EMPTY)
            .nonEmptyItems().iterator().hasNext();
    }

    public static boolean isCleanPayload(ItemStack stack) {
        return isCleanPayload(stack, 0);
    }

    private static boolean isCleanPayload(ItemStack stack, int depth) {
        if (hasFuze(stack) || depth >= 16) return false;
        if (stack.getOrDefault(DataComponentRegistry.CLUSTER_FUZES, ItemContainerContents.EMPTY)
            .nonEmptyItems().iterator().hasNext()) return false;
        for (ItemStack nested : stack.getOrDefault(CBCDataComponents.PROJECTILE, ItemContainerContents.EMPTY).nonEmptyItems()) {
            if (!isCleanPayload(nested, depth + 1)) return false;
        }
        return true;
    }
}
