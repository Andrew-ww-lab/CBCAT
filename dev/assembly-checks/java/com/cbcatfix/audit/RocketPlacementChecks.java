package com.cbcatfix.audit;

import com.cbcatfix.rocket.*;
import com.cbcatfix.munitions.CbcatFixMunitions;
import net.createmod.catnip.levelWrappers.SchematicLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/** Simulation must match actual admission, preserve storage, and use the mounted/stack render frames. */
final class RocketPlacementChecks {
    private static int checks;
    static int run(ServerLevel world) {
        checks = 0;
        BlockPos pos = new BlockPos(0, 80, 0);
        for (var tier : RocketBalance.Tier.values()) {
            ItemStack held = switch (tier) {
                case SMALL -> com.dsvv.cbcat.registry.ItemRegister.HE_ROCKET_ITEM.get().getDefaultInstance();
                case MEDIUM -> com.dsvv.cbcat.registry.ItemRegister.MEDIUM_HE_ROCKET_ITEM.get().getDefaultInstance();
                case BIG -> CbcatFixMunitions.BIG_HE_ROCKET_ITEM.get().getDefaultInstance();
            };
            held.setCount(5);
            String rail = switch (tier) {
                case SMALL -> "cbc_at:steel_rocket_pod_rail";
                case MEDIUM -> "cbc_at:wrought_iron_medium_rocket_rail";
                case BIG -> "cbcatfix:big_rocket_rail";
            };
            String breechId = tier == RocketBalance.Tier.SMALL ? "cbc_at:steel_rocket_pod_breech" : rail + "_breech";
            for (Direction facing : Direction.values()) {
                var level = new SchematicLevel(world) { @Override public void blockEntityChanged(BlockPos p) {} };
                level.setBlock(pos, BuiltInRegistries.BLOCK.get(ResourceLocation.parse(breechId)).defaultBlockState()
                    .setValue(BlockStateProperties.FACING, facing), 3);
                for (int i = 1; i < 3; i++) level.setBlock(pos.relative(facing, i),
                    BuiltInRegistries.BLOCK.get(ResourceLocation.parse(rail)).defaultBlockState().setValue(BlockStateProperties.FACING, facing), 3);
                var breech = level.getBlockEntity(pos);
                var storage = (MountedRocketStorage) breech;
                for (int slot = 0; slot < storage.cbcatfix$slotCount(); slot++) {
                    var mount = RocketMounts.transform(breech, slot);
                    var selection = new MountedRocketInteraction.Selection(slot, mount, mount.nose());
                    var before = breech.saveWithFullMetadata(world.registryAccess());
                    var candidate = RocketPlacementCandidate.launcher(breech, selection, held);
                    check(candidate != null && candidate.center().equals(mount.center()) && candidate.facing() == facing, "exact mounted frame");
                    check(before.equals(breech.saveWithFullMetadata(world.registryAccess())) && held.getCount() == 5, "preview has no mutations");
                    check(RocketPlacementCandidate.launcher(breech, selection, new ItemStack(Items.STICK)) == null, "wrong item");
                    ItemStack wrongSize = tier == RocketBalance.Tier.SMALL ? CbcatFixMunitions.BIG_HE_ROCKET_ITEM.get().getDefaultInstance()
                        : com.dsvv.cbcat.registry.ItemRegister.HE_ROCKET_ITEM.get().getDefaultInstance();
                    check(RocketPlacementCandidate.launcher(breech, selection, wrongSize) == null, "wrong size");
                    var remainder = new MountedRocketItemHandler(breech).insertItem(slot, held, false);
                    check(remainder.getCount() == 4, "server accepts preview");
                    check(RocketPlacementCandidate.launcher(breech, selection, held) == null, "occupied exact slot hidden");
                    new MountedRocketItemHandler(breech).extractItem(slot, 1, false);
                }
                var selection = new MountedRocketInteraction.Selection(0, RocketMounts.transform(breech, 0), net.minecraft.world.phys.Vec3.ZERO);
                breech.setRemoved();
                check(RocketPlacementCandidate.launcher(breech, selection, held) == null, "removed/detached launcher");
            }
            for (Direction facing : Direction.Plane.HORIZONTAL) {
                var level = new SchematicLevel(world) { @Override public void blockEntityChanged(BlockPos p) {} };
                var state = CbcatFixMunitions.ROCKET_BLOCK.get().defaultBlockState()
                    .setValue(RocketBlock.TIER, tier.ordinal()).setValue(RocketBlock.FACING, facing);
                level.setBlock(pos, state, 3);
                var be = (RocketBlockEntity) level.getBlockEntity(pos);
                var root = new RocketTargetResolver.Root(pos, state, be);
                for (int slot = 0; slot < RocketBlock.capacity(tier); slot++) {
                    var candidate = RocketPlacementCandidate.pile(root, held);
                    check(candidate != null && candidate.center().equals(RocketGeometry.bodyCenter(tier, facing, slot, false)), "next stack frame");
                    check(be.getRockets().size() == slot && held.getCount() == 5, "stack preview read only");
                    check(be.addRocket(held), "stack accepts predicted rocket");
                }
                check(RocketPlacementCandidate.pile(root, held) == null, "full stack hidden");
                check(RocketPlacementCandidate.pile(root, ItemStack.EMPTY) == null, "empty hand hidden");
                BlockPos fresh = pos.offset(10, 0, 0);
                check(RocketGroundPlacement.canPlaceNew(level, fresh, facing, tier), "fresh footprint");
                level.setBlock(fresh, Blocks.STONE.defaultBlockState(), 3);
                check(!RocketGroundPlacement.canPlaceNew(level, fresh, facing, tier), "occupied root");
                level.setBlock(fresh, Blocks.AIR.defaultBlockState(), 3);
                if (tier != RocketBalance.Tier.SMALL) {
                    level.setBlock(fresh.relative(facing), Blocks.STONE.defaultBlockState(), 3);
                    check(!RocketGroundPlacement.canPlaceNew(level, fresh, facing, tier), "blocked extension");
                }
                be.setBlockState(state.setValue(RocketBlock.INDEPENDENT, true));
                check(!be.canAddRocket(held), "independent rocket cannot stack");
            }
        }
        System.out.println("CBCAT ROCKET HINT CHECKS PASSED: " + checks);
        return checks;
    }
    private static void check(boolean result, String label) {
        if (!result) throw new AssertionError(label);
        checks++;
    }
}
