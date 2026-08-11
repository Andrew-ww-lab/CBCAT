package com.cbcatfix.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.world.entity.Entity;
import net.minecraft.sounds.SoundSource;
import com.cbcatfix.CbcatFix;
import com.cbcatfix.munitions.BigHERocketProjectile;
import com.cbcatfix.munitions.BigAPRocketProjectile;
import com.cbcatfix.munitions.BigHEATRocketProjectile;

@Mixin(targets = {
    "com.dsvv.cbcat.cannon.rocketpod.munitions.AbstractRocket",
    "com.dsvv.cbcat.cannon.medium_rocketpod.munitions.AbstractMediumRocket"
}, remap = false)
public class RocketLaunchSoundMixin {

    @Inject(method = "shoot", at = @At("TAIL"))
    private void onShoot(double x, double y, double z, float speed, float inaccuracy, CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        if (!entity.level().isClientSide()) {
            float pitch = 1.0f;
            float volume = 2.0f;
            
            if (entity instanceof BigHERocketProjectile || entity instanceof BigAPRocketProjectile || entity instanceof BigHEATRocketProjectile) {
                pitch = 0.55f;
                volume = 3.0f; // More bass, more volume for the massive rocket!
            } else if (entity.getClass().getName().contains(".rocketpod.") && !entity.getClass().getName().contains("medium_rocketpod")) {
                pitch = 1.5f;
                volume = 1.5f;
            }

            entity.level().playSound(
                null,
                entity.getX(),
                entity.getY(),
                entity.getZ(),
                CbcatFix.MISSILE_LAUNCH.get(),
                SoundSource.PLAYERS,
                volume,
                pitch
            );
        }
    }
}
