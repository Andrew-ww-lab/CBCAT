package com.cbcatfix.rocket;

import com.dsvv.cbcat.cannon.rocketpod.RocketPodBarrelBlock;
import rbasamoyai.createbigcannons.index.CBCAutocannonMaterials;

/** A native Small steel section, including its connections, inventory view and shape. */
public final class LauncherModuleBlock extends RocketPodBarrelBlock {
    public enum Module { RAPID, TRAVERSE }

    private final Module module;
    private final com.mojang.serialization.MapCodec<? extends net.minecraft.world.level.block.DirectionalBlock> codec;

    public LauncherModuleBlock(Properties properties, Module module, boolean complete) {
        super(properties, CBCAutocannonMaterials.STEEL, complete);
        this.module = module;
        this.codec = simpleCodec(p -> new LauncherModuleBlock(p, module, complete));
    }

    @Override protected com.mojang.serialization.MapCodec<? extends net.minecraft.world.level.block.DirectionalBlock> codec() {
        return codec;
    }

    public Module module() {
        return module;
    }
}
