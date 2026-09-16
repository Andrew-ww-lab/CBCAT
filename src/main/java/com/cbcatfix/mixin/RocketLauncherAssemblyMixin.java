package com.cbcatfix.mixin;

import com.cbcatfix.rocket.LauncherAssembly;
import com.cbcatfix.rocket.MountedRocketStorage;
import com.cbcatfix.rocket.RocketMounts;
import com.dsvv.cbcat.cannon.rocketpod.RocketPodBlock;
import com.dsvv.cbcat.cannon.medium_rocketpod.MediumRocketPodBlock;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.simibubi.create.content.contraptions.AssemblyException;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rbasamoyai.createbigcannons.cannon_control.ControlPitchContraption;
import rbasamoyai.createbigcannons.cannon_control.contraption.AbstractMountedCannonContraption;
import rbasamoyai.createbigcannons.cannon_control.contraption.PitchOrientedContraptionEntity;
import rbasamoyai.createbigcannons.cannons.autocannon.material.AutocannonMaterial;

@Mixin(targets = {"com.dsvv.cbcat.cannon.rocketpod.contraption.MountedRocketPodContraption",
    "com.dsvv.cbcat.cannon.medium_rocketpod.contraption.MountedMediumRocketRailContraption"}, remap = false)
public abstract class RocketLauncherAssemblyMixin extends AbstractMountedCannonContraption implements LauncherAssembly.Access {
    @Shadow(remap = false) private AutocannonMaterial cannonMaterial;
    @Shadow(remap = false) private boolean collectCannonBlocks(Level level, BlockPos pos) throws AssemblyException { throw new AssertionError(); }
    // Only topology, derived from native blocks after assembly/load. No copied inventories or persistent parallel state.
    @Unique private java.util.List<BlockPos> cbcatfix$lanes = java.util.List.of();
    @Unique private int cbcatfix$nextLane;
    @Unique private double cbcatfix$intervalRemainder;

    @Override public boolean cbcatfix$collectLane(Level level, BlockPos pos) throws AssemblyException {
        return collectCannonBlocks(level, pos);
    }

    @Inject(method = "assemble", at = @At("RETURN"))
    private void cbcatfix$attach(Level level, BlockPos anchor, CallbackInfoReturnable<Boolean> cir) throws AssemblyException {
        if (!cir.getReturnValue()) return;
        LauncherAssembly.attachSides(this, level, anchor);
        cbcatfix$indexLanes();
    }

    @Inject(method = "readNBT", at = @At("RETURN"))
    private void cbcatfix$readLanes(Level level, CompoundTag tag, boolean clientData, CallbackInfo ci) {
        cbcatfix$indexLanes();
        cbcatfix$nextLane = Math.floorMod(tag.getInt("CbcatFixNextLane"), Math.max(1, cbcatfix$lanes.size()));
        double remainder = tag.getDouble("CbcatFixIntervalRemainder");
        cbcatfix$intervalRemainder = Double.isFinite(remainder) ? Math.clamp(remainder, 0, 0.999999) : 0;
    }

    @Inject(method = "readNBT", at = @At("HEAD"))
    private void cbcatfix$discardStaleBlockViews(Level level, CompoundTag tag, boolean clientData, CallbackInfo ci) {
        presentBlockEntities.clear(); // Super rebuilds these from the incoming block snapshot.
    }

    @Inject(method = "writeNBT", at = @At("RETURN"))
    private void cbcatfix$writeSequence(net.minecraft.core.HolderLookup.Provider provider, boolean clientData,
                                       CallbackInfoReturnable<CompoundTag> cir) {
        cir.getReturnValue().putInt("CbcatFixNextLane", cbcatfix$nextLane);
        cir.getReturnValue().putDouble("CbcatFixIntervalRemainder", cbcatfix$intervalRemainder);
    }

    @Unique private void cbcatfix$indexLanes() {
        var lanes = new java.util.ArrayList<BlockPos>(3);
        if (presentBlockEntities.get(startPos) instanceof MountedRocketStorage) lanes.add(startPos);
        var right = LauncherAssembly.right(initialOrientation);
        for (int sign : new int[] {-1, 1}) {
            BlockPos side = startPos.relative(right, sign);
            if (presentBlockEntities.get(side) instanceof MountedRocketStorage) lanes.add(side);
        }
        cbcatfix$lanes = java.util.List.copyOf(lanes);
        for (BlockPos pos : lanes)
            if (presentBlockEntities.get(pos) instanceof RocketMounts.MountLength cached)
                cached.cbcatfix$setMountLength(RocketMounts.length(this, pos));
        // Existing CBC bounds have a one-block margin; add one more for full-width side lanes when rotated.
        bounds = createBoundsFromExtensionLengths();
    }

    @Unique private void cbcatfix$forEachLane(java.util.function.Consumer<BlockPos> action) {
        BlockPos main = startPos;
        AutocannonMaterial material = cannonMaterial;
        try {
            for (BlockPos lane : cbcatfix$lanes.isEmpty() ? java.util.List.of(main) : cbcatfix$lanes) {
                startPos = lane;
                var info = blocks.get(lane);
                if (info != null) {
                    if (info.state().getBlock() instanceof RocketPodBlock small) cannonMaterial = small.getAutocannonMaterial();
                    else if (info.state().getBlock() instanceof MediumRocketPodBlock medium) cannonMaterial = medium.getAutocannonMaterial();
                }
                action.accept(lane);
            }
        } finally {
            startPos = main;
            cannonMaterial = material;
        }
    }

