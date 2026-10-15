package com.cbcatfix.rocket;

import com.cbcatfix.CbcatFix;
import com.happysg.radar.api.weapon.WeaponFirePreparation;
import com.happysg.radar.api.weapon.WeaponShotAdapterRegistry;
import com.happysg.radar.api.weapon.WeaponShotContext;
import com.happysg.radar.api.weapon.WeaponShotProfile;
import com.happysg.radar.compat.cbc.CbcRadarWeaponAdapter;
import rbasamoyai.createbigcannons.cannon_control.contraption.AbstractMountedCannonContraption;
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
        if (!(context.weapon() instanceof CbcRadarWeaponAdapter adapter)) return null;
        var entity = adapter.legacyCbcContext().getContraption();
        if (entity == null || !(entity.getContraption() instanceof AbstractMountedCannonContraption weapon)
            || !(weapon instanceof LauncherShotView view)) return null;
        var lane = view.cbcatfix$nextLoadedLane();
        if (lane == null || weapon.disassembled) {
            var fallback = RocketSableCompat.launchFrame(context.level(), entity.position(), Vec3.ZERO);
            return WeaponShotProfile.disabled(fallback.center(), fallback.inheritedVelocity(), "cbcatfix:empty",
                weapon.disassembled ? "rocket_launcher_invalid" : "rocket_launcher_empty");
        }
        var breech = weapon.presentBlockEntities.get(lane);
        if (!(breech instanceof MountedRocketStorage storage)) return null;
        int slot = storage.cbcatfix$firstOccupiedSlot();
        if (slot < 0) return null;
        var mount = RocketMounts.transform(breech, slot);
        Vec3 local = Vec3.atLowerCornerOf(lane).add(mount.center());
        Vec3 center = entity.toGlobalVector(local, 1.0f);
        Vec3 forward = entity.toGlobalVector(local.add(mount.forward()), 1.0f).subtract(center).normalize();
        var frame = RocketSableCompat.launchFrame(context.level(), center, forward);
        var rocket = storage.cbcatfix$rocketInSlot(slot);
        String fingerprint = "cbcatfix:" + Integer.toHexString(java.util.Objects.hash(lane, slot, rocket.getItem(), rocket.getComponents()));
        String reason = !entity.isAlive() ? "rocket_launcher_invalid"
            : weapon instanceof LauncherReadiness readiness && readiness.cbcatfix$armingTicksRemaining() > 0
                ? "rocket_launcher_arming"
            : LauncherTransfer.isLocked(breech) || LauncherTransfer.isLocked(context.level(), entity.blockPosition())
                ? "rocket_launcher_blocked"
            : RocketMounts.length(weapon, lane) < mount.length() || !(breech instanceof LauncherCooldown)
                ? "rocket_launcher_invalid"
            : ((LauncherCooldown) breech).cbcatfix$getCooldown() > 0 ? "rocket_launcher_cooldown"
            : "cbcatfix_rocket_direct";
        boolean ready = "cbcatfix_rocket_direct".equals(reason);
        // Use the actual ejection position, just as RocketMountLaunchMixin does.
        // No ballistic shell model, ammunition mutation or second guidance controller.
        return WeaponShotProfile.direct(frame.center(), frame.inheritedVelocity(), fingerprint, 0.0,
            ready, WeaponFirePreparation.READY, reason);
    }
}
