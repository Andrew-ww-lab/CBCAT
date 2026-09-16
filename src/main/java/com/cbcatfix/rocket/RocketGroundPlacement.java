package com.cbcatfix.rocket;

import com.cbcatfix.munitions.CbcatFixMunitions;
import com.dsvv.cbcat.cannon.medium_rocketpod.munitions.AbstractMediumRocketItem;
import com.dsvv.cbcat.cannon.rocketpod.munitions.AbstractRocketItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Converts CBCAT's non-BlockItem rocket items into the real RocketBlock. All
 * behavior after this one-time item/block conversion is vanilla/CBC behavior.
 */
public final class RocketGroundPlacement {
    private RocketGroundPlacement() {
    }

    public static InteractionResult place(UseOnContext context) {
        ItemStack held = context.getItemInHand();
        if (!isRocket(held)) {
            return InteractionResult.PASS;
        }

        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        java.util.Optional<RocketTargetResolver.Root> existing = RocketTargetResolver.findRoot(level, clickedPos);
        if (existing.isPresent()) {
            return addToExisting(level, existing.get().pos(), held, context.getPlayer());
        }
        if (context.getClickedFace() != Direction.UP) {
            return InteractionResult.FAIL;
        }

        Direction facing = context.getHorizontalDirection();
        BlockPos placementPos = clickedPos.above();
        if (!level.getBlockState(placementPos).canBeReplaced()) {
            return InteractionResult.FAIL;
        }

        RocketBalance.Tier tier = RocketBalance.tierForRocketItem(held.getItem());
        if (!RocketFootprint.canPlace(level, placementPos, facing, tier)) {
            return InteractionResult.FAIL;
        }
        BlockState rocketState = CbcatFixMunitions.ROCKET_BLOCK.get().defaultBlockState()
            .setValue(RocketBlock.FACING, facing)
            .setValue(RocketBlock.TIER, tier.ordinal());

        if (!level.isClientSide()) {
            if (!level.setBlock(placementPos, rocketState, 11)) {
                return InteractionResult.FAIL;
            }
            if (!(level.getBlockEntity(placementPos) instanceof RocketBlockEntity rocketBlockEntity)
                || !rocketBlockEntity.addRocket(held)) {
                level.removeBlock(placementPos, false);
                return InteractionResult.FAIL;
            }
            RocketFootprint.place(level, placementPos, facing, tier);
            consumeOne(held, context.getPlayer());
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    private static InteractionResult addToExisting(Level level, BlockPos pos, ItemStack held, Player player) {
        BlockState state = level.getBlockState(pos);
        RocketBalance.Tier heldTier = RocketBalance.tierForRocketItem(held.getItem());
        if (RocketBlock.tier(state) != heldTier) {
            return InteractionResult.FAIL;
        }
        if (!level.isClientSide()) {
            if (!(level.getBlockEntity(pos) instanceof RocketBlockEntity rocketBlockEntity)
                || !rocketBlockEntity.addRocket(held)) {
                return InteractionResult.FAIL;
            }
            consumeOne(held, player);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    private static void consumeOne(ItemStack held, Player player) {
        if (player == null || !player.getAbilities().instabuild) {
            held.shrink(1);
        }
    }

    public static boolean isRocket(ItemStack stack) {
        return stack.getItem() instanceof AbstractRocketItem
            || stack.getItem() instanceof AbstractMediumRocketItem;
    }
}
