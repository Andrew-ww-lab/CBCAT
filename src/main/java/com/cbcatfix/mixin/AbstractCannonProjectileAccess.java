package com.cbcatfix.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import net.minecraft.world.phys.HitResult;
import rbasamoyai.createbigcannons.munitions.ProjectileContext;
import rbasamoyai.createbigcannons.munitions.AbstractCannonProjectile;

@Mixin(AbstractCannonProjectile.class)
public interface AbstractCannonProjectileAccess {
    @Accessor("removeNextTick")
    boolean cbcatfix$isPendingRemoval();

    @Accessor("removeNextTick")
    void cbcatfix$setPendingRemoval(boolean pendingRemoval);

    @Invoker("onImpact")
    boolean cbcatfix$onImpact(HitResult hit, AbstractCannonProjectile.ImpactResult result, ProjectileContext context);

    @Accessor("damage")
    float cbcatfix$getDamage();

    @Accessor("damage")
    void cbcatfix$setDamage(float damage);
}
