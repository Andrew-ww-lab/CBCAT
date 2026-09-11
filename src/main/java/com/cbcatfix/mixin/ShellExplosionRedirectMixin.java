package com.cbcatfix.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Explosion;
import rbasamoyai.createbigcannons.munitions.ShellExplosion;
import com.cbcatfix.RocketDetonationContext;

@Mixin(targets = {
    "com.dsvv.cbcat.cannon.autocannon.munitions.he.AutocannonHEProjectile",
    "com.dsvv.cbcat.cannon.autocannon.munitions.hei.AutocannonHEIProjectile",
    "com.dsvv.cbcat.cannon.heavy_autocannon.munitions.heat_shell.HA_HEATProjectile",
    "com.dsvv.cbcat.cannon.heavy_autocannon.munitions.he_shell.HA_HEProjectile"
}, remap = false)
public class ShellExplosionRedirectMixin {

    @Redirect(
        method = "detonate",
        at = @At(
            value = "NEW",
            target = "rbasamoyai/createbigcannons/munitions/ShellExplosion"
        )
    )
    private ShellExplosion redirectShellExplosion(
        Level level,
        Entity entity,
        DamageSource damageSource,
        double x,
        double y,
        double z,
        float size,
        boolean fire,
        Explosion.BlockInteraction blockInteraction
    ) {
        float finalSize = size;
        if (RocketDetonationContext.get()) {
            finalSize = size * RocketDetonationContext.scale();
        }
        return new ShellExplosion(level, entity, damageSource, x, y, z, finalSize, finalSize, fire, blockInteraction);
    }
}
