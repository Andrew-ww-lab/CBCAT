package com.cbcatfix.rocket;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import rbasamoyai.createbigcannons.munitions.big_cannon.FuzedProjectileBlock;

import java.util.List;

/** A real CBC placed projectile block; only rocket data/model differences live here. */
public class RocketBlock extends FuzedProjectileBlock<RocketBlockEntity, RocketBlockProjectile> {
    public static final MapCodec<RocketBlock> CODEC = simpleCodec(RocketBlock::new);
    public static final IntegerProperty TIER = IntegerProperty.create("tier", 0, 2);
    public static final BooleanProperty INDEPENDENT = BooleanProperty.create("independent");

    public RocketBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState()
            .setValue(TIER, RocketBalance.Tier.SMALL.ordinal())
            .setValue(INDEPENDENT, false));
    }

    @Override
    protected MapCodec<? extends DirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(TIER, INDEPENDENT);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int count = level.getBlockEntity(pos) instanceof RocketBlockEntity rocketBlockEntity
            ? rocketBlockEntity.getRockets().size() : 1;
        if (context instanceof EntityCollisionContext entityContext
            && entityContext.getEntity() instanceof Player player) {
            return RocketTargetResolver.resolveViewed(level, pos, player)
                .filter(target -> target.root().pos().equals(pos))
                .map(target -> RocketGeometry.bodyShape(
                    tier(state), RocketGeometry.horizontalFacing(state.getValue(FACING)),
                    target.slot(), state.getValue(INDEPENDENT)
                ))
                .orElseGet(() -> RocketGeometry.shapes(
                    tier(state), RocketGeometry.horizontalFacing(state.getValue(FACING)),
                    count, state.getValue(INDEPENDENT)
                ));
        }
        return RocketGeometry.shapes(
            tier(state), RocketGeometry.horizontalFacing(state.getValue(FACING)),
            count, state.getValue(INDEPENDENT)
        );
    }

    @Override
    public VoxelShape getCollisionShape(
        BlockState state, BlockGetter level, BlockPos pos, CollisionContext context
    ) {
        boolean independent = state.getValue(INDEPENDENT);
        int count = level.getBlockEntity(pos) instanceof RocketBlockEntity rocketBlockEntity
            ? Math.max(1, rocketBlockEntity.getRockets().size()) : 1;
        return RocketGeometry.shapes(
            tier(state), RocketGeometry.horizontalFacing(state.getValue(FACING)), count, independent
        );
    }

    @Override
    public ItemInteractionResult useItemOn(
        ItemStack held,
        BlockState state,
        Level level,
        BlockPos pos,
        Player player,
        InteractionHand hand,
        BlockHitResult hit
    ) {
        if (RocketGroundPlacement.isRocket(held)
            && RocketBalance.tierForRocketItem(held.getItem()) == tier(state)
            && level.getBlockEntity(pos) instanceof RocketBlockEntity rocketBlockEntity) {
            if (!level.isClientSide() && rocketBlockEntity.addRocket(held) && !player.getAbilities().instabuild) {
                held.shrink(1);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }
        if (level.getBlockEntity(pos) instanceof RocketBlockEntity rocketBlockEntity) {
            int slot = selectedSlot(level, state, pos, hit, player, rocketBlockEntity);
            rocketBlockEntity.selectRocket(slot);
            try {
                return super.useItemOn(held, state, level, pos, player, hand, hit);
            } finally {
                rocketBlockEntity.clearSelection();
            }
        }
        return super.useItemOn(held, state, level, pos, player, hand, hit);
    }

    @Override
    public InteractionResult useWithoutItem(
        BlockState state,
        Level level,
        BlockPos pos,
        Player player,
        BlockHitResult hit
    ) {
        if (level.getBlockEntity(pos) instanceof RocketBlockEntity rocketBlockEntity) {
            int slot = selectedSlot(level, state, pos, hit, player, rocketBlockEntity);
            rocketBlockEntity.selectRocket(slot);
            InteractionResult inherited;
            try {
                inherited = super.useWithoutItem(state, level, pos, player, hit);
            } finally {
                rocketBlockEntity.clearSelection();
            }
            if (inherited != InteractionResult.PASS) {
                return inherited;
            }
            if (level.isClientSide()) {
                return InteractionResult.sidedSuccess(true);
            }

            ItemStack removed = rocketBlockEntity.removeRocket(slot);
            if (removed.isEmpty()) {
                return InteractionResult.PASS;
            }
            if (!player.addItem(removed)) {
                player.drop(removed, false);
            }
            level.playSound(
                null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.7f, 1.0f
            );
            if (rocketBlockEntity.getRockets().isEmpty()) {
                level.removeBlock(pos, false);
            }
            return InteractionResult.SUCCESS;
        }
        return super.useWithoutItem(state, level, pos, player, hit);
    }

    @Override
    public RocketBlockProjectile getProjectile(Level level, BlockPos pos, BlockState state) {
        RocketBlockProjectile projectile = (RocketBlockProjectile) super.getProjectile(level, pos, state);
        if (level.getBlockEntity(pos) instanceof RocketBlockEntity rocketBlockEntity) {
            projectile.setRocket(rocketBlockEntity.getSelectedRocket());
        }
        return projectile;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        BlockEntity blockEntity = params.getOptionalParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof RocketBlockEntity rocketBlockEntity) {
            return rocketBlockEntity.copyRockets();
        }
        return List.of(); // An adapter without stored rockets must never manufacture a wrapper item.
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof RocketBlockEntity rocketBlockEntity) {
            ItemStack rocket = rocketBlockEntity.getPrimaryRocket();
            if (!rocket.isEmpty()) {
                return rocket.copyWithCount(1);
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isBaseFuze() {
        return false;
    }

    @Override
    public EntityType<? extends RocketBlockProjectile> getAssociatedEntityType() {
        return com.cbcatfix.munitions.CbcatFixMunitions.ROCKET_BLOCK_PROJECTILE.get();
    }

    @Override
    public Class<RocketBlockEntity> getBlockEntityClass() {
        return RocketBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends RocketBlockEntity> getBlockEntityType() {
        return com.cbcatfix.munitions.CbcatFixMunitions.ROCKET_BLOCK_ENTITY.get();
    }

    @Override
    public <S extends BlockEntity> BlockEntityTicker<S> getTicker(
        Level level,
        BlockState state,
        BlockEntityType<S> type
    ) {
        return createTickerHelper(
            type,
            com.cbcatfix.munitions.CbcatFixMunitions.ROCKET_BLOCK_ENTITY.get(),
            (tickLevel, pos, tickState, blockEntity) -> blockEntity.tick()
        );
    }

    @Override
    public void tick(BlockState state, net.minecraft.server.level.ServerLevel level, BlockPos pos, net.minecraft.util.RandomSource random) {
        if (!(level.getBlockEntity(pos) instanceof RocketBlockEntity rocketBlockEntity)
            || rocketBlockEntity.getRockets().isEmpty()) {
            super.tick(state, level, pos, random);
            return;
        }
        for (int slot = 0; slot < rocketBlockEntity.getRockets().size(); slot++) {
            rocketBlockEntity.selectRocket(slot);
            super.tick(state, level, pos, random);
            if (level.getBlockState(pos).isAir()) {
                return;
            }
        }
        rocketBlockEntity.clearSelection();
    }

    public static RocketBalance.Tier tier(BlockState state) {
        return RocketBalance.Tier.values()[Math.clamp(state.getValue(TIER), 0, RocketBalance.Tier.values().length - 1)];
    }

    public static int capacity(RocketBalance.Tier tier) {
        return switch (tier) {
            case SMALL -> 12;
            case MEDIUM -> 4;
            case BIG -> 1;
        };
    }

    @Override
    public BlockState getRotatedBlockState(BlockState state, Direction targetedFace) {
        Direction current = RocketGeometry.horizontalFacing(state.getValue(FACING));
        if (targetedFace.getAxis() != Direction.Axis.Y) {
            return state.setValue(FACING, current);
        }
        return state.setValue(FACING, current.getClockWise());
    }

    @Override
    public BlockState updateAfterWrenched(BlockState rotatedState, UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState oldState = level.getBlockState(pos);
        RocketBalance.Tier tier = tier(oldState);
        Direction oldFacing = RocketGeometry.horizontalFacing(oldState.getValue(FACING));
        Direction newFacing = rotatedState.getValue(FACING);
        if (!newFacing.getAxis().isHorizontal()) {
            return oldState;
        }
        if (oldFacing == newFacing || !RocketFootprint.canPlace(level, pos, newFacing, tier)) {
            return oldFacing == newFacing ? rotatedState : oldState;
        }
        if (!level.isClientSide()) {
            RocketFootprint.remove(level, pos, oldFacing, tier);
            RocketFootprint.place(level, pos, newFacing, tier);
        }
        return net.minecraft.world.level.block.Block.updateFromNeighbourShapes(rotatedState, level, pos);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            RocketFootprint.remove(level, pos, state.getValue(FACING), tier(state));
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    private static int selectedSlot(
        Level level,
        BlockState state,
        BlockPos pos,
        BlockHitResult hit,
        Player player,
        RocketBlockEntity blockEntity
    ) {
        return RocketTargetResolver.resolveInteraction(level, hit, player)
            .filter(target -> target.root().pos().equals(pos))
            .map(RocketTargetResolver.Target::slot)
            .orElseGet(() -> RocketGeometry.closestSlot(
                tier(state), RocketGeometry.horizontalFacing(state.getValue(FACING)),
                blockEntity.getRockets(),
                hit.getLocation().subtract(pos.getX(), pos.getY(), pos.getZ())
            ));
    }
}
