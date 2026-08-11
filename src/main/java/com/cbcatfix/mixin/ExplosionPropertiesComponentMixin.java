package com.cbcatfix.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import rbasamoyai.createbigcannons.munitions.config.components.ExplosionPropertiesComponent;

@Mixin(value = ExplosionPropertiesComponent.class, remap = false)
public abstract class ExplosionPropertiesComponentMixin {

    @Shadow
    public abstract float blockDamagePower();

    public float explosivePower() {
        return this.blockDamagePower();
    }
}
