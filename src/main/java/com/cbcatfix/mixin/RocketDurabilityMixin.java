package com.cbcatfix.mixin;

import com.cbcatfix.rocket.RocketDamage;
import com.cbcatfix.rocket.RocketEngineAccess;
import com.cbcatfix.rocket.RocketPayloadAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rbasamoyai.createbigcannons.munitions.AbstractCannonProjectile;
import rbasamoyai.createbigcannons.munitions.ProjectileContext;

@Mixin(value = AbstractCannonProjectile.class, remap = false)
public abstract class RocketDurabilityMixin implements RocketEngineAccess {
    @Unique private static final EntityDataAccessor<Boolean> CBCATFIX_ENGINE_DISABLED =
        SynchedEntityData.defineId(AbstractCannonProjectile.class, EntityDataSerializers.BOOLEAN);

    public boolean isPickable() {
        return (Object) this instanceof RocketPayloadAccess;
    }

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void cbcatfix$defineEngineState(SynchedEntityData.Builder builder, CallbackInfo ci) {
        // The accessor is registered on AbstractCannonProjectile: vanilla requires every
        // subclass to initialize its inherited slots, including non-rocket warheads.
        builder.define(CBCATFIX_ENGINE_DISABLED, false);
    }

    @Override public boolean cbcatfix$isEngineDisabled() {
        return ((AbstractCannonProjectile) (Object) this).getEntityData().get(CBCATFIX_ENGINE_DISABLED);
    }

    @Override public void cbcatfix$setEngineDisabled(boolean disabled) {
        ((AbstractCannonProjectile) (Object) this).getEntityData().set(CBCATFIX_ENGINE_DISABLED, disabled);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void cbcatfix$saveEngineState(CompoundTag tag, CallbackInfo ci) {
        if ((Object) this instanceof RocketPayloadAccess payload) {
            tag.putBoolean("CbcatFixEngineDisabled", cbcatfix$isEngineDisabled());
            tag.putFloat("CbcatFixFlightMass", payload.cbcatfix$flightState().flightMass((AbstractCannonProjectile) (Object) this));
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void cbcatfix$loadEngineState(CompoundTag tag, CallbackInfo ci) {
        if ((Object) this instanceof RocketPayloadAccess payload) {
            cbcatfix$setEngineDisabled(tag.getBoolean("CbcatFixEngineDisabled"));
            float mass = tag.getFloat("CbcatFixFlightMass");
            payload.cbcatfix$flightState().launchMass = Float.isFinite(mass) && mass > 0 ? mass : 0;
        }
    }

    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void cbcatfix$damageRocket(DamageSource source, float damage, CallbackInfoReturnable<Boolean> cir) {
        if (!((Object) this instanceof RocketPayloadAccess)) return;
        AbstractCannonProjectile rocket = (AbstractCannonProjectile) (Object) this;
        if (!Float.isFinite(damage) || damage <= 0 || rocket.isInvulnerableTo(source) || rocket.isRemoved()) {
            cir.setReturnValue(false);
            return;
        }
        RocketDamage.damageHull(rocket, damage, rocket.position());
        cir.setReturnValue(true);
    }

    @Inject(method = "onImpact", at = @At("HEAD"))
    private void cbcatfix$disableOnSolidImpact(HitResult hit, AbstractCannonProjectile.ImpactResult result,
                                             ProjectileContext context, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof RocketPayloadAccess && !((AbstractCannonProjectile) (Object) this).level().isClientSide()) {
            RocketDamage.disableEngine((AbstractCannonProjectile) (Object) this);
        }
        // Keep CBC's entity mass cost and the subclass's original fuze result.
    }

    @WrapOperation(method = "clipAndDamage", at = @At(value = "INVOKE",
        target = "Lrbasamoyai/createbigcannons/munitions/AbstractCannonProjectile;setOrientation(Lnet/minecraft/world/phys/Vec3;)V"))
    private void cbcatfix$preservePoweredAttitude(AbstractCannonProjectile rocket, Vec3 trajectory, Operation<Void> original) {
        // CBC normally aligns shells to velocity. Powered rockets keep the same native
        // orientation that guidance/thrust use; otherwise CBC erases steering every tick.
        if (rocket instanceof RocketPayloadAccess payload && payload.cbcatfix$getPoweredFlightTicks() > 0) return;
        original.call(rocket, trajectory);
    }
}
