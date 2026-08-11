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
        double maxDist = 4.0;
        int blocksDestroyed = 0;
        BlockPos lastPos = BlockPos.containing(start);

        for (double d = 0.5; d <= maxDist && blocksDestroyed < 2; d += 0.5) {
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
        AABB spallBox = new AABB(
            spallOrigin.x - 2.0, spallOrigin.y - 2.0, spallOrigin.z - 2.0,
            spallOrigin.x + 2.0, spallOrigin.y + 2.0, spallOrigin.z + 2.0
        );
        List<Entity> entities = this.level().getEntities(this, spallBox);
        for (Entity entity : entities) {
            entity.hurt(this.level().damageSources().magic(), 35.0f); // 35 magic/spall damage
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
        return this.getAllProperties().bigCannonProperties();
    }

    @Override
    public EntityDamagePropertiesComponent getDamageProperties() {
        return this.getAllProperties().damage();
    }

    @Override
    protected BallisticPropertiesComponent getBallisticProperties() {
        return this.getAllProperties().ballistics();
    }

    protected BigCannonCommonShellProperties getAllProperties() {
        return CBCMunitionPropertiesHandlers.COMMON_SHELL_BIG_CANNON_PROJECTILE.getPropertiesOf(this);
    }
}
