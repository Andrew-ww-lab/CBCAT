package com.cbcatfix.munitions;

import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import rbasamoyai.createbigcannons.CreateBigCannons;
import rbasamoyai.createbigcannons.config.CBCConfigs;
import rbasamoyai.createbigcannons.index.CBCEntityTypes;
import rbasamoyai.createbigcannons.index.CBCMunitionPropertiesHandlers;
import rbasamoyai.createbigcannons.munitions.ShellExplosion;
import rbasamoyai.createbigcannons.munitions.big_cannon.FuzedBigCannonProjectile;
import rbasamoyai.createbigcannons.munitions.big_cannon.config.BigCannonCommonShellProperties;
import rbasamoyai.createbigcannons.munitions.big_cannon.config.BigCannonFuzePropertiesComponent;
import rbasamoyai.createbigcannons.munitions.big_cannon.config.BigCannonProjectilePropertiesComponent;
import rbasamoyai.createbigcannons.munitions.big_cannon.shrapnel.ShrapnelBurst;
import rbasamoyai.createbigcannons.munitions.config.components.BallisticPropertiesComponent;
import rbasamoyai.createbigcannons.munitions.config.components.EntityDamagePropertiesComponent;

public class FlakShellProjectile extends FuzedBigCannonProjectile {
    public FlakShellProjectile(EntityType<? extends FlakShellProjectile> type, Level level) {
        super(type, level);
    }

    @Override
    protected void detonate(Position pos) {
        ShellExplosion explosion = new ShellExplosion(
            this.level(),
            this,
            this.indirectArtilleryFire(false),
            pos.x(), pos.y(), pos.z(),
            2.0f,
            2.0f,
            false,
            CBCConfigs.server().munitions.damageRestriction.get().explosiveInteraction()
        );
        CreateBigCannons.handleCustomExplosion(this.level(), explosion);
        ShrapnelBurst burst = CBCEntityTypes.SHRAPNEL_BURST.get().create(this.level());
        if (burst != null) {
            burst.setPos(pos.x(), pos.y(), pos.z());
            RandomSource random = this.level().getRandom();
            double speedBase = this.getDeltaMovement().length();
            if (speedBase < 0.1) speedBase = 1.0;

            for (int i = 0; i < 30; i++) {
                double u = random.nextDouble();
                double v = random.nextDouble();
                double theta = u * 2.0 * Math.PI;
                double phi = Math.acos(2.0 * v - 1.0);
                double sinPhi = Math.sin(phi);
                double dx = sinPhi * Math.cos(theta);
                double dy = sinPhi * Math.sin(theta);
                double dz = Math.cos(phi);

                double speed = speedBase * (1.2 + 0.3 * random.nextDouble());
                dx *= speed;
                dy *= speed;
                dz *= speed;
                double rx = (random.nextDouble() - random.nextDouble()) * 0.0625;
                double ry = (random.nextDouble() - random.nextDouble()) * 0.0625;
                double rz = (random.nextDouble() - random.nextDouble()) * 0.0625;

                burst.addSubProjectile(rx, ry, rz, dx, dy, dz);
            }
            this.level().addFreshEntity(burst);
        }
        if (!this.level().isClientSide()) {
            RandomSource random = this.level().getRandom();
            double speedBase = this.getDeltaMovement().length();
            if (speedBase < 0.1) speedBase = 1.0;

            for (int i = 0; i < 45; i++) {
                rbasamoyai.createbigcannons.munitions.autocannon.ap_round.APAutocannonProjectile apRound =
                    rbasamoyai.createbigcannons.index.CBCEntityTypes.AP_AUTOCANNON.get().create(this.level());
                if (apRound != null) {
                    double u = random.nextDouble();
                    double v = random.nextDouble();
                    double theta = u * 2.0 * Math.PI;
                    double phi = Math.acos(2.0 * v - 1.0);
                    double sinPhi = Math.sin(phi);
                    double dx = sinPhi * Math.cos(theta);
                    double dy = sinPhi * Math.sin(theta);
                    double dz = Math.cos(phi);

                    double speed = speedBase * (3.0 + 2.0 * random.nextDouble());
                    dx *= speed;
                    dy *= speed;
                    dz *= speed;

                    apRound.setPos(pos.x(), pos.y(), pos.z());
                    apRound.setDeltaMovement(dx, dy, dz);
                    apRound.setOrientation(new Vec3(dx, dy, dz).normalize());
                    apRound.setTracer(true);

                    this.level().addFreshEntity(apRound);
                }
            }
        }
        if (this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            serverLevel.sendParticles(
                net.minecraft.core.particles.ParticleTypes.CAMPFIRE_COSY_SMOKE,
                pos.x(), pos.y(), pos.z(),
                25, 0.5, 0.5, 0.5, 0.05
            );
            serverLevel.sendParticles(
                net.minecraft.core.particles.ParticleTypes.EXPLOSION,
                pos.x(), pos.y(), pos.z(),
                8, 0.3, 0.3, 0.3, 0.0
            );
            serverLevel.sendParticles(
                net.minecraft.core.particles.ParticleTypes.CRIT,
                pos.x(), pos.y(), pos.z(),
                40, 0.4, 0.4, 0.4, 0.2
            );
        }
    }

    @Override
    public BlockState getRenderedBlockState() {
        return CbcatFixMunitions.FLAK_SHELL.get().defaultBlockState().setValue(BlockStateProperties.FACING, Direction.NORTH);
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
