package com.cbcatfix.mixin;

import com.cbcatfix.rocket.RocketGroundPlacement;
import com.cbcatfix.rocket.RocketStackFactory;
import com.simibubi.create.content.kinetics.crafter.MechanicalCraftingRecipe;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rbasamoyai.createbigcannons.index.CBCDataComponents;

import java.util.List;

/** Native Create matching/consumption, with only CBC payload component transfer added. */
@Mixin(value = MechanicalCraftingRecipe.class, remap = false)
public abstract class RocketMechanicalCraftingMixin extends ShapedRecipe {
    protected RocketMechanicalCraftingMixin(String group, CraftingBookCategory category,
        ShapedRecipePattern pattern, ItemStack result) {
        super(group, category, pattern, result);
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack result = super.assemble(input, registries);
        if (com.cbcatfix.munitions.ClusterPayload.isAssembly(result))
            return com.cbcatfix.munitions.ClusterPayload.assemble(input, result);
        if (cbcatfix$isWarheadAssembly(result) && input.items().stream()
            .anyMatch(stack -> !com.cbcatfix.crafting.AmmunitionFuzes.isCleanPayload(stack))) return ItemStack.EMPTY;
        if (!cbcatfix$isRocketAssembly(result)) return result;
        ItemStack template = result.getOrDefault(CBCDataComponents.PROJECTILE, ItemContainerContents.EMPTY).copyOne();
        ItemStack payload = cbcatfix$payload(input, template);
        if (payload.isEmpty()) return ItemStack.EMPTY;
        result.set(CBCDataComponents.PROJECTILE,
            ItemContainerContents.fromItems(List.of(payload.copyWithCount(template.getCount()))));
        return result;
    }

    @Inject(method = "matches(Lnet/minecraft/world/item/crafting/CraftingInput;Lnet/minecraft/world/level/Level;)Z",
        at = @At("RETURN"), cancellable = true)
    private void cbcatfix$requireCompatiblePayloads(CraftingInput input, Level level,
        CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) return;
        ItemStack result = this.getResultItem(level.registryAccess());
        if (com.cbcatfix.munitions.ClusterPayload.isAssembly(result)) {
            if (com.cbcatfix.munitions.ClusterPayload.assemble(input, result.copy()).isEmpty()) cir.setReturnValue(false);
            return;
        }
        if (cbcatfix$isWarheadAssembly(result) && input.items().stream()
            .anyMatch(stack -> !com.cbcatfix.crafting.AmmunitionFuzes.isCleanPayload(stack))) {
            cir.setReturnValue(false);
            return;
        }
        if (!cbcatfix$isRocketAssembly(result)) return;
        ItemStack template = result.getOrDefault(CBCDataComponents.PROJECTILE, ItemContainerContents.EMPTY).copyOne();
        if (cbcatfix$payload(input, template).isEmpty()) cir.setReturnValue(false);
    }

    @Unique
    private static boolean cbcatfix$isRocketAssembly(ItemStack result) {
        return RocketGroundPlacement.isRocket(result) && RocketStackFactory.usesConfiguredFuel(result);
    }

    @Unique
    private static boolean cbcatfix$isWarheadAssembly(ItemStack result) {
        // These combine several shells into one new warhead, which cannot retain several main fuzes.
        return result.is(com.cbcatfix.munitions.CbcatFixMunitions.HEAVY_HE_SHELL_ITEM.get())
            || result.is(com.cbcatfix.munitions.CbcatFixMunitions.HEAT_SHELL_ITEM.get())
            || result.is(com.cbcatfix.munitions.CbcatFixMunitions.FLAK_SHELL_ITEM.get());
    }

    @Unique
    private static ItemStack cbcatfix$payload(CraftingInput input, ItemStack template) {
        ItemStack selected = ItemStack.EMPTY;
        for (int slot = 0; slot < input.size(); slot++) {
            ItemStack stack = input.getItem(slot);
            if (!ItemStack.isSameItem(stack, template)) continue;
            if (!com.cbcatfix.crafting.AmmunitionFuzes.isCleanPayload(stack)) return ItemStack.EMPTY;
            // Native unfuzing can leave an empty container. It is equivalent to an absent fuze.
            ItemStack clean = stack.copy();
            clean.remove(CBCDataComponents.FUZE);
            if (!selected.isEmpty() && !ItemStack.isSameItemSameComponents(selected, clean)) return ItemStack.EMPTY;
            selected = clean;
        }
        return selected;
    }
}
