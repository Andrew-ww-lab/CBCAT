package com.cbcatfix.mixin;

import com.cbcatfix.rocket.RadarTraverseContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Constant;

@Pseudo
@Mixin(targets = "com.happysg.radar.compat.cbc.DirectCbcMountMotion", remap = false)
public abstract class RadarTraverseMotionMixin {
    @ModifyConstant(method = "maxSpeedForRpm", constant = @Constant(doubleValue = 5.0))
    private static double cbcatfix$maximumTraverseSpeed(double cap) {
        // Approved Radars adapter changes only its cap: low RPM, acceleration and braking stay native.
        return cap * RadarTraverseContext.MULTIPLIER.get();
    }
}
