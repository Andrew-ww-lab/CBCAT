package com.cbcatfix.audit;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

/** Opt-in startup observation, followed by normal client shutdown. */
@EventBusSubscriber(modid = "cbcatfix", value = Dist.CLIENT)
public final class ClientStartupProbe {
    private static int titleFrames;
    private static int worldTicks;
    @SubscribeEvent
    public static void rendered(ScreenEvent.Render.Post event) {
        if (event.getScreen() instanceof TitleScreen && Minecraft.getInstance().getOverlay() == null && ++titleFrames == 120) {
            System.out.println("CBCAT CLIENT PROBE: title screen rendered for 120 frames");
            Minecraft.getInstance().stop();
        }
    }
    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        var mc = Minecraft.getInstance();
        if (mc.level != null && mc.player != null && mc.screen == null && ++worldTicks == 100) {
            checkModels(mc);
            System.out.println("CBCAT CLIENT PROBE: existing world loaded; player active for 100 ticks");
            mc.stop();
        }
    }

    private static void checkModels(Minecraft mc) {
        int checked = 0;
        for (var item : net.minecraft.core.registries.BuiltInRegistries.ITEM) {
            if (!(item instanceof com.dsvv.cbcat.cannon.heavy_autocannon.munitions.AbstractHeavyAutocannonProjectileItem ammo)) continue;
            var stack = item.getDefaultInstance();
            var model = mc.getItemRenderer().getModel(stack, mc.level, mc.player, 0);
            if (model == mc.getModelManager().getMissingModel()) throw new AssertionError("Missing ammo model: " + item);
            var projectile = ammo.getAutocannonProjectile(stack, mc.level);
            var renderer = mc.getEntityRenderDispatcher().getRenderer(projectile);
            if (!(renderer instanceof com.dsvv.cbcat.cannon.heavy_autocannon.munitions.HeavyAutocannonProjectileRenderer))
                throw new AssertionError("Unexpected heavy renderer " + renderer);
            var pose = new com.mojang.blaze3d.vertex.PoseStack();
            pose.translate(0, -10000, 0); // Exercise both render branches without spawning or changing the world.
            for (boolean tracer : new boolean[] {false, true}) {
                projectile.setTracer(tracer);
                renderer.render(projectile, 0, 0.5f, pose, mc.renderBuffers().bufferSource(), 15728880);
            }
            checked++;
        }
        mc.renderBuffers().bufferSource().endBatch();
        if (checked != 7) throw new AssertionError("Expected seven heavy ammo models, got " + checked);
        System.out.println("CBCAT CONTENT CLIENT: seven native ammo models present; 14 heavy renderer calls passed");
    }
}
