package com.cbcatfix.munitions;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import rbasamoyai.createbigcannons.index.CBCMunitionPropertiesHandlers;
import rbasamoyai.createbigcannons.munitions.big_cannon.FuzedBigCannonProjectile;
import rbasamoyai.createbigcannons.munitions.big_cannon.config.BigCannonCommonShellProperties;
import rbasamoyai.createbigcannons.munitions.big_cannon.config.BigCannonFuzePropertiesComponent;
import rbasamoyai.createbigcannons.munitions.big_cannon.config.BigCannonProjectilePropertiesComponent;
import rbasamoyai.createbigcannons.munitions.config.components.BallisticPropertiesComponent;
import rbasamoyai.createbigcannons.munitions.config.components.EntityDamagePropertiesComponent;
import com.cbcatfix.config.CbcatFixConfig;

public class HESHShellProjectile extends FuzedBigCannonProjectile {
    public HESHShellProjectile(EntityType<? extends HESHShellProjectile> type, Level level) {
        super(type, level);
    }

    @Override
    protected void detonate(Position pos) {
        Vec3 start = new Vec3(pos.x(), pos.y(), pos.z());
        Vec3 dir = this.getDeltaMovement().normalize();
        if (dir.lengthSqr() < 0.001) {
            dir = new Vec3(0, -1, 0); // fallback down
        }
        double maxDist = CbcatFixConfig.HESH_SPALL_DISTANCE.get();
        int blocksDestroyed = 0;
        BlockPos lastPos = BlockPos.containing(start);

        for (double d = 0.5; d <= maxDist && blocksDestroyed < CbcatFixConfig.HESH_BLOCKS_DESTROYED.get(); d += 0.5) {
            Vec3 point = start.add(dir.scale(d));
            BlockPos blockPos = BlockPos.containing(point);
            BlockState state = this.level().getBlockState(blockPos);

            if (!state.isAir()) {
                float hardness = state.getDestroySpeed(this.level(), blockPos);
                if (hardness >= 0) { // not bedrock
                    this.level().destroyBlock(blockPos, false);
                    blocksDestroyed++;
                    lastPos = blockPos;
                } else {
                    break; // bedrock
                }
            }
        }
        Vec3 spallOrigin = Vec3.atCenterOf(lastPos).add(dir.scale(1.5));
        double spallRadius = CbcatFixConfig.HESH_SPALL_RADIUS.get();
        AABB spallBox = new AABB(
            spallOrigin.x - spallRadius, spallOrigin.y - spallRadius, spallOrigin.z - spallRadius,
            spallOrigin.x + spallRadius, spallOrigin.y + spallRadius, spallOrigin.z + spallRadius
        );
        List<Entity> entities = this.level().getEntities(this, spallBox);
        for (Entity entity : entities) {
            entity.hurt(this.level().damageSources().magic(), CbcatFixConfig.HESH_SPALL_DAMAGE.get().floatValue());
        }
        this.level().playSound(
            null,
            pos.x(), pos.y(), pos.z(),
            net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE,
            net.minecraft.sounds.SoundSource.BLOCKS,
            4.0f,
            (1.0f + (this.level().getRandom().nextFloat() - this.level().getRandom().nextFloat()) * 0.2f) * 0.7f
        );
        if (this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            serverLevel.sendParticles(
                net.minecraft.core.particles.ParticleTypes.EXPLOSION_EMITTER,
                pos.x(), pos.y(), pos.z(),
                1, 0.0, 0.0, 0.0, 0.0
            );
        }
    }

    @Override
    public BlockState getRenderedBlockState() {
        return CbcatFixMunitions.HESH_SHELL.get().defaultBlockState().setValue(BlockStateProperties.FACING, Direction.NORTH);
    }

    @Override
    protected BigCannonFuzePropertiesComponent getFuzeProperties() {
        return this.getAllProperties().fuze();
    }

    @Override
    protected BigCannonProjectilePropertiesComponent getBigCannonProjectileProperties() {
        return CbcatFixConfig.HESH_SHELL.bigCannonProperties();
    }

    @Override
    public EntityDamagePropertiesComponent getDamageProperties() {
        return CbcatFixConfig.HESH_SHELL.damageProperties();
    }

    @Override
    protected BallisticPropertiesComponent getBallisticProperties() {
        return CbcatFixConfig.HESH_SHELL.ballisticProperties();
    }

    protected BigCannonCommonShellProperties getAllProperties() {
        return CBCMunitionPropertiesHandlers.COMMON_SHELL_BIG_CANNON_PROJECTILE.getPropertiesOf(this);
    }
}
