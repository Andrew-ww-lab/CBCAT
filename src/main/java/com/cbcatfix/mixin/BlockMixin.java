package com.cbcatfix.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Block;
import com.dsvv.cbcat.cannon.medium_rocketpod.MediumRocketPodBarrelBlock;
import com.dsvv.cbcat.cannon.medium_rocketpod.breech.MediumRocketPodBreechBlock;
import com.dsvv.cbcat.cannon.medium_rocketpod.breech.MediumRocketPodBreechBlockEntity;
import com.cbcatfix.munitions.CbcatFixMunitions;
import com.cbcatfix.CbcatFix;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;

@Mixin(value = BlockBehaviour.class, remap = false)
public class BlockMixin {

    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true, remap = false)
    private void onUseItemOn(
        ItemStack stack,
        BlockState state,
        Level level,
        BlockPos pos,
        Player player,
        InteractionHand hand,
        BlockHitResult hitResult,
        CallbackInfoReturnable<ItemInteractionResult> cir
    ) {
        Block block = state.getBlock();
        if (block instanceof MediumRocketPodBarrelBlock || block instanceof MediumRocketPodBreechBlock) {
            if (stack.getItem() instanceof com.dsvv.cbcat.cannon.medium_rocketpod.munitions.AbstractMediumRocketItem) {
                if (player.getCooldowns().isOnCooldown(stack.getItem())) {
                    cir.setReturnValue(ItemInteractionResult.FAIL);
                    return;
                }

                MediumRocketPodBreechBlockEntity breechBE = null;
                if (block instanceof MediumRocketPodBreechBlock) {
                    BlockEntity be = level.getBlockEntity(pos);
                    if (be instanceof MediumRocketPodBreechBlockEntity) {
                        breechBE = (MediumRocketPodBreechBlockEntity) be;
                    }
                } else {
                    breechBE = cbcatfix$findBreechInWorld(level, pos, state);
                }

                if (breechBE != null) {
                    if (breechBE.addToInputBuffer(stack)) {
                        breechBE.notifyUpdate();
                        if (!player.isCreative()) {
                            ItemStack copy = stack.copy();
                            copy.shrink(1);
                            player.setItemInHand(hand, copy);
                        }
                        player.getCooldowns().addCooldown(stack.getItem(), 20);
                        cir.setReturnValue(ItemInteractionResult.sidedSuccess(level.isClientSide()));
                    }
                }
            }
        }
    }

    @Unique
    private MediumRocketPodBreechBlockEntity cbcatfix$findBreechInWorld(Level level, BlockPos pos, BlockState state) {
        if (!state.hasProperty(MediumRocketPodBarrelBlock.FACING)) return null;
        Direction facing = state.getValue(MediumRocketPodBarrelBlock.FACING);
        for (Direction dir : new Direction[]{facing, facing.getOpposite()}) {
            BlockPos.MutableBlockPos current = pos.mutable();
            for (int i = 0; i < 15; i++) {
                current.move(dir);
                BlockEntity be = level.getBlockEntity(current);
                if (be instanceof MediumRocketPodBreechBlockEntity breech) {
                    return breech;
                }
                BlockState s = level.getBlockState(current);
                if (!(s.getBlock() instanceof com.dsvv.cbcat.cannon.medium_rocketpod.MediumRocketPodBlock)) {
                    break;
                }
            }
        }
        return null;
    }
}
