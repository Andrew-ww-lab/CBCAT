package com.cbcatfix.rocket;

import net.minecraft.world.item.ItemStack;

/** Slot view over CBCAT's existing item storage, not a second rocket representation. */
public interface MountedRocketStorage {
    int cbcatfix$slotCount();
    ItemStack cbcatfix$rocketInSlot(int slot);
    void cbcatfix$setRocketInSlot(int slot, ItemStack stack);

    default int cbcatfix$firstOccupiedSlot() {
        for (int slot = 0; slot < cbcatfix$slotCount(); slot++) {
            if (!cbcatfix$rocketInSlot(slot).isEmpty()) return slot;
        }
        return -1;
    }
}
