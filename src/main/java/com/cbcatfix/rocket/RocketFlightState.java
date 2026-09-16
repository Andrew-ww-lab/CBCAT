package com.cbcatfix.rocket;

import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import rbasamoyai.createbigcannons.munitions.AbstractCannonProjectile;

/** Per-projectile state; no global entity caches and no second authoritative orientation. */
public final class RocketFlightState {
    public RocketSeeker seeker;
    double angularSpeed;
    public float launchMass;
    public double motorMassScale = 1.0;
    private double clearanceRemaining;
    private Vec3 clearanceAxis = Vec3.ZERO;
    private Vec3 clearanceCarrierVelocity = Vec3.ZERO;
    private Vec3 clearancePreviousPosition;

    public void beginClearance(Vec3 position, Vec3 axis, Vec3 carrierVelocity, double distance) {
        clearanceRemaining = Math.max(0, distance);
        clearanceAxis = axis.normalize();
        clearanceCarrierVelocity = carrierVelocity;
        clearancePreviousPosition = position;
    }

    public boolean hasClearedLauncher() { return clearanceRemaining <= 0; }

    public void saveClearance(net.minecraft.nbt.CompoundTag tag) {
        tag.remove("CbcatFixClearance");
        if (hasClearedLauncher() || clearancePreviousPosition == null) return;
        var saved = new net.minecraft.nbt.CompoundTag();
        saved.putDouble("Remaining", clearanceRemaining);
        saveVector(saved, "Axis", clearanceAxis);
        saveVector(saved, "Carrier", clearanceCarrierVelocity);
        saveVector(saved, "Position", clearancePreviousPosition);
        tag.put("CbcatFixClearance", saved);
    }

    public void loadClearance(net.minecraft.nbt.CompoundTag tag) {
        var saved = tag.getCompound("CbcatFixClearance");
        clearanceRemaining = 0;
        double remaining = saved.getDouble("Remaining");
        if (Double.isFinite(remaining) && remaining > 0) {
            Vec3 axis = loadVector(saved, "Axis");
            if (axis.lengthSqr() > 0) beginClearance(loadVector(saved, "Position"), axis, loadVector(saved, "Carrier"), remaining);
        }
    }

    private static void saveVector(net.minecraft.nbt.CompoundTag tag, String key, Vec3 vector) {
        tag.putDouble(key + "X", vector.x);
        tag.putDouble(key + "Y", vector.y);
        tag.putDouble(key + "Z", vector.z);
    }

    private static Vec3 loadVector(net.minecraft.nbt.CompoundTag tag, String key) {
        double x = tag.getDouble(key + "X"), y = tag.getDouble(key + "Y"), z = tag.getDouble(key + "Z");
        return Double.isFinite(x) && Double.isFinite(y) && Double.isFinite(z) ? new Vec3(x, y, z) : Vec3.ZERO;
    }

    public float flightMass(AbstractCannonProjectile rocket) {
        // Never use the eroded CBC penetration budget for steering inertia.
        if (launchMass <= 0) launchMass = Math.max(0.25f, rocket.getProjectileMass());
        return launchMass;
    }
    private Vec3 previousForward;
    Vec3 previousRear;

    public void beginTick(AbstractCannonProjectile rocket) {
        if (!hasClearedLauncher() && clearancePreviousPosition != null) {
            // Project actual displacement onto the launch axis, relative to the carrier's launch velocity.
            // No permanent launcher reference or arbitrary timer; reverse travel cannot grant clearance.
            clearanceRemaining -= rocket.position().subtract(clearancePreviousPosition)
                .subtract(rocket.tickCount == 0 ? Vec3.ZERO : clearanceCarrierVelocity).dot(clearanceAxis);
            clearancePreviousPosition = rocket.position();
        }
        previousForward = RocketFlightEffects.forward(rocket);
        rocket.yRotO = rocket.getYRot();
        rocket.xRotO = rocket.getXRot();
    }

    public Vec3 interpolatedForward(AbstractCannonProjectile rocket, float partialTick) {
        Vec3 current = RocketFlightEffects.forward(rocket);
        if (previousForward == null) return current;
        var rotation = new Quaternionf().rotationTo(previousForward.toVector3f(), current.toVector3f());
        return new Vec3(new Quaternionf().slerp(rotation, Math.clamp(partialTick, 0, 1))
            .transform(previousForward.toVector3f())).normalize();
    }
}
