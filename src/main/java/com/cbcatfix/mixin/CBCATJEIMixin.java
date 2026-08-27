package com.cbcatfix.mixin;

import com.dsvv.cbcat.cannon.autocannon.munitions.apds.AutocannonAPDSProjectileItem;
import com.dsvv.cbcat.cannon.autocannon.munitions.apdsfs.AutocannonAPDSFSProjectileItem;
import com.dsvv.cbcat.cannon.heavy_autocannon.munitions.AbstractHeavyAutocannonProjectileItem;
import com.dsvv.cbcat.cannon.heavy_autocannon.munitions.apds_shot.HA_APDSProjectileItem;
import com.dsvv.cbcat.cannon.heavy_autocannon.munitions.apdsfs.HA_APDSFSProjectileItem;
import com.dsvv.cbcat.cannon.heavy_autocannon.munitions.smoke_shell.HA_SmokeProjectileItem;
import com.dsvv.cbcat.cannon.medium_rocketpod.munitions.AbstractMediumRocketItem;
import com.dsvv.cbcat.cannon.rocketpod.munitions.AbstractRocketItem;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rbasamoyai.createbigcannons.index.CBCItems;
import rbasamoyai.createbigcannons.munitions.autocannon.AutocannonRoundItem;

import java.util.ArrayList;
import java.util.List;

@Mixin(targets = "com.dsvv.cbcat.crafting.jei.CBCATJEI", remap = false)
public class CBCATJEIMixin {

    @Inject(method = "getRocketAssemblyRecipes", at = @At("HEAD"), cancellable = true)
    private void onGetRocketAssemblyRecipes(
        CallbackInfoReturnable<List<RecipeHolder<CraftingRecipe>>> cir
    ) {
        List<Item> projectileItems = new ArrayList<>();
        List<Item> propellantItems = new ArrayList<>();
        List<AbstractRocketItem> rocketItems = new ArrayList<>();

        BuiltInRegistries.ITEM.forEach(item -> {
            if (item instanceof AutocannonRoundItem
                && !(item instanceof AutocannonAPDSProjectileItem)
                && !(item instanceof AutocannonAPDSFSProjectileItem)) {
                projectileItems.add(item);
            } else if (item instanceof AbstractRocketItem rocketItem) {
                rocketItems.add(rocketItem);
            } else if (isRocketPropellant(item)) {
                propellantItems.add(item);
            }
        });

        Ingredient projectiles = Ingredient.of(projectileItems.toArray(Item[]::new));
        Ingredient propellant = Ingredient.of(propellantItems.toArray(Item[]::new));
        Ingredient paper = Ingredient.of(Items.PAPER);
        List<RecipeHolder<CraftingRecipe>> recipes = new ArrayList<>();

        for (AbstractRocketItem rocketItem : rocketItems) {
            String rocketPath = BuiltInRegistries.ITEM.getKey(rocketItem).getPath();
            recipes.add(createJeiRecipe(
                "rocket_assembly/" + rocketPath + "_single_propellant",
                rocketItem,
                projectiles,
                propellant,
                paper
            ));
            recipes.add(createJeiRecipe(
                "rocket_assembly/" + rocketPath + "_full_propellant",
                rocketItem,
                projectiles,
                propellant,
                propellant,
                propellant,
                propellant,
                paper
            ));
        }

        cir.setReturnValue(recipes);
    }

    @Inject(method = "getMediumRocketAssemblyRecipes", at = @At("HEAD"), cancellable = true)
    private void onGetMediumRocketAssemblyRecipes(
        CallbackInfoReturnable<List<RecipeHolder<CraftingRecipe>>> cir
    ) {
        List<Item> projectileItems = new ArrayList<>();
        List<Item> propellantItems = new ArrayList<>();
        List<AbstractMediumRocketItem> rocketItems = new ArrayList<>();

        BuiltInRegistries.ITEM.forEach(item -> {
            if (item instanceof AbstractHeavyAutocannonProjectileItem
                && !(item instanceof HA_APDSProjectileItem)
                && !(item instanceof HA_APDSFSProjectileItem)
                && !(item instanceof HA_SmokeProjectileItem)) {
                projectileItems.add(item);
            } else if (item instanceof AbstractMediumRocketItem rocketItem) {
                rocketItems.add(rocketItem);
            } else if (isRocketPropellant(item)) {
                propellantItems.add(item);
            }
        });

        Ingredient projectiles = Ingredient.of(projectileItems.toArray(Item[]::new));
        Ingredient propellant = Ingredient.of(propellantItems.toArray(Item[]::new));
        Ingredient paper = Ingredient.of(Items.PAPER);
        List<RecipeHolder<CraftingRecipe>> recipes = new ArrayList<>();

        for (AbstractMediumRocketItem rocketItem : rocketItems) {
            String rocketPath = BuiltInRegistries.ITEM.getKey(rocketItem).getPath();
            recipes.add(createJeiRecipe(
                "medium_rocket_assembly/" + rocketPath + "_single_propellant",
                rocketItem,
                projectiles,
                propellant,
                paper,
                paper
            ));
            recipes.add(createJeiRecipe(
                "medium_rocket_assembly/" + rocketPath + "_full_propellant",
                rocketItem,
                projectiles,
                propellant,
                propellant,
                propellant,
                propellant,
                propellant,
                propellant,
                paper,
                paper
            ));
        }

        cir.setReturnValue(recipes);
    }

    private static boolean isRocketPropellant(Item item) {
        return item == Items.GUNPOWDER
            || item == CBCItems.GUNPOWDER_PINCH.get()
            || item == CBCItems.PACKED_GUNPOWDER.get();
    }

    private static RecipeHolder<CraftingRecipe> createJeiRecipe(
        String recipePath,
        Item result,
        Ingredient... ingredients
    ) {
        ShapelessRecipe recipe = new ShapelessRecipe(
            "",
            CraftingBookCategory.MISC,
            result.getDefaultInstance(),
            NonNullList.of(Ingredient.EMPTY, ingredients)
        );
        ResourceLocation recipeId = ResourceLocation.fromNamespaceAndPath("cbc_at", "jei/" + recipePath);
        return new RecipeHolder<>(recipeId, recipe);
    }
}
