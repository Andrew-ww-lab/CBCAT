package com.cbcatfix.mixin;

import com.cbcatfix.rocket.LauncherTransfer;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.ryanhcode.sable.api.SubLevelAssemblyHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

/** Both Simulated physicalization and dephysicalization use this native transfer boundary. */
@Pseudo
@Mixin(targets = "dev.ryanhcode.sable.api.SubLevelAssemblyHelper", remap = false)
public abstract class SableLauncherTransferMixin {
    @WrapMethod(method = "moveBlocks")
    private static void cbcatfix$transfer(ServerLevel level, SubLevelAssemblyHelper.AssemblyTransform transform,
                                         Iterable<BlockPos> blocks, Operation<Void> original) {
        // A position may be gathered both by the plot and by a disassembled CBC contraption.
        // Deduplicate coordinates, NEVER item stacks; do not replay a now-empty source.
        var positions = new java.util.LinkedHashSet<BlockPos>();
        for (var pos : blocks) if (!level.getBlockState(pos).isAir()) positions.add(pos.immutable());
        if (positions.isEmpty()) return;
        try (var transfer = LauncherTransfer.begin()) {
            for (var pos : positions)
                transfer.include(level.getBlockEntity(pos), transform.getLevel(), transform.apply(pos));
            original.call(level, transform, positions);
        }
    }

    @WrapOperation(method = "moveBlocks", at = @At(value = "INVOKE", target =
        "Lnet/minecraft/world/level/block/entity/BlockEntity;saveWithFullMetadata(Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/nbt/CompoundTag;"))
    private static CompoundTag cbcatfix$snapshot(BlockEntity be, HolderLookup.Provider registries, Operation<CompoundTag> original) {
        CompoundTag tag = original.call(be, registries);
        LauncherTransfer.snapshot(be, tag);
        return tag;
    }

    @WrapOperation(method = "moveBlocks", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/Clearable;tryClear(Ljava/lang/Object;)V"))
    private static void cbcatfix$deferClear(Object be, Operation<Void> original) {
        if (!(be instanceof BlockEntity source) || !LauncherTransfer.defersClear(source)) original.call(be);
    }

    @WrapOperation(method = "moveBlocks", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;removeBlockEntity(Lnet/minecraft/core/BlockPos;)V"))
    private static void cbcatfix$deferSilentRemoval(ServerLevel level, BlockPos pos, Operation<Void> original) {
        if (!LauncherTransfer.defersClear(level.getBlockEntity(pos))) original.call(level, pos);
    }

    @WrapOperation(method = "moveBlocks", at = @At(value = "INVOKE", target =
        "Lnet/minecraft/world/level/block/entity/BlockEntity;loadWithComponents(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/core/HolderLookup$Provider;)V"))
    private static void cbcatfix$receive(BlockEntity be, CompoundTag tag, HolderLookup.Provider registries, Operation<Void> original) {
        LauncherTransfer.load(be, tag, () -> original.call(be, tag, registries));
    }
}
