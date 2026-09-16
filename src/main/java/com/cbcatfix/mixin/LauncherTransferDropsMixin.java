package com.cbcatfix.mixin;

import com.cbcatfix.rocket.LauncherTransfer;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Containers.class)
public abstract class LauncherTransferDropsMixin {
    @WrapMethod(method = "dropItemStack")
    private static void cbcatfix$recoverFailedTransfer(Level level, double x, double y, double z,
                                                       ItemStack stack, Operation<Void> original) {
        var destination = LauncherTransfer.failedDropDestination(level, x, y, z);
        if (destination == null) original.call(level, x, y, z, stack);
        else original.call(destination.level(), destination.pos().getX() + .5,
            destination.pos().getY() + .5, destination.pos().getZ() + .5, stack);
    }
}
