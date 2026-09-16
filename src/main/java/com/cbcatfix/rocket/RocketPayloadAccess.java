package com.cbcatfix.rocket;

import net.minecraft.world.item.ItemStack;

public interface RocketPayloadAccess {
    RocketFlightState cbcatfix$flightState();

    void cbcatfix$configurePayload(int payloadCount, ItemStack fuze, boolean armorPiercing);

    void cbcatfix$configureFlight(double maxPoweredSpeed, float guidanceTurnRate);

    void cbcatfix$setPoweredFlightTicks(int poweredFlightTicks);

    int cbcatfix$getPayloadCount();

    double cbcatfix$getMaxPoweredSpeed();

    float cbcatfix$getGuidanceTurnRate();

    int cbcatfix$getPoweredFlightTicks();

    void cbcatfix$configureDurability(float maximumDurability);

    boolean cbcatfix$damageDurability(float damage);

    float cbcatfix$getDurability();

    float cbcatfix$getMaximumDurability();

    boolean cbcatfix$isArmorPiercing();

    boolean cbcatfix$hasGuidance();

    void cbcatfix$breakGuidance();
}
