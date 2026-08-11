package com.cbcatfix.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import com.dsvv.cbcat.cannon.medium_rocketpod.breech.MediumRocketPodBreechBlockEntity;
import com.cbcatfix.IMediumRocketPodBreechBlockEntity;
import com.cbcatfix.CbcatFixHelper;
import com.cbcatfix.munitions.CbcatFixMunitions;
import com.cbcatfix.munitions.BigHERocketItem;
import com.cbcatfix.munitions.BigAPRocketItem;
import com.cbcatfix.munitions.BigHEATRocketItem;
import rbasamoyai.createbigcannons.cannon_control.contraption.AbstractMountedCannonContraption;

@Mixin(value = MediumRocketPodBreechBlockEntity.class, remap = false)
public abstract class MediumRocketPodBreechBlockEntityMixin implements IMediumRocketPodBreechBlockEntity {

    @Shadow(remap = false)
    protected ItemStack[] inputBuffer;

    @Unique
    private boolean cbcatfix$isBigBreech = false;

    @Unique
    private int cbcatfix$railLength = 0;

    @Override
    public boolean cbcatfix$isBigBreech() {
        MediumRocketPodBreechBlockEntity breech = (MediumRocketPodBreechBlockEntity) (Object) this;
        if (breech.isVirtual()) {
            return this.cbcatfix$railLength > 0 || this.cbcatfix$isBigBreech;
        }
        int bigRailLength = CbcatFixHelper.getInWorldSpecificRailLength(breech, CbcatFixMunitions.BIG_ROCKET_RAIL.get());
        if (bigRailLength > 0) {
            return true;
        }
        if (this.cbcatfix$isBigBreech) {
            return true;
        }
        BlockState state = breech.getBlockState();
        if (state != null && state.getBlock() != null) {
            String descId = state.getBlock().getDescriptionId();
            if (descId.endsWith("big_rocket_rail_breech")) {
                this.cbcatfix$isBigBreech = true;
                return true;
            }
        }
        
        return false;
    }

    @Inject(method = "tickFromContraption", at = @At("HEAD"))
    private void onTickFromContraption(Level level, rbasamoyai.createbigcannons.cannon_control.contraption.PitchOrientedContraptionEntity entity, BlockPos pos, CallbackInfo ci) {
        if (entity != null && entity.getContraption() instanceof AbstractMountedCannonContraption mounted) {
            this.cbcatfix$railLength = ((AbstractMountedCannonContraptionAccessor) mounted).getFrontExtensionLength();
        }
    }

    @Override
    public boolean cbcatfix$hasAnyInput() {
        if (this.inputBuffer == null) return false;
        for (ItemStack s : this.inputBuffer) {
            if (s != null && !s.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    @Inject(method = "read", at = @At("TAIL"))
    private void onRead(net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider provider, boolean clientPacket, CallbackInfo ci) {
        this.cbcatfix$isBigBreech = tag.getBoolean("cbcatfix$isBigBreech");
    }

    @Inject(method = "write", at = @At("TAIL"))
    private void onWrite(net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider provider, boolean clientPacket, CallbackInfo ci) {
        tag.putBoolean("cbcatfix$isBigBreech", this.cbcatfix$isBigBreech());
    }

    @Inject(method = "addToInputBuffer", at = @At("HEAD"), cancellable = true)
    private void onAddToInputBuffer(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        boolean isBigBreech = this.cbcatfix$isBigBreech();

        if (stack.getItem() instanceof BigHERocketItem || stack.getItem() instanceof BigAPRocketItem || stack.getItem() instanceof BigHEATRocketItem) {
            if (!isBigBreech) {
                cir.setReturnValue(false);
                return;
            }
        } else if (stack.getItem() instanceof com.dsvv.cbcat.cannon.medium_rocketpod.munitions.AbstractMediumRocketItem) {
            if (isBigBreech) {
                cir.setReturnValue(false);
                return;
            }
        }
    }
}
