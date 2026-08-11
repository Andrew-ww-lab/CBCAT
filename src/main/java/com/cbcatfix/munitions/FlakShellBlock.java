package com.cbcatfix.munitions;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import rbasamoyai.createbigcannons.munitions.big_cannon.FuzedBlockEntity;
import rbasamoyai.createbigcannons.munitions.big_cannon.SimpleShellBlock;

public class FlakShellBlock extends SimpleShellBlock<FlakShellProjectile> {
    public static final MapCodec<FlakShellBlock> CODEC = simpleCodec(FlakShellBlock::new);

    public FlakShellBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends DirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    public boolean isBaseFuze() {
        return false;
    }

    @Override
    public EntityType<? extends FlakShellProjectile> getAssociatedEntityType() {
        return CbcatFixMunitions.FLAK_SHELL_PROJECTILE.get();
    }

    @Override
    public BlockEntityType<? extends FuzedBlockEntity> getBlockEntityType() {
        return CbcatFixMunitions.FUZED_BLOCK_ENTITY.get();
    }
}
