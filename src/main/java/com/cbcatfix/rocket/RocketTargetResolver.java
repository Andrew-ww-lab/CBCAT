package com.cbcatfix.rocket;

import com.cbcatfix.munitions.CbcatFixMunitions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.BlockHitResult;

import java.util.Optional;

/** Resolves any occupied rocket segment to its root block and individual stored rocket. */
public final class RocketTargetResolver {
    private RocketTargetResolver() {
    }

    public static Optional<Root> findRoot(BlockGetter level, BlockPos hitPos) {
        BlockState hitState = level.getBlockState(hitPos);
        // Sable's collider bakery uses a state-only BlockGetter. It is allowed to
        // return null for neighbouring states and block entities.
        if (hitState == null) {
            return Optional.empty();
        }
        if (hitState.is(CbcatFixMunitions.ROCKET_BLOCK.get())
            && level.getBlockEntity(hitPos) instanceof RocketBlockEntity blockEntity) {
            return Optional.of(new Root(hitPos, hitState, blockEntity));
        }
        if (!hitState.is(CbcatFixMunitions.ROCKET_BODY_EXTENSION.get())) {
            return Optional.empty();
        }

        RocketBalance.Tier tier = RocketBalance.Tier.values()[Math.clamp(
            hitState.getValue(RocketBodyExtensionBlock.TIER), 0, RocketBalance.Tier.values().length - 1
        )];
        Direction facing = RocketGeometry.horizontalFacing(hitState.getValue(RocketBodyExtensionBlock.FACING));
        int occupiedBlocks = (int) RocketGeometry.bodyLength(tier);
        for (int distance = 1; distance < occupiedBlocks; distance++) {
            BlockPos candidate = hitPos.relative(facing.getOpposite(), distance);
            BlockState candidateState = level.getBlockState(candidate);
            if (candidateState != null
                && candidateState.is(CbcatFixMunitions.ROCKET_BLOCK.get())
                && RocketBlock.tier(candidateState) == tier
                && RocketGeometry.horizontalFacing(candidateState.getValue(RocketBlock.FACING)) == facing
                && level.getBlockEntity(candidate) instanceof RocketBlockEntity blockEntity) {
                return Optional.of(new Root(candidate, candidateState, blockEntity));
            }
        }
        return Optional.empty();
    }

    public static Optional<Target> resolve(BlockGetter level, BlockPos hitPos, Vec3 worldHitLocation) {
        return findRoot(level, hitPos).flatMap(root -> {
            int count = root.blockEntity().getRockets().size();
            if (count == 0) {
                return Optional.empty();
            }
            RocketBalance.Tier tier = RocketBlock.tier(root.state());
            Direction facing = RocketGeometry.horizontalFacing(root.state().getValue(RocketBlock.FACING));
            Vec3 localHit = worldHitLocation.subtract(
                root.pos().getX(), root.pos().getY(), root.pos().getZ()
            );
            int slot = RocketGeometry.closestSlot(tier, facing, count, localHit);
            return Optional.of(new Target(root, slot, root.blockEntity().getRockets().get(slot), localHit));
        });
    }

    /**
     * Resolves a block interaction through the same view-ray path used by the
     * outline and Jade. The hit-location fallback is retained for non-player
     * callers such as deployers.
     */
    public static Optional<Target> resolveInteraction(
        BlockGetter level, BlockHitResult hit, Entity viewer
    ) {
        Optional<Target> viewed = resolveViewed(level, hit.getBlockPos(), viewer);
        return viewed.isPresent() ? viewed : resolve(level, hit.getBlockPos(), hit.getLocation());
    }

    /** Resolves the individual rocket selected by an entity's view ray. */
    public static Optional<Target> resolveViewed(BlockGetter level, BlockPos hitPos, Entity viewer) {
        return findRoot(level, hitPos).flatMap(root -> {
            int count = root.blockEntity().getRockets().size();
            if (count == 0) {
                return Optional.empty();
            }
            RocketBalance.Tier tier = RocketBlock.tier(root.state());
            Direction facing = RocketGeometry.horizontalFacing(root.state().getValue(RocketBlock.FACING));
            Vec3 eye = viewer.getEyePosition();
            double reach = viewer instanceof net.minecraft.world.entity.player.Player player
                ? player.blockInteractionRange() + RocketGeometry.bodyLength(tier)
                : 8.0 + RocketGeometry.bodyLength(tier);
            var ray = RocketViewRay.inBlockSpace(viewer.level(), root.pos(), eye,
                eye.add(viewer.getViewVector(1.0f).scale(reach)));
            Vec3 base = Vec3.atLowerCornerOf(root.pos());
            Vec3 localStart = ray.start().subtract(base);
            Vec3 localEnd = ray.end().subtract(base);
            return RocketGeometry.raycastSlot(
                tier, facing, count, root.state().getValue(RocketBlock.INDEPENDENT), localStart, localEnd
            ).map(hit -> target(root, hit));
        });
    }

    private static Target target(Root root, RocketGeometry.RaycastSlot hit) {
        return new Target(
            root, hit.slot(), root.blockEntity().getRockets().get(hit.slot()), hit.location()
        );
    }

    public record Root(BlockPos pos, BlockState state, RocketBlockEntity blockEntity) {
    }

    public record Target(Root root, int slot, ItemStack rocket, Vec3 localHit) {
    }
}
