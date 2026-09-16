package com.cbcatfix.rocket;

import com.dsvv.cbcat.registry.DataComponentRegistry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import rbasamoyai.createbigcannons.index.CBCDataComponents;

import java.util.List;

public final class RocketStackFactory {
    private static final String FUEL_TICKS_TAG = "CbcatFixFuelTicks";
    private static final String LIGHTWEIGHT_TAG = "CbcatFixLightweight";
    private static final String ASSEMBLY_FUEL_TAG = "CbcatFixAssemblyFuel";

    private RocketStackFactory() {
    }

    public static ItemStack create(Item rocket, Item warhead, int payloadCount, int fuelTicks) {
        return create(rocket, warhead, payloadCount, fuelTicks, false);
    }

    public static ItemStack create(Item rocket, Item warhead, int payloadCount, int fuelTicks, boolean lightweight) {
        ItemStack result = rocket.getDefaultInstance();
        ItemStack storedWarhead = warhead.getDefaultInstance().copyWithCount(Math.clamp(payloadCount, 1, 2));
        result.set(CBCDataComponents.PROJECTILE, ItemContainerContents.fromItems(List.of(storedWarhead)));
        int safeFuelTicks = Math.clamp(fuelTicks, 0, com.cbcatfix.balance.BalanceDefaults.MAX_FUEL_TICKS);
        result.set(DataComponentRegistry.ROCKET_FUEL, (byte) Math.min(safeFuelTicks, 127));
        CustomData.update(DataComponents.CUSTOM_DATA, result, tag -> {
            tag.putInt(FUEL_TICKS_TAG, safeFuelTicks);
            tag.putBoolean(LIGHTWEIGHT_TAG, lightweight);
        });
        return result;
    }

    public static int getFuelTicks(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        if (usesConfiguredFuel(stack)) {
            int full = RocketBalance.fullFlightTicks(RocketBalance.tierForRocketItem(stack.getItem()));
            if (isLightweight(stack)) {
                return Math.max(1, (int) Math.round(full
                    * com.cbcatfix.config.CbcatFixConfig.value(com.cbcatfix.config.CbcatFixConfig.LIGHTWEIGHT_FUEL_MULTIPLIER)));
            }
            ItemStack warhead = stack.getOrDefault(CBCDataComponents.PROJECTILE, ItemContainerContents.EMPTY).copyOne();
            return warhead.getCount() > 1 ? Math.max(1, (int) Math.round(full * com.cbcatfix.balance.BalanceDefaults.DOUBLE_PAYLOAD_FUEL)) : full;
        }
        if (customData.contains(FUEL_TICKS_TAG)) {
            return Math.clamp(customData.copyTag().getInt(FUEL_TICKS_TAG), 0, com.cbcatfix.balance.BalanceDefaults.MAX_FUEL_TICKS);
        }
        return stack.has(DataComponentRegistry.ROCKET_FUEL)
            ? Byte.toUnsignedInt(stack.get(DataComponentRegistry.ROCKET_FUEL))
            : 0;
    }

    public static boolean isLightweight(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getBoolean(LIGHTWEIGHT_TAG);
    }

    public static boolean usesConfiguredFuel(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
            .copyTag().getBoolean(ASSEMBLY_FUEL_TAG);
    }
}