    @WrapMethod(method = "fireShot")
    private void cbcatfix$fireLanes(ServerLevel level, PitchOrientedContraptionEntity entity, Operation<Void> original) {
        if (cbcatfix$lanes.isEmpty()) cbcatfix$indexLanes();
        if (cbcatfix$lanes.isEmpty()) { original.call(level, entity); return; }
        BlockPos main = startPos;
        AutocannonMaterial material = cannonMaterial;
        try {
            for (int offset = 0; offset < cbcatfix$lanes.size(); offset++) {
                int index = (cbcatfix$nextLane + offset) % cbcatfix$lanes.size();
                BlockPos lane = cbcatfix$lanes.get(index);
                var be = presentBlockEntities.get(lane);
                var info = blocks.get(lane);
                if (info == null) continue;
                if (!(be instanceof MountedRocketStorage storage)
                    || !(be instanceof com.cbcatfix.rocket.LauncherCooldown cooldown)
                    || storage.cbcatfix$firstOccupiedSlot() < 0 || cooldown.cbcatfix$getCooldown() > 0) continue;
                startPos = lane;
                if (info.state().getBlock() instanceof RocketPodBlock small) cannonMaterial = small.getAutocannonMaterial();
                else if (info.state().getBlock() instanceof MediumRocketPodBlock medium) cannonMaterial = medium.getAutocannonMaterial();
                original.call(level, entity);
                // Native barrel failure can disassemble/destroy the carrier. Never continue
                // firing another lane on that stale assembled owner.
                if (!entity.isAlive() || entity.getContraption() != this) break;
                if (cooldown.cbcatfix$getCooldown() <= 0) continue;
                double interval = cooldown.cbcatfix$getCooldown() / com.cbcatfix.rocket.RocketBalance.launcherRate(RocketMounts.tier(be), cbcatfix$lanes.size())
                    + cbcatfix$intervalRemainder;
                int ticks = Math.max(1, (int) Math.floor(interval));
                cbcatfix$intervalRemainder = interval - Math.floor(interval);
                for (BlockPos target : cbcatfix$lanes) {
                    if (presentBlockEntities.get(target) instanceof com.cbcatfix.rocket.LauncherCooldown other)
                        other.cbcatfix$setCooldown(ticks);
                }
                cbcatfix$nextLane = (index + 1) % cbcatfix$lanes.size();
                break;
            }
        } finally { startPos = main; cannonMaterial = material; }
    }

    @WrapMethod(method = "onRedstoneUpdate")
    private void cbcatfix$powerLanes(ServerLevel level, PitchOrientedContraptionEntity entity, boolean toggle,
        int power, ControlPitchContraption controller, Operation<Void> original) {
        cbcatfix$forEachLane(pos -> original.call(level, entity, toggle, power, controller));
    }

    @Inject(method = "getReferencedFireRate", at = @At("RETURN"), cancellable = true)
    private void cbcatfix$displayGroupRate(CallbackInfoReturnable<Integer> cir) {
        var breech = presentBlockEntities.get(startPos);
        if (!(breech instanceof MountedRocketStorage)) return;
        cir.setReturnValue(Math.min(1200, (int) Math.round(cir.getReturnValue()
            * com.cbcatfix.rocket.RocketBalance.launcherRate(RocketMounts.tier(breech), cbcatfix$lanes.size()))));
    }

    @Inject(method = "addBlocksToWorld", at = @At("HEAD"))
    private void cbcatfix$flushLiveSlots(Level level, com.simibubi.create.content.contraptions.StructureTransform transform,
                                       CallbackInfo ci) {
        // A shot/component edit may precede disassembly in the SAME tick, before
        // native tickFromContraption flushes updateInstance into StructureBlockInfo.
        for (var entry : presentBlockEntities.entrySet()) {
            var info = blocks.get(entry.getKey());
            if (info == null) continue;
            var tag = entry.getValue().saveWithFullMetadata(level.registryAccess());
            tag.remove("x"); tag.remove("y"); tag.remove("z");
            blocks.put(entry.getKey(), new net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo(
                info.pos(), info.state(), tag));
        }
    }

    @WrapMethod(method = "trySettingFireRateCarriage")
    private void cbcatfix$adjustLanes(int adjustment, Operation<Void> original) {
        cbcatfix$forEachLane(pos -> original.call(adjustment));
    }

    @Override public net.minecraft.world.phys.AABB createBoundsFromExtensionLengths() {
        var nativeBounds = super.createBoundsFromExtensionLengths();
        return cbcatfix$lanes != null && cbcatfix$lanes.size() > 1 ? nativeBounds.inflate(1) : nativeBounds;
    }

    @Inject(method = "getWeightForStress", at = @At("HEAD"), cancellable = true)
    private void cbcatfix$sideMaterials(CallbackInfoReturnable<Float> cir) {
        if (cbcatfix$lanes.size() <= 1) return;
        float weight = 0;
        for (var info : blocks.values()) {
            if (info.state().getBlock() instanceof RocketPodBlock small) weight += small.getAutocannonMaterial().properties().weight() * 2;
            else if (info.state().getBlock() instanceof MediumRocketPodBlock medium) weight += medium.getAutocannonMaterial().properties().weight() * 2;
        }
        cir.setReturnValue(weight);
    }
}
