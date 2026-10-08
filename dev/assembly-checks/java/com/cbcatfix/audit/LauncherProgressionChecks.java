package com.cbcatfix.audit;

import com.cbcatfix.rocket.*;
import com.dsvv.cbcat.cannon.rocketpod.contraption.MountedRocketPodContraption;
import com.dsvv.cbcat.cannon.medium_rocketpod.contraption.MountedMediumRocketRailContraption;
import com.simibubi.create.content.contraptions.AssemblyException;
import net.createmod.catnip.levelWrappers.SchematicLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import rbasamoyai.createbigcannons.cannon_control.contraption.AbstractMountedCannonContraption;
import rbasamoyai.createbigcannons.cannon_control.contraption.PitchOrientedContraptionEntity;

/** Native assembly and serialization, entirely in memory; never alters the loaded world. */
final class LauncherProgressionChecks {
    private static int checks;
    private static final BlockPos ROOT = new BlockPos(0, 80, 0);

    static int run(ServerLevel world) throws Exception {
        checks = 0;
        for (Direction facing : Direction.values()) {
            for (int length = 1; length <= 6; length++) {
                for (String module : new String[] {"", "rapid_cycling_module", "traverse_gear_module"}) {
                    var fixture = fixture(world);
                    lane(fixture, ROOT, facing, length, module);
                    var cannon = new MountedRocketPodContraption();
                    if (length < 2 || length > 3) {
                        rejects(cannon, fixture, facing, "length " + length);
                        continue;
                    }
                    check(cannon.assemble(fixture, ROOT), "valid " + facing + length + module);
                    check(((LauncherReadiness) cannon).cbcatfix$armingTicksRemaining() == 20, "new Small session");
                    check(cannon.getBlocks().size() == length, "total length includes module/breech");
                    check(SmallLauncherStructure.rateMultiplier(cannon) == (module.startsWith("rapid") ? 2 : 1), "Rapid once");
                    // A side module may face the opposite way along the same axis.
                    var side = ROOT.relative(LauncherAssembly.right(facing));
                    lane(fixture, side, facing, length, module);
                    if (!module.isEmpty()) put(fixture, side.relative(facing), "cbcatfix:" + module, facing.getOpposite());
                    var group = new MountedRocketPodContraption();
                    check(group.assemble(fixture, ROOT) && group.getBlocks().size() == length * 2, "matching normalized lanes");
                    if (!module.isEmpty()) {
                        put(fixture, side.relative(facing), "cbcatfix:" + (module.startsWith("rapid") ? "traverse_gear_module" : "rapid_cycling_module"), facing);
                        rejects(new MountedRocketPodContraption(), fixture, facing, "different side module");
                    }
                }
            }
            for (String bad : new String[] {"misplaced", "double_rapid", "double_traverse", "mixed", "unfinished", "unbored"}) {
                var fixture = fixture(world);
                lane(fixture, ROOT, facing, 3, "");
                if (bad.equals("misplaced")) put(fixture, ROOT.relative(facing, 2), "cbcatfix:rapid_cycling_module", facing);
                else if (bad.equals("unfinished")) put(fixture, ROOT.relative(facing, 2), "cbcatfix:unfinished_rapid_cycling_module", facing);
                else if (bad.equals("unbored")) put(fixture, ROOT.relative(facing, 2), "cbcatfix:unbored_rapid_cycling_module", facing);
                else {
                    put(fixture, ROOT.relative(facing), "cbcatfix:" + (bad.equals("double_traverse") ? "traverse_gear_module" : "rapid_cycling_module"), facing);
                    put(fixture, ROOT.relative(facing, 2), "cbcatfix:" + (bad.equals("double_rapid") ? "rapid_cycling_module" : "traverse_gear_module"), facing);
                }
                rejects(new MountedRocketPodContraption(), fixture, facing, bad);
            }
        }
        for (var tier : RocketBalance.Tier.values()) {
            String rail = switch (tier) {
                case SMALL -> "cbc_at:steel_rocket_pod_rail";
                case MEDIUM -> "cbc_at:wrought_iron_medium_rocket_rail";
                case BIG -> "cbcatfix:big_rocket_rail";
            };
            String breech = tier == RocketBalance.Tier.SMALL ? "cbc_at:steel_rocket_pod_breech" : rail + "_breech";
            int maximum = switch (tier) { case SMALL -> 3; case MEDIUM -> 5; case BIG -> 8; };
            for (Direction facing : Direction.values()) for (int length : new int[] {maximum, maximum + 1}) {
                var fixture = fixture(world);
                put(fixture, ROOT, breech, facing);
                for (int i = 1; i < length; i++) put(fixture, ROOT.relative(facing, i), rail, facing);
                AbstractMountedCannonContraption cannon = tier == RocketBalance.Tier.SMALL
                    ? new MountedRocketPodContraption() : new MountedMediumRocketRailContraption();
                if (length > maximum) rejects(cannon, fixture, facing, tier + " exceeds maximum");
                else check(cannon.assemble(fixture, ROOT) && cannon.getBlocks().size() == maximum, tier + " maximum accepted");
            }
        }
        for (var tier : RocketBalance.Tier.values()) readiness(world, tier);
        var resources = world.getServer().getResourceManager();
        check(resources.getResource(ResourceLocation.parse("createbigcannons:createbigcannons/block_recipes/casting/rocket_pod/unbored_bronze_rocket_pod_rail.json")).isEmpty(), "legacy casting removed by resource filter");
        check(resources.getResource(ResourceLocation.parse("createbigcannons:createbigcannons/block_recipes/drilling/rocket_pod/cast_iron_rocket_pod_breech.json")).isEmpty(), "legacy drilling removed by resource filter");
        for (var block : new net.minecraft.world.level.block.Block[] {LauncherModules.RAPID.get(), LauncherModules.TRAVERSE.get(), LauncherModules.UNFINISHED_RAPID.get(), LauncherModules.UNFINISHED_TRAVERSE.get()}) {
            var be = ((net.minecraft.world.level.block.EntityBlock) block).newBlockEntity(BlockPos.ZERO, block.defaultBlockState());
            check(be != null && be.getType().isValid(block.defaultBlockState()), "native module BE registered " + block);
            for (Direction facing : Direction.values()) {
                var moduleState = block.defaultBlockState().setValue(BlockStateProperties.FACING, facing);
                var railState = BuiltInRegistries.BLOCK.get(ResourceLocation.parse("cbc_at:steel_rocket_pod_rail"))
                    .defaultBlockState().setValue(BlockStateProperties.FACING, facing);
                check(!net.minecraft.world.phys.shapes.Shapes.joinIsNotEmpty(
                    moduleState.getCollisionShape(world, BlockPos.ZERO), railState.getCollisionShape(world, BlockPos.ZERO),
                    net.minecraft.world.phys.shapes.BooleanOp.NOT_SAME), "module collision matches native rail " + facing);
                check(!net.minecraft.world.phys.shapes.Shapes.joinIsNotEmpty(
                    moduleState.getShape(world, BlockPos.ZERO), railState.getShape(world, BlockPos.ZERO),
                    net.minecraft.world.phys.shapes.BooleanOp.NOT_SAME), "module selection matches native rail " + facing);
            }
        }
        check(LauncherModules.RAPID_SHAPE.get().fluidSize() == 900 && LauncherModules.TRAVERSE_SHAPE.get().fluidSize() == 900, "approved casting cost");
        manufacturing(world);
        productionRecipes(world);
        System.out.println("CBCAT LAUNCHER PROGRESSION: " + checks + " assertions passed");
        return checks;
    }

