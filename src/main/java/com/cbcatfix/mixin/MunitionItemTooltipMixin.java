package com.cbcatfix.mixin;

import com.cbcatfix.config.CbcatFixConfig;
import com.dsvv.cbcat.cannon.heavy_autocannon.munitions.AbstractHeavyAutocannonProjectileItem;
import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rbasamoyai.createbigcannons.munitions.autocannon.AutocannonRoundItem;
import rbasamoyai.createbigcannons.munitions.FuzedProjectileBlockItem;

import java.util.List;

@Mixin(value = {
    AbstractHeavyAutocannonProjectileItem.class,
    AutocannonRoundItem.class,
    FuzedProjectileBlockItem.class
}, remap = false)
public class MunitionItemTooltipMixin {
    @Inject(method = "appendHoverText", at = @At("TAIL"))
    private void cbcatfix$appendMunitionDescription(
        ItemStack stack,
        Item.TooltipContext context,
        List<Component> tooltip,
        TooltipFlag flag,
        CallbackInfo ci
    ) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        String profile = cbcatfix$profile(itemId);
        if (profile == null) {
            return;
        }

        boolean shiftDown = Screen.hasShiftDown();
        cbcatfix$addHoldShift(shiftDown, tooltip);
        if (!shiftDown) {
            return;
        }

        tooltip.add(Component.translatable("tooltip.cbcatfix.munition.summary").withStyle(ChatFormatting.GRAY));
        cbcatfix$addSpecification(
            tooltip,
            "tooltip.cbcatfix.munition.type",
            Component.translatable("tooltip.cbcatfix.munition.type." + profile)
        );
        tooltip.add(Component.literal("  ")
            .append(Component.translatable("tooltip.cbcatfix.munition.description." + profile))
            .withStyle(ChatFormatting.DARK_GRAY));

        CbcatFixConfig.HeatSettings heatSettings = cbcatfix$heatSettings(itemId);
        if (heatSettings != null) {
            cbcatfix$addSpecification(
                tooltip,
                "tooltip.cbcatfix.munition.heat_jet_length",
                Component.translatable("tooltip.cbcatfix.munition.blocks", format(heatSettings.jetLength()))
            );
            cbcatfix$addSpecification(
                tooltip,
                "tooltip.cbcatfix.munition.heat_penetration",
                Component.literal(format(heatSettings.jetPenetration()))
            );
            cbcatfix$addSpecification(
                tooltip,
                "tooltip.cbcatfix.munition.heat_energy",
                Component.literal(format(heatSettings.jetEnergy()))
            );
        }
    }

    private static String format(double value) {
        return value == Math.rint(value)
            ? Long.toString(Math.round(value))
            : String.format(java.util.Locale.ROOT, "%.1f", value);
    }

    private static void cbcatfix$addHoldShift(boolean shiftDown, List<Component> tooltip) {
        String[] prompt = CreateLang.translateDirect("tooltip.holdForDescription", "$").getString().split("\\$", 2);
        if (prompt.length != 2) {
            return;
        }
        MutableComponent line = Component.literal("")
            .append(Component.literal(prompt[0]).withStyle(ChatFormatting.DARK_GRAY))
            .append(CreateLang.translateDirect("tooltip.keyShift")
                .withStyle(shiftDown ? ChatFormatting.WHITE : ChatFormatting.GRAY))
            .append(Component.literal(prompt[1]).withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(line);
    }

    private static void cbcatfix$addSpecification(
        List<Component> tooltip,
        String labelKey,
        Component value
    ) {
        tooltip.add(Component.literal("  ").withStyle(ChatFormatting.DARK_GRAY)
            .append(Component.translatable(labelKey).withStyle(ChatFormatting.GRAY))
            .append(Component.literal(" "))
            .append(value.copy().withStyle(ChatFormatting.WHITE)));
    }

    private static String cbcatfix$profile(ResourceLocation itemId) {
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

    private static CbcatFixConfig.HeatSettings cbcatfix$heatSettings(ResourceLocation itemId) {
        if ("cbcatfix".equals(itemId.getNamespace()) && "heat_shell".equals(itemId.getPath())) {
            return CbcatFixConfig.BIG_CANNON_HEAT;
        }
        if ("cbc_at".equals(itemId.getNamespace()) && "ha_heat_item".equals(itemId.getPath())) {
            return CbcatFixConfig.HEAVY_AUTOCANNON_HEAT;
        }
        return null;
    }
}
