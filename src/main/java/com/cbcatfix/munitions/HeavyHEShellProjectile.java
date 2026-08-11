package com.cbcatfix.munitions;

import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
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

public class HeavyHEShellProjectile extends FuzedBigCannonProjectile {
    public HeavyHEShellProjectile(EntityType<? extends HeavyHEShellProjectile> type, Level level) {
        super(type, level);
    }

    @Override
    public void tick() {
        Vec3 velocity = this.getDeltaMovement();
        double speed = velocity.length();
        if (speed > 4.0) { // Capped at 80 m/s (4.0 blocks/tick)
            this.setDeltaMovement(velocity.scale(4.0 / speed));
        }
        super.tick();
    }

    @Override
    protected void detonate(Position pos) {
        BigCannonCommonShellProperties properties = this.getAllProperties();
        float blockPower = properties.explosion().blockDamagePower() * 2.0f;
        float entityPower = properties.explosion().entityDamagePower() * 2.0f;

        ShellExplosion explosion = new ShellExplosion(
            this.level(),
            this,
            this.indirectArtilleryFire(false),
            pos.x(), pos.y(), pos.z(),
            blockPower,
            entityPower,
            false,
            CBCConfigs.server().munitions.damageRestriction.get().explosiveInteraction()
        );
        CreateBigCannons.handleCustomExplosion(this.level(), explosion);
    }

    @Override
    public BlockState getRenderedBlockState() {
        return CbcatFixMunitions.HEAVY_HE_SHELL.get().defaultBlockState().setValue(BlockStateProperties.FACING, Direction.NORTH);
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
