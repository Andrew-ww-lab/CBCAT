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
    private static int ponderTicks;
    private static int ponderCapture;
    @SubscribeEvent
    public static void rendered(ScreenEvent.Render.Post event) {
        if (event.getScreen() instanceof net.createmod.ponder.foundation.ui.PonderUI
            && ponderCapture < 2 && ponderTicks >= (ponderCapture == 0 ? 270 : 450)) {
            try (var image = net.minecraft.client.Screenshot.takeScreenshot(Minecraft.getInstance().getMainRenderTarget())) {
                image.writeToFile(java.nio.file.Path.of("small-launcher-ponder-" + (++ponderCapture) + ".png"));
            } catch (java.io.IOException error) { throw new RuntimeException(error); }
        }
        if (event.getScreen() instanceof TitleScreen && Minecraft.getInstance().getOverlay() == null && ++titleFrames == 120) {
            System.out.println("CBCAT CLIENT PROBE: title screen rendered for 120 frames");
            Minecraft.getInstance().stop();
        }
    }
    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        var mc = Minecraft.getInstance();
        if (mc.screen instanceof net.createmod.ponder.foundation.ui.PonderUI && ++ponderTicks > 650) {
            System.out.println("CBCAT PONDER: full UI playback completed");
            mc.stop();
        }
        if (mc.level != null && mc.player != null && mc.screen == null && ++worldTicks == 100) {
            checkModels(mc);
            checkTooltips(mc);
            checkClusterVariants(mc);
            checkAmmunition(mc);
            System.out.println("CBCAT CLIENT PROBE: existing world loaded; player active for 100 ticks");
            if (Boolean.getBoolean("cbcatfix.hintProbe")) RocketHintClientProbe.start(mc);
            else mc.setScreen(new ModelReviewScreen());
        }
    }

    /** Native item rendering on a temporary screen; no blocks or entities placed in the world. */
    private static final class ModelReviewScreen extends net.minecraft.client.gui.screens.Screen {
        private int frames;
        private final String[] ids = {"cbc_at:steel_rocket_pod_rail", "cbc_at:steel_rocket_pod_breech",
            "cbcatfix:rapid_cycling_module", "cbcatfix:traverse_gear_module", "cbc_at:cluster",
            "cbc_at:cluster", "cbc_at:cluster", "cbc_at:cluster"};
        private final String[] labels = {"Rail", "Breech", "Rapid", "Traverse", "HE", "HEF", "HEAT", "Smoke"};
        ModelReviewScreen() { super(net.minecraft.network.chat.Component.literal("Launcher model review")); }
        @Override public void render(net.minecraft.client.gui.GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            graphics.fill(0, 0, width, height, 0xff20252b);
            int cellW = width / 4, cellH = height / 2;
            float scale = Math.min(cellW / 20f, (cellH - 24) / 20f);
            for (int i = 0; i < ids.length; i++) {
                int x = (i % 4) * cellW, y = (i / 4) * cellH;
                graphics.drawCenteredString(font, labels[i], x + cellW / 2, y + 5, 0xffdedede);
                graphics.pose().pushPose();
                graphics.pose().translate(x + cellW / 2f - 8 * scale, y + 22, 0);
                graphics.pose().scale(scale, scale, 1);
                var item = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(net.minecraft.resources.ResourceLocation.parse(ids[i]));
                var stack = item.getDefaultInstance();
                if (i >= 4) stack.set(com.dsvv.cbcat.registry.DataComponentRegistry.CLUSTER_PROJECTILE,
                    "tooltip.cbc_at.ha_" + new String[] {"he", "hef", "heat", "smoke"}[i-4]);
                graphics.renderItem(stack, 0, 0);
                graphics.pose().popPose();
            }
            graphics.flush();
            if (++frames == 20) {
                try (var image = net.minecraft.client.Screenshot.takeScreenshot(minecraft.getMainRenderTarget())) {
                    image.writeToFile(java.nio.file.Path.of("launcher-model-review.png"));
                } catch (java.io.IOException error) { throw new RuntimeException(error); }
                System.out.println("CBCAT CLIENT PROBE: native model preview saved");
                minecraft.setScreen(new TooltipReviewScreen());
            }
        }
    }

    private static final class TooltipReviewScreen extends net.minecraft.client.gui.screens.Screen {
        private int frames;
        TooltipReviewScreen() { super(net.minecraft.network.chat.Component.literal("Launcher tooltip review")); }
        @Override public void render(net.minecraft.client.gui.GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            graphics.fill(0, 0, width, height, 0xff20252b);
            var stack = com.dsvv.cbcat.registry.BlockRegister.STEEL_ROCKET_POD_BREECH.asStack();
            var lines = new java.util.ArrayList<net.minecraft.network.chat.Component>();
            lines.add(stack.getHoverName());
            com.cbcatfix.client.LauncherTooltips.append(stack, lines, true);
            graphics.renderComponentTooltip(font, lines, 12, 18);
            graphics.flush();
            if (++frames == 20) {
                try (var image = net.minecraft.client.Screenshot.takeScreenshot(minecraft.getMainRenderTarget())) {
                    image.writeToFile(java.nio.file.Path.of("launcher-tooltip-review.png"));
                } catch (java.io.IOException error) { throw new RuntimeException(error); }
                minecraft.setScreen(new AmmunitionReviewScreen());
            }
        }
    }

    private static void checkAmmunition(Minecraft mc) {
        int count = 0, cartridges = 0;
        var coverage = new java.util.ArrayList<String>();
        for (var item : net.minecraft.core.registries.BuiltInRegistries.ITEM) {
            var id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item);
            var stack = item.getDefaultInstance();
            boolean rocket = item instanceof com.dsvv.cbcat.cannon.rocketpod.munitions.AbstractRocketItem
                || item instanceof com.dsvv.cbcat.cannon.medium_rocketpod.munitions.AbstractMediumRocketItem;
            String profile = com.cbcatfix.client.AmmunitionDescriptions.munitionProfile(id);
            if (rocket) profile = com.cbcatfix.client.AmmunitionDescriptions.rocketProfile(stack);
            if (id.getNamespace().equals("cbc_at") && java.util.List.of("cluster", "cluster_cartridge", "cluster_caseless").contains(id.getPath())) profile = "cluster";
            if (id.getNamespace().equals("cbcatfix") && id.getPath().endsWith("_rocket_propellant")) profile = "propellant";
            if (profile == null) continue;
            var normal = stack.getTooltipLines(net.minecraft.world.item.Item.TooltipContext.of(mc.level), mc.player, net.minecraft.world.item.TooltipFlag.NORMAL);
            long prompts = normal.stream().filter(line -> line.getString().contains("Shift")).count();
            if (prompts != 1) throw new AssertionError("Expected one ammunition Shift prompt: " + id + " got " + prompts);
            var expanded = new java.util.ArrayList<net.minecraft.network.chat.Component>();
            com.cbcatfix.client.AmmunitionDescriptions.append(expanded, profile, rocket);
            if (expanded.stream().anyMatch(line -> line.getString().contains("tooltip.cbcatfix")))
                throw new AssertionError("Untranslated ammunition description: " + id);
            long actions = expanded.stream().filter(line -> line.getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents t
                && t.getKey().startsWith("tooltip.cbcatfix.ammo.action.")).count();
            if (actions != 1) throw new AssertionError("Duplicate or absent action heading: " + id);
            if (item instanceof com.dsvv.cbcat.cannon.heavy_autocannon.munitions.AbstractHeavyAutocannonProjectileItem) {
                var cartridge = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(net.minecraft.resources.ResourceLocation.parse("cbc_at:heavy_autocannon_cartridge")).getDefaultInstance();
                com.dsvv.cbcat.cannon.heavy_autocannon.munitions.HeavyAutocannonCartridgeItem.writeProjectile(stack, cartridge);
                var loaded = cartridge.getTooltipLines(net.minecraft.world.item.Item.TooltipContext.of(mc.level), mc.player, net.minecraft.world.item.TooltipFlag.NORMAL);
                if (loaded.stream().filter(line -> line.getString().contains("Shift")).count() != 1)
                    throw new AssertionError("Loaded cartridge description not forwarded: " + id);
                cartridges++;
            }
            coverage.add(id + " -> " + profile + (rocket ? " (rocket)" : ""));
            count++;
        }
        if (count != 53 || cartridges != 7) throw new AssertionError("Unexpected ammunition coverage: " + count + "/" + cartridges);
        try { java.nio.file.Files.write(java.nio.file.Path.of("ammunition-tooltip-coverage.txt"), coverage); }
        catch (java.io.IOException error) { throw new RuntimeException(error); }
        System.out.println("CBCAT UX: " + count + " ammunition prompts/descriptions and " + cartridges + " loaded cartridge forwarding paths checked");
    }

    private static final class AmmunitionReviewScreen extends net.minecraft.client.gui.screens.Screen {
        private int frames;
        AmmunitionReviewScreen() { super(net.minecraft.network.chat.Component.literal("Ammunition tooltip review")); }
        @Override public void render(net.minecraft.client.gui.GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            graphics.fill(0, 0, width, height, 0xff20252b);
            var lines = new java.util.ArrayList<net.minecraft.network.chat.Component>();
            lines.add(net.minecraft.network.chat.Component.literal("High Explosive (HE) Shell"));
            lines.add(net.minecraft.network.chat.Component.literal("Hold [Shift] for Summary").withStyle(net.minecraft.ChatFormatting.DARK_GRAY));
            com.cbcatfix.client.AmmunitionDescriptions.append(lines, "he", false);
            graphics.renderComponentTooltip(font, lines, 12, 18);
            graphics.flush();
            if (++frames == 20) {
                try (var image = net.minecraft.client.Screenshot.takeScreenshot(minecraft.getMainRenderTarget())) {
                    image.writeToFile(java.nio.file.Path.of("ammunition-tooltip-review.png"));
                } catch (java.io.IOException error) { throw new RuntimeException(error); }
                checkPonder(minecraft);
            }
        }
    }

    private static void checkPonder(Minecraft mc) {
        for (String item : new String[] {"steel_rocket_pod_breech", "steel_rocket_pod_rail"}) {
            var scenes = net.createmod.ponder.foundation.PonderIndex.getSceneAccess()
                .compile(net.minecraft.resources.ResourceLocation.parse("cbc_at:" + item));
            var scene = scenes.stream().filter(s -> s.getId().equals(net.minecraft.resources.ResourceLocation.parse("cbcatfix:small_launcher")))
                .findFirst().orElseThrow(() -> new AssertionError("Missing Ponder registration: " + item));
            if (!scene.getTitle().equals("Building a Small Rocket Launcher")) throw new AssertionError("Ponder title not localized");
            if (scene.getWorld().getBlockState(new net.minecraft.core.BlockPos(3,3,2)).isAir())
                throw new AssertionError("Missing launcher schematic");
            for (int i = 0; i < 600; i++) scene.tick();
            if (!scene.isFinished()) throw new AssertionError("Ponder scene did not finish");
            if (scene.getWorld().getBlockState(new net.minecraft.core.BlockPos(2,3,2)).getBlock() != com.cbcatfix.rocket.LauncherModules.TRAVERSE.get())
                throw new AssertionError("Ponder module replacement did not execute");
        }
        System.out.println("CBCAT PONDER: both item registrations, localized title, schematic and complete timeline checked");
        mc.setScreen(net.createmod.ponder.foundation.ui.PonderUI.of(net.minecraft.resources.ResourceLocation.parse("cbc_at:steel_rocket_pod_breech")));
    }

    private static void checkTooltips(Minecraft mc) {
        int unfinished = 0, finished = 0;
        for (var block : net.minecraft.core.registries.BuiltInRegistries.BLOCK) {
            if (!(block instanceof rbasamoyai.createbigcannons.cannons.CannonContraptionProviderBlock cannon)) continue;
            var stack = block.asItem().getDefaultInstance();
            if (stack.isEmpty()) continue;
            var expanded = new java.util.ArrayList<net.minecraft.network.chat.Component>();
            com.cbcatfix.client.LauncherTooltips.append(stack, expanded, true);
            if (!cannon.isComplete(block.defaultBlockState())) {
                if (!expanded.isEmpty()) throw new AssertionError("Unfinished launcher properties: " + block);
                var nativeLines = stack.getTooltipLines(net.minecraft.world.item.Item.TooltipContext.of(mc.level), mc.player, net.minecraft.world.item.TooltipFlag.NORMAL);
                if (nativeLines.stream().anyMatch(line -> line.getString().contains("Shift"))) throw new AssertionError("Unfinished Shift prompt: " + block);
                unfinished++;
            } else if (block instanceof com.dsvv.cbcat.cannon.rocketpod.RocketPodBlock || block instanceof com.dsvv.cbcat.cannon.medium_rocketpod.MediumRocketPodBlock) {
                long headings = expanded.stream().filter(line -> line.getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents t
                    && t.getKey().equals("tooltip.cbcatfix.launcher.properties")).count();
                if (headings != 1) throw new AssertionError("Expected one launcher property group: " + block);
                var compact = new java.util.ArrayList<net.minecraft.network.chat.Component>();
                com.cbcatfix.client.LauncherTooltips.append(stack, compact, false);
                if (compact.size() > 2) throw new AssertionError("Verbose unexpanded tooltip: " + block);
                finished++;
            }
        }
        System.out.println("CBCAT UX: " + unfinished + " unfinished tooltips suppressed; " + finished + " finished launcher summaries checked");
    }

    private static void checkClusterVariants(Minecraft mc) {
        for (var block : java.util.List.of(com.dsvv.cbcat.registry.BlockRegister.CLUSTER_BLOCK,
            com.dsvv.cbcat.registry.BlockRegister.CLUSTER_CARTRIDGE_BLOCK, com.dsvv.cbcat.registry.BlockRegister.CLUSTER_CASELESS_BLOCK)) {
            var models = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<net.minecraft.client.resources.model.BakedModel, Boolean>());
            for (String variant : new String[] {"he", "hef", "heat", "smoke"}) {
                String payload = "tooltip.cbc_at.ha_" + variant;
                var stack = block.asStack();
                stack.set(com.dsvv.cbcat.registry.DataComponentRegistry.CLUSTER_PROJECTILE, payload);
                var model = mc.getItemRenderer().getModel(stack, mc.level, mc.player, 0);
                if (model == mc.getModelManager().getMissingModel()) throw new AssertionError("Missing cluster variant");
                models.add(model);
                var projectile = (com.dsvv.cbcat.cluster_munition.FuzedClusterProjectile) com.dsvv.cbcat.registry.EntityRegister.CLUSTER_PROJECTILE.create(mc.level);
                projectile.setProjectile(payload);
                var clone = (com.dsvv.cbcat.cluster_munition.FuzedClusterProjectile) com.dsvv.cbcat.registry.EntityRegister.CLUSTER_PROJECTILE.create(mc.level);
                var buffer = new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(), mc.level.registryAccess());
                try {
                    projectile.writeSpawnData(buffer);
                    clone.readSpawnData(buffer);
                    if (!payload.equals(((com.cbcatfix.mixin.ClusterVisualAccessor) clone).cbcatfix$payloadType())) throw new AssertionError("Cluster spawn payload lost");
                } finally { buffer.release(); }
                var pose = new com.mojang.blaze3d.vertex.PoseStack();
                pose.translate(0, -10000, 0);
                var renderer = mc.getEntityRenderDispatcher().getRenderer(clone);
                renderer.render(clone, 0, 0.5f, pose, mc.renderBuffers().bufferSource(), 15728880);
                var be = (com.dsvv.cbcat.cluster_munition.FuzedClusterProjectileBlockEntity) block.get().newBlockEntity(net.minecraft.core.BlockPos.ZERO, block.getDefaultState());
                be.setLevel(mc.level);
                be.setProjectile(payload);
                mc.getBlockEntityRenderDispatcher().getRenderer(be).render(be, 0.5f, pose, mc.renderBuffers().bufferSource(), 15728880, 0);
            }
            if (models.size() != 4) throw new AssertionError("Cluster variants share generic model: " + block);
        }
        mc.renderBuffers().bufferSource().endBatch();
        System.out.println("CBCAT UX: 12 cluster item/placed/projectile variants and spawn payload roundtrips checked");
    }

    private static void checkModels(Minecraft mc) {
        for (var block : java.util.List.of(com.cbcatfix.rocket.LauncherModules.RAPID.get(),
            com.cbcatfix.rocket.LauncherModules.UNBORED_RAPID.get(), com.cbcatfix.rocket.LauncherModules.UNBORED_TRAVERSE.get(),
            com.cbcatfix.rocket.LauncherModules.TRAVERSE.get(), com.cbcatfix.rocket.LauncherModules.UNFINISHED_RAPID.get(),
            com.cbcatfix.rocket.LauncherModules.UNFINISHED_TRAVERSE.get(), com.cbcatfix.rocket.LauncherModules.RAPID_MOULD.get(),
            com.cbcatfix.rocket.LauncherModules.TRAVERSE_MOULD.get())) {
            for (var state : block.getStateDefinition().getPossibleStates())
                if (mc.getBlockRenderer().getBlockModel(state) == mc.getModelManager().getMissingModel())
                    throw new AssertionError("Missing module block model: " + state);
            if (mc.getItemRenderer().getModel(block.asItem().getDefaultInstance(), mc.level, mc.player, 0) == mc.getModelManager().getMissingModel())
                throw new AssertionError("Missing module item model: " + block);
        }
        System.out.println("CBCAT CLIENT PROBE: all eight module/mould item models and block states present");
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
