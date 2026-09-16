package com.cbcatfix.munitions;

import com.dsvv.cbcat.registry.DataComponentRegistry;
import com.dsvv.cbcat.registry.ExtraDataRegister;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.crafting.CraftingInput;
import rbasamoyai.createbigcannons.index.CBCDataComponents;
import java.util.ArrayList;

/** Only CBCAT's payload components; Create still matches and consumes the recipe. */
public final class ClusterPayload {
    public static final int SUBMUNITION_COUNT = com.cbcatfix.balance.BalanceDefaults.CLUSTER_RECIPE_COUNT;
    private ClusterPayload() {}

    public static boolean isAssembly(ItemStack result) {
        return result.is(com.dsvv.cbcat.registry.BlockRegister.CLUSTER_BLOCK.asItem());
    }

    public static ItemStack assemble(CraftingInput input, ItemStack result) {
        String type = result.get(DataComponentRegistry.CLUSTER_PROJECTILE);
        if (type == null) return ItemStack.EMPTY;
        var fuzes = new ArrayList<ItemStack>(SUBMUNITION_COUNT);
        for (int slot = 0; slot < input.size(); slot++) {
            ItemStack stack = input.getItem(slot);
            if (stack.isEmpty() || !type.equals(ExtraDataRegister.clusterPartsReverse(stack.getItem()))) continue;
            ItemStack fuze = stack.getOrDefault(CBCDataComponents.FUZE, ItemContainerContents.EMPTY).copyOne();
            if (fuze.isEmpty()) return ItemStack.EMPTY;
            fuzes.add(fuze);
        }
        if (fuzes.size() != SUBMUNITION_COUNT) return ItemStack.EMPTY;
        result.set(DataComponentRegistry.CLUSTER_FUZES, ItemContainerContents.fromItems(fuzes));
        return result;
    }

    public static ItemStack[] normalizeFuzes(ItemStack[] previous) {
        int count = com.cbcatfix.config.CbcatFixConfig.value(com.cbcatfix.config.CbcatFixConfig.CLUSTER_SUBMUNITION_COUNT);
        if (previous.length == count) return previous;
        var fuzes = new ItemStack[count];
        for (int slot = 0; slot < fuzes.length; slot++) {
            fuzes[slot] = previous.length == 0 ? ItemStack.EMPTY : previous[slot % previous.length].copy();
        }
        return fuzes;
    }
}
