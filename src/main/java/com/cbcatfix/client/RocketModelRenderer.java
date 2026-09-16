package com.cbcatfix.client;

import com.cbcatfix.rocket.RocketBalance;
import com.cbcatfix.rocket.RocketGeometry;
import com.mojang.blaze3d.vertex.PoseStack;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import rbasamoyai.createbigcannons.index.CBCBlockPartials;
import rbasamoyai.createbigcannons.index.CBCDataComponents;

/** The same full item model and CBC fuze partial for placed and mounted rockets. */
public final class RocketModelRenderer {
    private RocketModelRenderer() {}

    public static void render(ItemRenderer renderer, ItemStack rocket, RocketBalance.Tier tier,
                              Direction facing, Vec3 center, BlockState state, PoseStack pose,
                              MultiBufferSource buffers, int light) {
        Vec3 forward = Vec3.atLowerCornerOf(facing.getNormal());
        pose.pushPose();
        pose.translate(center.x, center.y, center.z);
        pose.mulPose(new Quaternionf().rotationTo(0, 1, 0, (float) forward.x, (float) forward.y, (float) forward.z));
        pose.scale(1, RocketGeometry.modelLengthScale(tier), 1);
        pose.translate(0, RocketGeometry.modelPivotOffset(tier), 0);
        renderer.renderStatic(rocket, ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY, pose, buffers, null, 0);
        pose.popPose();
        if (!rocket.getOrDefault(CBCDataComponents.FUZE, ItemContainerContents.EMPTY).copyOne().isEmpty()) {
            Vec3 offset = center.subtract(0.5, 0.5, 0.5)
                .add(forward.scale((RocketGeometry.bodyLength(tier) - 1) * 0.5));
            pose.pushPose();
            pose.translate(offset.x, offset.y, offset.z);
            CachedBuffers.partialFacing(CBCBlockPartials.FUZE, state, facing)
                .light(light).renderInto(pose, buffers.getBuffer(RenderType.cutout()));
            pose.popPose();
        }
    }
}
