package com.cbcatfix.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rbasamoyai.createbigcannons.cannon_control.contraption.PitchOrientedContraptionEntity;
import rbasamoyai.createbigcannons.cannon_control.contraption.AbstractMountedCannonContraption;
import rbasamoyai.createbigcannons.cannons.big_cannons.BigCannonBlock;





import net.minecraft.world.entity.player.Player;

import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;



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
            BlockPos selectedBreechPos = com.cbcatfix.rocket.LauncherAssembly.findBreech(mounted, localPos);
            if (selectedBreechPos == null) return;
            var mountedBreech = mounted.presentBlockEntities.get(selectedBreechPos);
            if (mountedBreech instanceof com.cbcatfix.rocket.MountedRocketStorage) {
                var base = net.minecraft.world.phys.Vec3.atLowerCornerOf(selectedBreechPos);
                var eye = player.getEyePosition();
                var localStart = com.cbcatfix.rocket.RocketMounts.toLocal(entity, eye).subtract(base);
                var localEnd = com.cbcatfix.rocket.RocketMounts.toLocal(entity,
                    eye.add(player.getViewVector(1).scale(player.blockInteractionRange()))).subtract(base);
                boolean loading = com.cbcatfix.rocket.RocketGroundPlacement.isRocket(player.getItemInHand(hand));
                var selection = com.cbcatfix.rocket.MountedRocketInteraction.select(mountedBreech, localStart, localEnd, loading);
                if (com.cbcatfix.rocket.MountedRocketInteraction.interact(mountedBreech, selection, player, hand)) {
                    if (!player.level().isClientSide()) {
                        var info = mounted.getBlocks().get(selectedBreechPos);
                        if (info != null) BigCannonBlock.writeAndSyncSingleBlockData(mountedBreech, info, entity, mounted);
                    }
                    cir.setReturnValue(true);
                    return;
                }
                if (loading) {
                    cir.setReturnValue(false); // Never fall through to native "fill next slot" handling.
                    return;
                }
            }
        }
    }
}
