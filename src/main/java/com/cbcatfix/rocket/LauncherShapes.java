package com.cbcatfix.rocket;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Quaterniond;
import org.joml.Vector3d;

/** Model-space solids, cached in all six blockstate rotations. Channels are real voids. */
public final class LauncherShapes {
    private static final VoxelShape[][][] SHAPES = new VoxelShape[3][2][6];
    static {
        for (RocketBalance.Tier tier : RocketBalance.Tier.values()) {
            for (int breech = 0; breech < 2; breech++) {
                VoxelShape source = canonical(tier, breech != 0);
                for (Direction facing : Direction.values())
                    SHAPES[tier.ordinal()][breech][facing.ordinal()] = rotate(source, tier, facing);
            }
        }
    }
    private LauncherShapes() {}

    public static VoxelShape get(RocketBalance.Tier tier, boolean breech, Direction facing) {
        return SHAPES[tier.ordinal()][breech ? 1 : 0][facing.ordinal()];
    }

    private static VoxelShape canonical(RocketBalance.Tier tier, boolean breech) {
        if (tier == RocketBalance.Tier.SMALL) {
            VoxelShape shape = Block.box(0, 0, 1.5, 16, 16, 14.5);
            double half = RocketMounts.SMALL_HOLE_WIDTH_PX / 2;
            for (int row = 0; row < RocketMounts.SMALL_ROWS; row++) {
                for (int column = 0; column < RocketMounts.SMALL_COLUMNS; column++) {
                    double x = 8 + (column - 1.5) * RocketMounts.SMALL_COLUMN_PITCH_PX;
                    double z = 8 + (row - 1) * RocketMounts.SMALL_ROW_PITCH_PX;
                    shape = Shapes.join(shape, Block.box(x-half, 0, z-half, x+half, 16, z+half), BooleanOp.ONLY_FIRST);
                }
            }
            return shape.optimize();
        }
        double[][] boxes = tier == RocketBalance.Tier.BIG ? new double[][] {
            {1,0,7,5,16,9}, {11,0,7,15,16,9}, {5,3,8,11,5,10}, {5,11,8,11,13,10}
        } : new double[][] {
            {1,0,0,5,16,2}, {1,0,8,5,16,10}, {11,0,0,15,16,2}, {11,0,8,15,16,10},
            {5,3,0,11,5,2}, {5,3,8,11,5,10}, {5,11,0,11,13,2}, {5,11,8,11,13,10},
            {7,3,2,9,5,8}, {7,11,2,9,13,8}
        };
        VoxelShape result = Shapes.empty();
        for (double[] box : boxes) result = Shapes.or(result, box(box));
        if (breech) {
            double[][] catches = tier == RocketBalance.Tier.BIG ? new double[][] {
                {3.25,6,6,5.25,10,8}, {10.75,6,6,12.75,10,8}
            } : new double[][] {
                {2,6,2,4,10,4}, {2,6,10,4,10,12}, {12,6,2,14,10,4}, {12,6,10,14,10,12}
            };
            for (double[] box : catches) result = Shapes.or(result, box(box));
        }
        return result.optimize();
    }

    private static VoxelShape box(double[] b) { return Block.box(b[0],b[1],b[2],b[3],b[4],b[5]); }

    private static VoxelShape rotate(VoxelShape source, RocketBalance.Tier tier, Direction facing) {
        int x = facing == Direction.UP ? 0 : facing == Direction.DOWN ? 180
            : tier != RocketBalance.Tier.SMALL || facing == Direction.SOUTH || facing == Direction.WEST ? 270 : 90;
        int y = facing.getAxis() == Direction.Axis.X ? 90 : 0;
        Quaterniond rotation = new Quaterniond().rotationY(Math.toRadians(-y)).rotateX(Math.toRadians(-x));
        VoxelShape result = Shapes.empty();
        for (AABB box : source.toAabbs()) {
            Vector3d min = new Vector3d(Double.POSITIVE_INFINITY), max = new Vector3d(Double.NEGATIVE_INFINITY);
            for (int corner = 0; corner < 8; corner++) {
                Vector3d v = new Vector3d((corner & 1) == 0 ? box.minX : box.maxX,
                    (corner & 2) == 0 ? box.minY : box.maxY, (corner & 4) == 0 ? box.minZ : box.maxZ);
                rotation.transform(v.sub(.5,.5,.5)).add(.5,.5,.5);
                v.set(Math.rint(v.x*64)/64, Math.rint(v.y*64)/64, Math.rint(v.z*64)/64);
                min.min(v); max.max(v);
            }
            result = Shapes.or(result, Shapes.box(min.x,min.y,min.z,max.x,max.y,max.z));
        }
        return result.optimize();
    }
}
