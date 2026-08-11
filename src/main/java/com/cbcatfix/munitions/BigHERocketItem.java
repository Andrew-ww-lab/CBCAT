package com.cbcatfix.munitions;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.EntityType;
import com.dsvv.cbcat.cannon.medium_rocketpod.munitions.medium_he_rocket.HEMediumRocketItem;
import com.dsvv.cbcat.cannon.medium_rocketpod.munitions.AbstractMediumRocket;

public class BigHERocketItem extends HEMediumRocketItem {
    public BigHERocketItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public AbstractMediumRocket<?> getAutocannonProjectile(ItemStack stack, Level level) {
        BigHERocketProjectile projectile = CbcatFixMunitions.BIG_HE_ROCKET_PROJECTILE.get().create(level);
        if (projectile != null) {
            projectile.setFuze(this.getFuze(stack));
            projectile.setFuel(127);
        }
        return projectile;
    }

    @Override
    public EntityType<?> getEntityType(ItemStack stack) {
        return CbcatFixMunitions.BIG_HE_ROCKET_PROJECTILE.get();
    }

    @Override
    public ItemStack getCreativeTabCartridgeItem(int tracerColor) {
        return new ItemStack(this);
    }
}
