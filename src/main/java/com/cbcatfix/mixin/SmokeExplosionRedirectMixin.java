package com.cbcatfix.mixin;

import net.minecraft.core.Position;
import com.cbcatfix.munitions.CbcatSmokeCompat;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Mixin;
import rbasamoyai.createbigcannons.munitions.AbstractCannonProjectile;

@Mixin(targets = "com.dsvv.cbcat.cannon.heavy_autocannon.munitions.smoke_shell.HA_SmokeProjectile", remap = false)
public class SmokeExplosionRedirectMixin {

    /**
     * CBC AT 0.1.4c links against the removed one-power SmokeExplosion constructor.
     * Replacing the complete method prevents the JVM from ever resolving that stale call.
     */
    @Overwrite
    protected void detonate(Position position) {
        CbcatSmokeCompat.detonate(
            (AbstractCannonProjectile) (Object) this,
            position,
            com.cbcatfix.RocketDetonationContext.get()
                ? com.cbcatfix.RocketDetonationContext.scale()
                : 1.0f
        );
    }
}
