package com.cbcatfix.client;

import com.cbcatfix.rocket.RocketGroundPlacement;
import com.cbcatfix.rocket.RocketPlacementCandidate;
import net.createmod.catnip.ghostblock.GhostBlockParams;
import net.createmod.catnip.ghostblock.GhostBlocks;
import net.createmod.catnip.placement.IPlacementHelper;
import net.createmod.catnip.placement.PlacementOffset;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.function.Predicate;

/** Create owns input discovery, indicator animation and the ghost lifecycle. */
public final class RocketPlacementHelper implements IPlacementHelper {
    private final RocketGhostRenderer renderer = new RocketGhostRenderer();

    @Override public Predicate<ItemStack> getItemPredicate() { return RocketGroundPlacement::isRocket; }
    @Override public Predicate<BlockState> getStatePredicate() { return state -> true; }

    @Override public PlacementOffset getOffset(Player player, Level level, BlockState state, BlockPos pos, BlockHitResult hit) {
        return getOffset(player, level, state, pos, hit, player.getMainHandItem());
    }

    @Override public PlacementOffset getOffset(Player player, Level level, BlockState state, BlockPos pos,
                                               BlockHitResult hit, ItemStack held) {
        var mc = Minecraft.getInstance();
        if (mc.screen != null || mc.options.hideGui) return PlacementOffset.fail();
        var candidate = RocketPlacementCandidate.resolve(level, player, hit, held);
        if (candidate == null) return PlacementOffset.fail();
        renderer.select(candidate, held);
        return PlacementOffset.success(candidate.origin()).withGhostState(candidate.state());
    }

    @Override public void renderAt(BlockPos pos, BlockState state, BlockHitResult hit, PlacementOffset offset) {
        GhostBlocks.getInstance().showGhost(this, renderer,
            GhostBlockParams.of(offset.getGhostState()).at(offset.getBlockPos()).breathingAlpha(), 1);
    }
}
