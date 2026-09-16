package com.cbcatfix.rocket;

import com.cbcatfix.munitions.CbcatFixMunitions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

final class RocketFootprint {
    private RocketFootprint() {
    }

    static boolean canPlace(Level level, BlockPos root, Direction facing, RocketBalance.Tier tier) {
        if (!facing.getAxis().isHorizontal()) {
            return false;
        }
        for (int segment = 1; segment < (int) RocketGeometry.bodyLength(tier); segment++) {
            BlockPos position = root.relative(facing, segment);
            BlockState state = level.getBlockState(position);
            if (state.is(CbcatFixMunitions.ROCKET_BODY_EXTENSION.get())) {
                boolean belongsToThisRocket = RocketTargetResolver.findRoot(level, position)
                    .map(existing -> existing.pos().equals(root))
                    .orElse(false);
                if (!belongsToThisRocket) {
                    return false;
                }
            } else if (!state.canBeReplaced()) {
                return false;
            }
        }
        return true;
    }

    static void place(Level level, BlockPos root, Direction facing, RocketBalance.Tier tier) {
        BlockState extension = CbcatFixMunitions.ROCKET_BODY_EXTENSION.get().defaultBlockState()
            .setValue(RocketBodyExtensionBlock.FACING, facing)
            .setValue(RocketBodyExtensionBlock.TIER, tier.ordinal())
            .setValue(
                RocketBodyExtensionBlock.INDEPENDENT,
                level.getBlockState(root).hasProperty(RocketBlock.INDEPENDENT)
                    && level.getBlockState(root).getValue(RocketBlock.INDEPENDENT)
            );
        for (int segment = 1; segment < (int) RocketGeometry.bodyLength(tier); segment++) {
            level.setBlock(root.relative(facing, segment), extension, 11);
        }
    }

    static void remove(Level level, BlockPos root, Direction facing, RocketBalance.Tier tier) {
        for (int segment = 1; segment < (int) RocketGeometry.bodyLength(tier); segment++) {
            BlockPos position = root.relative(facing, segment);
            if (level.getBlockState(position).is(CbcatFixMunitions.ROCKET_BODY_EXTENSION.get())) {
                level.removeBlock(position, false);
            }
        }
    }
}
