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
import rbasamoyai.createbigcannons.CreateBigCannons;
import rbasamoyai.createbigcannons.config.CBCConfigs;
import rbasamoyai.createbigcannons.index.CBCMunitionPropertiesHandlers;
import rbasamoyai.createbigcannons.munitions.ShellExplosion;
import rbasamoyai.createbigcannons.munitions.big_cannon.FuzedBigCannonProjectile;
import rbasamoyai.createbigcannons.munitions.big_cannon.config.BigCannonCommonShellProperties;
import rbasamoyai.createbigcannons.munitions.big_cannon.config.BigCannonFuzePropertiesComponent;
import rbasamoyai.createbigcannons.munitions.big_cannon.config.BigCannonProjectilePropertiesComponent;
import rbasamoyai.createbigcannons.munitions.config.components.BallisticPropertiesComponent;
import rbasamoyai.createbigcannons.munitions.config.components.EntityDamagePropertiesComponent;

public class HEATShellProjectile extends FuzedBigCannonProjectile {
    public HEATShellProjectile(EntityType<? extends HEATShellProjectile> type, Level level) {
        super(type, level);
    }

    @Override
    protected void detonate(Position pos) {
        ShellExplosion explosion = new ShellExplosion(
            this.level(),
            this,
            this.indirectArtilleryFire(false),
            pos.x(), pos.y(), pos.z(),
            2.5f,
            2.5f,
            false,
            CBCConfigs.server().munitions.damageRestriction.get().explosiveInteraction()
        );
        CreateBigCannons.handleCustomExplosion(this.level(), explosion);
        Vec3 start = new Vec3(pos.x(), pos.y(), pos.z());
        Vec3 dir = this.getDeltaMovement().normalize();
        if (dir.lengthSqr() < 0.001) {
            dir = new Vec3(0, -1, 0); // fallback downwards if velocity is zero
        }
        double maxDist = 6.0;
        double penPower = 2.0;

        for (double d = 0.5; d <= maxDist && penPower > 0; d += 0.5) {
            Vec3 point = start.add(dir.scale(d));
            BlockPos blockPos = BlockPos.containing(point);
            BlockState state = this.level().getBlockState(blockPos);

            if (!state.isAir()) {
                float hardness = state.getDestroySpeed(this.level(), blockPos);
                if (hardness >= 0) { // not bedrock
                    double cost = 0.5;
                    if (hardness > 10.0) cost = 2.0; // steel/obsidian/etc.
                    else if (hardness > 3.0) cost = 1.0; // iron/stone/etc.

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
                entity.hurt(this.level().damageSources().magic(), 40.0f); // 40 armor-penetrating damage
            }
        }
    }

    @Override
    public BlockState getRenderedBlockState() {
        return CbcatFixMunitions.HEAT_SHELL.get().defaultBlockState().setValue(BlockStateProperties.FACING, Direction.NORTH);
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
