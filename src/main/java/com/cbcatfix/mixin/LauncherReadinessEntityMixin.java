package com.cbcatfix.mixin;

import com.cbcatfix.rocket.LauncherReadiness;
import com.simibubi.create.content.contraptions.OrientedContraptionEntity;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rbasamoyai.createbigcannons.cannon_control.contraption.PitchOrientedContraptionEntity;

/** Vanilla tracked data sends countdown corrections, never full contraption state each tick. */
@Mixin(value = PitchOrientedContraptionEntity.class, remap = false)
public abstract class LauncherReadinessEntityMixin extends OrientedContraptionEntity implements LauncherReadiness.Sync {
    @Unique private static final EntityDataAccessor<Integer> CBCATFIX_ARMING = SynchedEntityData.defineId(
        PitchOrientedContraptionEntity.class, EntityDataSerializers.INT);

    protected LauncherReadinessEntityMixin(EntityType<?> type, Level level) { super(type, level); }

    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(CBCATFIX_ARMING, -1);
    }

    @Override public void cbcatfix$syncArming(int ticks) {
        if (!level().isClientSide()) entityData.set(CBCATFIX_ARMING, ticks);
    }

    @Inject(method = "onSyncedDataUpdated", at = @At("TAIL"))
    private void cbcatfix$receiveArming(EntityDataAccessor<?> key, CallbackInfo ci) {
        if (level().isClientSide() && CBCATFIX_ARMING.equals(key) && entityData.get(CBCATFIX_ARMING) >= 0
            && getContraption() instanceof LauncherReadiness ready)
            ready.cbcatfix$receiveArmingSnapshot(entityData.get(CBCATFIX_ARMING));
    }
}
