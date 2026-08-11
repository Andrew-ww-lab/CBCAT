package com.cbcatfix.munitions;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.EntityType;
import com.dsvv.cbcat.cannon.medium_rocketpod.munitions.medium_ap_rocket.APMediumRocketItem;
import com.dsvv.cbcat.cannon.medium_rocketpod.munitions.AbstractMediumRocket;

public class BigAPRocketItem extends APMediumRocketItem {
    public BigAPRocketItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public AbstractMediumRocket<?> getAutocannonProjectile(ItemStack stack, Level level) {
        BigAPRocketProjectile projectile = CbcatFixMunitions.BIG_AP_ROCKET_PROJECTILE.get().create(level);
        if (projectile != null) {
            projectile.setFuel(127);
        }
        return projectile;
    }

    @Override
    public EntityType<?> getEntityType(ItemStack stack) {
        return CbcatFixMunitions.BIG_AP_ROCKET_PROJECTILE.get();
    }

    @Override
    public ItemStack getCreativeTabCartridgeItem(int tracerColor) {
        return new ItemStack(this);
    }
}
