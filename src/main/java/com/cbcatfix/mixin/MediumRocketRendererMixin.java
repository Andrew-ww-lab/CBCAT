package com.cbcatfix.mixin;

import com.dsvv.cbcat.cannon.medium_rocketpod.munitions.AbstractMediumRocket;
import com.dsvv.cbcat.cannon.medium_rocketpod.munitions.MediumRocketRenderer;
import com.cbcatfix.rocket.RocketFlightEffects;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MediumRocketRenderer.class, remap = false)
public class MediumRocketRendererMixin {
    @Unique private Vec3 cbcatfix$renderDirection = new Vec3(0.0, 1.0, 0.0);

    @Inject(method = "render", at = @At("HEAD"))
    private void cbcatfix$captureFlightDirection(AbstractMediumRocket entity, float yaw, float partialTicks,
                                                  PoseStack poseStack, MultiBufferSource buffers, int packedLight,
                                                  CallbackInfo ci) {
        this.cbcatfix$renderDirection = RocketFlightEffects.forward(entity);
    }

    @Redirect(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;cameraOrientation()Lorg/joml/Quaternionf;"
        )
    )
    private Quaternionf cbcatfix$orientAlongVelocity(EntityRenderDispatcher dispatcher) {
        Vec3 direction = this.cbcatfix$renderDirection;
        return new Quaternionf().rotationTo(
            0.0f, 1.0f, 0.0f,
            (float) direction.x, (float) direction.y, (float) direction.z
        );
    }
}
