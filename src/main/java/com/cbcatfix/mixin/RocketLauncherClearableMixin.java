package com.cbcatfix.mixin;

import com.dsvv.cbcat.cannon.rocketpod.RocketPodBlockEntity;
import com.dsvv.cbcat.cannon.medium_rocketpod.MediumRocketPodBlockEntity;
import net.minecraft.world.Clearable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import rbasamoyai.createbigcannons.cannons.ItemCannonBehavior;

/** Native Create/Sable inventory-removal contract, including rounds stuck in a rail. */
@Mixin(value = {RocketPodBlockEntity.class, MediumRocketPodBlockEntity.class}, remap = false)
public abstract class RocketLauncherClearableMixin implements Clearable {
    @Shadow(remap = false) public abstract ItemCannonBehavior cannonBehavior();

    @Override public void clearContent() {
        cannonBehavior().removeItem();
    }
}
