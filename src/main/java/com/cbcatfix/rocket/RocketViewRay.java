package com.cbcatfix.rocket;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;

/** Shared input bridge for placed and mounted rockets; Sable owns the rigid-body pose. */
public record RocketViewRay(Vec3 start, Vec3 end) {
    public static RocketViewRay inBlockSpace(Level level, BlockPos pos, Vec3 start, Vec3 end) {
        return ModList.get().isLoaded("sable") ? SableAccess.convert(level, pos, start, end)
            : new RocketViewRay(start, end);
    }

    private static class SableAccess {
        private static RocketViewRay convert(Level level, BlockPos pos, Vec3 start, Vec3 end) {
            var subLevel = dev.ryanhcode.sable.Sable.HELPER.getContaining(level, pos);
            if (subLevel == null) return new RocketViewRay(start, end);
            var pose = level instanceof dev.ryanhcode.sable.mixinterface.clip_overwrite.LevelPoseProviderExtension provider
                ? provider.sable$getPose(subLevel) : subLevel.logicalPose();
            return new RocketViewRay(pose.transformPositionInverse(start), pose.transformPositionInverse(end));
        }
    }
}
