package com.cbcatfix.mixin;

import com.dsvv.cbcat.cluster_munition.FuzedClusterProjectile;
import com.dsvv.cbcat.registry.BlockRegister;
import com.dsvv.cbcat.registry.DataComponentRegistry;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import rbasamoyai.createbigcannons.munitions.big_cannon.AbstractBigCannonProjectile;
import rbasamoyai.createbigcannons.munitions.big_cannon.BigCannonProjectileRenderer;

/** Preserve CBC orientation and tracer rendering; select only the cluster's payload-aware model. */
@Mixin(value = BigCannonProjectileRenderer.class, remap = false)
public abstract class ClusterProjectileVisualMixin {
    @WrapOperation(method = "render(Lrbasamoyai/createbigcannons/munitions/big_cannon/AbstractBigCannonProjectile;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/BlockRenderDispatcher;renderSingleBlock(Lnet/minecraft/world/level/block/state/BlockState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V"))
    private void cbcatfix$variant(BlockRenderDispatcher renderer, BlockState state, PoseStack pose,
        MultiBufferSource buffers, int light, int overlay, Operation<Void> original,
        @Local(argsOnly = true) AbstractBigCannonProjectile projectile) {
        if (!(projectile instanceof FuzedClusterProjectile)) { original.call(renderer, state, pose, buffers, light, overlay); return; }
        var stack = BlockRegister.CLUSTER_BLOCK.asStack();
        stack.set(DataComponentRegistry.CLUSTER_PROJECTILE, ((ClusterVisualAccessor) projectile).cbcatfix$payloadType());
        pose.pushPose();
        pose.translate(0.5, 0.5, 0.5);
        pose.mulPose(Direction.NORTH.getRotation());
        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.NONE, light, overlay, pose, buffers, projectile.level(), projectile.getId());
        pose.popPose();
    }
}
