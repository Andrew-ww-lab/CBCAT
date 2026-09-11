package com.cbcatfix;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(CbcatFix.MOD_ID)
public class CbcatFix {

    public static final String MOD_ID = "cbcatfix";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> MISSILE_LAUNCH = SOUNDS.register("missile_launch",
        () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "missile_launch"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> ROCKET_ENGINE = SOUNDS.register("rocket_engine",
        () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "rocket_engine"))
    );

    public CbcatFix(IEventBus modEventBus, net.neoforged.fml.ModContainer modContainer) {
        LOGGER.info("[CBCAT Fix] Loaded — ControlPitchContraption.onRecoil compatibility patch active.");
        LOGGER.info("[CBCAT Fix] cbc_at cannons should no longer crash on fire.");

        initializeCbcatRecipeSerializers();
        modContainer.registerConfig(net.neoforged.fml.config.ModConfig.Type.COMMON, com.cbcatfix.config.CbcatFixConfig.SPEC);
        SOUNDS.register(modEventBus);
        com.cbcatfix.rocket.RocketArmPoint.register(modEventBus);
        modEventBus.addListener((net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent event) ->
            event.enqueueWork(com.cbcatfix.rocket.LauncherTransfer::verifyIntegration));
        modEventBus.addListener(com.cbcatfix.rocket.RocketArmPoint::registerCapabilities);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(com.cbcatfix.rocket.RocketSounds::onEntityJoin);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(com.cbcatfix.rocket.MountedRocketInteraction::onRightClick);
        com.cbcatfix.munitions.CbcatFixMunitions.register(modEventBus);

        modEventBus.addListener(EventPriority.LOWEST, this::onBuildCreativeTabContents);

        if (isClientDist()) {
            modEventBus.register(com.cbcatfix.client.ClientSetup.class);
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
                com.cbcatfix.client.RocketClientEvents::onEntityTick
            );
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(com.cbcatfix.client.RocketClientEvents::onEntityLeave);
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(com.cbcatfix.client.RocketClientEvents::onLevelUnload);
        }
    }

    private void onBuildCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        try {
            boolean isCbcAtTab = false;
            if (event.getTabKey() != null && "cbc_at".equals(event.getTabKey().location().getNamespace())) {
                isCbcAtTab = true;
            }
            try {
                if (com.dsvv.cbcat.registry.TabRegister.SIMPLE_TAB != null && com.dsvv.cbcat.registry.TabRegister.SIMPLE_TAB.get() == event.getTab()) {
                    isCbcAtTab = true;
                }
            } catch (Throwable ignored) {}

            if (isCbcAtTab) {
                net.minecraft.world.item.CreativeModeTab.TabVisibility vis = net.minecraft.world.item.CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS;

                removeDefaultRocketEntries(event, vis);
                addBalancedRocketVariants(event, vis);
                event.accept(com.cbcatfix.munitions.CbcatFixMunitions.SMALL_ROCKET_PROPELLANT.get());
                event.accept(com.cbcatfix.munitions.CbcatFixMunitions.MEDIUM_ROCKET_PROPELLANT.get());
                event.accept(com.cbcatfix.munitions.CbcatFixMunitions.LARGE_ROCKET_PROPELLANT.get());

                try {
                    net.minecraft.world.item.ItemStack anchorRail = new net.minecraft.world.item.ItemStack(com.dsvv.cbcat.registry.BlockRegister.WROUGHT_IRON_MEDIUM_ROCKET_RAIL.get());
                    event.insertAfter(anchorRail, com.cbcatfix.munitions.CbcatFixMunitions.BIG_ROCKET_RAIL_ITEM.get().getDefaultInstance(), vis);
                    event.insertAfter(anchorRail, com.cbcatfix.munitions.CbcatFixMunitions.BIG_ROCKET_RAIL_BREECH_ITEM.get().getDefaultInstance(), vis);
                } catch (Exception e) {
                    event.accept(com.cbcatfix.munitions.CbcatFixMunitions.BIG_ROCKET_RAIL_ITEM.get());
                    event.accept(com.cbcatfix.munitions.CbcatFixMunitions.BIG_ROCKET_RAIL_BREECH_ITEM.get());
                }

                try {
                    net.minecraft.world.item.ItemStack anchorHA = com.dsvv.cbcat.registry.ItemRegister.HA_AP_ITEM.asStack();
                    event.insertAfter(anchorHA, com.cbcatfix.munitions.CbcatFixMunitions.FLAK_SHELL_ITEM.get().getDefaultInstance(), vis);
                    event.insertAfter(anchorHA, com.cbcatfix.munitions.CbcatFixMunitions.HEAVY_HE_SHELL_ITEM.get().getDefaultInstance(), vis);
                    event.insertAfter(anchorHA, com.cbcatfix.munitions.CbcatFixMunitions.HEAT_SHELL_ITEM.get().getDefaultInstance(), vis);
                    event.insertAfter(anchorHA, com.cbcatfix.munitions.CbcatFixMunitions.HESH_SHELL_ITEM.get().getDefaultInstance(), vis);
                } catch (Exception e) {
                    event.accept(com.cbcatfix.munitions.CbcatFixMunitions.FLAK_SHELL_ITEM.get());
                    event.accept(com.cbcatfix.munitions.CbcatFixMunitions.HEAVY_HE_SHELL_ITEM.get());
                    event.accept(com.cbcatfix.munitions.CbcatFixMunitions.HEAT_SHELL_ITEM.get());
                    event.accept(com.cbcatfix.munitions.CbcatFixMunitions.HESH_SHELL_ITEM.get());
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to inject items into CBCAT creative mode tab", e);
        }
    }

    private static boolean isRocketStack(net.minecraft.world.item.ItemStack stack) {
        return stack.getItem() instanceof com.dsvv.cbcat.cannon.rocketpod.munitions.AbstractRocketItem
            || stack.getItem() instanceof com.dsvv.cbcat.cannon.medium_rocketpod.munitions.AbstractMediumRocketItem;
    }

    private static void removeDefaultRocketEntries(
        BuildCreativeModeTabContentsEvent event,
        net.minecraft.world.item.CreativeModeTab.TabVisibility visibility
    ) {
        java.util.List<net.minecraft.world.item.ItemStack> existingRockets = java.util.stream.Stream.concat(
                event.getParentEntries().stream(),
                event.getSearchEntries().stream()
            )
            .filter(CbcatFix::isRocketStack)
            .map(net.minecraft.world.item.ItemStack::copy)
            .toList();
        for (net.minecraft.world.item.ItemStack stack : existingRockets) {
            event.remove(stack, visibility);
        }
    }

    private static void addBalancedRocketVariants(
        BuildCreativeModeTabContentsEvent event,
        net.minecraft.world.item.CreativeModeTab.TabVisibility visibility
    ) {
        java.util.List<RocketCreativePair> rockets = java.util.List.of(
            new RocketCreativePair(rbasamoyai.createbigcannons.index.CBCItems.AP_AUTOCANNON_ROUND.get(), com.dsvv.cbcat.registry.ItemRegister.AP_ROCKET_ITEM.get(), com.cbcatfix.rocket.RocketBalance.Tier.SMALL),
            new RocketCreativePair(rbasamoyai.createbigcannons.index.CBCItems.FLAK_AUTOCANNON_ROUND.get(), com.dsvv.cbcat.registry.ItemRegister.FLAK_ROCKET_ITEM.get(), com.cbcatfix.rocket.RocketBalance.Tier.SMALL),
            new RocketCreativePair(com.dsvv.cbcat.registry.ItemRegister.HE_ITEM.get(), com.dsvv.cbcat.registry.ItemRegister.HE_ROCKET_ITEM.get(), com.cbcatfix.rocket.RocketBalance.Tier.SMALL),
            new RocketCreativePair(com.dsvv.cbcat.registry.ItemRegister.HEI_ITEM.get(), com.dsvv.cbcat.registry.ItemRegister.HEI_ROCKET_ITEM.get(), com.cbcatfix.rocket.RocketBalance.Tier.SMALL),
            new RocketCreativePair(com.dsvv.cbcat.registry.ItemRegister.HA_AP_ITEM.get(), com.dsvv.cbcat.registry.ItemRegister.MEDIUM_AP_ROCKET_ITEM.get(), com.cbcatfix.rocket.RocketBalance.Tier.MEDIUM),
            new RocketCreativePair(com.dsvv.cbcat.registry.ItemRegister.HA_HE_ITEM.get(), com.dsvv.cbcat.registry.ItemRegister.MEDIUM_HE_ROCKET_ITEM.get(), com.cbcatfix.rocket.RocketBalance.Tier.MEDIUM),
            new RocketCreativePair(com.dsvv.cbcat.registry.ItemRegister.HA_HEF_ITEM.get(), com.dsvv.cbcat.registry.ItemRegister.MEDIUM_HEF_ROCKET_ITEM.get(), com.cbcatfix.rocket.RocketBalance.Tier.MEDIUM),
            new RocketCreativePair(com.dsvv.cbcat.registry.ItemRegister.HA_HEAT_ITEM.get(), com.dsvv.cbcat.registry.ItemRegister.MEDIUM_HEAT_ROCKET_ITEM.get(), com.cbcatfix.rocket.RocketBalance.Tier.MEDIUM),
            new RocketCreativePair(rbasamoyai.createbigcannons.index.CBCBlocks.AP_SHOT.asItem(), com.cbcatfix.munitions.CbcatFixMunitions.BIG_AP_ROCKET_ITEM.get(), com.cbcatfix.rocket.RocketBalance.Tier.BIG),
            new RocketCreativePair(rbasamoyai.createbigcannons.index.CBCBlocks.HE_SHELL.asItem(), com.cbcatfix.munitions.CbcatFixMunitions.BIG_HE_ROCKET_ITEM.get(), com.cbcatfix.rocket.RocketBalance.Tier.BIG),
            new RocketCreativePair(com.cbcatfix.munitions.CbcatFixMunitions.HEAT_SHELL_ITEM.get(), com.cbcatfix.munitions.CbcatFixMunitions.BIG_HEAT_ROCKET_ITEM.get(), com.cbcatfix.rocket.RocketBalance.Tier.BIG)
        );

        for (RocketCreativePair pair : rockets) {
            int fullFuelTicks = com.cbcatfix.rocket.RocketBalance.fullFlightTicks(pair.tier());
            int lightweightFuelTicks = Math.max(1, (int) Math.round(fullFuelTicks
                * com.cbcatfix.config.CbcatFixConfig.LIGHTWEIGHT_FUEL_MULTIPLIER.get()));
            event.accept(com.cbcatfix.rocket.RocketStackFactory.create(
                pair.rocket(), pair.warhead(), 1, lightweightFuelTicks, true
            ), visibility);
        }
    }

    private record RocketCreativePair(
        net.minecraft.world.item.Item warhead,
        net.minecraft.world.item.Item rocket,
        com.cbcatfix.rocket.RocketBalance.Tier tier
    ) {
    }

    private static void initializeCbcatRecipeSerializers() {
        int recipeSerializerCount = com.dsvv.cbcat.registry.RecipeRegister.values().length;
        LOGGER.info("[CBCAT Fix] Initialized {} cbc_at recipe serializers before registry events.", recipeSerializerCount);
    }

    private static boolean isClientDist() {
        try {
            Object dist = Class.forName("net.neoforged.fml.loading.FMLEnvironment")
                               .getField("dist")
                               .get(null);
            return dist != null && dist.toString().equals("CLIENT");
        } catch (Exception e) {
            return false;
        }
    }
}
