package com.cbcatfix.mixin;

import com.cbcatfix.rocket.MountedRocketStorage;
import com.cbcatfix.rocket.RocketMounts;
import com.dsvv.cbcat.cannon.rocketpod.breech.RocketPodBreechBlockEntity;
import com.dsvv.cbcat.cannon.rocketpod.munitions.AbstractRocketItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Deque;
import java.util.List;

@Mixin(value = RocketPodBreechBlockEntity.class, remap = false)
public abstract class SmallRocketBreechSlotsMixin implements MountedRocketStorage, net.minecraft.world.Clearable {
    @Shadow @Final private Deque<ItemStack> inputBuffer;
    @Shadow private ItemStack outputBuffer;
    @Shadow private boolean updateInstance;

    @Override public void clearContent() {
        // Clear the actual native buffers, including legacy overflow, not the slot view.
        inputBuffer.clear();
        outputBuffer = ItemStack.EMPTY;
        var breech = (RocketPodBreechBlockEntity) (Object) this;
        breech.cannonBehavior().removeItem();
        cbcatfix$normalizeSlots();
        updateInstance = true;
        breech.setChanged();
    }

    @Inject(method = "getQueueLimit", at = @At("HEAD"), cancellable = true)
    private void cbcatfix$capacity(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(RocketMounts.SMALL_SLOT_COUNT);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void cbcatfix$allocate(CallbackInfo ci) { cbcatfix$normalizeSlots(); }

    @Inject(method = "read", at = @At("RETURN"))
    private void cbcatfix$readSlots(CompoundTag tag, HolderLookup.Provider registries, boolean packet, CallbackInfo ci) {
        cbcatfix$normalizeSlots();
    }

    @Redirect(method = "lambda$write$0", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/item/ItemStack;save(Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/nbt/Tag;"))
    private static Tag cbcatfix$saveEmptySlot(ItemStack stack, HolderLookup.Provider registries) {
        return stack.saveOptional(registries);
    }

    @Unique private void cbcatfix$normalizeSlots() {
        while (inputBuffer.size() < cbcatfix$slotCount()) inputBuffer.addLast(ItemStack.EMPTY);
        // Preserve legacy 13th–15th rounds; promote them into vacancies instead of deleting them.
        List<ItemStack> slots = (List<ItemStack>) inputBuffer;
        for (int i = 0; i < cbcatfix$slotCount() && slots.size() > cbcatfix$slotCount(); i++) {
            if (slots.get(i).isEmpty()) slots.set(i, slots.remove(cbcatfix$slotCount()));
        }
    }

    @Override public int cbcatfix$slotCount() { return RocketMounts.SMALL_SLOT_COUNT; }
    @Override public ItemStack cbcatfix$rocketInSlot(int slot) {
        return slot >= 0 && slot < cbcatfix$slotCount() && slot < inputBuffer.size()
            ? ((List<ItemStack>) inputBuffer).get(slot) : ItemStack.EMPTY;
    }
    @Override public void cbcatfix$setRocketInSlot(int slot, ItemStack stack) {
        if (slot < 0 || slot >= cbcatfix$slotCount()) return;
        cbcatfix$normalizeSlots();
        ((List<ItemStack>) inputBuffer).set(slot, stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1));
        cbcatfix$normalizeSlots();
        updateInstance = true;
        ((RocketPodBreechBlockEntity) (Object) this).notifyUpdate();
    }

    @Inject(method = "isInputFull", at = @At("HEAD"), cancellable = true)
    private void cbcatfix$full(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(inputBuffer.size() >= cbcatfix$slotCount()
            && inputBuffer.stream().limit(cbcatfix$slotCount()).noneMatch(ItemStack::isEmpty));
    }

    @Inject(method = "addToInputBuffer", at = @At("HEAD"), cancellable = true)
    private void cbcatfix$insert(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        var inventory = new com.cbcatfix.rocket.MountedRocketItemHandler((RocketPodBreechBlockEntity) (Object) this);
        cir.setReturnValue(inventory.insertFirstAvailable(stack, false).getCount() < stack.getCount());
    }

    @Inject(method = "extractNextInput", at = @At("HEAD"), cancellable = true)
    private void cbcatfix$extract(CallbackInfoReturnable<ItemStack> cir) {
        if (com.cbcatfix.rocket.LauncherTransfer.isLocked((RocketPodBreechBlockEntity) (Object) this)) {
            cir.setReturnValue(ItemStack.EMPTY);
            return;
        }
        int slot = cbcatfix$firstOccupiedSlot();
        ItemStack rocket = cbcatfix$rocketInSlot(slot);
        if (slot >= 0) cbcatfix$setRocketInSlot(slot, ItemStack.EMPTY);
        cir.setReturnValue(rocket);
    }
}
