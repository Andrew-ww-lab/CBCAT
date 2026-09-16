package com.cbcatfix.rocket;

import com.cbcatfix.IMediumRocketPodBreechBlockEntity;
import com.dsvv.cbcat.cannon.medium_rocketpod.MediumRocketPodBlock;
import com.dsvv.cbcat.cannon.rocketpod.RocketPodBlock;
import com.dsvv.cbcat.cannon.rocketpod.breech.RocketPodBreechBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import rbasamoyai.createbigcannons.cannon_control.contraption.AbstractMountedCannonContraption;

/** Shared slot frame for models, fuzes, selection and launch. No second rocket state. */
public final class RocketMounts {
    public static final int SMALL_COLUMNS = 4;
    public static final int SMALL_ROWS = 3;
    public static final double SMALL_COLUMN_PITCH_PX = 3.5;
    public static final double SMALL_ROW_PITCH_PX = 4.0;
    public static final double SMALL_HOLE_WIDTH_PX = 3.0;
    public static final int SMALL_SLOT_COUNT = SMALL_COLUMNS * SMALL_ROWS;
    private RocketMounts() {}

    public static RocketBalance.Tier tier(BlockEntity breech) {
        return breech instanceof RocketPodBreechBlockEntity ? RocketBalance.Tier.SMALL
            : ((IMediumRocketPodBreechBlockEntity) breech).cbcatfix$isBigBreech()
                ? RocketBalance.Tier.BIG : RocketBalance.Tier.MEDIUM;
    }

    public static int length(BlockEntity breech) {
        if (breech instanceof MountLength cached && cached.cbcatfix$mountLength() > 0) return cached.cbcatfix$mountLength();
        if (breech.getLevel() == null) return 1;
        Direction facing = breech.getBlockState().getValue(BlockStateProperties.FACING);
        int count = 1;
        while (count < 128 && connected(breech.getLevel().getBlockState(
            breech.getBlockPos().relative(facing, count)), breech.getBlockState(), facing)) count++;
        return count;
    }

    public static int length(AbstractMountedCannonContraption cannon) {
        return length(cannon, cannon.getStartPos());
    }

    public static int length(AbstractMountedCannonContraption cannon, BlockPos start) {
        var root = cannon.getBlocks().get(start);
        if (root == null) return 1;
        Direction facing = root.state().getValue(BlockStateProperties.FACING);
        int count = 1;
        while (count <= cannon.getBlocks().size()) {
            var next = cannon.getBlocks().get(start.relative(facing, count));
            if (next == null || !connected(next.state(), root.state(), facing)) break;
            count++;
        }
        return count;
    }

    private static boolean connected(BlockState next, BlockState root, Direction facing) {
        // CBC rails are axial: reversing a rail's FACING does not shorten its tube.
        return next.hasProperty(BlockStateProperties.FACING) && next.getValue(BlockStateProperties.FACING).getAxis() == facing.getAxis()
            && (root.getBlock() instanceof RocketPodBlock ? next.getBlock() instanceof RocketPodBlock
                : next.getBlock() instanceof MediumRocketPodBlock);
    }

    public static Vec3 toLocal(rbasamoyai.createbigcannons.cannon_control.contraption.PitchOrientedContraptionEntity entity, Vec3 world) {
        return entity.toLocalVector(world, 1.0f);
    }

    public static Transform transform(BlockEntity breech, int slot) {
        RocketBalance.Tier tier = tier(breech);
        Direction facing = breech.getBlockState().getValue(BlockStateProperties.FACING);
        Vec3 forward = Vec3.atLowerCornerOf(facing.getNormal());
        Vec3 right = facing.getAxis().isHorizontal() ? new Vec3(-forward.z, 0, forward.x) : new Vec3(1, 0, 0);
        Vec3 up = forward.cross(right);
        double across = 0, vertical = 0;
        if (tier == RocketBalance.Tier.SMALL) {
            across = (slot % SMALL_COLUMNS - (SMALL_COLUMNS - 1) * 0.5) * SMALL_COLUMN_PITCH_PX / 16.0;
            vertical = (slot / SMALL_COLUMNS - (SMALL_ROWS - 1) * 0.5) * SMALL_ROW_PITCH_PX / 16.0;
        } else if (tier == RocketBalance.Tier.MEDIUM) {
            // Fixture strips occupy x=1..5 and 11..15 in the installed rail model.
            across = (slot % 2 - 0.5) * 10.0 / 16.0;
            vertical = (slot / 2 - 0.5) * 8.0 / 16.0;
        } else {
            // Horizontal rail top: 9 px; body radius: 2 px -> centre at 11 px.
            // This frame's second transverse axis points down for horizontal rails.
            vertical = -3.0 / 16.0;
        }
        double length = RocketGeometry.bodyLength(tier);
        Vec3 center = new Vec3(0.5, 0.5, 0.5).add(right.scale(across)).add(up.scale(vertical))
            .add(forward.scale((length - 1.0) * 0.5));
        return new Transform(center, forward, facing, tier, length, RocketGeometry.bodyWidth(tier));
    }

    public record Transform(Vec3 center, Vec3 forward, Direction facing, RocketBalance.Tier tier,
                            double length, double width) {
        public Vec3 nose() { return center.add(forward.scale(length * 0.5)); }
        public Vec3 rear() { return center.subtract(forward.scale(length * 0.5)); }
        public AABB bounds() {
            return AABB.ofSize(center, Math.abs(forward.x) > 0 ? length : width,
                Math.abs(forward.y) > 0 ? length : width, Math.abs(forward.z) > 0 ? length : width);
        }
    }

    public interface MountLength {
        int cbcatfix$mountLength();
        void cbcatfix$setMountLength(int length);
    }
}
