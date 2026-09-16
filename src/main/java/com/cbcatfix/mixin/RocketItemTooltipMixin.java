package com.cbcatfix.mixin;

import com.dsvv.cbcat.cannon.medium_rocketpod.munitions.AbstractMediumRocketItem;
import com.dsvv.cbcat.cannon.rocketpod.munitions.AbstractRocketItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import com.dsvv.cbcat.registry.DataComponentRegistry;
import com.cbcatfix.rocket.RocketBalance;
import com.cbcatfix.rocket.RocketStackFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rbasamoyai.createbigcannons.index.CBCDataComponents;

import java.util.List;
import com.simibubi.create.foundation.utility.CreateLang;

@Mixin(value = {AbstractRocketItem.class, AbstractMediumRocketItem.class}, remap = false)
public class RocketItemTooltipMixin {
    @Inject(method = "appendHoverText", at = @At("TAIL"))
    private void cbcatfix$showPayloadMode(
        ItemStack stack,
        Item.TooltipContext context,
        List<Component> tooltip,
        TooltipFlag flag,
        CallbackInfo ci
    ) {
        ItemContainerContents payload = stack.getOrDefault(CBCDataComponents.PROJECTILE, ItemContainerContents.EMPTY);
        ItemStack warhead = payload.copyOne();
        if (warhead.isEmpty()) {
            return;
        }

        tooltip.removeIf(component -> component.getContents() instanceof TranslatableContents translated
            && "tooltip.cbc_at.rocket.fuel".equals(translated.getKey()));

        boolean shiftDown = Screen.hasShiftDown();
        cbcatfix$addHoldShift(shiftDown, tooltip);
        if (!shiftDown) {
            return;
        }

        int count = Math.clamp(warhead.getCount(), 1, 2);
        int fuelTicks = RocketStackFactory.getFuelTicks(stack);
        double speed = RocketBalance.maxSpeed(stack);

        tooltip.add(Component.translatable("tooltip.cbcatfix.rocket.summary").withStyle(ChatFormatting.GRAY));
        String modeKey = RocketStackFactory.isLightweight(stack)
            ? "tooltip.cbcatfix.rocket.mode.lightweight"
            : count == 2 ? "tooltip.cbcatfix.rocket.mode.double_payload"
            : "tooltip.cbcatfix.rocket.mode.long_range";
        cbcatfix$addSpecification(tooltip, "tooltip.cbcatfix.rocket.mode", Component.translatable(modeKey));
        cbcatfix$addSpecification(tooltip, "tooltip.cbcatfix.rocket.charge",
            Component.literal(count + "× ").append(warhead.getHoverName()));
        cbcatfix$addSpecification(tooltip, "tooltip.cbcatfix.rocket.flight_time",
            Component.translatable("tooltip.cbcatfix.rocket.seconds",
                String.format(java.util.Locale.ROOT, "%.1f", fuelTicks / 20.0)));
        cbcatfix$addSpecification(tooltip, "tooltip.cbcatfix.rocket.speed",
            Component.translatable("tooltip.cbcatfix.rocket.blocks_per_second", Math.round(speed * 20.0)));
        cbcatfix$addSpecification(tooltip, "tooltip.cbcatfix.rocket.durability",
            Component.translatable("tooltip.cbcatfix.rocket.damage_points",
                Math.round(RocketBalance.maximumDurability(stack))));
        float payloadScale = RocketBalance.payloadScale(count);
        double massScale = payloadScale * (RocketStackFactory.isLightweight(stack)
            ? com.cbcatfix.config.CbcatFixConfig.value(com.cbcatfix.config.CbcatFixConfig.LIGHTWEIGHT_MASS_MULTIPLIER) : 1.0);
        double baseResponse = RocketBalance.responseTicks(RocketBalance.tierForRocketItem(stack.getItem()));
        cbcatfix$addSpecification(tooltip, "tooltip.cbcatfix.rocket.payload_scale",
            Component.literal(String.format(java.util.Locale.ROOT, "%.0f%%", payloadScale * 100)));
        cbcatfix$addSpecification(tooltip, "tooltip.cbcatfix.rocket.acceleration_scale",
            Component.literal(String.format(java.util.Locale.ROOT, "%.0f%%",
                baseResponse / Math.max(1.0, baseResponse * massScale
                    / com.cbcatfix.config.CbcatFixConfig.value(com.cbcatfix.config.CbcatFixConfig.ACCELERATION_MULTIPLIER)) * 100)));
        ItemStack fuze = stack.getOrDefault(CBCDataComponents.FUZE, ItemContainerContents.EMPTY).copyOne();
        cbcatfix$addSpecification(
            tooltip,
            "tooltip.cbcatfix.rocket.fuze",
            fuze.isEmpty()
                ? Component.translatable("tooltip.cbcatfix.rocket.fuze.none")
                : fuze.getHoverName()
        );
    }

    private static void cbcatfix$addHoldShift(boolean shiftDown, List<Component> tooltip) {
        String[] prompt = CreateLang.translateDirect("tooltip.holdForDescription", "$").getString().split("\\$", 2);
        if (prompt.length != 2) {
            return;
        }
        MutableComponent line = Component.literal("")
            .append(Component.literal(prompt[0]).withStyle(ChatFormatting.DARK_GRAY))
            .append(CreateLang.translateDirect("tooltip.keyShift").withStyle(shiftDown ? ChatFormatting.WHITE : ChatFormatting.GRAY))
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

}
