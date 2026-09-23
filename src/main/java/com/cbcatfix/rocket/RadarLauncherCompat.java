package com.cbcatfix.rocket;

import com.cbcatfix.CbcatFix;
import com.happysg.radar.api.weapon.WeaponFirePreparation;
import com.happysg.radar.api.weapon.WeaponShotAdapterRegistry;
import com.happysg.radar.api.weapon.WeaponShotContext;
import com.happysg.radar.api.weapon.WeaponShotProfile;
import net.minecraft.world.phys.Vec3;

/** Optional Radars 5 API bridge. Loaded only after the mod-presence check. */
public final class RadarLauncherCompat {
    private RadarLauncherCompat() {}

    public static void register() {
        com.cbcatfix.compat.radars.RadarSeekerCompat.register();
        WeaponShotAdapterRegistry.register("cbcatfix:rocket_launchers", RadarLauncherCompat::resolve);
        CbcatFix.LOGGER.info("[CBCAT Fix] Radars 5 rocket launcher adapter registered (native direct mount aiming).");
    }

    private static WeaponShotProfile resolve(WeaponShotContext context) {
        if (!(context.weapon() instanceof LauncherShotView view)) return null;
        var lane = view.cbcatfix$nextLoadedLane();
        if (lane == null || context.weapon().disassembled) {
            var fallback = RocketSableCompat.launchFrame(context.level(), context.entity().position(), Vec3.ZERO);
            return WeaponShotProfile.disabled(fallback.center(), fallback.inheritedVelocity(), "cbcatfix:empty", "rocket_launcher_empty");
        }
        var breech = context.weapon().presentBlockEntities.get(lane);
        if (!(breech instanceof MountedRocketStorage storage)) return null;
        int slot = storage.cbcatfix$firstOccupiedSlot();
        if (slot < 0) return null;
        var mount = RocketMounts.transform(breech, slot);
        Vec3 local = Vec3.atLowerCornerOf(lane).add(mount.center());
        Vec3 center = context.entity().toGlobalVector(local, 1.0f);
        Vec3 forward = context.entity().toGlobalVector(local.add(mount.forward()), 1.0f).subtract(center).normalize();
        var frame = RocketSableCompat.launchFrame(context.level(), center, forward);
        var rocket = storage.cbcatfix$rocketInSlot(slot);
        String fingerprint = "cbcatfix:" + Integer.toHexString(java.util.Objects.hash(lane, slot, rocket.getItem(), rocket.getComponents()));
        boolean ready = context.entity().isAlive() && !LauncherTransfer.isLocked(breech)
            && !LauncherTransfer.isLocked(context.level(), context.entity().blockPosition())
            && RocketMounts.length(context.weapon(), lane) >= mount.length()
            && breech instanceof LauncherCooldown cooldown && cooldown.cbcatfix$getCooldown() <= 0;
        // Use the actual ejection position, just as RocketMountLaunchMixin does.
        // No ballistic shell model, ammunition mutation or second guidance controller.
        return WeaponShotProfile.direct(frame.center(), frame.inheritedVelocity(), fingerprint, 0.0,
            ready, WeaponFirePreparation.READY, "cbcatfix_rocket_direct");
    }
}
