package com.cbcatfix.mixin;

import com.cbcatfix.config.CbcatFixConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(targets = "com.dsvv.cbcat.cluster_munition.FuzedClusterProjectile", remap = false)
public class ClusterSubmunitionConfigMixin {
    @org.spongepowered.asm.mixin.Shadow protected net.minecraft.world.item.ItemStack[] secondaryFuzes;

    @org.spongepowered.asm.mixin.injection.Inject(method = "spawnClusterParts", at = @At("HEAD"))
    private void cbcatfix$eightParts(org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        secondaryFuzes = com.cbcatfix.munitions.ClusterPayload.normalizeFuzes(secondaryFuzes);
    }

    @ModifyArg(method = "spawnClusterParts", at = @At(value = "INVOKE", target = "Lcom/dsvv/cbcat/cannon/heavy_autocannon/munitions/AbstractFuzedHeavyAutocannonProjectile;setLifetime(I)V"), index = 0)
    private int cbcatfix$submunitionLifetime(int original) {
        return CbcatFixConfig.value(CbcatFixConfig.CLUSTER_SUBMUNITION_LIFETIME);
    }

    @ModifyArg(method = "spawnClusterParts", at = @At(value = "INVOKE", target = "Lcom/dsvv/cbcat/cannon/heavy_autocannon/munitions/AbstractFuzedHeavyAutocannonProjectile;shoot(DDDFF)V"), index = 3)
    private float cbcatfix$submunitionSpeed(float original) {
        return original * CbcatFixConfig.value(CbcatFixConfig.CLUSTER_SUBMUNITION_SPEED_SCALE).floatValue();
    }

    @ModifyArg(method = "spawnClusterParts", at = @At(value = "INVOKE", target = "Lcom/dsvv/cbcat/cannon/heavy_autocannon/munitions/AbstractFuzedHeavyAutocannonProjectile;shoot(DDDFF)V"), index = 4)
    private float cbcatfix$submunitionInaccuracy(float original) {
        return CbcatFixConfig.value(CbcatFixConfig.CLUSTER_SUBMUNITION_INACCURACY).floatValue();
    }
}
