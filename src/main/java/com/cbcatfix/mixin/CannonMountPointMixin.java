package com.cbcatfix.mixin;

import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rbasamoyai.createbigcannons.cannon_control.contraption.AbstractMountedCannonContraption;
import rbasamoyai.createbigcannons.cannon_control.contraption.PitchOrientedContraptionEntity;
import rbasamoyai.createbigcannons.cannons.big_cannons.breeches.quickfiring_breech.CannonMountPoint;

/** Reject CBCAT's controller-loading shortcut before its own injector handles rockets. */
@Mixin(value = CannonMountPoint.class, priority = 1100, remap = false)
public class CannonMountPointMixin {
    @Inject(method = "getInsertedResultAndDoSomething", at = @At("HEAD"), cancellable = true)
    private void cbcatfix$rejectAssembledLauncher(ItemStack stack, boolean simulate,
        AbstractMountedCannonContraption cannon, PitchOrientedContraptionEntity entity,
        CallbackInfoReturnable<ItemStack> cir) {
        if (cannon.presentBlockEntities.get(cannon.getStartPos()) instanceof com.cbcatfix.rocket.MountedRocketStorage)
            cir.setReturnValue(stack);
    }
}
