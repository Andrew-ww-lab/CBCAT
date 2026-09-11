package com.cbcatfix.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.cbcatfix.rocket.RocketPayloadAccess;
import com.cbcatfix.rocket.RocketBalance;
import com.cbcatfix.rocket.RocketFlightEffects;
import com.cbcatfix.rocket.SableGuidanceCompat;
import com.dsvv.cbcat.cannon.rocketpod.munitions.AbstractRocket;
import com.dsvv.cbcat.cannon.rocketpod.munitions.AbstractFuzedRocket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import com.cbcatfix.rocket.RocketFlightState;
import com.cbcatfix.rocket.RocketSeeker;
import com.cbcatfix.rocket.RocketSteering;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.level.Level;
import rbasamoyai.createbigcannons.munitions.fuzes.FuzeItem;

@Mixin(value = AbstractRocket.class, remap = false)
public class AbstractRocketFlightMixin implements RocketPayloadAccess {

    @Shadow private int fuel;

    @Unique private final RocketFlightState cbcatfix$flightState = new RocketFlightState();

    @Override
    public RocketFlightState cbcatfix$flightState() { return this.cbcatfix$flightState; }

    @Unique private int cbcatfix$payloadCount = 1;
    @Unique private ItemStack cbcatfix$universalFuze = ItemStack.EMPTY;
    @Unique private double cbcatfix$maxPoweredSpeed = RocketBalance.maxSpeed(RocketBalance.Tier.SMALL);
    @Unique private float cbcatfix$guidanceTurnRate = RocketBalance.guidanceTurnRate(RocketBalance.Tier.SMALL);
    @Unique private int cbcatfix$poweredFlightTicks = -1;
    @Unique private float cbcatfix$maximumDurability = RocketBalance.maximumDurability(RocketBalance.Tier.SMALL);
    @Unique private float cbcatfix$durability = this.cbcatfix$maximumDurability;
    @Unique private boolean cbcatfix$armorPiercing;
    @Unique private boolean cbcatfix$guidanceBroken;

    @Override
    public void cbcatfix$configurePayload(int payloadCount, ItemStack fuze, boolean armorPiercing) {
        this.cbcatfix$payloadCount = Math.clamp(payloadCount, 1, 2);
        this.cbcatfix$universalFuze = fuze.copy();
        this.cbcatfix$armorPiercing = armorPiercing;
        this.cbcatfix$guidanceBroken = false;
    }

    @Override
    public int cbcatfix$getPayloadCount() {
        return this.cbcatfix$payloadCount;
    }

    @Override
    public void cbcatfix$configureFlight(double maxPoweredSpeed, float guidanceTurnRate) {
        this.cbcatfix$maxPoweredSpeed = maxPoweredSpeed;
        this.cbcatfix$guidanceTurnRate = guidanceTurnRate;
    }

    @Override
    public void cbcatfix$setPoweredFlightTicks(int poweredFlightTicks) {
        this.cbcatfix$poweredFlightTicks = Math.max(0, poweredFlightTicks);
    }

    @Override
    public double cbcatfix$getMaxPoweredSpeed() {
        return this.cbcatfix$maxPoweredSpeed;
    }

    @Override
    public float cbcatfix$getGuidanceTurnRate() {
        return this.cbcatfix$guidanceTurnRate;
    }

    @Override
    public int cbcatfix$getPoweredFlightTicks() {
        return this.cbcatfix$poweredFlightTicks >= 0 ? this.cbcatfix$poweredFlightTicks : this.fuel;
    }

    @Override
    public void cbcatfix$configureDurability(float maximumDurability) {
        this.cbcatfix$maximumDurability = Math.max(1.0f, maximumDurability);
        this.cbcatfix$durability = this.cbcatfix$maximumDurability;
    }

    @Override
    public boolean cbcatfix$damageDurability(float damage) {
        this.cbcatfix$durability = Math.max(0.0f, this.cbcatfix$durability - Math.max(0.0f, damage));
        return this.cbcatfix$durability > 0.0f;
    }

    @Override public float cbcatfix$getDurability() { return this.cbcatfix$durability; }
    @Override public float cbcatfix$getMaximumDurability() { return this.cbcatfix$maximumDurability; }
    @Override public boolean cbcatfix$isArmorPiercing() { return this.cbcatfix$armorPiercing; }

