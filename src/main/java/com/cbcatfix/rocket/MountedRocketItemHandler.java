package com.cbcatfix.rocket;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.IItemHandler;

/** Automation accesses the same individual stacks that players see and launch. */
public record MountedRocketItemHandler(BlockEntity breech) implements IItemHandler {
    private MountedRocketStorage storage() { return (MountedRocketStorage) breech; }
    @Override public int getSlots() { return storage().cbcatfix$slotCount(); }
    @Override public ItemStack getStackInSlot(int slot) { return storage().cbcatfix$rocketInSlot(slot); }
    @Override public int getSlotLimit(int slot) { return 1; }
    @Override public boolean isItemValid(int slot, ItemStack stack) {
        return slot >= 0 && slot < getSlots() && RocketGroundPlacement.isRocket(stack)
            && RocketBalance.tierForRocketItem(stack.getItem()) == RocketMounts.tier(breech)
            && RocketMounts.length(breech) >= RocketGeometry.bodyLength(RocketMounts.tier(breech));
    }
    @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (!LauncherAccess.canTransfer(breech) || !isItemValid(slot, stack) || !getStackInSlot(slot).isEmpty()) return stack;
        if (!simulate && breech.getLevel().isClientSide()) return stack;
        if (!simulate) storage().cbcatfix$setRocketInSlot(slot, stack);
        return stack.copyWithCount(stack.getCount() - 1);
    }
    public ItemStack insertFirstAvailable(ItemStack stack, boolean simulate) {
        for (int slot = 0; slot < getSlots(); slot++) {
            ItemStack remainder = insertItem(slot, stack, simulate);
            if (remainder.getCount() < stack.getCount()) return remainder;
        }
        return stack;
    }
    @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (!LauncherAccess.canTransfer(breech) || amount <= 0 || slot < 0 || slot >= getSlots()) return ItemStack.EMPTY;
        if (!simulate && breech.getLevel().isClientSide()) return ItemStack.EMPTY;
        ItemStack result = getStackInSlot(slot).copy();
        if (!simulate && !result.isEmpty()) storage().cbcatfix$setRocketInSlot(slot, ItemStack.EMPTY);
        return result;
    }
}
