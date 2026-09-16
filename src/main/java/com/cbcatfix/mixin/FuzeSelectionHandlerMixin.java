package com.cbcatfix.mixin;

import com.cbcatfix.rocket.RocketBalance;
import com.cbcatfix.rocket.RocketBlock;
import com.cbcatfix.rocket.RocketGeometry;
import com.cbcatfix.rocket.RocketTargetResolver;
import net.createmod.catnip.outliner.Outliner;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rbasamoyai.createbigcannons.index.CBCDataComponents;
import rbasamoyai.createbigcannons.munitions.fuzes.FuzeItem;
import rbasamoyai.createbigcannons.munitions.fuzes.FuzeSelectionHandler;

/** Makes CBC's fuze placement hint follow one stored rocket instead of its root block. */
@Mixin(value = FuzeSelectionHandler.class, remap = false)
public abstract class FuzeSelectionHandlerMixin {
    @Shadow private Object bbOutlineSlot;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void cbcatfix$outlineIndividualRocketFuze(CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!rbasamoyai.createbigcannons.config.CBCConfigs.client().highlightFuzeInputOnShellBlocks.get()) return;
        if (minecraft.player == null || minecraft.level == null
            || !(minecraft.hitResult instanceof BlockHitResult blockHit)
            || minecraft.hitResult.getType() != HitResult.Type.BLOCK) {
            return;
        }

        RocketTargetResolver.Root root = RocketTargetResolver.findRoot(
            minecraft.level, blockHit.getBlockPos()
        ).orElse(null);
        if (root == null) {
            var breech = com.cbcatfix.rocket.MountedRocketInteraction.findBreech(minecraft.level, blockHit.getBlockPos());
            if (breech != null) {
                ci.cancel();
                Vec3 base = Vec3.atLowerCornerOf(breech.getBlockPos());
                Vec3 eye = minecraft.player.getEyePosition();
                var ray = com.cbcatfix.rocket.RocketViewRay.inBlockSpace(minecraft.level, breech.getBlockPos(), eye,
                    eye.add(minecraft.player.getViewVector(1).scale(minecraft.player.blockInteractionRange())));
                Vec3 start = ray.start().subtract(base);
                var selection = com.cbcatfix.rocket.MountedRocketInteraction.select(breech, start,
                    ray.end().subtract(base));
                if (selection != null) {
                    ItemStack rocket = ((com.cbcatfix.rocket.MountedRocketStorage) breech).cbcatfix$rocketInSlot(selection.slot());
                    ItemStack fuze = rocket.getOrDefault(CBCDataComponents.FUZE, ItemContainerContents.EMPTY).copyOne();
                    ItemStack held = minecraft.player.getMainHandItem();
                    if ((held.getItem() instanceof FuzeItem && fuze.isEmpty()) || (held.isEmpty() && !fuze.isEmpty())) {
                        var mount = selection.mount();
                        Vec3 center = mount.nose().add(base).subtract(mount.forward().scale(1.0 / 16.0));
                        double width = mount.width() * 0.5;
                        AABB outline = new AABB(center, center).inflate(
                            mount.facing().getAxis() == Direction.Axis.X ? 0.125 : width,
                            mount.facing().getAxis() == Direction.Axis.Y ? 0.125 : width,
                            mount.facing().getAxis() == Direction.Axis.Z ? 0.125 : width);
                        Outliner.getInstance().showAABB(this.bbOutlineSlot, outline).colored(0xffff55)
                            .clearTextures().disableLineNormals().lineWidth(1.0f / 32.0f);
                    }
                }
            }
            return;
        }

        // The vanilla CBC handler only understands one FuzedBlockEntity per
        // BlockPos. Once a rocket root/proxy is hit, it must not draw that
        // rack-wide block-space hint.
        ci.cancel();
        RocketTargetResolver.Target target = RocketTargetResolver.resolveInteraction(
            minecraft.level, blockHit, minecraft.player
        ).orElse(null);
        if (target == null) {
            return;
        }

        ItemStack held = minecraft.player.getMainHandItem();
        ItemStack installedFuze = target.rocket().getOrDefault(
            CBCDataComponents.FUZE, ItemContainerContents.EMPTY
        ).copyOne();
        boolean installing = held.getItem() instanceof FuzeItem && installedFuze.isEmpty();
        boolean removing = held.isEmpty() && !installedFuze.isEmpty();
        if (!installing && !removing) {
            return;
        }

        RocketBalance.Tier tier = RocketBlock.tier(root.state());
        Direction facing = RocketGeometry.horizontalFacing(root.state().getValue(RocketBlock.FACING));
        Vec3 forward = RocketGeometry.basis(facing).forward();
        Vec3 center = RocketGeometry.nosePoint(tier, facing, target.slot(), root.blockEntity().isIndependent())
            .add(Vec3.atLowerCornerOf(root.pos()))
            .subtract(forward.scale(1.0 / 16.0));
        double halfLength = 2.0 / 16.0;
        double halfWidth = RocketGeometry.bodyWidth(tier) * 0.5;
        AABB outline = new AABB(center, center).inflate(
            facing.getAxis() == Direction.Axis.X ? halfLength : halfWidth,
            halfWidth,
            facing.getAxis() == Direction.Axis.Z ? halfLength : halfWidth
        );
        Outliner.getInstance().showAABB(this.bbOutlineSlot, outline)
            .colored(0xffff55)
            .clearTextures()
            .disableLineNormals()
            .lineWidth(1.0f / 32.0f);
    }
}
