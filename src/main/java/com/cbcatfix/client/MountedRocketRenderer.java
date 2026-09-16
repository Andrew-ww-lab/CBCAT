package com.cbcatfix.client;

import com.cbcatfix.munitions.CbcatFixMunitions;
import com.cbcatfix.rocket.MountedRocketStorage;
import com.cbcatfix.rocket.RocketBlock;
import com.cbcatfix.rocket.RocketMounts;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class MountedRocketRenderer<T extends BlockEntity> implements BlockEntityRenderer<T> {
    public MountedRocketRenderer(BlockEntityRendererProvider.Context context) {}
    @Override public void render(T breech, float partialTicks, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        renderContents(breech, pose, buffers, light);
    }
    @Override public boolean shouldRenderOffScreen(T breech) {
        return com.cbcatfix.rocket.RocketGeometry.bodyLength(RocketMounts.tier(breech)) > 1;
    }
    @Override public net.minecraft.world.phys.AABB getRenderBoundingBox(T breech) {
        return contentsBounds(breech);
    }

    public static net.minecraft.world.phys.AABB contentsBounds(BlockEntity breech) {
        var bounds = new net.minecraft.world.phys.AABB(breech.getBlockPos());
        if (breech instanceof MountedRocketStorage storage) {
            for (int slot = 0; slot < storage.cbcatfix$slotCount(); slot++) {
                if (!storage.cbcatfix$rocketInSlot(slot).isEmpty())
                    bounds = bounds.minmax(RocketMounts.transform(breech, slot).bounds().move(breech.getBlockPos()));
            }
        }
        return bounds.inflate(1.0 / 16.0);
    }

    public static void renderContents(BlockEntity breech, PoseStack pose, MultiBufferSource buffers, int light) {
        if (!(breech instanceof MountedRocketStorage storage)) return;
        var renderer = Minecraft.getInstance().getItemRenderer();
        for (int slot = 0; slot < storage.cbcatfix$slotCount(); slot++) {
            var rocket = storage.cbcatfix$rocketInSlot(slot);
            if (rocket.isEmpty()) continue;
            var mount = RocketMounts.transform(breech, slot);
            var state = CbcatFixMunitions.ROCKET_BLOCK.get().defaultBlockState()
                .setValue(RocketBlock.TIER, mount.tier().ordinal()).setValue(RocketBlock.FACING, mount.facing());
            RocketModelRenderer.render(renderer, rocket, mount.tier(), mount.facing(), mount.center(), state, pose, buffers, light);
        }
    }
}
