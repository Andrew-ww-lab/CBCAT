package com.cbcatfix.rocket;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.fml.ModList;
import rbasamoyai.createbigcannons.munitions.AbstractCannonProjectile;

/** Launch-only bridge. No radar/controller is consulted by the flight controller. */
public final class SableGuidanceCompat {
    private SableGuidanceCompat() {}

    public static boolean isGuidedFuze(ItemStack stack) {
        return !stack.isEmpty()
            && "com.happysg.radar.item.GuidedFuzeItem".equals(stack.getItem().getClass().getName());
    }

    public static void initializeAtLaunch(ItemStack fuze, AbstractCannonProjectile projectile) {
        if (projectile.level().isClientSide() || !(projectile instanceof RocketPayloadAccess payload)) return;
        payload.cbcatfix$flightState().seeker = null;
        if (!com.cbcatfix.config.CbcatFixConfig.value(com.cbcatfix.config.CbcatFixConfig.GUIDANCE_ENABLED) || !isGuidedFuze(fuze) || !ModList.get().isLoaded("create_radar")) return;
        BlockPos monitor = NbtUtils.readBlockPos(
            fuze.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag(), "monitorPos").orElse(null);
        if (monitor != null && projectile.level().hasChunkAt(monitor)) {
            payload.cbcatfix$flightState().seeker = RadarDesignation.capture(projectile.level(), monitor);
        }
    }

}
