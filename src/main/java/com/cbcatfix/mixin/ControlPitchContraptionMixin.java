package com.cbcatfix.mixin;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import rbasamoyai.createbigcannons.cannon_control.ControlPitchContraption;

@Mixin(value = ControlPitchContraption.class, remap = false)
public interface ControlPitchContraptionMixin {

    @Shadow
    void onRecoil(Vec3 recoilVec, Vec3 cannonPos, AbstractContraptionEntity entity);

    default void onRecoil(Vec3 recoilVec, AbstractContraptionEntity entity) {
        this.onRecoil(recoilVec, entity.position(), entity);
    }
}
