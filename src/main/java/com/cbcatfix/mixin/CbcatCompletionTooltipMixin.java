package com.cbcatfix.mixin;

import com.dsvv.cbcat.base.Tooltips;
import com.dsvv.cbcat.cannon.rocketpod.RocketPodBlock;
import com.dsvv.cbcat.cannon.medium_rocketpod.MediumRocketPodBlock;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rbasamoyai.createbigcannons.cannons.CannonContraptionProviderBlock;
import java.util.List;

@Mixin(value = Tooltips.class, remap = false)
public abstract class CbcatCompletionTooltipMixin {
    @Inject(method = {"appendTextTwinAutocannon", "appendTextHeavyAutocannon", "appendTextRocketPod", "appendTextMediumRocketPod"},
        at = @At("HEAD"), cancellable = true)
    private static void cbcatfix$completeOnly(ItemStack stack, List<Component> lines, TooltipFlag flag, Block block, CallbackInfo ci) {
        // Launchers receive their current, authoritative property block from LauncherTooltips.
        if (block instanceof RocketPodBlock || block instanceof MediumRocketPodBlock
            || block instanceof CannonContraptionProviderBlock cannon && !cannon.isComplete(block.defaultBlockState())) ci.cancel();
    }
}
