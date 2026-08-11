package com.cbcatfix.munitions;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import rbasamoyai.createbigcannons.munitions.big_cannon.FuzedBlockEntity;
import rbasamoyai.createbigcannons.munitions.big_cannon.SimpleShellBlock;

public class HeavyHEShellBlock extends SimpleShellBlock<HeavyHEShellProjectile> {
    public static final MapCodec<HeavyHEShellBlock> CODEC = simpleCodec(HeavyHEShellBlock::new);

    public HeavyHEShellBlock(Properties properties) {
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
    public EntityType<? extends HeavyHEShellProjectile> getAssociatedEntityType() {
        return CbcatFixMunitions.HEAVY_HE_SHELL_PROJECTILE.get();
    }

    @Override
    public BlockEntityType<? extends FuzedBlockEntity> getBlockEntityType() {
        return CbcatFixMunitions.FUZED_BLOCK_ENTITY.get();
    }
}
