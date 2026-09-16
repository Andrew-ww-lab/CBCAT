package com.cbcatfix.rocket;

import com.dsvv.cbcat.cannon.medium_rocketpod.munitions.AbstractMediumRocketItem;
import com.dsvv.cbcat.cannon.rocketpod.munitions.AbstractRocketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import rbasamoyai.createbigcannons.munitions.AbstractCannonProjectile;

/** Converts a stored CBCAT rocket item through CBCAT's own projectile factory. */
public final class RocketProjectileFactory {
    private RocketProjectileFactory() {
    }

    public static AbstractCannonProjectile create(ItemStack rocket, Level level) {
        if (rocket.getItem() instanceof AbstractRocketItem smallRocket) {
            return smallRocket.getAutocannonProjectile(rocket, level);
        }
        if (rocket.getItem() instanceof AbstractMediumRocketItem mediumRocket) {
            return mediumRocket.getAutocannonProjectile(rocket, level);
        }
        return null;
    }
}
