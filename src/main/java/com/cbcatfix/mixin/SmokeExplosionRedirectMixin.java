package com.cbcatfix.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import rbasamoyai.createbigcannons.munitions.big_cannon.smoke_shell.SmokeExplosion;

@Mixin(targets = "com.dsvv.cbcat.cannon.heavy_autocannon.munitions.smoke_shell.HA_SmokeProjectile", remap = false)
public class SmokeExplosionRedirectMixin {

    @Redirect(
        method = "detonate",
        at = @At(
            value = "NEW",
            target = "rbasamoyai/createbigcannons/munitions/big_cannon/smoke_shell/SmokeExplosion"
        )
    )
    private SmokeExplosion redirectSmokeExplosion(
        Level level,
        Entity entity,
        double x,
        double y,
        double z,
        float size,
        Explosion.BlockInteraction blockInteraction
    ) {
        return new SmokeExplosion(level, entity, x, y, z, size, size, blockInteraction);
    }
}
