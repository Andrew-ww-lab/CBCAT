package com.cbcatfix.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.createmod.catnip.render.SuperByteBuffer;
import dev.engine_room.flywheel.lib.transform.Translate;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import com.dsvv.cbcat.cannon.medium_rocketpod.breech.MediumRocketPodBreechRenderer;
import com.dsvv.cbcat.cannon.medium_rocketpod.breech.MediumRocketPodBreechBlockEntity;
import com.cbcatfix.IMediumRocketPodBreechBlockEntity;

@Mixin(value = MediumRocketPodBreechRenderer.class, remap = false)
public class MediumRocketPodBreechRendererMixin {

    @Unique
    private boolean cbcatfix$bigBreechRender = false;

    @Inject(method = "renderSafe", at = @At("HEAD"), remap = false)
    private void cbcatfix$onRenderHead(
        MediumRocketPodBreechBlockEntity breech,
        float partialTicks,
        PoseStack poseStack,
        MultiBufferSource bufferSource,
        int packedLight,
        int packedOverlay,
        CallbackInfo ci
    ) {
        this.cbcatfix$bigBreechRender = ((IMediumRocketPodBreechBlockEntity) breech).cbcatfix$isBigBreech();
    }

    @Redirect(
        method = "renderSafe",
        at = @At(value = "INVOKE",
            target = "Lcom/dsvv/cbcat/registry/ExtraDataRegister;mediumRocketModel()Ldev/engine_room/flywheel/lib/model/baked/PartialModel;"),
        remap = false
    )
    private PartialModel cbcatfix$redirectModel() {
        if (this.cbcatfix$bigBreechRender) {
            return com.cbcatfix.client.ClientSetup.BIG_ROCKET_MODEL;
        }
        return com.dsvv.cbcat.registry.ExtraDataRegister.mediumRocketModel();
    }

    @Redirect(
        method = "renderSafe",
        at = @At(value = "INVOKE",
            target = "Lnet/createmod/catnip/render/SuperByteBuffer;translate(FFF)Ldev/engine_room/flywheel/lib/transform/Translate;"),
        remap = false
    )
    private Translate cbcatfix$redirectTranslate(SuperByteBuffer buffer, float x, float y, float z) {
        if (this.cbcatfix$bigBreechRender) {
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

            return buffer.translate(0.0f, 0.25f, newZ);
        }
        return buffer.translate(x, y, z);
    }
}
