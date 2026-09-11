package com.cbcatfix.munitions;

import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import rbasamoyai.createbigcannons.index.CBCMunitionPropertiesHandlers;
import rbasamoyai.createbigcannons.munitions.big_cannon.FuzedBigCannonProjectile;
import rbasamoyai.createbigcannons.munitions.big_cannon.config.BigCannonCommonShellProperties;
import rbasamoyai.createbigcannons.munitions.big_cannon.config.BigCannonFuzePropertiesComponent;
import rbasamoyai.createbigcannons.munitions.big_cannon.config.BigCannonProjectilePropertiesComponent;
import rbasamoyai.createbigcannons.munitions.config.components.BallisticPropertiesComponent;
import rbasamoyai.createbigcannons.munitions.config.components.EntityDamagePropertiesComponent;
import com.cbcatfix.config.CbcatFixConfig;

public class HEATShellProjectile extends FuzedBigCannonProjectile {
    public HEATShellProjectile(EntityType<? extends HEATShellProjectile> type, Level level) {
        super(type, level);
    }

    @Override
    protected void detonate(Position pos) {
        HeatEffect.detonate(this, pos, this.getDeltaMovement(), 1.0f, CbcatFixConfig.BIG_CANNON_HEAT);
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
        return CbcatFixConfig.HEAT_SHELL.bigCannonProperties();
    }

    @Override
    public EntityDamagePropertiesComponent getDamageProperties() {
        return CbcatFixConfig.HEAT_SHELL.damageProperties();
    }

    @Override
    protected BallisticPropertiesComponent getBallisticProperties() {
        return CbcatFixConfig.HEAT_SHELL.ballisticProperties();
    }

    protected BigCannonCommonShellProperties getAllProperties() {
        return CBCMunitionPropertiesHandlers.COMMON_SHELL_BIG_CANNON_PROJECTILE.getPropertiesOf(this);
    }
}
