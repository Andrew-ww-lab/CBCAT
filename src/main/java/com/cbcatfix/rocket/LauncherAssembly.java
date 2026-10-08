package com.cbcatfix.rocket;

import com.dsvv.cbcat.cannon.medium_rocketpod.contraption.MountedMediumRocketRailContraption;
import com.dsvv.cbcat.cannon.rocketpod.contraption.MountedRocketPodContraption;
import com.simibubi.create.content.contraptions.AssemblyException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import rbasamoyai.createbigcannons.cannon_control.contraption.AbstractMountedCannonContraption;

/** Assembly-time adapter: collect real CBCAT lanes, then let Create own transport and NBT. */
public final class LauncherAssembly {
    private LauncherAssembly() {}

    public interface Access {
        boolean cbcatfix$collectLane(Level level, BlockPos pos) throws AssemblyException;
    }

    public static int maximumLength(RocketBalance.Tier tier) {
        return switch (tier) {
            case SMALL -> com.cbcatfix.balance.BalanceDefaults.SMALL_LAUNCHER_MAX_LENGTH;
            case MEDIUM -> com.cbcatfix.balance.BalanceDefaults.MEDIUM_LAUNCHER_MAX_LENGTH;
            case BIG -> com.cbcatfix.balance.BalanceDefaults.BIG_LAUNCHER_MAX_LENGTH;
        };
    }

    private static void validateLength(AbstractMountedCannonContraption cannon, RocketBalance.Tier tier)
        throws AssemblyException {
        if (RocketMounts.length(cannon) > maximumLength(tier))
            throw new AssemblyException(Component.translatable("exception.cbcatfix.launcher.too_long", maximumLength(tier)));
    }

    public static Direction right(Direction forward) {
        return forward.getAxis().isHorizontal() ? forward.getClockWise() : Direction.EAST;
    }

    public static void attachSides(AbstractMountedCannonContraption main, Level level, BlockPos anchor)
        throws AssemblyException {
        BlockEntity breech = main.presentBlockEntities.get(main.getStartPos());
        if (!(breech instanceof MountedRocketStorage)) return;
        validateLength(main, RocketMounts.tier(breech));
        if (RocketMounts.tier(breech) == RocketBalance.Tier.BIG) return;
        BlockPos worldRoot = anchor.offset(main.getStartPos());
        Direction forward = main.initialOrientation();
        Direction right = right(forward);
        int length = RocketMounts.length(main);
        var mainStructure = SmallLauncherStructure.validate(main);
        rejectUnfinishedEnd(main, level, worldRoot);
        // Collect/validate both sides before changing the parent; a failed assembly never removes world blocks.
        var sides = new java.util.LinkedHashMap<BlockPos, AbstractMountedCannonContraption>(2);
        for (int sign : new int[] {-1, 1}) {
            BlockPos sideRoot = worldRoot.relative(right, sign);
            BlockEntity side = level.getBlockEntity(sideRoot);
            if (!(side instanceof MountedRocketStorage)) continue;
            if (RocketMounts.tier(side) != RocketMounts.tier(breech)
                || side.getBlockState().getValue(BlockStateProperties.FACING) != forward)
                throw failure("incompatible_side", sideRoot);
            if (level.getBlockEntity(worldRoot.relative(right, sign * 2)) instanceof MountedRocketStorage)
                throw failure("too_many_launchers", sideRoot);
            AbstractMountedCannonContraption lane = main instanceof MountedRocketPodContraption
                ? new MountedRocketPodContraption() : new MountedMediumRocketRailContraption();
            if (!((Access) lane).cbcatfix$collectLane(level, sideRoot)
                || !lane.getStartPos().equals(BlockPos.ZERO) || lane.initialOrientation() != forward)
                throw failure("incompatible_side", sideRoot);
            validateLength(lane, RocketMounts.tier(side));
            int sideLength = RocketMounts.length(lane);
            var sideStructure = SmallLauncherStructure.validate(lane);
            rejectUnfinishedEnd(lane, level, sideRoot);
            if ((SmallLauncherStructure.isSteel(breech.getBlockState()) || SmallLauncherStructure.isSteel(side.getBlockState()))
                && (!SmallLauncherStructure.isSteel(side.getBlockState()) || !mainStructure.equals(sideStructure)))
                throw AbstractMountedCannonContraption.invalidCannon();
            if (sideLength != lane.getBlocks().size()) throw failure("incompatible_side", sideRoot);
            if (sideLength != length)
                throw new AssemblyException(Component.translatable("exception.cbcatfix.launcher.length_mismatch", length, sideLength));
            sides.put(sideRoot.subtract(anchor), lane);
        }
        for (var entry : sides.entrySet()) {
            BlockPos offset = entry.getKey();
            var lane = entry.getValue();
            for (var info : lane.getBlocks().values()) {
                BlockPos local = info.pos().offset(offset);
                var moved = new StructureBlockInfo(local, info.state(), info.nbt());
                main.getBlocks().put(local, moved);
                if (info.nbt() != null) {
                    var be = BlockEntity.loadStatic(local, info.state(), info.nbt(), level.registryAccess());
                    if (be != null) {
                        be.setLevel(level);
                        main.presentBlockEntities.put(local, be);
                    }
                }
            }
        }
    }

