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
        modContainer.registerConfig(net.neoforged.fml.config.ModConfig.Type.SERVER, com.cbcatfix.config.CbcatFixConfig.SPEC);
        SOUNDS.register(modEventBus);
        com.cbcatfix.rocket.RocketArmPoint.register(modEventBus);
        modEventBus.addListener((net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent event) ->
            event.enqueueWork(com.cbcatfix.rocket.LauncherTransfer::verifyIntegration));
        modEventBus.addListener(com.cbcatfix.rocket.RocketArmPoint::registerCapabilities);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(com.cbcatfix.rocket.RocketSounds::onEntityJoin);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(com.cbcatfix.rocket.MountedRocketInteraction::onRightClick);
        com.cbcatfix.munitions.CbcatFixMunitions.register(modEventBus);
        modEventBus.addListener((net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent event) -> {
            event.modify(com.dsvv.cbcat.registry.BlockEntityRegister.MEDIUM_ROCKET_POD_BARREL_BLOCK_ENTITY.get(),
                com.cbcatfix.munitions.CbcatFixMunitions.BIG_ROCKET_RAIL.get());
            event.modify(com.dsvv.cbcat.registry.BlockEntityRegister.MEDIUM_ROCKET_POD_BREECH_BLOCK_ENTITY.get(),
                com.cbcatfix.munitions.CbcatFixMunitions.BIG_ROCKET_RAIL_BREECH.get());
        });

        modEventBus.addListener(EventPriority.LOWEST, this::onBuildCreativeTabContents);

    }

    private void onBuildCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab() != com.dsvv.cbcat.registry.TabRegister.SIMPLE_TAB.get()) return;
        var vis = net.minecraft.world.item.CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS;
        var ordered = new java.util.ArrayList<net.minecraft.world.item.ItemStack>();
        // Preserve native unrelated entries and third-party contributions in their existing order.
        var launchers = event.getParentEntries().stream().filter(CbcatFix::isLauncherStack)
            .filter(stack -> !MOD_ID.equals(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace()))
            .sorted(java.util.Comparator.<net.minecraft.world.item.ItemStack>comparingInt(stack ->
                ((net.minecraft.world.item.BlockItem) stack.getItem()).getBlock() instanceof com.dsvv.cbcat.cannon.rocketpod.RocketPodBlock ? 0 : 1)
                .thenComparing(stack -> net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).toString())).toList();
        ordered.addAll(launchers);
        ordered.add(com.cbcatfix.munitions.CbcatFixMunitions.BIG_ROCKET_RAIL_BREECH_ITEM.get().getDefaultInstance());
        ordered.add(com.cbcatfix.munitions.CbcatFixMunitions.BIG_ROCKET_RAIL_ITEM.get().getDefaultInstance());
        addBalancedRocketVariants(ordered);
        ordered.add(com.cbcatfix.munitions.CbcatFixMunitions.FLAK_SHELL_ITEM.get().getDefaultInstance());
        ordered.add(com.cbcatfix.munitions.CbcatFixMunitions.HEAVY_HE_SHELL_ITEM.get().getDefaultInstance());
        ordered.add(com.cbcatfix.munitions.CbcatFixMunitions.HEAT_SHELL_ITEM.get().getDefaultInstance());
        ordered.add(com.cbcatfix.munitions.CbcatFixMunitions.SMALL_ROCKET_PROPELLANT.get().getDefaultInstance());
        ordered.add(com.cbcatfix.munitions.CbcatFixMunitions.MEDIUM_ROCKET_PROPELLANT.get().getDefaultInstance());
        ordered.add(com.cbcatfix.munitions.CbcatFixMunitions.LARGE_ROCKET_PROPELLANT.get().getDefaultInstance());
        var owned = ordered.stream().map(net.minecraft.world.item.ItemStack::getItem).collect(java.util.stream.Collectors.toSet());
        var remove = java.util.stream.Stream.concat(event.getParentEntries().stream(), event.getSearchEntries().stream())
            .filter(stack -> owned.contains(stack.getItem()) || isRocketStack(stack)).toList();
        remove.forEach(stack -> event.remove(stack, vis));
        var anchor = event.getParentEntries().stream().findFirst().orElse(net.minecraft.world.item.ItemStack.EMPTY);
        for (var stack : ordered) {
            if (anchor.isEmpty()) event.accept(stack, vis);
            else event.insertBefore(anchor, stack, vis);
        }
    }

    private static boolean isLauncherStack(net.minecraft.world.item.ItemStack stack) {
        if (!(stack.getItem() instanceof net.minecraft.world.item.BlockItem blockItem)) return false;
        return blockItem.getBlock() instanceof com.dsvv.cbcat.cannon.rocketpod.RocketPodBlock
            || blockItem.getBlock() instanceof com.dsvv.cbcat.cannon.medium_rocketpod.MediumRocketPodBlock;
    }

    private static boolean isRocketStack(net.minecraft.world.item.ItemStack stack) {
        return stack.getItem() instanceof com.dsvv.cbcat.cannon.rocketpod.munitions.AbstractRocketItem
            || stack.getItem() instanceof com.dsvv.cbcat.cannon.medium_rocketpod.munitions.AbstractMediumRocketItem;
    }

    private static void addBalancedRocketVariants(java.util.List<net.minecraft.world.item.ItemStack> ordered) {
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
            int fuel = com.cbcatfix.rocket.RocketBalance.fullFlightTicks(pair.tier());
            var lightweight = com.cbcatfix.rocket.RocketStackFactory.create(pair.rocket(), pair.warhead(), 1, fuel, true);
            net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                lightweight, tag -> tag.putBoolean("CbcatFixAssemblyFuel", true));
            ordered.add(lightweight);
            String base = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(pair.rocket()).getPath().replaceFirst("_item$", "");
            for (String suffix : java.util.List.of("_double_fuel", "_double_payload")) {
                var item = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(MOD_ID, base + suffix));
                ordered.add(item.getDefaultInstance());
            }
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

}
