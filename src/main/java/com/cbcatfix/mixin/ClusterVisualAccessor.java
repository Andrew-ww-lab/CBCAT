package com.cbcatfix.mixin;

import com.dsvv.cbcat.cluster_munition.FuzedClusterProjectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = FuzedClusterProjectile.class, remap = false)
public interface ClusterVisualAccessor {
    @Accessor("projectile") String cbcatfix$payloadType();
}
