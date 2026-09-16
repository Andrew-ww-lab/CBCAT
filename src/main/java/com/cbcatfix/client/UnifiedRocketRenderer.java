package com.cbcatfix.client;

import com.cbcatfix.rocket.RocketBalance;
import com.cbcatfix.rocket.RocketFlightEffects;
import com.cbcatfix.rocket.RocketGeometry;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import rbasamoyai.createbigcannons.munitions.AbstractCannonProjectile;

public class UnifiedRocketRenderer<T extends AbstractCannonProjectile> extends EntityRenderer<T> {
    private final ItemRenderer itemRenderer;

    public UnifiedRocketRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(T entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffers, int light) {
        Vec3 direction = entity instanceof com.cbcatfix.rocket.RocketPayloadAccess payload
            ? payload.cbcatfix$flightState().interpolatedForward(entity, partialTicks)
            : RocketFlightEffects.forward(entity);
        RocketBalance.Tier tier = RocketBalance.tierForProjectile(entity);
        ItemStack modelStack = modelStack(entity);

        poseStack.pushPose();
        poseStack.mulPose(new Quaternionf().rotationTo(
            0.0f, 1.0f, 0.0f,
            (float) direction.x, (float) direction.y, (float) direction.z
        ));
        float lengthScale = RocketGeometry.modelLengthScale(tier);
        poseStack.scale(1.0f, lengthScale, 1.0f);
        poseStack.translate(0.0f, (float) RocketGeometry.modelPivotOffset(tier), 0.0f);
        this.itemRenderer.renderStatic(
            modelStack, ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY,
            poseStack, buffers, entity.level(), entity.getId()
        );

        poseStack.popPose();
        super.render(entity, yaw, partialTicks, poseStack, buffers, light);
    }

    private static ItemStack modelStack(AbstractCannonProjectile entity) {
        ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        String itemPath = entityId.getNamespace().equals("cbc_at")
            ? entityId.getPath() + "_item"
            : entityId.getPath();
        return new ItemStack(BuiltInRegistries.ITEM.get(
            ResourceLocation.fromNamespaceAndPath(entityId.getNamespace(), itemPath)
        ));
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return net.minecraft.world.inventory.InventoryMenu.BLOCK_ATLAS;
    }
}
