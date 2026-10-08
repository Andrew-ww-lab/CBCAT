package com.cbcatfix.rocket;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

/** Keep registry entries for saves, while removing the superseded production choices. */
public final class LegacySmallLaunchers {
    private LegacySmallLaunchers() {}

    public static boolean isLegacy(ItemStack stack) {
        var id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id.getNamespace().equals("cbc_at")
            && id.getPath().matches("(?:unbored_)?(?:bronze|cast_iron)_rocket_pod_(?:rail|breech)");
    }
}
