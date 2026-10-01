package com.cbcatfix.mixin;

import com.dsvv.cbcat.cannon.twin_autocannon.TwinAutocannonBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Welding must use the same paired-barrel layout rule as native assembly. */
@Mixin(value = TwinAutocannonBlock.class, remap = false)
public interface TwinAutocannonConnectivityMixin {
    @Inject(method = "canWeldSide", at = @At("HEAD"), cancellable = true)
    private void cbcatfix$matchBarrelLayout(Level level, Direction side, BlockState state, BlockState other,
        BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (other.getBlock() instanceof TwinAutocannonBlock neighbor
            && ((TwinAutocannonBlock) state.getBlock()).isVertical() != neighbor.isVertical()) {
            cir.setReturnValue(false);
        }
    }
}
