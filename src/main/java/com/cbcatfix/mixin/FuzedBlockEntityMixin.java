package com.cbcatfix.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import rbasamoyai.createbigcannons.munitions.big_cannon.FuzedBlockEntity;
import rbasamoyai.createbigcannons.munitions.big_cannon.SimpleShellBlock;
import com.dsvv.cbcat.cartridge.FuzedProjectileCartridgeBlock;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.Direction;

@Mixin(FuzedBlockEntity.class)
public class FuzedBlockEntityMixin {

    @Inject(method = "setFuze", at = @At("RETURN"), remap = false)
    private void cbcatfix$onSetFuze(ItemStack fuze, CallbackInfo ci) {
        FuzedBlockEntity be = (FuzedBlockEntity) (Object) this;
        be.setChanged();
        if (be.getLevel() != null && !be.getLevel().isClientSide()) {
            be.getLevel().sendBlockUpdated(be.getBlockPos(), be.getBlockState(), be.getBlockState(), 3);
        }
    }

    @Inject(method = "detonate", at = @At("HEAD"), cancellable = true, remap = false)
    private void cbcatfix$detonateAllShells(CallbackInfo ci) {
        FuzedBlockEntity be = (FuzedBlockEntity) (Object) this;
        if (be.getLevel() == null) return;
        BlockState state = be.getBlockState();
        Block block = state.getBlock();
        if (block instanceof SimpleShellBlock shellBlock) {
            Direction dir = state.hasProperty(SimpleShellBlock.FACING) ? state.getValue(SimpleShellBlock.FACING) : Direction.UP;
            shellBlock.detonateProjectileOnTheSpot(be.getLevel(), be.getBlockPos(), state, dir);
            ci.cancel();
        } else if (block instanceof FuzedProjectileCartridgeBlock cartridgeBlock) {
            Direction dir = state.hasProperty(FuzedProjectileCartridgeBlock.FACING) ? state.getValue(FuzedProjectileCartridgeBlock.FACING) : Direction.UP;
            cartridgeBlock.detonateProjectileOnTheSpot(be.getLevel(), be.getBlockPos(), state, dir);
            ci.cancel();
        }
    }
}
