package com.cbcatfix.munitions;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import com.dsvv.cbcat.cannon.medium_rocketpod.munitions.MediumRocketRenderer;
import com.dsvv.cbcat.cannon.medium_rocketpod.munitions.AbstractMediumRocket;

public class BigRocketRenderer extends MediumRocketRenderer<AbstractMediumRocket<?>> {
    public BigRocketRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(AbstractMediumRocket<?> entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
}
