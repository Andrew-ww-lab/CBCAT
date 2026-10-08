package com.cbcatfix.compat;

import com.cbcatfix.rocket.LegacySmallLaunchers;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;

/** Discovered only by JEI; no common initializer references optional JEI types. */
@JeiPlugin
public final class LauncherJeiPlugin implements IModPlugin {
    @Override public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath("cbcatfix", "launcher_progression");
    }

    @Override public void onRuntimeAvailable(IJeiRuntime runtime) {
        var ingredients = runtime.getIngredientManager();
        var hidden = ingredients.getAllIngredients(VanillaTypes.ITEM_STACK).stream()
            .filter(LegacySmallLaunchers::isLegacy).toList();
        // JEI normally omits them already because the creative entries were removed.
        if (!hidden.isEmpty()) ingredients.removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, hidden);
    }
}
