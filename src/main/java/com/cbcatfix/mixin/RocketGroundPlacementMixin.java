package com.cbcatfix.mixin;

import com.cbcatfix.rocket.RocketGroundPlacement;
import com.dsvv.cbcat.cannon.medium_rocketpod.munitions.AbstractMediumRocketItem;
import com.dsvv.cbcat.cannon.rocketpod.munitions.AbstractRocketItem;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public class RocketGroundPlacementMixin {
    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void cbcatfix$placeRocketOnGround(
        UseOnContext context,
        CallbackInfoReturnable<InteractionResult> cir
    ) {
        Item item = (Item) (Object) this;
        if (item instanceof AbstractRocketItem || item instanceof AbstractMediumRocketItem) {
            InteractionResult result = RocketGroundPlacement.place(context);
            if (result != InteractionResult.PASS) {
                cir.setReturnValue(result);
            }
        }
    }
}
