package com.cbcatfix.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.core.BlockPos;

import com.dsvv.cbcat.cartridge.ProjectileCartridgeBlock;
import rbasamoyai.createbigcannons.munitions.big_cannon.SimpleShellBlock;

@Mixin(BlockBehaviour.BlockStateBase.class)
public class BlockStateBaseMixin {

    @Inject(method = "getDestroySpeed", at = @At("HEAD"), cancellable = true)
    private void cbcatfix$shellDestroySpeed(BlockGetter level, BlockPos pos, CallbackInfoReturnable<Float> cir) {
        BlockState state = (BlockState) (Object) this;
        Block block = state.getBlock();
        if (block instanceof ProjectileCartridgeBlock || block instanceof SimpleShellBlock) {
            cir.setReturnValue(2.0f);
        }
    }
}
