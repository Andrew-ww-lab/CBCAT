package com.cbcatfix.mixin;

import net.minecraft.core.Position;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(targets = {
    "com.dsvv.cbcat.cannon.rocketpod.munitions.AbstractFuzedRocket",
    "com.dsvv.cbcat.cannon.medium_rocketpod.munitions.AbstractMediumFuzedRocket"
}, remap = false)
public interface RocketDetonationInvoker {
    @Invoker("detonate")
    void cbcatfix$detonate(Position position);
}
