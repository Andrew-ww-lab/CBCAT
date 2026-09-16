package com.cbcatfix.mixin;

import com.cbcatfix.rocket.LauncherShapes;
import com.cbcatfix.rocket.RocketBalance;
import com.cbcatfix.munitions.BigRocketRailBlock;
import com.cbcatfix.munitions.BigRocketRailBreechBlock;
import com.dsvv.cbcat.cannon.rocketpod.RocketPodBlock;
import com.dsvv.cbcat.cannon.rocketpod.breech.RocketPodBreechBlock;
import com.dsvv.cbcat.cannon.medium_rocketpod.breech.MediumRocketPodBreechBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = {"com.dsvv.cbcat.cannon.rocketpod.RocketPodBarrelBlock",
    "com.dsvv.cbcat.cannon.rocketpod.breech.RocketPodBreechBlock",
    "com.dsvv.cbcat.cannon.medium_rocketpod.MediumRocketPodBarrelBlock",
    "com.dsvv.cbcat.cannon.medium_rocketpod.breech.MediumRocketPodBreechBlock"})
public abstract class RocketLauncherShapeMixin {
    @Inject(method = "getShape", at = @At("HEAD"), cancellable = true)
    private void cbcatfix$modelShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context,
                                    CallbackInfoReturnable<VoxelShape> cir) {
        var block = state.getBlock();
        boolean big = block instanceof BigRocketRailBlock || block instanceof BigRocketRailBreechBlock;
        var tier = big ? RocketBalance.Tier.BIG : block instanceof RocketPodBlock ? RocketBalance.Tier.SMALL : RocketBalance.Tier.MEDIUM;
        boolean breech = block instanceof RocketPodBreechBlock || block instanceof MediumRocketPodBreechBlock;
        VoxelShape shape = LauncherShapes.get(tier, breech, state.getValue(BlockStateProperties.FACING));
        var root = com.cbcatfix.rocket.LauncherAssembly.findBreech(level, pos);
        if (root instanceof com.cbcatfix.rocket.MountedRocketStorage storage) {
            var offset = root.getBlockPos().subtract(pos);
            boolean loading = context instanceof net.minecraft.world.phys.shapes.EntityCollisionContext ec
                && ec.getEntity() instanceof net.minecraft.world.entity.player.Player player
                && com.cbcatfix.rocket.RocketGroundPlacement.isRocket(player.getMainHandItem());
            for (int slot = 0; slot < storage.cbcatfix$slotCount(); slot++) {
                // A placement ray needs a target inside an EMPTY tube, without
                // turning that tube into a solid collision wall for other entities.
                if (!loading && storage.cbcatfix$rocketInSlot(slot).isEmpty()) continue;
                var bounds = com.cbcatfix.rocket.RocketMounts.transform(root, slot).bounds()
                    .move(offset.getX(), offset.getY(), offset.getZ());
                var body = net.minecraft.world.phys.shapes.Shapes.create(bounds);
                shape = net.minecraft.world.phys.shapes.Shapes.or(shape,
                    net.minecraft.world.phys.shapes.Shapes.join(body, net.minecraft.world.phys.shapes.Shapes.block(),
                        net.minecraft.world.phys.shapes.BooleanOp.AND));
            }
        }
        cir.setReturnValue(shape);
    }
}
