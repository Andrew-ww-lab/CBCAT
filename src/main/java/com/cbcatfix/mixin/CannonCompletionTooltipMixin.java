package com.cbcatfix.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rbasamoyai.createbigcannons.base.CBCTooltip;
import rbasamoyai.createbigcannons.cannons.CannonContraptionProviderBlock;
import java.util.List;

/** Keep native crafting hints, but never advertise weapon properties on an unfinished casting. */
@Mixin(value = CBCTooltip.class, remap = false)
public abstract class CannonCompletionTooltipMixin {
    @Inject(method = {"appendCannonBlockText", "appendTextAutocannon"}, at = @At("HEAD"), cancellable = true)
    private static void cbcatfix$completeOnly(ItemStack stack, Item.TooltipContext context, List<Component> lines,
                                             TooltipFlag flag, Block block, CallbackInfo ci) {
        if (block instanceof CannonContraptionProviderBlock cannon && !cannon.isComplete(block.defaultBlockState())) ci.cancel();
    }
}
