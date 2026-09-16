package com.cbcatfix.mixin;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** The projectile join event supplies ignition for rails and accidental ground launches alike. */
@Mixin(targets = {
    "com.dsvv.cbcat.cannon.rocketpod.contraption.MountedRocketPodContraption",
    "com.dsvv.cbcat.cannon.medium_rocketpod.contraption.MountedMediumRocketRailContraption"
}, remap = false)
public class RocketIgnitionSoundMixin {
    @Redirect(method = "fireShot", at = @At(value = "INVOKE",
        target = "Lrbasamoyai/createbigcannons/utils/CBCUtils;playBlastLikeSoundOnServer(Lnet/minecraft/server/level/ServerLevel;DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FFF)V"))
    private void cbcatfix$replaceLauncherBlast(ServerLevel level, double x, double y, double z,
        SoundEvent sound, SoundSource source, float volume, float pitch, float airAbsorption) {
        // Do not overlay the old autocannon blast on the one-shot rocket ignition.
    }
}
