package com.cbcatfix.rocket;

import com.cbcatfix.config.CbcatFixConfig;
import com.cbcatfix.mixin.RocketDetonationInvoker;
import com.cbcatfix.munitions.CbcatFixMunitions;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import rbasamoyai.createbigcannons.index.CBCMunitionPropertiesHandlers;
import rbasamoyai.createbigcannons.munitions.AbstractCannonProjectile;
import rbasamoyai.createbigcannons.munitions.big_cannon.FuzedBigCannonProjectile;
import rbasamoyai.createbigcannons.munitions.big_cannon.config.BigCannonCommonShellProperties;
import rbasamoyai.createbigcannons.munitions.big_cannon.config.BigCannonFuzePropertiesComponent;
import rbasamoyai.createbigcannons.munitions.big_cannon.config.BigCannonProjectilePropertiesComponent;
import rbasamoyai.createbigcannons.munitions.config.components.BallisticPropertiesComponent;
import rbasamoyai.createbigcannons.munitions.config.components.EntityDamagePropertiesComponent;

/**
 * Narrow type adapter required by CBC's ProjectileBlock generic bound. It hands
 * flight back to the original CBCAT rocket immediately; it is not a second
 * missile implementation.
 */
public class RocketBlockProjectile extends FuzedBigCannonProjectile {
    private ItemStack rocket = ItemStack.EMPTY;

    public RocketBlockProjectile(EntityType<? extends RocketBlockProjectile> type, Level level) {
        super(type, level);
    }

    public void setRocket(ItemStack rocket) {
        this.rocket = rocket.copyWithCount(1);
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide() && this.convertToOriginalRocket()) {
            return;
        }
        super.tick();
    }

    private boolean convertToOriginalRocket() {
        AbstractCannonProjectile original = RocketProjectileFactory.create(this.rocket, this.level());
        if (original == null) {
            return false;
        }
        original.setPos(this.position());
        original.setDeltaMovement(this.getDeltaMovement());
        Vec3 direction = this.getDeltaMovement();
        if (direction.lengthSqr() > 1.0e-8) {
            original.setOrientation(direction.normalize());
        }
        if (!this.level().addFreshEntity(original)) {
            return false;
        }
        this.discard();
        return true;
    }

    @Override
    protected void detonate(Position position) {
        AbstractCannonProjectile original = RocketProjectileFactory.create(this.rocket, this.level());
        if (!(original instanceof RocketDetonationInvoker invoker)) {
            return;
        }
        original.setPos(position.x(), position.y(), position.z());
        original.setDeltaMovement(this.getDeltaMovement());
        Vec3 direction = this.getDeltaMovement();
        if (direction.lengthSqr() > 1.0e-8) {
            original.setOrientation(direction.normalize());
        }
        this.level().addFreshEntity(original);
        invoker.cbcatfix$detonate(position);
        original.discard();
    }

    @Override
    public BlockState getRenderedBlockState() {
        int tier = this.rocket.isEmpty()
            ? RocketBalance.Tier.SMALL.ordinal()
            : RocketBalance.tierForRocketItem(this.rocket.getItem()).ordinal();
        return CbcatFixMunitions.ROCKET_BLOCK.get().defaultBlockState()
            .setValue(BlockStateProperties.FACING, Direction.NORTH)
            .setValue(RocketBlock.TIER, tier);
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

    private BigCannonCommonShellProperties getAllProperties() {
        return CBCMunitionPropertiesHandlers.COMMON_SHELL_BIG_CANNON_PROJECTILE.getPropertiesOf(this);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put("Rocket", this.rocket.saveOptional(this.level().registryAccess()));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.rocket = ItemStack.parseOptional(this.level().registryAccess(), tag.getCompound("Rocket"));
    }
}
