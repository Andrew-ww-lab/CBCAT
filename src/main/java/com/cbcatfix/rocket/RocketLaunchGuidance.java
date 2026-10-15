package com.cbcatfix.rocket;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.fml.ModList;
import rbasamoyai.createbigcannons.cannon_control.contraption.PitchOrientedContraptionEntity;
import rbasamoyai.createbigcannons.munitions.AbstractCannonProjectile;

/** Called only at actual entity insertion, after the authoritative lane/slot has been selected. */
public final class RocketLaunchGuidance {
    private RocketLaunchGuidance() {}

    public static void initialize(ItemStack firedRound, AbstractCannonProjectile projectile,
                                  PitchOrientedContraptionEntity carrier) {
        if (!(projectile.level() instanceof ServerLevel level)
            || !(projectile instanceof RocketPayloadAccess payload)
            || !RocketSeekerEquipment.hasActiveRadar(firedRound)) return;
        // Equipment is copied through the ordinary item -> projectile lifecycle.
        // No controller, mount or network reference survives this call.
        payload.cbcatfix$flightState().activeRadarEquipped = true;
        payload.cbcatfix$flightState().seeker = null;
        if (!com.cbcatfix.config.CbcatFixConfig.value(com.cbcatfix.config.CbcatFixConfig.GUIDANCE_ENABLED)
            || !ModList.get().isLoaded("create_radar") || carrier == null
            || !(carrier.getController() instanceof BlockEntity mount)) return;
        var endpoint = com.cbcatfix.compat.radars.RadarLauncherNetwork.endpoint(level, mount.getBlockPos());
        if (endpoint != null) payload.cbcatfix$flightState().seeker = RadarDesignation.captureNetwork(level, endpoint);
    }
}
