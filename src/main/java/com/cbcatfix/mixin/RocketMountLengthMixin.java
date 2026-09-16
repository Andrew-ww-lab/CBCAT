package com.cbcatfix.mixin;

import com.cbcatfix.rocket.RocketMounts;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rbasamoyai.createbigcannons.cannon_control.contraption.AbstractMountedCannonContraption;
import rbasamoyai.createbigcannons.cannon_control.contraption.PitchOrientedContraptionEntity;

@Mixin(targets = {"com.dsvv.cbcat.cannon.rocketpod.breech.RocketPodBreechBlockEntity",
    "com.dsvv.cbcat.cannon.medium_rocketpod.breech.MediumRocketPodBreechBlockEntity"}, remap = false)
public class RocketMountLengthMixin implements RocketMounts.MountLength, com.cbcatfix.rocket.LauncherCooldown {
    @org.spongepowered.asm.mixin.Shadow(remap = false) private int firingCooldown;
    @Override public int cbcatfix$getCooldown() { return firingCooldown; }
    @Override public void cbcatfix$setCooldown(int ticks) { firingCooldown = Math.max(0, ticks); }
    @Unique private int cbcatfix$length;
    @Unique private net.neoforged.neoforge.items.IItemHandler cbcatfix$inventory;
    @Inject(method = "createItemHandler", at = @At("HEAD"), cancellable = true)
    private void cbcatfix$itemHandler(org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<net.neoforged.neoforge.items.IItemHandler> cir) {
        if (cbcatfix$inventory == null) cbcatfix$inventory = new com.cbcatfix.rocket.MountedRocketItemHandler((BlockEntity) (Object) this);
        cir.setReturnValue(cbcatfix$inventory);
    }
    @Override public int cbcatfix$mountLength() { return cbcatfix$length; }
    @Override public void cbcatfix$setMountLength(int length) { cbcatfix$length = length; }
    @Inject(method = "tick", at = @At("HEAD"))
    private void cbcatfix$worldTick(CallbackInfo ci) { cbcatfix$length = 0; }
    @Inject(method = "tickFromContraption", at = @At("HEAD"))
    private void cbcatfix$carrierTick(Level level, PitchOrientedContraptionEntity entity, BlockPos pos, CallbackInfo ci) {
        if (cbcatfix$length == 0 && entity.getContraption() instanceof AbstractMountedCannonContraption cannon)
            cbcatfix$length = RocketMounts.length(cannon, pos);
    }
    @Inject(method = "write", at = @At("TAIL"))
    private void cbcatfix$writeLength(CompoundTag tag, HolderLookup.Provider registries, boolean packet, CallbackInfo ci) {
        tag.putInt("CbcatFixMountLength", RocketMounts.length((BlockEntity) (Object) this));
    }
    @Inject(method = "read", at = @At("RETURN"))
    private void cbcatfix$readLength(CompoundTag tag, HolderLookup.Provider registries, boolean packet, CallbackInfo ci) {
        cbcatfix$length = tag.getInt("CbcatFixMountLength");
    }
}
