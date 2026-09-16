package com.cbcatfix.rocket;

import com.cbcatfix.CbcatFix;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.SubLevelAssemblyHelper;
import dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle;
import dev.ryanhcode.sable.companion.math.BoundingBox3i;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaterniond;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

/** Optional Sable adapter: move one last slot per tick, keeping the remaining stack intact. */
final class SableRocketDetacher {
    private SableRocketDetacher() {}

    static boolean tryDetachUnsupported(RocketBlockEntity source) {
        if (source.isIndependent() || source.splitFailed() || source.isRemoved()
            || !(source.getLevel() instanceof ServerLevel level)
            || level.getBlockEntity(source.getBlockPos()) != source) return false;
        BlockPos root = source.getBlockPos();
        if (!level.getBlockState(root.below()).isAir()
            || !(Sable.HELPER.getContaining(level, root) instanceof ServerSubLevel parent)) return false;
        List<ItemStack> original = source.copyRockets();
        if (original.isEmpty()) return false;

        var state = source.getBlockState();
        var tier = RocketBlock.tier(state);
        var facing = RocketGeometry.horizontalFacing(state.getValue(RocketBlock.FACING));
        int last = original.size() - 1;
        ItemStack moving = original.get(last);
        var remaining = new ArrayList<>(original.subList(0, last));
        var orientation = new Quaterniond(parent.logicalPose().orientation());
        var offset = RocketGeometry.slotCrossOffset(tier, facing, last);
        var worldOffset = orientation.transform(new Vector3d(offset.x, offset.y, offset.z));
        var angular = RigidBodyHandle.of(parent).getAngularVelocity(new Vector3d());
        ServerSubLevel detached = null;
        boolean transferred = false;
        boolean prepared = false;
        try {
            // The complete one-rocket footprint moves through Sable's native block
            // transfer (including source BE invalidation), not root-only + later proxies.
            level.setBlock(root, state.setValue(RocketBlock.INDEPENDENT, true), 11);
            source.replaceWithSingleRocket(moving, true);
            prepared = true;
            RocketFootprint.place(level, root, facing, tier);
            var positions = new ArrayList<BlockPos>();
            for (int i = 0; i < (int) RocketGeometry.bodyLength(tier); i++) positions.add(root.relative(facing, i));
            BlockPos end = positions.getLast();
            var bounds = new BoundingBox3i(Math.min(root.getX(),end.getX()), root.getY(), Math.min(root.getZ(),end.getZ()),
                Math.max(root.getX(),end.getX()), root.getY(), Math.max(root.getZ(),end.getZ()));
            detached = SubLevelAssemblyHelper.assembleBlocks(level, root, positions, bounds);
            BlockPos destination = detached.getPlot().getCenterBlock();
            if (!(level.getBlockEntity(destination) instanceof RocketBlockEntity target)
                || !ItemStack.matches(target.getPrimaryRocket(), moving))
                throw new IllegalStateException("Sable did not restore the moved rocket at " + destination);
            transferred = true;
            // Sable supplies the parent pose/velocities; add only the slot displacement.
            var pose = detached.logicalPose();
            var handle = RigidBodyHandle.of(detached);
            handle.teleport(new Vector3d(pose.position()).add(worldOffset), pose.orientation());
            handle.addLinearAndAngularVelocity(new Vector3d(angular).cross(worldOffset), new Vector3d());
            return true;
        } catch (RuntimeException | LinkageError failure) {
            // Native move can fail after copying but before removing the source.
            // If a destination owns the round, never put that round back into the source.
            if (detached != null && level.getBlockEntity(detached.getPlot().getCenterBlock()) instanceof RocketBlockEntity target)
                transferred = ItemStack.matches(target.getPrimaryRocket(), moving);
            if (!transferred && (detached != null || level.getBlockEntity(root) == source)) remaining.add(moving);
            CbcatFix.LOGGER.error("Rocket split failed at {}; remaining stack retained, automatic retry disabled", root, failure);
            return true;
        } finally {
            if (prepared) {
                if (!remaining.isEmpty()) {
                    level.setBlock(root, state.setValue(RocketBlock.INDEPENDENT, false), 11);
                    if (level.getBlockEntity(root) instanceof RocketBlockEntity restored) {
                        restored.replaceRockets(remaining, false);
                        if (!transferred) restored.stopSplittingAfterFailure();
                        RocketFootprint.place(level, root, facing, tier);
                    } else {
                        // Do not silently erase the untransferred remainder if a foreign hook removed the BE.
                        for (ItemStack item : remaining) net.minecraft.world.level.block.Block.popResource(level, root, item);
                    }
                } else if (transferred && level.getBlockEntity(root) == source) {
                    // A partially completed native move left its invalidated old owner behind.
                    source.replaceRockets(List.of(), true);
                    level.removeBlock(root, false);
                }
            }
        }
    }
}
