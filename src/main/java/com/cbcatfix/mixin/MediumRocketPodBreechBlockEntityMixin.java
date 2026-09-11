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
public abstract class MediumRocketPodBreechBlockEntityMixin implements IMediumRocketPodBreechBlockEntity, com.cbcatfix.rocket.MountedRocketStorage, net.minecraft.world.Clearable {

    @Shadow(remap = false)
    protected ItemStack[] inputBuffer;
    @Shadow(remap = false) private ItemStack outputBuffer;
    @Shadow(remap = false) private boolean updateInstance;

    @Unique
    private boolean cbcatfix$isBigBreech = false;

    @Override public void clearContent() {
        // All four native cells must be cleared, including legacy large-rocket overflow.
        java.util.Arrays.fill(inputBuffer, ItemStack.EMPTY);
        outputBuffer = ItemStack.EMPTY;
        var breech = (MediumRocketPodBreechBlockEntity) (Object) this;
        breech.cannonBehavior().removeItem();
        updateInstance = true;
        breech.setChanged();
    }

    @Override
    public boolean cbcatfix$isBigBreech() {
        MediumRocketPodBreechBlockEntity breech = (MediumRocketPodBreechBlockEntity) (Object) this;
        if (breech.isVirtual()) {
            return this.cbcatfix$isBigBreech || breech.getBlockState().is(CbcatFixMunitions.BIG_ROCKET_RAIL_BREECH.get());
        }
        int bigRailLength = CbcatFixHelper.getInWorldSpecificRailLength(breech, CbcatFixMunitions.BIG_ROCKET_RAIL.get());
        if (bigRailLength > 0) {
            return true;
        }
        if (this.cbcatfix$isBigBreech) {
            return true;
        }
        BlockState state = breech.getBlockState();
        if (state != null && state.is(CbcatFixMunitions.BIG_ROCKET_RAIL_BREECH.get())) {
            this.cbcatfix$isBigBreech = true;
            return true;
        }
        
        return false;
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

    @Override public int cbcatfix$slotCount() {
        return cbcatfix$isBigBreech() ? 1 : ((MediumRocketPodBreechBlockEntity) (Object) this).getQueueLimit();
    }

    @Override public ItemStack cbcatfix$rocketInSlot(int slot) {
        return slot >= 0 && slot < inputBuffer.length ? inputBuffer[slot] : ItemStack.EMPTY;
    }

    @Override public void cbcatfix$setRocketInSlot(int slot, ItemStack stack) {
        if (slot < 0 || slot >= cbcatfix$slotCount()) return;
        inputBuffer[slot] = stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1);
        cbcatfix$promoteLegacyBigRound();
        updateInstance = true;
        ((MediumRocketPodBreechBlockEntity) (Object) this).notifyUpdate();
    }

    @Inject(method = "read", at = @At("RETURN"))
    private void onRead(net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider provider, boolean clientPacket, CallbackInfo ci) {
        this.cbcatfix$isBigBreech = tag.getBoolean("cbcatfix$isBigBreech");
        cbcatfix$promoteLegacyBigRound();
    }

    @Unique private void cbcatfix$promoteLegacyBigRound() {
        if (!cbcatfix$isBigBreech() || inputBuffer.length == 0 || !inputBuffer[0].isEmpty()) return;
        for (int slot = 1; slot < inputBuffer.length; slot++) {
            if (!inputBuffer[slot].isEmpty()) {
                inputBuffer[0] = inputBuffer[slot];
                inputBuffer[slot] = ItemStack.EMPTY;
                break;
            }
        }
    }

    @Inject(method = "extractNextInput", at = @At("RETURN"))
    private void cbcatfix$syncExtraction(CallbackInfoReturnable<ItemStack> cir) {
        if (!cir.getReturnValue().isEmpty()) {
            cbcatfix$promoteLegacyBigRound();
            updateInstance = true;
            ((MediumRocketPodBreechBlockEntity) (Object) this).notifyUpdate();
        }
    }

    @Inject(method = "extractNextInput", at = @At("HEAD"), cancellable = true)
    private void cbcatfix$rejectTransferExtraction(CallbackInfoReturnable<ItemStack> cir) {
        if (com.cbcatfix.rocket.LauncherTransfer.isLocked((MediumRocketPodBreechBlockEntity) (Object) this))
            cir.setReturnValue(ItemStack.EMPTY);
    }

    @Inject(method = "write", at = @At("TAIL"))
    private void onWrite(net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider provider, boolean clientPacket, CallbackInfo ci) {
        tag.putBoolean("cbcatfix$isBigBreech", this.cbcatfix$isBigBreech());
    }

    @Inject(method = "addToInputBuffer", at = @At("HEAD"), cancellable = true)
    private void onAddToInputBuffer(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        var inventory = new com.cbcatfix.rocket.MountedRocketItemHandler((MediumRocketPodBreechBlockEntity) (Object) this);
        cir.setReturnValue(inventory.insertFirstAvailable(stack, false).getCount() < stack.getCount());
    }

    @Inject(method = "isInputFull", at = @At("HEAD"), cancellable = true)
    private void onIsInputFull(CallbackInfoReturnable<Boolean> cir) {
        if (this.cbcatfix$isBigBreech()) {
            cir.setReturnValue(this.cbcatfix$hasAnyInput());
        }
    }
}
