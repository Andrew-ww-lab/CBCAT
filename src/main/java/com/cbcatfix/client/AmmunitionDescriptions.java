package com.cbcatfix.client;

import com.simibubi.create.foundation.item.TooltipHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import java.util.List;
import rbasamoyai.createbigcannons.base.CBCTooltip;

/** Shared CBC-style prose; callers retain native fuze and technical information. */
@net.neoforged.fml.common.EventBusSubscriber(modid = "cbcatfix", value = net.neoforged.api.distmarker.Dist.CLIENT)
public final class AmmunitionDescriptions {
    private AmmunitionDescriptions() {}

    public static void append(List<Component> lines, String profile, boolean rocket) {
        lines.add(Component.empty());
        text(lines, "tooltip.cbcatfix.ammo.summary." + (rocket ? "rocket_" : "") + profile, 0);
        lines.add(Component.empty());
        String action = switch (profile) {
            case "ap", "apds", "apdsfs" -> "impact";
            case "cluster" -> "opening";
            case "propellant" -> "assembly";
            case "rocket" -> "launch";
            default -> "detonation";
        };
        lines.add(Component.translatable("tooltip.cbcatfix.ammo.action." + action).withStyle(ChatFormatting.GRAY));
        text(lines, "tooltip.cbcatfix.ammo.behavior." + profile, 1);
    }

    @net.neoforged.bus.api.SubscribeEvent
    public static void propellantTooltip(net.neoforged.neoforge.event.entity.player.ItemTooltipEvent event) {
        var id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(event.getItemStack().getItem());
        if (!id.getNamespace().equals("cbcatfix") || !id.getPath().endsWith("_rocket_propellant")) return;
        var lines = event.getToolTip();
        String[] prompt = com.simibubi.create.foundation.utility.CreateLang.translateDirect("tooltip.holdForDescription", "$")
            .getString().split("\\$", 2);
        if (prompt.length == 2) lines.add(Component.literal(prompt[0]).withStyle(ChatFormatting.DARK_GRAY)
            .append(com.simibubi.create.foundation.utility.CreateLang.translateDirect("tooltip.keyShift").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(prompt[1]).withStyle(ChatFormatting.DARK_GRAY)));
        if (net.minecraft.client.gui.screens.Screen.hasShiftDown()) append(lines, "propellant", false);
    }

    public static void text(List<Component> lines, String key, int indent) {
        var palette = CBCTooltip.getPalette();
        lines.addAll(TooltipHelper.cutStringTextComponent(Component.translatable(key).getString(),
            palette.primary(), palette.highlight(), indent));
    }

    public static String rocketProfile(net.minecraft.world.item.ItemStack stack) {
        String path = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        for (String profile : List.of("heat", "hef", "hei", "flak", "smoke", "he", "ap"))
            if (path.startsWith(profile + "_rocket") || path.contains("_" + profile + "_rocket")) return profile;
        return "rocket";
    }
    public static String munitionProfile(net.minecraft.resources.ResourceLocation itemId) {
        String namespace = itemId.getNamespace();
        String path = itemId.getPath();
        if ("cbcatfix".equals(namespace)) {
            return switch (path) {
                case "flak_shell" -> "flak";
                case "heavy_he_shell" -> "he";
                case "heat_shell" -> "heat";
                default -> null;
            };
        }
        if (!"cbc_at".equals(namespace)) {
            return null;
        }
        return switch (path) {
            case "ha_ap_item" -> "ap";
            case "apds_item", "ha_apds_item" -> "apds";
            case "apdsfs_item", "ha_apdsfs_item" -> "apdsfs";
            case "he_item", "ha_he_item" -> "he";
            case "hei_item" -> "hei";
            case "ha_hef_item" -> "hef";
            case "ha_heat_item" -> "heat";
            case "ha_smoke_item" -> "smoke";
            default -> null;
        };
    }


}