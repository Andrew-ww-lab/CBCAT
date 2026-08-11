package com.cbcatfix.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import rbasamoyai.createbigcannons.munitions.big_cannon.FuzedBlockEntityRenderer;
import rbasamoyai.createbigcannons.munitions.big_cannon.FuzedBlockEntity;
import rbasamoyai.createbigcannons.munitions.big_cannon.SimpleShellBlock;
import rbasamoyai.createbigcannons.index.CBCBlockPartials;
import com.dsvv.cbcat.cartridge.FuzedProjectileCartridgeBlock;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Block;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;

@Mixin(FuzedBlockEntityRenderer.class)
public class FuzedBlockEntityRendererMixin {

    @Inject(method = "renderSafe", at = @At("HEAD"), cancellable = true, remap = false)
    private void cbcatfix$renderFuzeAllShells(FuzedBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay, CallbackInfo ci) {
        if (!be.hasFuze()) return;
        BlockState state = be.getBlockState();
        Block block = state.getBlock();
        Direction facing = state.hasProperty(SimpleShellBlock.FACING) ? state.getValue(SimpleShellBlock.FACING) : Direction.UP;
        
        boolean isBaseFuze = false;
        if (block instanceof SimpleShellBlock shellBlock) {
            isBaseFuze = shellBlock.isBaseFuze();
        } else if (block instanceof FuzedProjectileCartridgeBlock cartridgeBlock) {
            isBaseFuze = cartridgeBlock.isBaseFuze();
        }
        
        if (isBaseFuze) {
            facing = facing.getOpposite();
        }

        SuperByteBuffer buf = CachedBuffers.partialFacing(CBCBlockPartials.FUZE, state, facing);
        buf.light(light).renderInto(ms, buffer.getBuffer(RenderType.cutout()));
        ci.cancel();
    }
}
