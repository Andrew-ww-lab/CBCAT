package com.cbcatfix.mixin;

import com.dsvv.cbcat.cannon.rocketpod.munitions.AbstractRocketItem;
import com.dsvv.cbcat.cannon.medium_rocketpod.munitions.AbstractMediumRocketItem;
import com.cbcatfix.rocket.RocketBalance;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class RocketItemStackSizeMixin {
    @Inject(method = "getMaxStackSize", at = @At("HEAD"), cancellable = true)
    private void cbcatfix$rocketInventoryStackSize(CallbackInfoReturnable<Integer> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        if (stack.getItem() instanceof AbstractRocketItem) {
            cir.setReturnValue(64);
        } else if (stack.getItem() instanceof AbstractMediumRocketItem) {
            cir.setReturnValue(RocketBalance.tierForRocketItem(stack.getItem()) == RocketBalance.Tier.BIG ? 16 : 32);
        }
    }
}
