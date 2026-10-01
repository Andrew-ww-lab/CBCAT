package com.cbcatfix.mixin;

import com.dsvv.cbcat.cannon.heavy_autocannon.munitions.AbstractHeavyAutocannonProjectile;
import com.dsvv.cbcat.cannon.heavy_autocannon.munitions.AbstractHeavyAutocannonProjectileItem;
import com.dsvv.cbcat.cannon.heavy_autocannon.munitions.HeavyAutocannonProjectileRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

/** Use the ammo's existing item model; retain CBCA's tracer and unknown-projectile paths. */
@Mixin(value = HeavyAutocannonProjectileRenderer.class, remap = false)
public class HeavyAutocannonProjectileRendererMixin {
    @Unique private static final Map<EntityType<?>, ItemStack> cbcatfix$models = new HashMap<>();

    @Inject(method = "render(Lcom/dsvv/cbcat/cannon/heavy_autocannon/munitions/AbstractHeavyAutocannonProjectile;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
        at = @At("HEAD"), cancellable = true)
    private void cbcatfix$renderAmmo(AbstractHeavyAutocannonProjectile entity, float yaw, float partialTicks,
                                    PoseStack pose, MultiBufferSource buffers, int light, CallbackInfo ci) {
        ItemStack model = cbcatfix$models.computeIfAbsent(entity.getType(), type -> {
            for (var item : BuiltInRegistries.ITEM) {
                if (item instanceof AbstractHeavyAutocannonProjectileItem ammo
                    && ammo.getEntityType(item.getDefaultInstance()) == type) return item.getDefaultInstance();
            }
            return ItemStack.EMPTY;
        });
        if (model.isEmpty()) return;
        Vec3 direction = entity.getOrientation().normalize();
        if (direction.lengthSqr() < 1.0e-8) direction = new Vec3(0, 1, 0);
        pose.pushPose();
        pose.mulPose(new Quaternionf().rotationTo(0, 1, 0,
            (float) direction.x, (float) direction.y, (float) direction.z));
        // NONE avoids GUI/held-item transforms. The native model occupies the lower half of a block.
        pose.translate(0, 0.25, 0);
        Minecraft.getInstance().getItemRenderer().renderStatic(model, ItemDisplayContext.NONE,
            light, OverlayTexture.NO_OVERLAY, pose, buffers, entity.level(), entity.getId());
        pose.popPose();
        if (!entity.isTracer()) ci.cancel();
    }
}
