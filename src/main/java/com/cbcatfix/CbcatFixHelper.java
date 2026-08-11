package com.cbcatfix;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import rbasamoyai.createbigcannons.cannon_control.contraption.AbstractMountedCannonContraption;
import com.dsvv.cbcat.cannon.medium_rocketpod.IMediumRocketPodBlockEntity;
import com.dsvv.cbcat.registry.BlockRegister;

public class CbcatFixHelper {
    public static int getInWorldRailLength(BlockEntity breechBE) {
        Level level = breechBE.getLevel();
        if (level == null) return 3;
        BlockPos pos = breechBE.getBlockPos();
        BlockState state = breechBE.getBlockState();
        if (level.getBlockState(pos).getBlock() != state.getBlock()) {
            return 3;
        }
        if (!state.hasProperty(BlockStateProperties.FACING)) return 0;
        Direction dir = state.getValue(BlockStateProperties.FACING);
        int length = 0;
        BlockPos current = pos.relative(dir);
        while (true) {
            BlockState nextState = level.getBlockState(current);
            if (nextState.getBlock() == BlockRegister.WROUGHT_IRON_MEDIUM_ROCKET_RAIL.get()) {
                length++;
                current = current.relative(dir);
            } else {
                break;
            }
        }
        if (length == 0) {
            Direction opp = dir.getOpposite();
            current = pos.relative(opp);
            while (true) {
                BlockState nextState = level.getBlockState(current);
                if (nextState.getBlock() == BlockRegister.WROUGHT_IRON_MEDIUM_ROCKET_RAIL.get()) {
                    length++;
                    current = current.relative(opp);
                } else {
                    break;
                }
            }
        }
        return length;
    }

    public static int getInWorldSpecificRailLength(BlockEntity breechBE, Block railBlock) {
        Level level = breechBE.getLevel();
        if (level == null) return 0;
        BlockPos pos = breechBE.getBlockPos();
        BlockState state = breechBE.getBlockState();
        if (!state.hasProperty(BlockStateProperties.FACING)) return 0;
        Direction dir = state.getValue(BlockStateProperties.FACING);
        int length = 0;
        BlockPos current = pos.relative(dir);
        while (true) {
            BlockState nextState = level.getBlockState(current);
            if (nextState.getBlock() == railBlock) {
                length++;
                current = current.relative(dir);
            } else {
                break;
            }
        }
        if (length == 0) {
            Direction opp = dir.getOpposite();
            current = pos.relative(opp);
            while (true) {
                BlockState nextState = level.getBlockState(current);
                if (nextState.getBlock() == railBlock) {
                    length++;
                    current = current.relative(opp);
                } else {
                    break;
                }
            }
        }
        return length;
    }

    public static int getContraptionRailLength(AbstractMountedCannonContraption contraption) {
        BlockPos start = contraption.getStartPos();
        Direction dir = contraption.initialOrientation();
        int length = 0;
        BlockPos current = start.relative(dir);
        while (true) {
            BlockEntity be = contraption.presentBlockEntities.get(current);
            if (be instanceof IMediumRocketPodBlockEntity) {
                length++;
                current = current.relative(dir);
            } else {
                break;
            }
        }
        return length;
    }

    public static void spawnBigRocketParticles(com.dsvv.cbcat.cannon.medium_rocketpod.munitions.AbstractMediumRocket<?> rocket) {
        if (rocket.level().isClientSide() && !rocket.isInGround()) {
            net.minecraft.world.phys.Vec3 motion = rocket.getDeltaMovement();
            net.minecraft.world.phys.Vec3 pos = rocket.position();

            double bx = pos.x - motion.x * 0.5;
            double by = pos.y - motion.y * 0.5;
            double bz = pos.z - motion.z * 0.5;

            rocket.level().addParticle(net.minecraft.core.particles.ParticleTypes.CAMPFIRE_COSY_SMOKE,
                bx, by, bz,
                -motion.x * 0.05, -motion.y * 0.05 + 0.02, -motion.z * 0.05);
            rocket.level().addParticle(net.minecraft.core.particles.ParticleTypes.FLAME,
                bx, by, bz,
                -motion.x * 0.1, -motion.y * 0.1, -motion.z * 0.1);
        }
    }
}
