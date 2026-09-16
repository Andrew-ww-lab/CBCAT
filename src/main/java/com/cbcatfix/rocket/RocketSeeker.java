package com.cbcatfix.rocket;

import com.cbcatfix.config.CbcatFixConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;
import rbasamoyai.createbigcannons.munitions.AbstractCannonProjectile;
import java.util.UUID;

/** Designated-target-only seeker: live detection or a frozen last-known point. */
public final class RocketSeeker {
    private final UUID designatedTargetId;
    private final boolean subLevelTarget;
    private Vec3 lastKnownPosition;

    public RocketSeeker(UUID targetId, boolean subLevelTarget, Vec3 initialPosition) {
        this.designatedTargetId = targetId;
        this.subLevelTarget = subLevelTarget;
        this.lastKnownPosition = initialPosition;
    }

    public Vec3 aimPoint(AbstractCannonProjectile rocket) {
        if (!(rocket.level() instanceof ServerLevel level)) return lastKnownPosition;
        Observation observation = observe(level);
        if (observation == null || !isFinite(observation.position()) || !isFinite(observation.velocity())) {
            return lastKnownPosition;
        }
        Vec3 displacement = observation.position().subtract(rocket.position());
        double range = CbcatFixConfig.settings(RocketBalance.tierForProjectile(rocket)).maximumGuidanceRange();
        if (displacement.lengthSqr() > range * range || displacement.lengthSqr() < 1.0e-8
            || RocketFlightEffects.forward(rocket).dot(displacement.normalize()) < Math.cos(Math.toRadians(com.cbcatfix.config.CbcatFixConfig.value(com.cbcatfix.config.CbcatFixConfig.SEEKER_HALF_ANGLE)))) {
            return lastKnownPosition;
        }
        // Do not force-load terrain for a seeker ray.
        double distance = displacement.length();
        if (!loadedRay(level, rocket.position(), observation.position())) return lastKnownPosition;
        ClipContext ray = new ClipContext(rocket.position(), observation.position(),
            ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, rocket);
        if (subLevelTarget && ModList.get().isLoaded("sable")) {
            SableSeekerTarget.ignoreTargetHull(level, designatedTargetId, ray);
        }
        if (level.clip(ray).getType() != HitResult.Type.MISS) return lastKnownPosition;
        lastKnownPosition = observation.position();
        // Lead only a currently detected target; do not extrapolate an unseen target.
        double leadTicks = Math.min(com.cbcatfix.balance.BalanceDefaults.SEEKER_LEAD_TICKS, distance / Math.max(0.1, rocket.getDeltaMovement().length()));
        return lastKnownPosition.add(observation.velocity().scale(leadTicks));
    }

    private Observation observe(ServerLevel level) {
        if (subLevelTarget) {
            return ModList.get().isLoaded("sable") ? SableSeekerTarget.observe(level, designatedTargetId) : null;
        }
        var target = level.getEntity(designatedTargetId);
        return target == null || !target.isAlive() || target.isRemoved() ? null
            : new Observation(target.getBoundingBox().getCenter(), target.getDeltaMovement());
    }

    /** Chunk-grid DDA: sampling at fixed distances can miss a diagonally crossed chunk. */
    private static boolean loadedRay(ServerLevel level, Vec3 from, Vec3 to) {
        int x = BlockPos.containing(from).getX() >> 4;
        int z = BlockPos.containing(from).getZ() >> 4;
        int endX = BlockPos.containing(to).getX() >> 4;
        int endZ = BlockPos.containing(to).getZ() >> 4;
        double dx = to.x - from.x, dz = to.z - from.z;
        int stepX = Double.compare(dx, 0.0), stepZ = Double.compare(dz, 0.0);
        double nextX = stepX == 0 ? Double.POSITIVE_INFINITY : ((x + (stepX > 0 ? 1 : 0)) * 16.0 - from.x) / dx;
        double nextZ = stepZ == 0 ? Double.POSITIVE_INFINITY : ((z + (stepZ > 0 ? 1 : 0)) * 16.0 - from.z) / dz;
        while (true) {
            if (!level.hasChunk(x, z)) return false;
            if (x == endX && z == endZ) return true;
            if (nextX < nextZ) {
                x += stepX;
                nextX += 16.0 / Math.abs(dx);
            } else {
                z += stepZ;
                nextZ += 16.0 / Math.abs(dz);
            }
        }
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("Target", designatedTargetId);
        tag.putBoolean("SubLevel", subLevelTarget);
        tag.putDouble("X", lastKnownPosition.x);
        tag.putDouble("Y", lastKnownPosition.y);
        tag.putDouble("Z", lastKnownPosition.z);
        return tag;
    }

    public static RocketSeeker load(CompoundTag tag) {
        if (!tag.hasUUID("Target")) return null;
        Vec3 position = new Vec3(tag.getDouble("X"), tag.getDouble("Y"), tag.getDouble("Z"));
        if (!isFinite(position)) return null;
        RocketSeeker seeker = new RocketSeeker(tag.getUUID("Target"), tag.getBoolean("SubLevel"), position);
        return seeker;
    }

    static boolean isFinite(Vec3 vector) {
        return vector != null && Double.isFinite(vector.x) && Double.isFinite(vector.y) && Double.isFinite(vector.z);
    }

    record Observation(Vec3 position, Vec3 velocity) {}
}
