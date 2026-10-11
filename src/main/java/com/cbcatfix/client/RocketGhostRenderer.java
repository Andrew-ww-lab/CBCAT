package com.cbcatfix.client;

import com.cbcatfix.rocket.RocketPlacementCandidate;
import com.mojang.blaze3d.vertex.PoseStack;
import net.createmod.catnip.ghostblock.GhostBlockParams;
import net.createmod.catnip.ghostblock.GhostBlockRenderer;
import net.createmod.catnip.ghostblock.GhostBlocks;
import net.createmod.catnip.impl.client.render.ColoringVertexConsumer;
import net.createmod.catnip.placement.PlacementClient;
import net.createmod.catnip.render.SuperRenderTypeBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;

/** Approved model adapter for Catnip's public ghost extension, not a second placement renderer framework. */
final class RocketGhostRenderer extends GhostBlockRenderer {
    private RocketPlacementCandidate candidate;
    private ItemStack held = ItemStack.EMPTY;

    void select(RocketPlacementCandidate candidate, ItemStack held) {
        this.candidate = candidate;
        this.held = held;
    }

    @Override public void render(PoseStack pose, SuperRenderTypeBuffer buffer, Vec3 camera, GhostBlockParams params) {
        var mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.screen != null || mc.options.hideGui
            || !(mc.hitResult instanceof BlockHitResult hit)
            || (held != mc.player.getMainHandItem() && held != mc.player.getOffhandItem())) return;
        // Revalidate immediately: a ghost must not linger on a newly filled or invalid slot.
        var current = RocketPlacementCandidate.resolve(mc.level, mc.player, hit, held);
        if (current == null || !current.equals(candidate)) return;
        float alpha = (float) GhostBlocks.getBreathingAlpha() * PlacementClient.getCurrentAlpha() * .75f;
        var vertices = new ColoringVertexConsumer(buffer.getEarlyBuffer(RenderType.translucent()), 1, 1, 1, alpha);
        pose.pushPose();
        Vec3 origin = Vec3.atLowerCornerOf(current.origin());
        if (ModList.get().isLoaded("sable")) origin = SableTransform.apply(pose, origin);
        pose.translate(origin.x - camera.x, origin.y - camera.y, origin.z - camera.z);
        RocketModelRenderer.render(mc.getItemRenderer(), held, current.tier(), current.facing(), current.center(),
            current.state(), pose, ignored -> vertices, LightTexture.FULL_BRIGHT);
        pose.popPose();
    }

    /** Isolated so Sable stays optional; use its own render-pose bridge. */
    private static final class SableTransform {
        static Vec3 apply(PoseStack pose, Vec3 origin) {
            var sublevel = dev.ryanhcode.sable.Sable.HELPER.getContainingClient(origin);
            dev.ryanhcode.sable.util.SublevelRenderOffsetHelper.posePlotToProjected(sublevel, pose);
            return origin.subtract(dev.ryanhcode.sable.util.SublevelRenderOffsetHelper.translation(origin));
        }
    }
}
