package com.cbcatfix.mixin;

import com.cbcatfix.rocket.MountedRocketItemHandler;
import com.cbcatfix.rocket.MountedRocketStorage;
import com.cbcatfix.rocket.RocketFlightEffects;
import com.cbcatfix.rocket.RocketGeometry;
import com.cbcatfix.rocket.RocketMounts;
import com.cbcatfix.rocket.RocketSableCompat;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rbasamoyai.createbigcannons.cannon_control.contraption.AbstractMountedCannonContraption;
import rbasamoyai.createbigcannons.cannon_control.contraption.PitchOrientedContraptionEntity;
import rbasamoyai.createbigcannons.munitions.AbstractCannonProjectile;

@Mixin(targets = {"com.dsvv.cbcat.cannon.rocketpod.contraption.MountedRocketPodContraption",
    "com.dsvv.cbcat.cannon.medium_rocketpod.contraption.MountedMediumRocketRailContraption"}, remap = false)
public class RocketMountLaunchMixin {
    @Unique private Vec3 cbcatfix$launchCenter;
    @Unique private Vec3 cbcatfix$launchForward;
    @Unique private Vec3 cbcatfix$inheritedVelocity;
    @Unique private PitchOrientedContraptionEntity cbcatfix$carrier;
    @Unique private double cbcatfix$bodyLength;
    @Unique private double cbcatfix$clearanceDistance;
    @Unique private Vec3 cbcatfix$carrierVelocity;
    @Unique private MountedRocketStorage cbcatfix$launchStorage;
    @Unique private int cbcatfix$launchSlot;
    @Unique private ItemStack cbcatfix$launchItem = ItemStack.EMPTY;

    @Inject(method = "fireShot", at = @At("HEAD"), cancellable = true)
    private void cbcatfix$captureSlot(ServerLevel level, PitchOrientedContraptionEntity entity, CallbackInfo ci) {
        cbcatfix$launchCenter = null;
        cbcatfix$launchStorage = null;
        cbcatfix$launchItem = ItemStack.EMPTY;
        var cannon = (AbstractMountedCannonContraption) (Object) this;
        if (cannon.disassembled) { ci.cancel(); return; }
        var breech = cannon.presentBlockEntities.get(cannon.getStartPos());
        if (com.cbcatfix.rocket.LauncherTransfer.isLocked(breech)
            || com.cbcatfix.rocket.LauncherTransfer.isLocked(level, entity.blockPosition())) { ci.cancel(); return; }
        if (!(breech instanceof MountedRocketStorage storage)) return;
        int slot = storage.cbcatfix$firstOccupiedSlot();
        if (slot < 0) return;
        int usableLength = RocketMounts.length(cannon);
        cbcatfix$bodyLength = RocketGeometry.bodyLength(RocketMounts.tier(breech));
        if (usableLength < cbcatfix$bodyLength) { ci.cancel(); return; }
        // The mounted body's rear starts at the breech's back face. Its rear must pass the muzzle.
        cbcatfix$clearanceDistance = usableLength + com.cbcatfix.balance.BalanceDefaults.LAUNCH_CLEARANCE_MARGIN;
        cbcatfix$launchStorage = storage;
        cbcatfix$launchSlot = slot;
        cbcatfix$launchItem = storage.cbcatfix$rocketInSlot(slot).copyWithCount(1);
        RocketMounts.Transform mount = RocketMounts.transform(breech, slot);
        Vec3 localCenter = Vec3.atLowerCornerOf(cannon.getStartPos()).add(mount.center());
        cbcatfix$launchCenter = entity.toGlobalVector(localCenter, 1.0f);
        cbcatfix$launchForward = entity.toGlobalVector(localCenter.add(mount.forward()), 1.0f)
            .subtract(cbcatfix$launchCenter).normalize();
        var frame = RocketSableCompat.launchFrame(level, cbcatfix$launchCenter, cbcatfix$launchForward);
        cbcatfix$launchCenter = frame.center();
        cbcatfix$launchForward = frame.forward();
        cbcatfix$inheritedVelocity = frame.inheritedVelocity();
        cbcatfix$carrierVelocity = frame.velocity();
        cbcatfix$carrier = entity;
    }

