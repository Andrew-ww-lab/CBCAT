package com.cbcatfix.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import rbasamoyai.createbigcannons.base.PartialBlockDamageManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;

@Mixin(value = PartialBlockDamageManager.class, remap = false)
public class PartialBlockDamageManagerMixin {

    @Shadow
    public boolean damageBlock(BlockPos pos, float damage, BlockState state, Level level) {
        throw new AssertionError();
    }

    public boolean damageBlock(BlockPos pos, int damage, BlockState state, Level level) {
        return this.damageBlock(pos, (float) damage, state, level);
    }
}
