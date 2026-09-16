package com.cbcatfix.mixin;

import com.cbcatfix.RocketDetonationContext;
import com.cbcatfix.munitions.HeatEffect;
import com.cbcatfix.config.CbcatFixConfig;
import com.cbcatfix.rocket.RocketBalance;
import net.minecraft.core.Position;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rbasamoyai.createbigcannons.munitions.AbstractCannonProjectile;

@Mixin(targets = "com.dsvv.cbcat.cannon.heavy_autocannon.munitions.heat_shell.HA_HEATProjectile", remap = false)
public class HeavyAutocannonHeatMixin {
    @Inject(method = "detonate", at = @At("HEAD"), cancellable = true)
    private void cbcatfix$useCbcArmorAwareHeatJet(Position position, CallbackInfo ci) {
        AbstractCannonProjectile projectile = (AbstractCannonProjectile) (Object) this;
        Vec3 direction = RocketDetonationContext.get()
            ? RocketDetonationContext.direction()
            : projectile.getOrientation();
        CbcatFixConfig.HeatSettings settings = RocketDetonationContext.tier() == RocketBalance.Tier.MEDIUM
            ? CbcatFixConfig.MEDIUM_ROCKET_HEAT
            : CbcatFixConfig.HEAVY_AUTOCANNON_HEAT;
        HeatEffect.detonate(
            projectile,
            position,
            direction,
            RocketDetonationContext.scale(),
            settings
        );
        ci.cancel();
    }
}
