package com.cbcatfix.client;

import com.cbcatfix.rocket.RocketBlock;
import com.cbcatfix.rocket.RocketBlockEntity;
import com.cbcatfix.rocket.RocketGeometry;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;

/** CBC placed state; shared visual implementation also serves launcher-mounted rockets. */
public class RocketBlockEntityRenderer extends SafeBlockEntityRenderer<RocketBlockEntity> {
    private final ItemRenderer itemRenderer;
    public RocketBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        itemRenderer = context.getItemRenderer();
    }
    @Override protected void renderSafe(RocketBlockEntity blockEntity, float partialTicks, PoseStack pose,
                                        MultiBufferSource buffers, int light, int overlay) {
        var state = blockEntity.getBlockState();
        var tier = RocketBlock.tier(state);
        var facing = RocketGeometry.horizontalFacing(state.getValue(RocketBlock.FACING));
        var rockets = blockEntity.getRockets();
        for (int slot = 0; slot < rockets.size(); slot++) {
            var center = RocketGeometry.bodyCenter(tier, facing, slot, blockEntity.isIndependent());
            RocketModelRenderer.render(itemRenderer, rockets.get(slot), tier, facing, center, state, pose, buffers, light);
        }
    }

    @Override public net.minecraft.world.phys.AABB getRenderBoundingBox(RocketBlockEntity blockEntity) {
        return blockEntity.getRenderBoundingBox();
    }

    @Override public boolean shouldRenderOffScreen(RocketBlockEntity blockEntity) {
        // Multi-section objects need the global candidate list, but NeoForge still
        // frustum-culls their precise bounding box and retains normal view distance.
        return RocketGeometry.bodyLength(RocketBlock.tier(blockEntity.getBlockState())) > 1;
    }
}
