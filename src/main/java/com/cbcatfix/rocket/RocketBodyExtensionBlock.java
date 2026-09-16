package com.cbcatfix.rocket;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.BlockHitResult;

/** Invisible reserved segment that forwards targeting and interaction to its rocket root. */
public class RocketBodyExtensionBlock extends DirectionalBlock implements IWrenchable {
    public static final MapCodec<RocketBodyExtensionBlock> CODEC = simpleCodec(RocketBodyExtensionBlock::new);
    public static final IntegerProperty TIER = RocketBlock.TIER;
    public static final BooleanProperty INDEPENDENT = RocketBlock.INDEPENDENT;

    public RocketBodyExtensionBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState()
            .setValue(TIER, RocketBalance.Tier.MEDIUM.ordinal())
            .setValue(INDEPENDENT, false));
    }

    @Override
    protected MapCodec<? extends DirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING, TIER, INDEPENDENT);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return rootShape(level, pos, state, context);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        RocketBalance.Tier tier = tier(state);
        boolean independent = state.getValue(INDEPENDENT);
        int count = independent ? 1 : RocketBlock.capacity(tier);
        // Collision must be state-only: Sable caches it without a complete world.
        return RocketGeometry.segmentShapes(
            tier, RocketGeometry.horizontalFacing(state.getValue(FACING)), count, independent
        );
    }

    @Override
    public VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    protected ItemInteractionResult useItemOn(
        ItemStack stack,
        BlockState state,
        Level level,
        BlockPos pos,
        Player player,
        InteractionHand hand,
        BlockHitResult hit
    ) {
        return RocketTargetResolver.findRoot(level, pos)
            .map(root -> ((RocketBlock) root.state().getBlock()).useItemOn(
                stack, root.state(), level, root.pos(), player, hand, rootHit(hit, root.pos())
            ))
            .orElse(ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION);
    }

    @Override
    protected InteractionResult useWithoutItem(
        BlockState state,
        Level level,
        BlockPos pos,
        Player player,
        BlockHitResult hit
    ) {
        return RocketTargetResolver.findRoot(level, pos)
            .map(root -> ((RocketBlock) root.state().getBlock()).useWithoutItem(
                root.state(), level, root.pos(), player, rootHit(hit, root.pos())
            ))
            .orElse(InteractionResult.PASS);
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        return RocketTargetResolver.findRoot(context.getLevel(), context.getClickedPos())
            .map(root -> ((RocketBlock) root.state().getBlock()).onWrenched(
                root.state(), rootContext(context, root.pos())
            ))
            .orElse(InteractionResult.PASS);
    }

    @Override
    public InteractionResult onSneakWrenched(BlockState state, UseOnContext context) {
        return RocketTargetResolver.findRoot(context.getLevel(), context.getClickedPos())
            .map(root -> ((RocketBlock) root.state().getBlock()).onSneakWrenched(
                root.state(), rootContext(context, root.pos())
            ))
            .orElse(InteractionResult.PASS);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        return RocketTargetResolver.findRoot(level, pos)
            .map(root -> root.state().getDestroyProgress(player, level, root.pos()))
            .orElseGet(() -> super.getDestroyProgress(state, player, level, pos));
    }

    private static VoxelShape rootShape(
        BlockGetter level,
        BlockPos pos,
        BlockState fallbackState,
        CollisionContext context
    ) {
        return RocketTargetResolver.findRoot(level, pos)
            .map(root -> ((RocketBlock) root.state().getBlock())
                .getShape(root.state(), level, root.pos(), context)
                .move(
                    root.pos().getX() - pos.getX(),
                    root.pos().getY() - pos.getY(),
                    root.pos().getZ() - pos.getZ()
                ))
            .orElseGet(() -> {
                RocketBalance.Tier tier = tier(fallbackState);
                return RocketGeometry.segmentShape(
                    tier, RocketGeometry.horizontalFacing(fallbackState.getValue(FACING))
                );
            });
    }

    private static RocketBalance.Tier tier(BlockState state) {
        return RocketBalance.Tier.values()[Math.clamp(
            state.getValue(TIER), 0, RocketBalance.Tier.values().length - 1
        )];
    }

    private static BlockHitResult rootHit(BlockHitResult hit, BlockPos rootPos) {
        return new BlockHitResult(hit.getLocation(), hit.getDirection(), rootPos, hit.isInside());
    }

    private static UseOnContext rootContext(UseOnContext context, BlockPos rootPos) {
        return new UseOnContext(
            context.getLevel(), context.getPlayer(), context.getHand(), context.getItemInHand(),
            new BlockHitResult(
                context.getClickLocation(), context.getClickedFace(), rootPos, context.isInside()
            )
        );
    }
}
