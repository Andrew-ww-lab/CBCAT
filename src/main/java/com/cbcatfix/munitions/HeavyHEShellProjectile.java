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
import com.cbcatfix.config.CbcatFixConfig;

public class HeavyHEShellProjectile extends FuzedBigCannonProjectile {
    public HeavyHEShellProjectile(EntityType<? extends HeavyHEShellProjectile> type, Level level) {
        super(type, level);
    }

    @Override
    public void tick() {
        Vec3 velocity = this.getDeltaMovement();
        double speed = velocity.length();
        double maximumSpeed = CbcatFixConfig.value(CbcatFixConfig.HEAVY_HE_MAX_SPEED);
        if (speed > maximumSpeed) {
            this.setDeltaMovement(velocity.scale(maximumSpeed / speed));
        }
        super.tick();
    }

    @Override
    protected void detonate(Position pos) {
        float blockPower = CbcatFixConfig.value(CbcatFixConfig.HEAVY_HE_BLOCK_POWER).floatValue();
        float entityPower = CbcatFixConfig.value(CbcatFixConfig.HEAVY_HE_ENTITY_POWER).floatValue();

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
        return CbcatFixConfig.HEAVY_HE_SHELL.bigCannonProperties();
    }

    @Override
    public EntityDamagePropertiesComponent getDamageProperties() {
        return CbcatFixConfig.HEAVY_HE_SHELL.damageProperties();
    }

    @Override
    protected BallisticPropertiesComponent getBallisticProperties() {
        return CbcatFixConfig.HEAVY_HE_SHELL.ballisticProperties();
    }

    protected BigCannonCommonShellProperties getAllProperties() {
        return CBCMunitionPropertiesHandlers.COMMON_SHELL_BIG_CANNON_PROJECTILE.getPropertiesOf(this);
    }
}
