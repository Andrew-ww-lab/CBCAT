package com.cbcatfix.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.dsvv.cbcat.cannon.medium_rocketpod.breech.MediumRocketPodBreechInstance;
import com.dsvv.cbcat.cannon.medium_rocketpod.breech.MediumRocketPodBreechBlockEntity;
import com.cbcatfix.IMediumRocketPodBreechBlockEntity;
import dev.engine_room.flywheel.lib.instance.OrientedInstance;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(value = MediumRocketPodBreechInstance.class, remap = false)
public class MediumRocketPodBreechInstanceMixin {

    @Inject(method = "getPartialModelForState", at = @At("HEAD"), cancellable = true, remap = false)
    private void cbcatfix$onGetPartialModelForState(BlockState state, CallbackInfoReturnable<PartialModel> cir) {
        MediumRocketPodBreechInstance instance = (MediumRocketPodBreechInstance) (Object) this;
        try {
            java.lang.reflect.Field field = MediumRocketPodBreechInstance.class.getSuperclass().getDeclaredField("blockEntity");
            field.setAccessible(true);
            MediumRocketPodBreechBlockEntity breech = (MediumRocketPodBreechBlockEntity) field.get(instance);
            if (breech != null && ((IMediumRocketPodBreechBlockEntity) breech).cbcatfix$isBigBreech()) {
                cir.setReturnValue(com.cbcatfix.client.ClientSetup.BIG_ROCKET_MODEL);
            }
        } catch (Exception e) {
        }
    }

    @Redirect(
        method = "<init>",
        at = @At(value = "INVOKE",
            target = "Ldev/engine_room/flywheel/lib/instance/OrientedInstance;translatePosition(FFF)Ldev/engine_room/flywheel/lib/instance/OrientedInstance;"),
        remap = false
    )
    private OrientedInstance cbcatfix$redirectTranslatePosition(OrientedInstance instance, float x, float y, float z) {
        MediumRocketPodBreechInstance visual = (MediumRocketPodBreechInstance) (Object) this;
        try {
            java.lang.reflect.Field field = MediumRocketPodBreechInstance.class.getSuperclass().getDeclaredField("blockEntity");
            field.setAccessible(true);
            MediumRocketPodBreechBlockEntity breech = (MediumRocketPodBreechBlockEntity) field.get(visual);
            if (breech != null && ((IMediumRocketPodBreechBlockEntity) breech).cbcatfix$isBigBreech()) {
                int slot = 0;
                if (x < 0 && y > 0) {
                    slot = 0;
                } else if (x > 0 && y < 0) {
                    slot = 1;
                } else if (x < 0 && y < 0) {
                    slot = 2;
                } else if (x > 0 && y > 0) {
                    slot = 3;
                }
                float newZ = 0.0f;
                if (slot == 0) newZ = 0.0f;
                else if (slot == 1) newZ = -0.4f;
                else if (slot == 2) newZ = -0.8f;
                else if (slot == 3) newZ = -1.2f;

                return instance.translatePosition(0.0f, 0.25f, newZ);
            }
        } catch (Exception e) {
        }
        return instance.translatePosition(x, y, z);
    }
}
