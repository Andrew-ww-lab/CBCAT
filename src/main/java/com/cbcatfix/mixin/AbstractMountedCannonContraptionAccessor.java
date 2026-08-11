package com.cbcatfix.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import rbasamoyai.createbigcannons.cannon_control.contraption.AbstractMountedCannonContraption;

@Mixin(value = AbstractMountedCannonContraption.class, remap = false)
public interface AbstractMountedCannonContraptionAccessor {
    @Accessor("frontExtensionLength")
    int getFrontExtensionLength();
}
