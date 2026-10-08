package com.cbcatfix.audit;

import com.cbcatfix.rocket.RadarTraverseContext;
import com.happysg.radar.compat.cbc.DirectCbcMountMotion;

/** Loaded only in the optional runtime; validates the installed Radars algorithm after weaving. */
final class OptionalTraverseChecks {
    static int run() throws Exception {
        Class.forName("com.happysg.radar.block.controller.pitch.CannonMountPitch");
        Class.forName("com.happysg.radar.block.controller.yaw.CannonMountYaw");
        Class.forName("com.happysg.radar.block.behavior.networks.WeaponFiringControl");
        close(DirectCbcMountMotion.maxSpeedForRpm(120), 5, "ordinary cap");
        try {
            RadarTraverseContext.MULTIPLIER.set(1.5);
            close(DirectCbcMountMotion.maxSpeedForRpm(120), 7.5, "Traverse cap");
            close(DirectCbcMountMotion.maxSpeedForRpm(12), 1, "low RPM remains native");
            close(DirectCbcMountMotion.maxSpeedForRpm(0), 0, "no drive");
            var state = new DirectCbcMountMotion.State();
            for (int i = 1; i <= 20; i++) close(state.nextStep(180, 120), Math.min(i * .5, 7.5), "native acceleration and capped speed");
            close(state.nextStep(.1, 120), .1, "no target overshoot");
            close(state.nextStep(180, 0), 0, "drive loss stops");
        } finally { RadarTraverseContext.MULTIPLIER.set(1.0); }
        close(DirectCbcMountMotion.maxSpeedForRpm(120), 5, "scope restored");
        System.out.println("CBCAT RADARS TRAVERSE: 27 assertions passed; both controller mixins loaded");
        return 27;
    }
    private static void close(double actual, double expected, String label) {
        if (Math.abs(actual - expected) > 1e-8) throw new AssertionError(label + ": " + actual);
    }
}