    private static void manufacturing(ServerLevel world) throws Exception {
        for (var unfinished : java.util.List.of(LauncherModules.UNFINISHED_RAPID.get(), LauncherModules.UNFINISHED_TRAVERSE.get())) {
            var unbored = unfinished == LauncherModules.UNFINISHED_RAPID.get() ? LauncherModules.UNBORED_RAPID.get() : LauncherModules.UNBORED_TRAVERSE.get();
            check(rbasamoyai.createbigcannons.crafting.BlockRecipesManager.getRecipes().stream()
                .anyMatch(recipe -> recipe instanceof rbasamoyai.createbigcannons.crafting.casting.CannonCastingRecipe casting
                    && casting.getResultBlock() == unbored && casting.shape().fluidSize() == 900), "casting produces unbored blank");
            var drilling = rbasamoyai.createbigcannons.crafting.BlockRecipesManager.getRecipes().stream()
                .filter(recipe -> recipe instanceof rbasamoyai.createbigcannons.crafting.boring.DrillBoringBlockRecipe)
                .map(recipe -> (rbasamoyai.createbigcannons.crafting.boring.DrillBoringBlockRecipe) recipe)
                .filter(recipe -> recipe.getResultBlock() == unfinished).findFirst().orElseThrow();
            for (var direction : Direction.values()) {
                var blank = unbored.defaultBlockState().setValue(BlockStateProperties.FACING, direction);
                check(drilling.matches(blank, direction), "drilling accepts axial blank");
                check(drilling.getResultState(blank).getValue(BlockStateProperties.FACING) == direction, "drilling preserves facing");
                check(!drilling.matches(blank, direction.getAxis() == Direction.Axis.Y ? Direction.NORTH : Direction.UP), "drilling rejects transverse axis");
                var be = unbored.newBlockEntity(ROOT, blank);
                check(be != null && be.getType().isValid(blank), "native unbored block entity registered");
            }
            var fixture = fixture(world);
            var state = unfinished.defaultBlockState().setValue(BlockStateProperties.FACING, Direction.NORTH);
            fixture.setBlock(ROOT, state, 3);
            check(!fixture.isClientSide, "finishing fixture uses server side");
            var ingredient = new net.minecraft.world.item.ItemStack(unfinished.requiredItems().getFirst(), 2);
            var player = net.neoforged.neoforge.common.util.FakePlayerFactory.getMinecraft(world);
            boolean creative = player.getAbilities().instabuild;
            player.getAbilities().instabuild = false;
            try {
                unfinished.useItemOn(ingredient, state, fixture, ROOT, player, net.minecraft.world.InteractionHand.MAIN_HAND,
                    new net.minecraft.world.phys.BlockHitResult(ROOT.getCenter(), Direction.UP, ROOT, false));
                check(ingredient.getCount() == 1, "finishing consumes exactly one component");
                check(fixture.getBlockState(ROOT).equals(unfinished.getCompleteBlockState(state)), "native finishing result/orientation");
                check(fixture.getBlockEntity(ROOT) instanceof com.dsvv.cbcat.cannon.rocketpod.RocketPodBlockEntity, "native finished rail BE");
            } finally { player.getAbilities().instabuild = creative; }
        }
    }

