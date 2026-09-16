package com.cbcatfix.rocket;

import com.cbcatfix.CbcatFix;
import net.minecraft.sounds.SoundSource;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import rbasamoyai.createbigcannons.munitions.AbstractCannonProjectile;

/** Server-owned ignition event, independent of launcher and client tracking changes. */
public final class RocketSounds {
    private static final String IGNITED = "CbcatFixIgnitionPlayed";
    private RocketSounds() {}

    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || event.loadedFromDisk()
            || !(event.getEntity() instanceof AbstractCannonProjectile rocket)
            || !(rocket instanceof RocketPayloadAccess payload)
            || payload.cbcatfix$getPoweredFlightTicks() <= 0
            || rocket.getPersistentData().getBoolean(IGNITED)) return;
        rocket.getPersistentData().putBoolean(IGNITED, true);
        event.getLevel().playSound(null, rocket.getX(), rocket.getY(), rocket.getZ(),
            CbcatFix.MISSILE_LAUNCH.get(), SoundSource.NEUTRAL, 2.0f, 1.0f);
    }
}
