package com.cbcatfix.rocket;

import com.cbcatfix.munitions.CbcatFixMunitions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/** Read-only candidates over the existing insertion rules and model geometry. */
public record RocketPlacementCandidate(BlockPos origin, BlockState state, RocketBalance.Tier tier,
                                       Direction facing, Vec3 center) {
    public RocketPlacementCandidate {
        origin = origin.immutable();
    }

    public static RocketPlacementCandidate resolve(Level level, Player player, BlockHitResult hit, ItemStack held) {
        if (held.isEmpty() || !RocketGroundPlacement.isRocket(held) || player.isSpectator()
            || player.isShiftKeyDown() || hit.getType() != net.minecraft.world.phys.HitResult.Type.BLOCK) return null;
        // RightClickBlock handles launchers before item use; never fall through to ground placement.
        BlockEntity breech = MountedRocketInteraction.findBreech(level, hit.getBlockPos());
        if (breech != null) {
            if (held != player.getMainHandItem()) return null;
            return launcher(breech, MountedRocketInteraction.viewed(breech, player, true), held);
        }
        var root = RocketTargetResolver.findRoot(level, hit.getBlockPos());
        return root.map(existing -> pile(existing, held)).orElse(null);
    }

    public static RocketPlacementCandidate launcher(BlockEntity breech, MountedRocketInteraction.Selection selection, ItemStack held) {
        if (selection == null || held.isEmpty()) return null;
        ItemStack remainder = new MountedRocketItemHandler(breech).insertItem(selection.slot(), held, true);
        if (remainder.getCount() == held.getCount()) return null;
        var mount = selection.mount();
        BlockState modelState = CbcatFixMunitions.ROCKET_BLOCK.get().defaultBlockState()
            .setValue(RocketBlock.TIER, mount.tier().ordinal()).setValue(RocketBlock.FACING, mount.facing());
        return new RocketPlacementCandidate(breech.getBlockPos(), modelState,
            mount.tier(), mount.facing(), mount.center());
    }

    public static RocketPlacementCandidate pile(RocketTargetResolver.Root root, ItemStack held) {
        if (held.isEmpty() || !RocketGroundPlacement.isRocket(held)) return null;
        RocketBalance.Tier tier = RocketBlock.tier(root.state());
        if (RocketBalance.tierForRocketItem(held.getItem()) != tier || !root.blockEntity().canAddRocket(held)) return null;
        Direction facing = RocketGeometry.horizontalFacing(root.state().getValue(RocketBlock.FACING));
        return new RocketPlacementCandidate(root.pos(), root.state(), tier, facing,
            RocketGeometry.bodyCenter(tier, facing, root.blockEntity().getRockets().size(), root.blockEntity().isIndependent()));
    }
}
