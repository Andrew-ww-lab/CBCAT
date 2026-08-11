package com.cbcatfix;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
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

    public CbcatFix(IEventBus modEventBus) {
        LOGGER.info("[CBCAT Fix] Loaded — ControlPitchContraption.onRecoil compatibility patch active.");
        LOGGER.info("[CBCAT Fix] cbc_at cannons should no longer crash on fire.");

        SOUNDS.register(modEventBus);
        com.cbcatfix.munitions.CbcatFixMunitions.register(modEventBus);

        modEventBus.addListener(this::onBuildCreativeTabContents);

        if (isClientDist()) {
            modEventBus.register(com.cbcatfix.client.ClientSetup.class);
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
                
                try {
                    net.minecraft.world.item.ItemStack anchorRocket = com.dsvv.cbcat.registry.ItemRegister.MEDIUM_AP_ROCKET_ITEM.asStack();
                    event.insertAfter(anchorRocket, com.cbcatfix.munitions.CbcatFixMunitions.BIG_AP_ROCKET_ITEM.get().getDefaultInstance(), vis);
                    event.insertAfter(anchorRocket, com.cbcatfix.munitions.CbcatFixMunitions.BIG_HE_ROCKET_ITEM.get().getDefaultInstance(), vis);
                    event.insertAfter(anchorRocket, com.cbcatfix.munitions.CbcatFixMunitions.BIG_HEAT_ROCKET_ITEM.get().getDefaultInstance(), vis);
                } catch (Exception e) {
                    event.accept(com.cbcatfix.munitions.CbcatFixMunitions.BIG_AP_ROCKET_ITEM.get());
                    event.accept(com.cbcatfix.munitions.CbcatFixMunitions.BIG_HE_ROCKET_ITEM.get());
                    event.accept(com.cbcatfix.munitions.CbcatFixMunitions.BIG_HEAT_ROCKET_ITEM.get());
                }

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
