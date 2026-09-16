package com.cbcatfix.rocket;

import net.neoforged.fml.ModList;

/** Keeps optional Sable linkage out of RocketBlockEntity when Sable is absent. */
public final class RocketSableCompat {
    private RocketSableCompat() {
    }

    static boolean tryDetachUnsupported(RocketBlockEntity blockEntity) {
        return ModList.get().isLoaded("sable") && SableRocketDetacher.tryDetachUnsupported(blockEntity);
    }

    public record LaunchFrame(net.minecraft.world.phys.Vec3 center, net.minecraft.world.phys.Vec3 forward,
                              net.minecraft.world.phys.Vec3 velocity) {
        public net.minecraft.world.phys.Vec3 inheritedVelocity() {
            return velocity.scale(com.cbcatfix.config.CbcatFixConfig.value(com.cbcatfix.config.CbcatFixConfig.INHERITED_VELOCITY_MULTIPLIER));
        }
    }

    public static boolean isSubLevel(net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos) {
        return ModList.get().isLoaded("sable") && SableAccess.isSubLevel(level, pos);
    }

    public static LaunchFrame launchFrame(net.minecraft.world.level.Level level,
        net.minecraft.world.phys.Vec3 center, net.minecraft.world.phys.Vec3 forward) {
        return ModList.get().isLoaded("sable") ? SableAccess.launchFrame(level, center, forward)
            : new LaunchFrame(center, forward, net.minecraft.world.phys.Vec3.ZERO);
    }

    private static final class SableAccess {
        private static boolean isSubLevel(net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos) {
            return dev.ryanhcode.sable.Sable.HELPER.getContaining(level, pos) != null;
        }
        private static LaunchFrame launchFrame(net.minecraft.world.level.Level level,
            net.minecraft.world.phys.Vec3 center, net.minecraft.world.phys.Vec3 forward) {
            var subLevel = dev.ryanhcode.sable.Sable.HELPER.getContaining(level, center);
            if (subLevel == null) return new LaunchFrame(center, forward, net.minecraft.world.phys.Vec3.ZERO);
            var pose = subLevel.logicalPose();
            return new LaunchFrame(pose.transformPosition(center), pose.transformNormal(forward).normalize(),
                dev.ryanhcode.sable.Sable.HELPER.getVelocity(level, subLevel, center).scale(1.0 / 20.0));
        }
    }
}
