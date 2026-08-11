package com.cbcatfix.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.level.Level;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import com.dsvv.cbcat.crafting.ClusterMunitionAssemblyRecipe;
import com.dsvv.cbcat.cannon.heavy_autocannon.munitions.AbstractFuzedHeavyAutocannonProjectileItem;
import com.dsvv.cbcat.registry.BlockRegister;
import com.dsvv.cbcat.registry.DataComponentRegistry;
import com.dsvv.cbcat.registry.ExtraDataRegister;
import rbasamoyai.createbigcannons.index.CBCDataComponents;
import net.minecraft.world.item.component.ItemContainerContents;
import java.util.List;
import java.util.ArrayList;

@Mixin(value = ClusterMunitionAssemblyRecipe.class, remap = false)
public class ClusterMunitionAssemblyRecipeMixin {

    @Inject(method = "matches(Lnet/minecraft/world/item/crafting/CraftingInput;Lnet/minecraft/world/level/Level;)Z", at = @At("HEAD"), cancellable = true)
    private void onMatches(CraftingInput input, Level level, CallbackInfoReturnable<Boolean> cir) {
        ItemStack slab = ItemStack.EMPTY;
        ItemStack gunpowder = ItemStack.EMPTY;
        List<ItemStack> projectiles = new ArrayList<>();

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;

            Item item = stack.getItem();
            if (item instanceof BlockItem blockItem) {
                Block block = blockItem.getBlock();
                if (block instanceof SlabBlock && new ItemStack(block).is(ItemTags.WOODEN_SLABS)) {
                    if (!slab.isEmpty()) {
                        cir.setReturnValue(false);
                        return;
                    }
                    slab = stack;
                    continue;
                }
            }

            if (ItemStack.isSameItem(stack, Items.GUNPOWDER.getDefaultInstance())) {
                if (!gunpowder.isEmpty()) {
                    cir.setReturnValue(false);
                    return;
                }
                gunpowder = stack;
                continue;
            }

            if (item instanceof AbstractFuzedHeavyAutocannonProjectileItem) {
                if (!stack.has(CBCDataComponents.FUZE)) {
                    cir.setReturnValue(false);
                    return;
                }
                projectiles.add(stack);
                continue;
            }

            cir.setReturnValue(false);
            return;
        }

        cir.setReturnValue(!slab.isEmpty() && !gunpowder.isEmpty() && projectiles.size() >= 4);
    }

    @Inject(method = "assemble(Lnet/minecraft/world/item/crafting/CraftingInput;Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/world/item/ItemStack;", at = @At("HEAD"), cancellable = true)
    private void onAssemble(CraftingInput input, HolderLookup.Provider provider, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack slab = ItemStack.EMPTY;
        ItemStack gunpowder = ItemStack.EMPTY;
        List<ItemStack> projectiles = new ArrayList<>();

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;

            Item item = stack.getItem();
            if (item instanceof BlockItem blockItem) {
                Block block = blockItem.getBlock();
                if (block instanceof SlabBlock && new ItemStack(block).is(ItemTags.WOODEN_SLABS)) {
                    if (!slab.isEmpty()) {
                        cir.setReturnValue(ItemStack.EMPTY);
                        return;
                    }
                    slab = stack;
                    continue;
                }
            }

            if (ItemStack.isSameItem(stack, Items.GUNPOWDER.getDefaultInstance())) {
                if (!gunpowder.isEmpty()) {
                    cir.setReturnValue(ItemStack.EMPTY);
                    return;
                }
                gunpowder = stack;
                continue;
            }

            if (item instanceof AbstractFuzedHeavyAutocannonProjectileItem) {
                if (!stack.has(CBCDataComponents.FUZE)) {
                    cir.setReturnValue(ItemStack.EMPTY);
                    return;
                }
                projectiles.add(stack);
                continue;
            }

            cir.setReturnValue(ItemStack.EMPTY);
            return;
        }

        if (slab.isEmpty() || gunpowder.isEmpty() || projectiles.size() < 4) {
            cir.setReturnValue(ItemStack.EMPTY);
            return;
        }

        AbstractFuzedHeavyAutocannonProjectileItem projectileItem = (AbstractFuzedHeavyAutocannonProjectileItem) projectiles.get(0).getItem();
        for (ItemStack p : projectiles) {
            if (!p.getItem().equals(projectileItem)) {
                cir.setReturnValue(ItemStack.EMPTY);
                return;
            }
        }

        ItemStack result = BlockRegister.CLUSTER_BLOCK.asStack(1);
        result.set(DataComponentRegistry.CLUSTER_PROJECTILE, ExtraDataRegister.clusterPartsReverse(projectileItem));

        List<ItemStack> fuzes = new ArrayList<>();
        for (ItemStack p : projectiles) {
            ItemContainerContents fuzeContent = p.get(CBCDataComponents.FUZE);
            if (fuzeContent != null) {
                fuzes.add(fuzeContent.copyOne());
            } else {
                fuzes.add(ItemStack.EMPTY);
            }
        }
        result.set(DataComponentRegistry.CLUSTER_FUZES, ItemContainerContents.fromItems(fuzes));

        cir.setReturnValue(result);
    }
}
