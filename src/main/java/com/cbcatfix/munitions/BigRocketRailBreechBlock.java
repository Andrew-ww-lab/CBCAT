package com.cbcatfix.munitions;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import com.dsvv.cbcat.cannon.medium_rocketpod.breech.MediumRocketPodBreechBlock;
import com.dsvv.cbcat.cannon.medium_rocketpod.breech.MediumRocketPodBreechBlockEntity;
import com.dsvv.cbcat.registry.BlockEntityRegister;
import rbasamoyai.createbigcannons.cannons.autocannon.material.AutocannonMaterial;
import rbasamoyai.createbigcannons.index.CBCAutocannonMaterials;

public class BigRocketRailBreechBlock extends MediumRocketPodBreechBlock {
    public static final MapCodec<BigRocketRailBreechBlock> CODEC = simpleCodec(properties -> new BigRocketRailBreechBlock(properties, CBCAutocannonMaterials.STEEL));

    public BigRocketRailBreechBlock(Properties properties, AutocannonMaterial material) {
        super(properties, material);
    }

    @Override
    protected MapCodec<? extends DirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntityType<? extends MediumRocketPodBreechBlockEntity> getBlockEntityType() {
        return BlockEntityRegister.MEDIUM_ROCKET_POD_BREECH_BLOCK_ENTITY.get();
    }
}
