package com.cbcatfix.mixin;

import com.cbcatfix.rocket.LauncherReadiness;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.simibubi.create.content.contraptions.ContraptionHandler;
import com.simibubi.create.content.contraptions.ContraptionHandlerClient;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import rbasamoyai.createbigcannons.base.goggles.EntityGoggleOverlayRenderer;

/** Connect Create's native contraption picking to CBC's existing entity goggles renderer. */
@Mixin(value = {EntityGoggleOverlayRenderer.class,
    com.simibubi.create.content.equipment.goggles.GoggleOverlayRenderer.class}, remap = false)
public abstract class LauncherGogglesTargetMixin {
    @ModifyExpressionValue(method = "renderOverlay", at = @At(value = "FIELD", target =
        "Lnet/minecraft/client/Minecraft;hitResult:Lnet/minecraft/world/phys/HitResult;"))
    private static HitResult cbcatfix$lookedAtLauncher(HitResult original) {
        var mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return original;
        var ray = ContraptionHandlerClient.getRayInputs(mc.player);
        var start = ray.getFirst();
        var end = ray.getSecond(); // Native input already stops at the nearer world/entity hit.
        var search = new AABB(start, end).inflate(16);
        double nearest = start.distanceToSqr(end);
        HitResult selected = original;
        for (var reference : ContraptionHandler.loadedContraptions.get(mc.level).values()) {
            var entity = reference.get();
            if (entity == null || !entity.isAlive() || !entity.getBoundingBox().intersects(search)) continue;
            var hit = ContraptionHandlerClient.rayTraceContraption(start, end, entity);
            if (hit == null) continue;
            var location = entity.toGlobalVector(hit.getLocation(), 1);
            double distance = start.distanceToSqr(location);
            if (distance >= nearest) continue;
            nearest = distance;
            selected = entity.getContraption() instanceof LauncherReadiness ? new EntityHitResult(entity, location) : original;
        }
        // This affects the overlay only; placement, clicking and combat keep their original hit result.
        return selected;
    }
}
