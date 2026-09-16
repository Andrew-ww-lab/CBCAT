package com.cbcatfix.rocket;

import com.cbcatfix.config.CbcatFixConfig;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import rbasamoyai.createbigcannons.munitions.AbstractCannonProjectile;

/** One full-3D, acceleration-limited steering controller for every rocket size. */
public final class RocketSteering {
    private RocketSteering() {}

    /** CBC integrates this acceleration into the existing velocity, in blocks/tick. */
    public static Vec3 poweredForces(AbstractCannonProjectile rocket, Vec3 velocity, double cruiseSpeed) {
        Vec3 forward = RocketFlightEffects.forward(rocket);
        double forwardSpeed = velocity.dot(forward);
        Vec3 crossflow = velocity.subtract(forward.scale(forwardSpeed));
        double responseTicks = RocketBalance.responseTicks(RocketBalance.tierForProjectile(rocket))
            / CbcatFixConfig.value(CbcatFixConfig.ACCELERATION_MULTIPLIER);
        if (rocket instanceof RocketPayloadAccess payload) {
            responseTicks *= payload.cbcatfix$flightState().motorMassScale;
        }
        responseTicks = Math.max(1.0, responseTicks);
        // Strong short acceleration, but gentle deceleration for a faster carrier.
        double error = cruiseSpeed - forwardSpeed;
        double acceleration = error >= 0
            ? Math.min(error / responseTicks, cruiseSpeed / responseTicks)
            : error * com.cbcatfix.balance.BalanceDefaults.CARRIER_OVERSPEED_DRAG;
        return forward.scale(acceleration).subtract(crossflow.scale(1.0 / responseTicks));
    }

    public static void tick(AbstractCannonProjectile rocket) {
        if (!CbcatFixConfig.value(CbcatFixConfig.GUIDANCE_ENABLED)
            || rocket.level().isClientSide() || !(rocket instanceof RocketPayloadAccess payload)
            || !payload.cbcatfix$hasGuidance() || payload.cbcatfix$getPoweredFlightTicks() <= 0) return;
        RocketFlightState state = payload.cbcatfix$flightState();
        if (!state.hasClearedLauncher()) return;
        if (state.seeker == null) return;
        Vec3 toAim = state.seeker.aimPoint(rocket).subtract(rocket.position());
        if (toAim.lengthSqr() < 1.0e-8) return;
        Vec3 current = RocketFlightEffects.forward(rocket);
        Vec3 desired = toAim.normalize();
        double angle = Math.acos(Math.clamp(current.dot(desired), -1.0, 1.0));
        double speed = rocket.getDeltaMovement().length();
        double inertia = Math.sqrt(state.flightMass(rocket) * Math.max(0.25, speed));
        double rateLimit = Math.toRadians(payload.cbcatfix$getGuidanceTurnRate()) / inertia;
        double requestedRate = Math.min(angle, rateLimit);
        double acceleration = rateLimit * com.cbcatfix.balance.BalanceDefaults.ANGULAR_RESPONSE;
        state.angularSpeed += Math.clamp(requestedRate - state.angularSpeed, -acceleration, acceleration);
        double turn = Math.min(angle, state.angularSpeed);
        if (turn < 1.0e-8 || angle < 1.0e-8) return;
        var rotation = new Quaternionf().rotationTo(current.toVector3f(), desired.toVector3f());
        Vec3 steered = new Vec3(new Quaternionf().slerp(rotation, (float) (turn / angle))
            .transform(current.toVector3f())).normalize();
        double loss = Math.min(CbcatFixConfig.value(CbcatFixConfig.GUIDANCE_MAX_SPEED_LOSS),
            Math.toDegrees(turn) * CbcatFixConfig.value(CbcatFixConfig.GUIDANCE_SPEED_LOSS_PER_DEGREE));
        // Do not erase inherited crossflow when the seeker changes attitude.
        rocket.setDeltaMovement(rocket.getDeltaMovement().scale(1.0 - loss));
        RocketFlightEffects.setForward(rocket, steered);
    }

    /** The same orientation follows velocity in ballistic flight, even on vertical trajectories. */
    public static void alignToVelocity(AbstractCannonProjectile rocket) {
        if (!rocket.isInGround() && !rocket.level().isClientSide()
            && (!(rocket instanceof RocketPayloadAccess payload) || payload.cbcatfix$getPoweredFlightTicks() <= 0)) {
            RocketFlightEffects.setForward(rocket, rocket.getDeltaMovement());
        }
    }
}
