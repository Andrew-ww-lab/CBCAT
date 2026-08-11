package com.cbcatfix.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Explosion;
import rbasamoyai.createbigcannons.munitions.autocannon.flak.FlakExplosion;
import com.cbcatfix.RocketDetonationContext;

@Mixin(targets = {
    "com.dsvv.cbcat.cannon.heavy_autocannon.munitions.hef_shell.HA_HEFProjectile",
    "com.dsvv.cbcat.cannon.autocannon.munitions.flak.AutocannonFlakProjectile"
}, remap = false)
public class FlakExplosionRedirectMixin {

    @Redirect(
        method = "detonate",
        at = @At(
            value = "NEW",
            target = "rbasamoyai/createbigcannons/munitions/autocannon/flak/FlakExplosion"
        )
    )
    private FlakExplosion redirectFlakExplosion(
        Level level,
        Entity entity,
        DamageSource damageSource,
        double x,
        double y,
        double z,
        float size,
        Explosion.BlockInteraction blockInteraction
    ) {
        float finalSize = size;
        if (RocketDetonationContext.get()) {
            finalSize = size * 2.0f; // Scale rocket flak explosions by 2.0
        }
        return new FlakExplosion(level, entity, damageSource, x, y, z, finalSize, finalSize, blockInteraction);
    }
}