    @Override
    public boolean cbcatfix$hasGuidance() {
        return !this.cbcatfix$guidanceBroken
            && "com.happysg.radar.item.GuidedFuzeItem".equals(this.cbcatfix$universalFuze.getItem().getClass().getName());
    }

    @Override
    public void cbcatfix$breakGuidance() {
        if (this.cbcatfix$hasGuidance()) {
            this.cbcatfix$guidanceBroken = true;
        }
    }

    @Inject(method = "getForces", at = @At("HEAD"), cancellable = true, remap = false)
    private void cbcatfix$flyStraightWhileFueled(Vec3 position, Vec3 velocity, CallbackInfoReturnable<Vec3> cir) {
        if (this.cbcatfix$getPoweredFlightTicks() <= 0) {
            return;
        }
        cir.setReturnValue(RocketSteering.poweredForces((AbstractRocket<?>) (Object) this,
            velocity, this.cbcatfix$maxPoweredSpeed));
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE",
        target = "Lcom/dsvv/cbcat/cannon/rocketpod/munitions/AbstractRocket;expireProjectile()V"))
    private void cbcatfix$keepBallisticFlight(AbstractRocket<?> rocket) {
        // Only CBCAT's post-fuel age expiry is suppressed; impact/fuze removal is unchanged.
    }

    @Inject(method = "tick", at = @At("HEAD"), remap = false)
    private void cbcatfix$prepareExtendedFuel(CallbackInfo ci) {
        AbstractRocket<?> rocket = (AbstractRocket<?>) (Object) this;
        RocketFlightEffects.initializeOrientation(rocket);
        this.cbcatfix$flightState.beginTick(rocket);
        if (this.cbcatfix$poweredFlightTicks >= 0) {
            this.fuel = this.cbcatfix$poweredFlightTicks > 0 ? 2 : 0;
        }
        if (!rocket.level().isClientSide() && this.cbcatfix$hasGuidance()) {
            RocketSteering.tick(rocket);
        }
    }

    @Inject(method = "tick", at = @At("TAIL"), remap = false)
    private void cbcatfix$tickUniversalFuze(CallbackInfo ci) {
        RocketFlightEffects.spawnExhaust((AbstractRocket<?>) (Object) this);
        if (this.cbcatfix$poweredFlightTicks > 0) {
            this.cbcatfix$poweredFlightTicks--;
        }
        AbstractRocket<?> rocket = (AbstractRocket<?>) (Object) this;
        rocket.refreshDimensions();
        if (!(rocket instanceof AbstractFuzedRocket<?>)
            && !rocket.level().isClientSide()
            && this.cbcatfix$universalFuze.getItem() instanceof FuzeItem fuze
            && !SableGuidanceCompat.isGuidedFuze(this.cbcatfix$universalFuze)
            && fuze.onProjectileTick(this.cbcatfix$universalFuze, rocket)) {
            rocket.discard();
        }
    }

    @Redirect(
        method = "tick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;addAlwaysVisibleParticle(Lnet/minecraft/core/particles/ParticleOptions;ZDDDDDD)V",
            remap = true
        ),
        remap = false
    )
    private void cbcatfix$replaceOriginalRocketTrail(
        Level level,
        ParticleOptions particle,
        boolean force,
        double x,
        double y,
        double z,
        double velocityX,
        double velocityY,
        double velocityZ
    ) {
        // One rear emitter is added at TAIL from the shared rocket transform.
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"), remap = false)
    private void cbcatfix$saveBalancedPayload(CompoundTag tag, CallbackInfo ci) {
        if (this.cbcatfix$flightState.seeker != null) {
            tag.put("CbcatFixSeeker", this.cbcatfix$flightState.seeker.save());
        }
        tag.putInt("CbcatFixPayloadCount", this.cbcatfix$payloadCount);
        tag.putDouble("CbcatFixMaxPoweredSpeed", this.cbcatfix$maxPoweredSpeed);
        tag.putFloat("CbcatFixGuidanceTurnRate", this.cbcatfix$guidanceTurnRate);
        tag.putInt("CbcatFixPoweredFlightTicks", this.cbcatfix$getPoweredFlightTicks());
        tag.putFloat("CbcatFixDurability", this.cbcatfix$durability);
        tag.putFloat("CbcatFixMaximumDurability", this.cbcatfix$maximumDurability);
        tag.putBoolean("CbcatFixArmorPiercing", this.cbcatfix$armorPiercing);
        tag.putBoolean("CbcatFixGuidanceBroken", this.cbcatfix$guidanceBroken);
        if (!this.cbcatfix$universalFuze.isEmpty()) {
            tag.put("CbcatFixUniversalFuze", this.cbcatfix$universalFuze.saveOptional(((AbstractRocket<?>) (Object) this).level().registryAccess()));
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"), remap = false)
    private void cbcatfix$loadBalancedPayload(CompoundTag tag, CallbackInfo ci) {
        this.cbcatfix$flightState.seeker = RocketSeeker.load(tag.getCompound("CbcatFixSeeker"));
        this.fuel = tag.getInt("Fuel");
        this.cbcatfix$payloadCount = Math.clamp(tag.getInt("CbcatFixPayloadCount"), 1, 2);
        if (tag.contains("CbcatFixMaxPoweredSpeed")) {
            this.cbcatfix$maxPoweredSpeed = tag.getDouble("CbcatFixMaxPoweredSpeed");
        }
        if (tag.contains("CbcatFixGuidanceTurnRate")) {
            this.cbcatfix$guidanceTurnRate = tag.getFloat("CbcatFixGuidanceTurnRate");
        }
        this.cbcatfix$poweredFlightTicks = tag.contains("CbcatFixPoweredFlightTicks")
            ? Math.max(0, tag.getInt("CbcatFixPoweredFlightTicks"))
            : Math.max(0, this.fuel);
        this.cbcatfix$maximumDurability = tag.contains("CbcatFixMaximumDurability")
            ? Math.max(1.0f, tag.getFloat("CbcatFixMaximumDurability"))
            : RocketBalance.maximumDurability(RocketBalance.Tier.SMALL);
        this.cbcatfix$durability = tag.contains("CbcatFixDurability")
            ? Math.clamp(tag.getFloat("CbcatFixDurability"), 0.0f, this.cbcatfix$maximumDurability)
            : this.cbcatfix$maximumDurability;
        this.cbcatfix$armorPiercing = tag.getBoolean("CbcatFixArmorPiercing");
        this.cbcatfix$guidanceBroken = tag.getBoolean("CbcatFixGuidanceBroken");
        if (tag.contains("CbcatFixUniversalFuze", Tag.TAG_COMPOUND)) {
            this.cbcatfix$universalFuze = ItemStack.parseOptional(
                ((AbstractRocket<?>) (Object) this).level().registryAccess(),
                tag.getCompound("CbcatFixUniversalFuze")
            );
        }
    }

    @Inject(method = "baseWriteSpawnData", at = @At("TAIL"), remap = false)
    private void cbcatfix$writeBalancedFlight(RegistryFriendlyByteBuf buffer, CallbackInfo ci) {
        buffer.writeVarInt(this.cbcatfix$getPoweredFlightTicks());
        buffer.writeDouble(this.cbcatfix$maxPoweredSpeed);
        buffer.writeFloat(this.cbcatfix$guidanceTurnRate);
        buffer.writeFloat(this.cbcatfix$durability);
        buffer.writeFloat(this.cbcatfix$maximumDurability);
        buffer.writeBoolean(this.cbcatfix$armorPiercing);
        buffer.writeBoolean(this.cbcatfix$guidanceBroken);
    }

    @Inject(method = "baseReadSpawnData", at = @At("TAIL"), remap = false)
    private void cbcatfix$readBalancedFlight(RegistryFriendlyByteBuf buffer, CallbackInfo ci) {
        this.cbcatfix$poweredFlightTicks = buffer.readVarInt();
        this.cbcatfix$maxPoweredSpeed = buffer.readDouble();
        this.cbcatfix$guidanceTurnRate = buffer.readFloat();
        this.cbcatfix$durability = buffer.readFloat();
        this.cbcatfix$maximumDurability = buffer.readFloat();
        this.cbcatfix$armorPiercing = buffer.readBoolean();
        this.cbcatfix$guidanceBroken = buffer.readBoolean();
    }

    @Inject(method = "onTickRotate", at = @At("HEAD"), cancellable = true, remap = false)
    private void cbcatfix$alignFlightOrientation(CallbackInfo ci) {
        RocketSteering.alignToVelocity((AbstractRocket<?>) (Object) this);
        ci.cancel();
    }
}
