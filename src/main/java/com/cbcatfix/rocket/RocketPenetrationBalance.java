package com.cbcatfix.rocket;

import com.cbcatfix.mixin.AbstractCannonProjectileAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import rbasamoyai.createbigcannons.config.CBCCfgMunitions.GriefState;
import rbasamoyai.createbigcannons.munitions.AbstractCannonProjectile;
import rbasamoyai.createbigcannons.munitions.AbstractCannonProjectile.ImpactResult;
import rbasamoyai.createbigcannons.munitions.AbstractCannonProjectile.ImpactResult.KinematicOutcome;
import rbasamoyai.createbigcannons.munitions.ProjectileContext;
import rbasamoyai.createbigcannons.munitions.ProjectileDamageHooks;

public final class RocketPenetrationBalance {
    public static final TagKey<Block> FOLIAGE = tag("rocket_permeable_foliage");
    public static final TagKey<Block> FRAGILE = tag("rocket_permeable_fragile");
    public static final TagKey<Block> SOFT = tag("rocket_permeable_soft");

    private RocketPenetrationBalance() {}

    private static TagKey<Block> tag(String path) {
        return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("cbcatfix", path));
    }

    /** Null delegates to the complete original CBCAT calculation, including shatter and removal. */
    public static ImpactResult tryPermeable(AbstractCannonProjectile rocket, ProjectileContext context,
                                           BlockState block, BlockHitResult hit) {
        if (context.griefState() == GriefState.NO_DAMAGE
            || !ProjectileDamageHooks.canDamageTerrain(rocket.level(), hit.getBlockPos())) {
            // CBCAT's STOP path still calls partial block damage; do not enter it for protected terrain.
            if (!rocket.level().isClientSide()) {
                rocket.setProjectileMass(0);
                var stopped = new ImpactResult(KinematicOutcome.STOP, false);
                ((AbstractCannonProjectileAccess) rocket).cbcatfix$onImpact(hit, stopped, context);
            }
            return new ImpactResult(KinematicOutcome.STOP, true);
        }
        Obstacle obstacle = block.is(FOLIAGE) ? Obstacle.FOLIAGE
            : block.is(FRAGILE) ? Obstacle.FRAGILE : block.is(SOFT) ? Obstacle.SOFT : null;
        if (obstacle == null
            || block.getDestroySpeed(rocket.level(), hit.getBlockPos()) < 0
            || block.hasBlockEntity()
            || rocket.getDeltaMovement().lengthSqr() < 0.0625) return null;

        if (!rocket.level().isClientSide()) {
            // Vanilla owns removal, waterlogged fluid restoration, particles, sound and game events.
            if (!rocket.level().destroyBlock(hit.getBlockPos(), false, rocket)) return null;
            rocket.setDeltaMovement(rocket.getDeltaMovement().scale(1.0 - obstacle.speedLoss));
            if (obstacle.hullDamage > 0) RocketDamage.damageHull(rocket, obstacle.hullDamage, hit.getLocation());
        }
        // These obstacles do not trigger impact fuzes or spend penetrator mass.
        return new ImpactResult(KinematicOutcome.PENETRATE, rocket.isRemoved());
    }

    private enum Obstacle {
        FOLIAGE(0, com.cbcatfix.balance.BalanceDefaults.FOLIAGE_SPEED_LOSS),
        FRAGILE(com.cbcatfix.balance.BalanceDefaults.FRAGILE_HULL_DAMAGE, com.cbcatfix.balance.BalanceDefaults.FRAGILE_SPEED_LOSS),
        SOFT(com.cbcatfix.balance.BalanceDefaults.SOFT_HULL_DAMAGE, com.cbcatfix.balance.BalanceDefaults.SOFT_SPEED_LOSS);

        final float hullDamage;
        final double speedLoss;

        Obstacle(float hullDamage, double speedLoss) {
            this.hullDamage = hullDamage;
            this.speedLoss = speedLoss;
        }
    }
}
