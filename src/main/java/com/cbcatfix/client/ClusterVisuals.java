package com.cbcatfix.client;

import com.dsvv.cbcat.registry.BlockRegister;
import com.dsvv.cbcat.registry.DataComponentRegistry;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

/** Native item predicates and native ammunition models; no additional payload state. */
@EventBusSubscriber(modid = "cbcatfix", value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class ClusterVisuals {
    private ClusterVisuals() {}

    @SubscribeEvent public static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            for (var block : java.util.List.of(BlockRegister.CLUSTER_BLOCK, BlockRegister.CLUSTER_CARTRIDGE_BLOCK, BlockRegister.CLUSTER_CASELESS_BLOCK))
                ItemProperties.register(block.asItem(), ResourceLocation.parse("cbcatfix:cluster_payload"),
                    (stack, level, entity, seed) -> switch (stack.getOrDefault(DataComponentRegistry.CLUSTER_PROJECTILE, "")) {
                        case "tooltip.cbc_at.ha_he" -> 1;
                        case "tooltip.cbc_at.ha_hef" -> 2;
                        case "tooltip.cbc_at.ha_heat" -> 3;
                        case "tooltip.cbc_at.ha_smoke" -> 4;
                        default -> 0;
                    });
        });
    }

    /** Render the same six-plane body as the inventory/projectile, without a second payload mesh. */
    public static void renderPayload(String payload, net.minecraft.world.level.block.state.BlockState state,
            PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        var stack = state.getBlock().asItem().getDefaultInstance();
        stack.set(DataComponentRegistry.CLUSTER_PROJECTILE, payload);
        var mc = Minecraft.getInstance();
        pose.pushPose();
        pose.translate(0.5, 0.5, 0.5);
        mc.getItemRenderer().renderStatic(stack, ItemDisplayContext.NONE, light, overlay, pose, buffers, mc.level, 0);
        pose.popPose();
    }
}