    @Redirect(method = "fireShot", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean cbcatfix$spawnFromMountedFrame(ServerLevel level, Entity entity) {
        if (cbcatfix$launchCenter != null && entity instanceof AbstractCannonProjectile rocket) {
            double speed = com.cbcatfix.balance.BalanceDefaults.EJECTION_SPEED; // Small ejection impulse; the existing force integrator supplies motor thrust.
            rocket.setPos(cbcatfix$launchCenter);
            ((com.cbcatfix.rocket.RocketPayloadAccess) rocket).cbcatfix$flightState().beginClearance(
                cbcatfix$launchCenter, cbcatfix$launchForward, cbcatfix$carrierVelocity, cbcatfix$clearanceDistance);
            Vec3 ejection = rocket.getDeltaMovement(); // Native shoot() already applied length/material spread.
            rocket.setDeltaMovement(cbcatfix$inheritedVelocity.add(ejection));
            RocketFlightEffects.setForward(rocket, ejection.normalize());
            rocket.xRotO = rocket.getXRot();
            rocket.yRotO = rocket.getYRot();
            rocket.addUntouchableEntity(cbcatfix$carrier,
                Math.clamp((int) Math.ceil(cbcatfix$bodyLength / Math.max(speed, 0.1)) + 1, 2, 40));
        }
        boolean spawned = level.addFreshEntity(entity);
        if (!spawned && cbcatfix$launchStorage != null && !cbcatfix$launchItem.isEmpty()) {
            // A cancelled entity-spawn event must not silently consume the round.
            int free = cbcatfix$launchStorage.cbcatfix$rocketInSlot(cbcatfix$launchSlot).isEmpty() ? cbcatfix$launchSlot : -1;
            for (int slot = 0; free < 0 && slot < cbcatfix$launchStorage.cbcatfix$slotCount(); slot++)
                if (cbcatfix$launchStorage.cbcatfix$rocketInSlot(slot).isEmpty()) free = slot;
            if (free >= 0 && cbcatfix$carrier.isAlive()) cbcatfix$launchStorage.cbcatfix$setRocketInSlot(free, cbcatfix$launchItem);
            else net.minecraft.world.level.block.Block.popResource(level, entity.blockPosition(), cbcatfix$launchItem.copy());
        }
        cbcatfix$launchItem = ItemStack.EMPTY;
        return spawned;
    }

    @org.spongepowered.asm.mixin.injection.Group(name = "rocketShoot", min = 1, max = 1)
    @ModifyArgs(method = "fireShot", at = @At(value = "INVOKE",
        target = "Lcom/dsvv/cbcat/cannon/rocketpod/munitions/AbstractRocket;shoot(DDDFF)V"), require = 0)
    private void cbcatfix$smallSpread(Args args) { cbcatfix$preserveNativeSpread(args); }

    @org.spongepowered.asm.mixin.injection.Group(name = "rocketShoot", min = 1, max = 1)
    @ModifyArgs(method = "fireShot", at = @At(value = "INVOKE",
        target = "Lcom/dsvv/cbcat/cannon/medium_rocketpod/munitions/AbstractMediumRocket;shoot(DDDFF)V"), require = 0)
    private void cbcatfix$mediumSpread(Args args) { cbcatfix$preserveNativeSpread(args); }

    @Unique private void cbcatfix$preserveNativeSpread(Args args) {
        if (cbcatfix$launchCenter == null) return;
        args.set(0, cbcatfix$launchForward.x);
        args.set(1, cbcatfix$launchForward.y);
        args.set(2, cbcatfix$launchForward.z);
        args.set(3, com.cbcatfix.balance.BalanceDefaults.EJECTION_SPEED);
        args.set(4, (float) args.get(4) * com.cbcatfix.config.CbcatFixConfig.value(com.cbcatfix.config.CbcatFixConfig.LAUNCHER_SPREAD_MULTIPLIER).floatValue());
        // Argument 4 is CBCAT's original spread; do not replace its formula or RNG.
    }

    @Redirect(method = "fireShot", at = @At(value = "INVOKE",
        target = "Lrbasamoyai/createbigcannons/cannon_control/ControlPitchContraption;onRecoil(Lnet/minecraft/world/phys/Vec3;Lcom/simibubi/create/content/contraptions/AbstractContraptionEntity;)V"))
    private void cbcatfix$noRocketRecoil(rbasamoyai.createbigcannons.cannon_control.ControlPitchContraption controller,
        Vec3 recoil, com.simibubi.create.content.contraptions.AbstractContraptionEntity carrier) {
        // Rocket propulsion is self-contained. Suppress the launcher impulse at its source.
    }

    @Redirect(method = "fireShot", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/server/level/ServerLevel;sendParticles(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/core/particles/ParticleOptions;ZDDDIDDDD)Z"))
    private boolean cbcatfix$initialExhaustAtRear(ServerLevel level, net.minecraft.server.level.ServerPlayer player,
        net.minecraft.core.particles.ParticleOptions particle, boolean force, double x, double y, double z,
        int count, double dx, double dy, double dz, double speed) {
        if (cbcatfix$launchCenter != null) {
            Vec3 rear = cbcatfix$launchCenter.subtract(cbcatfix$launchForward.scale(cbcatfix$bodyLength * 0.5));
            return level.sendParticles(player, particle, force, rear.x, rear.y, rear.z, count,
                -cbcatfix$launchForward.x * 1.25, -cbcatfix$launchForward.y * 1.25,
                -cbcatfix$launchForward.z * 1.25, speed);
        }
        return level.sendParticles(player, particle, force, x, y, z, count, dx, dy, dz, speed);
    }

    @Inject(method = "insertItemIntoCannon", at = @At("HEAD"), cancellable = true)
    private void cbcatfix$loadAnyFreeSlot(ItemStack stack, boolean simulate, CallbackInfoReturnable<ItemStack> cir) {
        var cannon = (AbstractMountedCannonContraption) (Object) this;
        var breech = cannon.presentBlockEntities.get(cannon.getStartPos());
        if (!(breech instanceof MountedRocketStorage)) return;
        var inventory = new MountedRocketItemHandler(breech);
        ItemStack remaining = stack;
        for (int slot = 0; slot < inventory.getSlots() && !remaining.isEmpty(); slot++) {
            remaining = inventory.insertItem(slot, remaining, simulate);
        }
        cir.setReturnValue(remaining);
    }

    @Inject(method = "extractItemFromCannon", at = @At("HEAD"), cancellable = true)
    private void cbcatfix$unloadNextSlot(boolean simulate, CallbackInfoReturnable<ItemStack> cir) {
        var cannon = (AbstractMountedCannonContraption) (Object) this;
        var breech = cannon.presentBlockEntities.get(cannon.getStartPos());
        if (breech instanceof MountedRocketStorage storage) {
            cir.setReturnValue(new MountedRocketItemHandler(breech).extractItem(storage.cbcatfix$firstOccupiedSlot(), 1, simulate));
        }
    }
}
