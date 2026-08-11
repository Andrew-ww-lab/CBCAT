package com.cbcatfix.munitions;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import rbasamoyai.createbigcannons.munitions.big_cannon.FuzedBlockEntity;

public class CbcatFixFuzedBlockEntity extends FuzedBlockEntity {
    public CbcatFixFuzedBlockEntity(BlockPos pos, BlockState state) {
        super(CbcatFixMunitions.FUZED_BLOCK_ENTITY.get(), pos, state);
    }
}
