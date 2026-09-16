package com.cbcatfix.rocket;

import com.cbcatfix.munitions.CbcatFixMunitions;
import com.dsvv.cbcat.cannon.medium_rocketpod.MediumRocketPodBlock;
import com.dsvv.cbcat.cannon.rocketpod.RocketPodBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import rbasamoyai.createbigcannons.index.CBCItems;
import rbasamoyai.createbigcannons.munitions.fuzes.FuzeItem;

/** Input adapter over the placed rocket's existing CBC inventory/component implementation. */
public final class MountedRocketInteraction {
    private MountedRocketInteraction() {}

    public static BlockEntity findBreech(BlockGetter level, BlockPos hit) {
        return LauncherAssembly.findBreech(level, hit);
    }

    public static Selection select(BlockEntity breech, Vec3 start, Vec3 end) {
        return select(breech, start, end, false);
    }

    public static Selection select(BlockEntity breech, Vec3 start, Vec3 end, boolean loading) {
        if (!(breech instanceof MountedRocketStorage storage)) return null;
        Selection best = null;
        double distance = Double.MAX_VALUE;
        for (int slot = 0; slot < storage.cbcatfix$slotCount(); slot++) {
            if (!loading && storage.cbcatfix$rocketInSlot(slot).isEmpty()) continue;
            var mount = RocketMounts.transform(breech, slot);
            var hit = mount.bounds().clip(start, end);
            if (hit.isPresent() && hit.get().distanceToSqr(start) < distance) {
                distance = hit.get().distanceToSqr(start);
                best = new Selection(slot, mount, hit.get());
            }
        }
        return best;
    }

    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (player.isShiftKeyDown() || event.getHand() != InteractionHand.MAIN_HAND) return;
        BlockEntity breech = findBreech(event.getLevel(), event.getPos());
        if (breech == null) return;
        boolean loading = RocketGroundPlacement.isRocket(player.getItemInHand(event.getHand()));
        Selection selection = viewed(breech, player, loading);
        if (interact(breech, selection, player, event.getHand()) || loading) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide()));
        }
    }

    public static boolean interact(BlockEntity breech, Selection selection, Player player, InteractionHand hand) {
        if (LauncherTransfer.isLocked(breech)) return false;
        if (hand != InteractionHand.MAIN_HAND || player.isShiftKeyDown() || selection == null) return false;
        var storage = (MountedRocketStorage) breech;
        ItemStack held = player.getItemInHand(hand);
        if (RocketGroundPlacement.isRocket(held)) {
            var handler = new MountedRocketItemHandler(breech);
            ItemStack remainder = handler.insertItem(selection.slot(), held, player.level().isClientSide());
            if (remainder.getCount() == held.getCount()) return false;
            if (!player.level().isClientSide() && !player.isCreative()) player.setItemInHand(hand, remainder);
            return true;
        }
        ItemStack rocket = storage.cbcatfix$rocketInSlot(selection.slot());
        if (rocket.isEmpty()) return false;
        ItemStack edited = rocket.copyWithCount(1);
        ItemStack fuze = rbasamoyai.createbigcannons.munitions.big_cannon.FuzedProjectileBlock.getFuzeFromItemStack(rocket);
        ItemStack tracer = rbasamoyai.createbigcannons.munitions.big_cannon.ProjectileBlock.getTracerFromItemStack(rocket);
        boolean nose = selection.hit().subtract(selection.mount().center()).dot(selection.mount().forward())
            > selection.mount().length() * 0.5 - 0.125;
        boolean client = player.level().isClientSide();
        if (held.getItem() instanceof FuzeItem && nose && fuze.isEmpty()) {
            if (!client) {
                setComponent(edited, rbasamoyai.createbigcannons.index.CBCDataComponents.FUZE, held);
                storage.cbcatfix$setRocketInSlot(selection.slot(), edited);
                if (!player.isCreative()) held.shrink(1);
            }
        } else if (CBCItems.TRACER_TIP.isIn(held) && tracer.isEmpty()) {
            if (!client) {
                setComponent(edited, rbasamoyai.createbigcannons.index.CBCDataComponents.TRACER, held);
                storage.cbcatfix$setRocketInSlot(selection.slot(), edited);
                if (!player.isCreative()) held.shrink(1);
            }
        } else if (held.isEmpty()) {
            boolean componentRemoval = !tracer.isEmpty() || (nose && !fuze.isEmpty());
            if (!componentRemoval && !LauncherAccess.canTransfer(breech)) return false;
            if (!client) {
                ItemStack removed;
                if (!tracer.isEmpty()) {
                    removed = tracer;
                    setComponent(edited, rbasamoyai.createbigcannons.index.CBCDataComponents.TRACER, ItemStack.EMPTY);
                    storage.cbcatfix$setRocketInSlot(selection.slot(), edited);
                } else if (nose && !fuze.isEmpty()) {
                    removed = fuze;
                    setComponent(edited, rbasamoyai.createbigcannons.index.CBCDataComponents.FUZE, ItemStack.EMPTY);
                    storage.cbcatfix$setRocketInSlot(selection.slot(), edited);
                } else {
                    removed = new MountedRocketItemHandler(breech).extractItem(selection.slot(), 1, false);
                }
                if (!player.addItem(removed)) player.drop(removed, false);
            }
        } else return false;
        if (!client) player.level().playSound(null, player.blockPosition(),
            SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.7f, 1);
        return true;
    }

    private static void setComponent(ItemStack rocket,
        net.minecraft.core.component.DataComponentType<net.minecraft.world.item.component.ItemContainerContents> type, ItemStack item) {
        rocket.set(type, item.isEmpty() ? net.minecraft.world.item.component.ItemContainerContents.EMPTY
            : net.minecraft.world.item.component.ItemContainerContents.fromItems(java.util.List.of(item.copyWithCount(1))));
    }

    public static Selection viewed(BlockEntity breech, Player player, boolean loading) {
        Vec3 eye = player.getEyePosition();
        var ray = RocketViewRay.inBlockSpace(player.level(), breech.getBlockPos(), eye,
            eye.add(player.getViewVector(1).scale(player.blockInteractionRange())));
        Vec3 base = Vec3.atLowerCornerOf(breech.getBlockPos());
        return select(breech, ray.start().subtract(base), ray.end().subtract(base), loading);
    }

    public record Selection(int slot, RocketMounts.Transform mount, Vec3 hit) {}
}
