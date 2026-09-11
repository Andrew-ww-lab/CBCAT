package com.cbcatfix.mixin;

import com.dsvv.cbcat.cannon.medium_rocketpod.breech.MediumRocketPodBreechInstance;
import dev.engine_room.flywheel.lib.instance.OrientedInstance;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MediumRocketPodBreechInstance.class, remap = false)
public class MediumRocketPodBreechInstanceMixin {
    @Shadow(remap = false)
    @Final
    private OrientedInstance[] rockets;

    @Inject(method = "transformModels", at = @At("HEAD"), cancellable = true, remap = false)
    private void cbcatfix$hideGenericRocketModels(CallbackInfo ci) {
        for (OrientedInstance rocket : this.rockets) {
            rocket.setVisible(false);
            rocket.setChanged();
        }
        ci.cancel();
    }
}
