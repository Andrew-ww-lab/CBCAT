package com.cbcatfix.mixin;

import com.cbcatfix.client.MountedRocketRenderer;
import com.dsvv.cbcat.cannon.medium_rocketpod.breech.MediumRocketPodBreechBlockEntity;
import com.dsvv.cbcat.cannon.medium_rocketpod.breech.MediumRocketPodBreechRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Keep CBCAT's registered renderer; both sizes delegate to the same authoritative slot renderer. */
@Mixin(value = MediumRocketPodBreechRenderer.class, remap = false)
public abstract class MediumRocketPodBreechRendererMixin
    extends SmartBlockEntityRenderer<MediumRocketPodBreechBlockEntity> {
    protected MediumRocketPodBreechRendererMixin(BlockEntityRendererProvider.Context context) { super(context); }

    @Inject(method = "renderSafe(Lcom/dsvv/cbcat/cannon/medium_rocketpod/breech/MediumRocketPodBreechBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
        at = @At("HEAD"), cancellable = true)
    private void cbcatfix$renderContents(MediumRocketPodBreechBlockEntity breech, float partialTicks,
        PoseStack pose, MultiBufferSource buffers, int light, int overlay, CallbackInfo ci) {
        MountedRocketRenderer.renderContents(breech, pose, buffers, light);
        ci.cancel();
    }

    @Override public AABB getRenderBoundingBox(MediumRocketPodBreechBlockEntity breech) {
        return MountedRocketRenderer.contentsBounds(breech);
    }

    @Override public boolean shouldRenderOffScreen(MediumRocketPodBreechBlockEntity breech) { return true; }
}
