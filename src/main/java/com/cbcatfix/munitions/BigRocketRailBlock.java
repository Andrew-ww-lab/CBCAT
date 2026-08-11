package com.cbcatfix.munitions;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import com.dsvv.cbcat.cannon.medium_rocketpod.MediumRocketPodBarrelBlock;
import com.dsvv.cbcat.cannon.medium_rocketpod.MediumRocketPodBlockEntity;
import com.dsvv.cbcat.registry.BlockEntityRegister;
import rbasamoyai.createbigcannons.index.CBCAutocannonMaterials;

public class BigRocketRailBlock extends MediumRocketPodBarrelBlock {
    public static final MapCodec<BigRocketRailBlock> CODEC = simpleCodec(properties -> new BigRocketRailBlock(properties, CBCAutocannonMaterials.STEEL));

    public BigRocketRailBlock(Properties properties, rbasamoyai.createbigcannons.cannons.autocannon.material.AutocannonMaterial material) {
        super(properties, material);
    }

    @Override
    protected MapCodec<? extends DirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntityType<? extends MediumRocketPodBlockEntity> getBlockEntityType() {
        return BlockEntityRegister.MEDIUM_ROCKET_POD_BARREL_BLOCK_ENTITY.get();
    }
}