    public static BlockPos findBreech(AbstractMountedCannonContraption cannon, BlockPos hit) {
        return findBreech(hit, pos -> {
            var info = cannon.getBlocks().get(pos);
            return info == null ? null : info.state();
        }, cannon.presentBlockEntities::get);
    }

    private static void rejectUnfinishedEnd(AbstractMountedCannonContraption cannon, Level level, BlockPos root)
        throws AssemblyException {
        if (!(level.getBlockState(root).getBlock() instanceof com.dsvv.cbcat.cannon.rocketpod.RocketPodBlock)) return;
        var end = root.relative(cannon.initialOrientation(), RocketMounts.length(cannon));
        if (LauncherModules.isUnfinished(level.getBlockState(end).getBlock()))
            throw AbstractMountedCannonContraption.invalidCannon();
    }

    public static BlockEntity findBreech(net.minecraft.world.level.BlockGetter level, BlockPos hit) {
        BlockPos root = findBreech(hit, level::getBlockState, level::getBlockEntity);
        return root == null ? null : level.getBlockEntity(root);
    }

    private static BlockPos findBreech(BlockPos hit,
        java.util.function.Function<BlockPos, net.minecraft.world.level.block.state.BlockState> states,
        java.util.function.Function<BlockPos, BlockEntity> entities) {
        var state = states.apply(hit);
        if (state == null || !state.hasProperty(BlockStateProperties.FACING)) return null;
        boolean small = state.getBlock() instanceof com.dsvv.cbcat.cannon.rocketpod.RocketPodBlock;
        if (!small && !(state.getBlock() instanceof com.dsvv.cbcat.cannon.medium_rocketpod.MediumRocketPodBlock)) return null;
        if (entities.apply(hit) instanceof MountedRocketStorage) return hit;
        var axis = state.getValue(BlockStateProperties.FACING).getAxis();
        for (var sign : Direction.AxisDirection.values()) {
            Direction search = Direction.get(sign, axis);
            for (int step = 1; step <= AbstractMountedCannonContraption.getMaxCannonLength(); step++) {
                BlockPos pos = hit.relative(search, step);
                var candidate = states.apply(pos);
                if (candidate == null || !candidate.hasProperty(BlockStateProperties.FACING)
                    || candidate.getValue(BlockStateProperties.FACING).getAxis() != axis
                    || (small ? !(candidate.getBlock() instanceof com.dsvv.cbcat.cannon.rocketpod.RocketPodBlock)
                        : !(candidate.getBlock() instanceof com.dsvv.cbcat.cannon.medium_rocketpod.MediumRocketPodBlock))) break;
                if (entities.apply(pos) instanceof MountedRocketStorage) {
                    if (candidate.getValue(BlockStateProperties.FACING) == search.getOpposite()) return pos;
                    break;
                }
            }
        }
        return null;
    }

    private static AssemblyException failure(String key, BlockPos pos) {
        return new AssemblyException(Component.translatable("exception.cbcatfix.launcher." + key,
            pos.getX(), pos.getY(), pos.getZ()));
    }
}
