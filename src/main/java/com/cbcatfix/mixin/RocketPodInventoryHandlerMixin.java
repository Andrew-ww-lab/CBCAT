package com.cbcatfix.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.world.item.ItemStack;
import com.dsvv.cbcat.cannon.rocketpod.breech.RocketPodInventoryHandler;
import com.dsvv.cbcat.cannon.rocketpod.munitions.RocketItem;

@Mixin(value = RocketPodInventoryHandler.class, remap = false)
public class RocketPodInventoryHandlerMixin {

    @Inject(method = "isItemValid", at = @At("HEAD"), cancellable = true)
    private void onIsItemValid(int slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(stack.getItem() instanceof RocketItem);
    }

    @Inject(method = "insertItem", at = @At("HEAD"), cancellable = true)
    private void onInsertItem(int slot, ItemStack stack, boolean simulate, CallbackInfoReturnable<ItemStack> cir) {
        if (slot == 1) {
            RocketPodInventoryHandler handler = (RocketPodInventoryHandler) (Object) this;
            if (handler.isItemValid(slot, stack)) {
                var breech = handler.breech();
                if (breech != null && !breech.isInputFull()) {
                    if (simulate) {
                        ItemStack result = stack.copy();
                        result.shrink(1);
                        cir.setReturnValue(result);
                        return;
                    }
                    if (breech.addToInputBuffer(stack)) {
                        breech.setChanged();
                        ItemStack result = stack.copy();
                        result.shrink(1);
                        cir.setReturnValue(result);
                        return;
                    }
                }
            }
        }
        cir.setReturnValue(stack);
    }
}
