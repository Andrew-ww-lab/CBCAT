package com.cbcatfix.rocket;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/** Equipment only; target snapshots belong exclusively to flying projectiles.
 * Uses the same per-round CustomData transport as fuel and lightweight construction.
 * No item, recipe or installation interaction is registered here.
 */
public final class RocketSeekerEquipment {
    private static final String KEY = "CbcatFixSeekerEquipment";
    private static final String ACTIVE_RADAR = "active_radar";

    private RocketSeekerEquipment() {}

    public static boolean hasActiveRadar(ItemStack rocket) {
        return ACTIVE_RADAR.equals(rocket.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
            .copyTag().getString(KEY));
    }

    /** Serialization seam for migration and future equipment installation, not an installation UX. */
    public static void setActiveRadar(ItemStack rocket) {
        CustomData.update(DataComponents.CUSTOM_DATA, rocket, tag -> tag.putString(KEY, ACTIVE_RADAR));
    }
}
