package com.cbcatfix.client;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import com.cbcatfix.CbcatFix;
import com.cbcatfix.munitions.CbcatFixMunitions;
import rbasamoyai.createbigcannons.munitions.big_cannon.FuzedBlockEntityRenderer;
import rbasamoyai.createbigcannons.munitions.big_cannon.FuzedBlockVisual;
import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.resources.ResourceLocation;

public class ClientSetup {

    public static final PartialModel BIG_ROCKET_MODEL = 
        PartialModel.of(ResourceLocation.fromNamespaceAndPath("cbcatfix", "block/big_rocket"));

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(CbcatFixMunitions.FUZED_BLOCK_ENTITY.get(), FuzedBlockEntityRenderer::new);
        try {
            event.registerBlockEntityRenderer(com.dsvv.cbcat.registry.BlockEntityRegister.FUZED_PROJECTILE_CARTRIDGE_BLOCK_ENTITY.get(), FuzedBlockEntityRenderer::new);
            event.registerBlockEntityRenderer(com.dsvv.cbcat.registry.BlockEntityRegister.FUZED_CLUSTER_PROJECTILE_BLOCK_ENTITY.get(), FuzedBlockEntityRenderer::new);
        } catch (Exception e) {
            CbcatFix.LOGGER.error("Failed to register cbc_at block entity renderers", e);
        }
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            try {
                SimpleBlockEntityVisualizer.builder(CbcatFixMunitions.FUZED_BLOCK_ENTITY.get())
                    .factory((context, be, partialTicks) -> new FuzedBlockVisual(context, be, partialTicks))
                    .apply();
                SimpleBlockEntityVisualizer.builder(com.dsvv.cbcat.registry.BlockEntityRegister.FUZED_PROJECTILE_CARTRIDGE_BLOCK_ENTITY.get())
                    .factory((context, be, partialTicks) -> new FuzedBlockVisual(context, be, partialTicks))
                    .apply();
            } catch (Exception e) {
                CbcatFix.LOGGER.error("Failed to register Flywheel visualizer for FuzedBlockEntity", e);
            }
        });
    }
}
