package com.cbcatfix.rocket;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.phys.Vec3;
import rbasamoyai.createbigcannons.munitions.AbstractCannonProjectile;
import rbasamoyai.createbigcannons.effects.particles.smoke.CannonSmokeParticleData;
import rbasamoyai.createbigcannons.effects.particles.smoke.TrailSmokeParticleData;

/** Authoritative forward/rear transform and lightweight exhaust for flying rockets. */
public final class RocketFlightEffects {
    private static final double MIN_DIRECTION_LENGTH_SQUARED = 1.0e-8;

    private RocketFlightEffects() {
    }

    /**
     * Returns the direction in which the rocket body is currently pointing.
     * CBC's synchronized orientation is authoritative; velocity is only a
     * launch-time fallback for entities whose orientation has not been set yet.
     */
    public static Vec3 forward(AbstractCannonProjectile rocket) {
        Vec3 orientation = rocket.getOrientation();
        if (isUsableDirection(orientation)) {
            return orientation.normalize();
        }

        Vec3 movement = rocket.getDeltaMovement();
        if (isUsableDirection(movement)) {
            return movement.normalize();
        }

        Vec3 rotationDirection = directionFromRocketRotation(rocket.getXRot(), rocket.getYRot());
        return isUsableDirection(rotationDirection) ? rotationDirection.normalize() : new Vec3(0.0, 0.0, 1.0);
    }

    /** Establishes CBC's synchronized orientation before the first flight step. */
    public static void initializeOrientation(AbstractCannonProjectile rocket) {
        if (isUsableDirection(rocket.getOrientation())) {
            return;
        }
        setForward(rocket, forward(rocket));
    }

    /** Updates all rotation representations from one authoritative forward vector. */
    public static void setForward(AbstractCannonProjectile rocket, Vec3 direction) {
        if (!isUsableDirection(direction)) {
            return;
        }
        Vec3 normalized = direction.normalize();
        rocket.setOrientation(normalized);
        double horizontal = Math.sqrt(normalized.x * normalized.x + normalized.z * normalized.z);
        rocket.setYRot((float) Math.toDegrees(Math.atan2(normalized.x, normalized.z)));
        rocket.setXRot((float) Math.toDegrees(Math.atan2(normalized.y, horizontal)));
    }

    /** Mirrors the yaw/pitch convention used by CBCAT's rocket entities. */
    public static Vec3 directionFromRocketRotation(float pitch, float yaw) {
        double pitchRadians = Math.toRadians(pitch);
        double yawRadians = Math.toRadians(yaw);
        double horizontal = Math.cos(pitchRadians);
        return new Vec3(
            Math.sin(yawRadians) * horizontal,
            Math.sin(pitchRadians),
            Math.cos(yawRadians) * horizontal
        );
    }

    public static Vec3 rearPosition(AbstractCannonProjectile rocket) {
        RocketBalance.Tier tier = RocketBalance.tierForProjectile(rocket);
        return rearPosition(rocket.position(), forward(rocket), tier);
    }

    public static void spawnExhaust(AbstractCannonProjectile rocket) {
        if (!rocket.level().isClientSide() || rocket.isInGround()) {
            return;
        }
        Vec3 forward = forward(rocket);
        RocketBalance.Tier tier = RocketBalance.tierForProjectile(rocket);
        Vec3 rear = rearPosition(rocket.position(), forward, tier);
        RocketFlightState state = ((RocketPayloadAccess) rocket).cbcatfix$flightState();
        Vec3 previousRear = state.previousRear == null ? rear : state.previousRear;
        state.previousRear = rear;
        // Do not draw a long trail across a teleport or an initial spawn position.
        if (previousRear.distanceToSqr(rear) > 4096.0) previousRear = rear;
        boolean powered = rocket instanceof RocketPayloadAccess payload && payload.cbcatfix$getPoweredFlightTicks() > 0;
        int lifetime = tier == RocketBalance.Tier.SMALL
            ? 80 + rocket.level().random.nextInt(20)
            : 100 + rocket.level().random.nextInt(30);
        ParticleOptions particle = powered
            ? new CannonSmokeParticleData(
                tier == RocketBalance.Tier.SMALL ? 2.0f : 4.0f,
                tier == RocketBalance.Tier.SMALL ? 0.25f : 0.33f,
                lifetime,
                tier == RocketBalance.Tier.SMALL ? 0.66f : 0.75f
            )
            : new TrailSmokeParticleData(lifetime);

        // Restore CBCAT's dense interpolated trail appearance, but cap it well
        // below the old 30 particles/tick and anchor it to the corrected rear.
        int emissions = Math.clamp((int) Math.ceil(previousRear.distanceTo(rear) * 4.0), 3, 8);
        Vec3 rearwardVelocity = powered ? forward.scale(-0.01) : Vec3.ZERO;
        for (int index = 0; index < emissions; index++) {
            double partial = emissions == 1 ? 1.0 : (double) index / (emissions - 1);
            Vec3 position = previousRear.lerp(rear, partial);
            double spreadX = rocket.level().random.nextDouble() * 0.004 - 0.002;
            double spreadY = rocket.level().random.nextDouble() * 0.004 - 0.002;
            double spreadZ = rocket.level().random.nextDouble() * 0.004 - 0.002;
            rocket.level().addAlwaysVisibleParticle(
                particle, true, position.x, position.y, position.z,
                spreadX + rearwardVelocity.x,
                spreadY + rearwardVelocity.y,
                spreadZ + rearwardVelocity.z
            );
        }
    }

    private static Vec3 rearPosition(Vec3 center, Vec3 forward, RocketBalance.Tier tier) {
        return center.subtract(forward.scale(rearOffset(tier)));
    }

    private static double rearOffset(RocketBalance.Tier tier) {
        return RocketGeometry.bodyLength(tier) * 0.5;
    }

    private static boolean isUsableDirection(Vec3 direction) {
        return direction != null
            && Double.isFinite(direction.x)
            && Double.isFinite(direction.y)
            && Double.isFinite(direction.z)
            && direction.lengthSqr() >= MIN_DIRECTION_LENGTH_SQUARED;
    }
}