    private static void productionRecipes(ServerLevel world) {
        var manager = world.getRecipeManager();
        check(manager.byKey(ResourceLocation.parse("cbcatfix:cutting/big_rocket_rail")).isEmpty(), "Medium to Large shortcut removed");
        for (String tier : new String[] {"medium", "large"}) {
            for (boolean breech : new boolean[] {false, true}) {
                String id = (tier.equals("medium") ? "cbc_at:medium_rocket_rail" : "cbcatfix:big_rocket_rail") + (breech ? "_breech" : "");
                var recipe = (net.minecraft.world.item.crafting.ShapedRecipe) manager.byKey(ResourceLocation.parse(id)).orElseThrow().value();
                var slots = new java.util.ArrayList<net.minecraft.world.item.ItemStack>();
                for (var ingredient : recipe.getIngredients()) slots.add(ingredient.isEmpty() ? net.minecraft.world.item.ItemStack.EMPTY : ingredient.getItems()[0].copyWithCount(1));
                check(slots.stream().filter(stack -> stack.is(rbasamoyai.createbigcannons.index.CBCBlocks.STEEL_BLOCK.asItem())).count() == (tier.equals("medium") ? 1 : 2), "approved steel block count");
                check(slots.stream().filter(stack -> stack.is(rbasamoyai.createbigcannons.index.CBCItems.STEEL_INGOT.get())).count() == (tier.equals("medium") ? 4 : 0), "approved loose steel count");
                check(slots.stream().filter(stack -> stack.is(com.simibubi.create.AllBlocks.SHAFT.asItem())).count() == 1, "one shaft per component");
                check(slots.stream().filter(stack -> stack.is(rbasamoyai.createbigcannons.index.CBCItems.STEEL_SLIDING_BREECHBLOCK.get())).count() == (breech ? 1 : 0), "breech mechanical component");
                var input = net.minecraft.world.item.crafting.CraftingInput.of(recipe.getWidth(), recipe.getHeight(), slots);
                check(recipe.matches(input, world), "approved independent launcher recipe " + id);
                check(recipe.assemble(input, world.registryAccess()).getCount() == 1, "one launcher component output");
                for (int i = 0; i < slots.size(); i++) if (slots.get(i).is(rbasamoyai.createbigcannons.index.CBCBlocks.STEEL_BLOCK.asItem())) {
                    slots.set(i, net.minecraft.world.item.Items.IRON_BLOCK.getDefaultInstance());
                    break;
                }
                check(!recipe.matches(net.minecraft.world.item.crafting.CraftingInput.of(recipe.getWidth(), recipe.getHeight(), slots), world), "cheap iron substitution rejected");
            }
        }
        check(manager.byKey(ResourceLocation.parse("cbcatfix:compacting/cluster_casing_blank")).isEmpty(), "casing production removed");
        check(manager.byKey(ResourceLocation.parse("cbcatfix:sequenced_assembly/cluster_casing")).isEmpty(), "casing sequence removed");
        for (String removed : new String[] {"cluster_casing", "cluster_casing_blank", "incomplete_cluster_casing"})
            check(!BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse("cbcatfix:" + removed)), "approved casing ID removal");
    }
    private static void readiness(ServerLevel world, RocketBalance.Tier tier) throws Exception {
        var fixture = fixture(world);
        String base = switch (tier) {
            case SMALL -> "cbc_at:steel_rocket_pod_";
            case MEDIUM -> "cbc_at:wrought_iron_medium_rocket_rail";
            case BIG -> "cbcatfix:big_rocket_rail";
        };
        String breech = tier == RocketBalance.Tier.SMALL ? base + "breech" : base + "_breech";
        String rail = tier == RocketBalance.Tier.SMALL ? base + "rail" : base;
        put(fixture, ROOT, breech, Direction.NORTH);
        for (int i = 1; i < (tier == RocketBalance.Tier.BIG ? 3 : 2); i++) put(fixture, ROOT.north(i), rail, Direction.NORTH);
        AbstractMountedCannonContraption cannon = tier == RocketBalance.Tier.SMALL ? new MountedRocketPodContraption() : new MountedMediumRocketRailContraption();
        check(cannon.assemble(fixture, ROOT), "assemble " + tier);
        var ready = (LauncherReadiness) cannon;
        int duration = switch (tier) { case SMALL -> 20; case MEDIUM -> 40; case BIG -> 100; };
        check(ready.cbcatfix$armingTicksRemaining() == duration, "class arming duration");
        var entity = PitchOrientedContraptionEntity.create(world, cannon, Direction.NORTH, true);
        var storage = (MountedRocketStorage) cannon.presentBlockEntities.get(cannon.getStartPos());
        var rocket = switch (tier) {
            case SMALL -> com.dsvv.cbcat.registry.ItemRegister.HE_ROCKET_ITEM.get();
            case MEDIUM -> com.dsvv.cbcat.registry.ItemRegister.MEDIUM_HE_ROCKET_ITEM.get();
            case BIG -> com.cbcatfix.munitions.CbcatFixMunitions.BIG_HE_ROCKET_ITEM.get();
        };
        var loaded = rocket.getDefaultInstance();
        check(!loaded.isEmpty(), "real input rocket exists");
        storage.cbcatfix$setRocketInSlot(0, loaded);
        check(ready.cbcatfix$hasRockets(), "group sees loaded rocket");
        var before = cannon.writeNBT(world.registryAccess(), false);
        cannon.fireShot(world, entity);
        check(before.equals(cannon.writeNBT(world.registryAccess(), false)), "arming fire guard changes no state");
        check(net.minecraft.world.item.ItemStack.isSameItemSameComponents(loaded, storage.cbcatfix$rocketInSlot(0)), "arming guard does not consume real rocket");
        storage.cbcatfix$setRocketInSlot(0, net.minecraft.world.item.ItemStack.EMPTY);
        for (int i = 0; i < 7; i++) cannon.tick(world, entity);
        check(ready.cbcatfix$armingTicksRemaining() == duration - 7, "active ticks advance once");
        var saved = cannon.writeNBT(world.registryAccess(), false);
        AbstractMountedCannonContraption restored = tier == RocketBalance.Tier.SMALL ? new MountedRocketPodContraption() : new MountedMediumRocketRailContraption();
        restored.readNBT(world, saved, false);
        check(((LauncherReadiness) restored).cbcatfix$armingTicksRemaining() == duration - 7, "same-session persistence");
        entity.setPos(29, 90, -13); entity.yaw = 40; entity.pitch = 12;
        check(ready.cbcatfix$armingTicksRemaining() == duration - 7, "movement/aim do not reset");
        for (int i = 7; i < duration; i++) cannon.tick(world, entity);
        check(ready.cbcatfix$armingTicksRemaining() == 0, "armed after exact active duration");
        cannon.tick(world, entity);
        check(ready.cbcatfix$armingTicksRemaining() == 0, "clamped at zero");
        for (int value : new int[] {-200, duration + 200}) {
            saved.putInt("CbcatFixArmingTicks", value);
            restored.readNBT(world, saved, false);
            check(((LauncherReadiness) restored).cbcatfix$armingTicksRemaining() == Math.clamp(value, 0, duration), "invalid NBT clamped");
        }
        saved.remove("CbcatFixArmingTicks"); restored.readNBT(world, saved, false);
        check(((LauncherReadiness) restored).cbcatfix$armingTicksRemaining() == duration, "old assembled save gets full duration");
    }

    private static SchematicLevel fixture(ServerLevel world) {
        return new SchematicLevel(world) { @Override public void blockEntityChanged(BlockPos pos) {} };
    }
    private static void lane(SchematicLevel fixture, BlockPos root, Direction facing, int length, String module) {
        put(fixture, root, "cbc_at:steel_rocket_pod_breech", facing);
        for (int i = 1; i < length; i++) put(fixture, root.relative(facing, i), i == 1 && !module.isEmpty()
            ? "cbcatfix:" + module : "cbc_at:steel_rocket_pod_rail", facing);
    }
    private static void put(SchematicLevel fixture, BlockPos pos, String id, Direction facing) {
        var block = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(id));
        fixture.setBlock(pos, block.defaultBlockState().setValue(BlockStateProperties.FACING, facing), 3);
        if (fixture.getBlockEntity(pos) instanceof com.simibubi.create.foundation.blockEntity.SmartBlockEntity be) be.initialize();
    }
    private static void rejects(AbstractMountedCannonContraption cannon, SchematicLevel fixture, Direction facing, String label) throws Exception {
        try { check(!cannon.assemble(fixture, ROOT), "reject " + label); }
        catch (AssemblyException expected) { checks++; }
        check(!fixture.getBlockState(ROOT).isAir(), "failed assembly leaves breech intact: " + label);
    }
    private static void check(boolean value, String label) {
        if (!value) throw new AssertionError(label);
        checks++;
    }
}
