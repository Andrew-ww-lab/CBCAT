package com.cbcatfix.mixin;

import com.dsvv.cbcat.cluster_munition.FuzedClusterProjectile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import rbasamoyai.createbigcannons.munitions.big_cannon.FuzedBigCannonProjectile;

/** CBCAT omits its cluster payload from saves; serialize its native fields directly. */
@Mixin(value = FuzedClusterProjectile.class, remap = false)
public abstract class FuzedClusterProjectileMixin extends FuzedBigCannonProjectile {
    @Shadow protected ItemStack[] secondaryFuzes;
    @Shadow protected String projectile;

    protected FuzedClusterProjectileMixin(EntityType<? extends FuzedBigCannonProjectile> type, Level level) {
        super(type, level);
    }

    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        ListTag fuzes = new ListTag();
        for (ItemStack fuze : secondaryFuzes) fuzes.add(fuze.saveOptional(level().registryAccess()));
        tag.put("SecondaryFuzes", fuzes);
        tag.putString("Projectile", projectile);
    }

    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("SecondaryFuzes", 9)) {
            ListTag fuzes = tag.getList("SecondaryFuzes", 10);
            secondaryFuzes = new ItemStack[fuzes.size()];
            for (int i = 0; i < fuzes.size(); i++)
                secondaryFuzes[i] = ItemStack.parseOptional(level().registryAccess(), fuzes.getCompound(i));
        }
        if (tag.contains("Projectile", 8)) projectile = tag.getString("Projectile");
    }
}
