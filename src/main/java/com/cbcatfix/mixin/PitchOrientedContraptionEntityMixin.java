package com.cbcatfix.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rbasamoyai.createbigcannons.cannon_control.contraption.PitchOrientedContraptionEntity;
import rbasamoyai.createbigcannons.cannon_control.contraption.AbstractMountedCannonContraption;
import rbasamoyai.createbigcannons.cannons.big_cannons.BigCannonBlock;
import com.dsvv.cbcat.cannon.medium_rocketpod.MediumRocketPodBarrelBlock;
import com.dsvv.cbcat.cannon.medium_rocketpod.breech.MediumRocketPodBreechBlock;
import com.dsvv.cbcat.cannon.medium_rocketpod.breech.MediumRocketPodBreechBlockEntity;
import com.cbcatfix.CbcatFix;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

@Mixin(value = PitchOrientedContraptionEntity.class, remap = false)
public class PitchOrientedContraptionEntityMixin {

    @Inject(method = "handlePlayerInteraction", at = @At("HEAD"), cancellable = true, remap = false)
    private void onHandlePlayerInteraction(
        Player player,
        BlockPos localPos,
        Direction side,
        InteractionHand hand,
        CallbackInfoReturnable<Boolean> cir
    ) {
        PitchOrientedContraptionEntity entity = (PitchOrientedContraptionEntity) (Object) this;
        var contraption = entity.getContraption();
        if (contraption instanceof AbstractMountedCannonContraption mounted) {
            var blockInfo = contraption.getBlocks().get(localPos);
            if (blockInfo != null && (blockInfo.state().getBlock() instanceof MediumRocketPodBarrelBlock || blockInfo.state().getBlock() instanceof MediumRocketPodBreechBlock)) {
                ItemStack stack = player.getItemInHand(hand);
                if (stack.getItem() instanceof com.dsvv.cbcat.cannon.medium_rocketpod.munitions.AbstractMediumRocketItem) {
                    if (player.getCooldowns().isOnCooldown(stack.getItem())) {
                        cir.setReturnValue(false);
                        return;
                    }

                    BlockPos breechPos = mounted.getStartPos();
                    BlockEntity breechBE = mounted.presentBlockEntities.get(breechPos);
                    var breechInfo = mounted.getBlocks().get(breechPos);
                    if (breechBE instanceof MediumRocketPodBreechBlockEntity breech) {
                        if (breech.addToInputBuffer(stack)) {
                            Level level = player.level();
                            if (!level.isClientSide()) {
                                if (breechInfo != null) {
                                    BigCannonBlock.writeAndSyncSingleBlockData(breech, breechInfo, entity, contraption);
                                }
                                if (!player.isCreative()) {
                                    ItemStack copy = stack.copy();
                                    copy.shrink(1);
                                    player.setItemInHand(hand, copy);
                                }
                                player.getCooldowns().addCooldown(stack.getItem(), 20);
                            }
                            cir.setReturnValue(true);
                        }
                    }
                }
            }
        }
    }
}
