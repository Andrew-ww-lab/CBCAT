package com.cbcatfix.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.world.item.ItemStack;
import rbasamoyai.createbigcannons.cannon_control.contraption.AbstractMountedCannonContraption;
import rbasamoyai.createbigcannons.cannon_control.contraption.PitchOrientedContraptionEntity;
import rbasamoyai.createbigcannons.cannon_control.contraption.ItemCannon;
import rbasamoyai.createbigcannons.cannons.big_cannons.breeches.quickfiring_breech.CannonMountPoint;
import rbasamoyai.createbigcannons.cannons.big_cannons.BigCannonBlock;
import com.dsvv.cbcat.cannon.rocketpod.contraption.MountedRocketPodContraption;
import com.dsvv.cbcat.cannon.medium_rocketpod.contraption.MountedMediumRocketRailContraption;
import com.cbcatfix.munitions.BigHERocketItem;
import com.cbcatfix.munitions.BigAPRocketItem;
import com.cbcatfix.munitions.BigHEATRocketItem;
import com.cbcatfix.munitions.CbcatFixMunitions;
import com.cbcatfix.IMediumRocketPodBreechBlockEntity;

@Mixin(value = CannonMountPoint.class, remap = false)
public class CannonMountPointMixin {

    @Inject(method = "insert", at = @At("HEAD"), cancellable = true)
    private void onInsert(com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity arm, ItemStack stack, boolean simulate, CallbackInfoReturnable<ItemStack> cir) {
        CannonMountPoint point = (CannonMountPoint) (Object) this;
        net.minecraft.world.level.block.entity.BlockEntity be = point.getLevel().getBlockEntity(point.getPos());
        PitchOrientedContraptionEntity contraptionEntity = null;

        if (be instanceof rbasamoyai.createbigcannons.cannon_control.cannon_mount.ExtendsCannonMount) {
            var mount = ((rbasamoyai.createbigcannons.cannon_control.cannon_mount.ExtendsCannonMount) be).getCannonMount();
            if (mount != null) {
                contraptionEntity = mount.getContraption();
            }
        } else if (be instanceof rbasamoyai.createbigcannons.cannon_control.fixed_cannon_mount.FixedCannonMountBlockEntity) {
            contraptionEntity = ((rbasamoyai.createbigcannons.cannon_control.fixed_cannon_mount.FixedCannonMountBlockEntity) be).getContraption();
        }

        if (contraptionEntity != null) {
            var contraption = contraptionEntity.getContraption();
            if (contraption instanceof MountedRocketPodContraption || contraption instanceof MountedMediumRocketRailContraption) {
                cir.setReturnValue(point.getInsertedResultAndDoSomething(stack, simulate, (AbstractMountedCannonContraption) contraption, contraptionEntity));
            }
        }
    }

    @Inject(method = "getInsertedResultAndDoSomething", at = @At("HEAD"), cancellable = true)
    private void onGetInsertedResultAndDoSomething(
        ItemStack stack,
        boolean simulate,
        AbstractMountedCannonContraption contraption,
        PitchOrientedContraptionEntity entity,
        CallbackInfoReturnable<ItemStack> cir
    ) {
        if (contraption instanceof MountedMediumRocketRailContraption) {
            var breechPos = contraption.getStartPos();
            var breechBE = contraption.presentBlockEntities.get(breechPos);
            if (breechBE != null) {
                boolean isBigBreech = ((IMediumRocketPodBreechBlockEntity) breechBE).cbcatfix$isBigBreech();
                if (stack.getItem() instanceof BigHERocketItem || stack.getItem() instanceof BigAPRocketItem || stack.getItem() instanceof BigHEATRocketItem) {
                    if (!isBigBreech) {
                        cir.setReturnValue(stack); // Reject! Not a big breech.
                        return;
                    }
                } else {
                    if (isBigBreech) {
                        cir.setReturnValue(stack); // Reject! Cannot load medium rocket into big breech.
                        return;
                    }
                }
            }

            if (contraption instanceof ItemCannon) {
                ItemStack result = ((ItemCannon) contraption).insertItemIntoCannon(stack, simulate);
                if (!simulate && result.getCount() < stack.getCount()) {
                    syncContraptionBreech(contraption, entity);
                }
                cir.setReturnValue(result);
            }
        } else if (contraption instanceof MountedRocketPodContraption) {
            if (contraption instanceof ItemCannon) {
                ItemStack result = ((ItemCannon) contraption).insertItemIntoCannon(stack, simulate);
                if (!simulate && result.getCount() < stack.getCount()) {
                    syncContraptionBreech(contraption, entity);
                }
                cir.setReturnValue(result);
            }
        }
    }

    private void syncContraptionBreech(AbstractMountedCannonContraption contraption, PitchOrientedContraptionEntity entity) {
        var breechPos = contraption.getStartPos();
        var breechBE = contraption.presentBlockEntities.get(breechPos);
        var blockInfo = contraption.getBlocks().get(breechPos);
        if (breechBE != null && blockInfo != null) {
            BigCannonBlock.writeAndSyncSingleBlockData(breechBE, blockInfo, entity, contraption);
        }
    }
}
