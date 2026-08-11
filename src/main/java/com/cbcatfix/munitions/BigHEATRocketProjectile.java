package com.cbcatfix.munitions;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import com.dsvv.cbcat.cannon.medium_rocketpod.munitions.AbstractMediumFuzedRocket;
import com.dsvv.cbcat.cannon.heavy_autocannon.munitions.he_shell.HA_HEProjectile;
import com.dsvv.cbcat.registry.EntityRegister;
import rbasamoyai.createbigcannons.config.CBCConfigs;
import rbasamoyai.createbigcannons.CreateBigCannons;
import rbasamoyai.createbigcannons.munitions.ShellExplosion;
import com.cbcatfix.CbcatFixHelper;

public class BigHEATRocketProjectile extends AbstractMediumFuzedRocket<HA_HEProjectile> {
    public BigHEATRocketProjectile(EntityType<? extends BigHEATRocketProjectile> type, Level level) {
        super(type, level, 1.0d, 9.0d, EntityRegister.HA_HE_PROJECTILE);
    }

    @Override
    public void tick() {
        super.tick();
        CbcatFixHelper.spawnBigRocketParticles(this);
    }

    @Override
    protected void detonate(Position pos) {
        if (!this.level().isClientSide()) {
            ShellExplosion explosion = new ShellExplosion(
                this.level(),
                this,
                this.indirectArtilleryFire(false),
                pos.x(), pos.y(), pos.z(),
                8.0f,
                10.0f,
                false,
                CBCConfigs.server().munitions.damageRestriction.get().explosiveInteraction()
            );
            CreateBigCannons.handleCustomExplosion(this.level(), explosion);
            Vec3 start = new Vec3(pos.x(), pos.y(), pos.z());
            Vec3 dir = this.getDeltaMovement().normalize();
            if (dir.lengthSqr() < 0.001) {
                dir = new Vec3(0, -1, 0); // fallback downwards if velocity is zero
            }
            double maxDist = 16.0;
            double penPower = 8.0;

            for (double d = 0.5; d <= maxDist && penPower > 0; d += 0.5) {
                Vec3 point = start.add(dir.scale(d));
                BlockPos blockPos = BlockPos.containing(point);
                BlockState state = this.level().getBlockState(blockPos);

                if (!state.isAir()) {
                    float hardness = state.getDestroySpeed(this.level(), blockPos);
                    if (hardness >= 0) { // not bedrock
                        double cost = 0.5;
                        if (hardness > 10.0) cost = 1.5; // steel/obsidian/etc.
                        else if (hardness > 3.0) cost = 0.8; // iron/stone/etc.

                        penPower -= cost;
                        if (penPower >= 0) {
                            this.level().destroyBlock(blockPos, false);
                        }
                    } else {
                        break; // bedrock
                    }
                }
                AABB aabb = new AABB(point.x - 0.75, point.y - 0.75, point.z - 0.75, point.x + 0.75, point.y + 0.75, point.z + 0.75);
                List<Entity> entities = this.level().getEntities(this, aabb);
                for (Entity entity : entities) {
                    entity.hurt(this.level().damageSources().magic(), 80.0f); // 80 armor-penetrating damage
                }
            }

        }
    }

    @Override
    protected double getDefaultGravity() {
        return -0.015d;
    }
}
