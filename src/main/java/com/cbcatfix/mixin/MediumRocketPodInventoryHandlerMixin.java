package com.cbcatfix.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.world.item.ItemStack;
import com.dsvv.cbcat.cannon.medium_rocketpod.breech.MediumRocketPodInventoryHandler;
import com.dsvv.cbcat.cannon.medium_rocketpod.breech.MediumRocketPodBreechBlockEntity;
import com.dsvv.cbcat.cannon.medium_rocketpod.munitions.AbstractMediumRocketItem;
import com.cbcatfix.munitions.BigHERocketItem;
import com.cbcatfix.munitions.BigAPRocketItem;
import com.cbcatfix.munitions.BigHEATRocketItem;
import com.cbcatfix.munitions.CbcatFixMunitions;
import com.cbcatfix.CbcatFixHelper;
import com.cbcatfix.IMediumRocketPodBreechBlockEntity;

@Mixin(value = MediumRocketPodInventoryHandler.class, remap = false)
public class MediumRocketPodInventoryHandlerMixin {

    @Inject(method = "isItemValid", at = @At("HEAD"), cancellable = true)
    private void onIsItemValid(int slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack.getItem() instanceof AbstractMediumRocketItem) {
            MediumRocketPodBreechBlockEntity breechBE = ((MediumRocketPodInventoryHandler) (Object) this).breech();
            if (breechBE != null) {
                boolean isBigBreech = ((IMediumRocketPodBreechBlockEntity) breechBE).cbcatfix$isBigBreech();

                if (stack.getItem() instanceof BigHERocketItem || stack.getItem() instanceof BigAPRocketItem || stack.getItem() instanceof BigHEATRocketItem) {
                    if (!isBigBreech) {
                        cir.setReturnValue(false);
                        return;
                    }
                } else {
                    if (isBigBreech) {
                        cir.setReturnValue(false);
                        return;
                    }
                }
            }
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "insertItem", at = @At("HEAD"), cancellable = true)
    private void onInsertItem(int slot, ItemStack stack, boolean simulate, CallbackInfoReturnable<ItemStack> cir) {
        MediumRocketPodInventoryHandler handler = (MediumRocketPodInventoryHandler) (Object) this;
        MediumRocketPodBreechBlockEntity breech = handler.breech();
        if (breech == null) {
            cir.setReturnValue(stack);
            return;
        }

        if (!handler.isItemValid(slot, stack)) {
            cir.setReturnValue(stack);
            return;
        }

        if (breech.isInputFull()) {
            cir.setReturnValue(stack);
            return;
        }

        if (!simulate) {
            breech.addToInputBuffer(stack);
            breech.notifyUpdate();
        }

        ItemStack result = stack.copy();
        result.shrink(1);
        cir.setReturnValue(result);
    }
}
