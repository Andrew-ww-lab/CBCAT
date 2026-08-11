package com.cbcatfix.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.Entity;
import rbasamoyai.createbigcannons.munitions.big_cannon.FuzedBigCannonProjectile;
import com.dsvv.cbcat.cluster_munition.FuzedClusterProjectile;

import java.lang.reflect.Field;

@Mixin(value = FuzedBigCannonProjectile.class, remap = false)
public class FuzedClusterProjectileMixin {

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void onAddAdditionalSaveData(CompoundTag tag, CallbackInfo ci) {
        if (!((Object) this instanceof FuzedClusterProjectile)) return;
        try {
            Field fuzesField = FuzedClusterProjectile.class.getDeclaredField("secondaryFuzes");
            fuzesField.setAccessible(true);
            ItemStack[] fuzes = (ItemStack[]) fuzesField.get(this);
            if (fuzes != null) {
                ListTag list = new ListTag();
                for (ItemStack stack : fuzes) {
                    list.add(stack.saveOptional(((Entity)(Object)this).level().registryAccess()));
                }
                tag.put("SecondaryFuzes", list);
            }

            Field projField = FuzedClusterProjectile.class.getDeclaredField("projectile");
            projField.setAccessible(true);
            String projectile = (String) projField.get(this);
            if (projectile != null) {
                tag.putString("Projectile", projectile);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void onReadAdditionalSaveData(CompoundTag tag, CallbackInfo ci) {
        if (!((Object) this instanceof FuzedClusterProjectile)) return;
        try {
            if (tag.contains("SecondaryFuzes", 9)) {
                ListTag list = tag.getList("SecondaryFuzes", 10);
                ItemStack[] fuzes = new ItemStack[list.size()];
                for (int i = 0; i < list.size(); i++) {
                    fuzes[i] = ItemStack.parseOptional(
                        ((Entity)(Object)this).level().registryAccess(),
                        list.getCompound(i)
                    );
                }
                Field fuzesField = FuzedClusterProjectile.class.getDeclaredField("secondaryFuzes");
                fuzesField.setAccessible(true);
                fuzesField.set(this, fuzes);
            }

            if (tag.contains("Projectile", 8)) {
                Field projField = FuzedClusterProjectile.class.getDeclaredField("projectile");
                projField.setAccessible(true);
                projField.set(this, tag.getString("Projectile"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
