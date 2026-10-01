package com.cbcatfix.audit;

import com.cbcatfix.crafting.AmmunitionFuzes;
import com.cbcatfix.rocket.RocketGroundPlacement;
import com.cbcatfix.rocket.RocketStackFactory;
import com.dsvv.cbcat.cannon.heavy_autocannon.munitions.HeavyAutocannonCartridgeItem;
import com.dsvv.cbcat.cannon.twin_autocannon.TwinAutocannonBlock;
import com.dsvv.cbcat.cannon.twin_autocannon.ITwinAutocannonBlockEntity;
import com.dsvv.cbcat.cannon.twin_autocannon.contraption.MountedTwinAutocannonContraption;
import com.dsvv.cbcat.crafting.*;
import com.dsvv.cbcat.registry.DataComponentRegistry;
import com.simibubi.create.content.contraptions.AssemblyException;
import com.simibubi.create.content.kinetics.crafter.MechanicalCraftingRecipe;
import net.createmod.catnip.levelWrappers.SchematicLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import rbasamoyai.createbigcannons.crafting.welding.CannonWelderItem;
import rbasamoyai.createbigcannons.index.CBCDataComponents;
import java.util.ArrayList;
import java.util.List;

/** Opt-in checks against transformed dependency classes; no saved world or gameplay blocks are created. */
@EventBusSubscriber(modid = "cbcatfix")
public final class AssemblyAuditChecks {
    private static int checks;
    @SubscribeEvent
    public static void started(ServerStartedEvent event) throws Exception {
        if (!event.getServer().isDedicatedServer()) return;
        ServerLevel level = event.getServer().overworld();
        checkTwinAssembly(level);
        checkRecipes(level);
        checks += ContentChecks.run(level);
        checks += OrientationChecks.run(level);
        System.out.println("CBCAT ASSEMBLY AUDIT PASSED: " + checks + " assertions; in-memory fixtures only");
        event.getServer().halt(false);
    }

    private static void checkTwinAssembly(ServerLevel original) throws Exception {
        for (String material : List.of("cast_iron", "bronze", "steel")) {
            for (boolean vertical : new boolean[] {false, true}) {
                for (Direction facing : Direction.values()) {
                    var level = new Fixture(original);
                    String base = "cbc_at:" + material + (vertical ? "_vert_twin_autocannon_" : "_twin_autocannon_");
                    BlockPos root = new BlockPos(0, 80, 0);
                    put(level, root, base + "breech", facing);
                    put(level, root.relative(facing), base + "barrel", facing);
                    checkFails(level, root, "unwelded " + base + facing);
                    check(CannonWelderItem.weldBlocks(level, root, root.relative(facing), false), "weld " + base + facing);
                    for (BlockPos anchor : List.of(root, root.relative(facing))) {
                        var cannon = new MountedTwinAutocannonContraption();
                        check(cannon.assemble(level, anchor), "assemble " + base + facing);
                        check(cannon.getBlocks().size() == 2 && cannon.initialOrientation() == facing, "native topology");
                    }
                    put(level, root.relative(facing, 2), base + "breech", facing.getOpposite());
                    check(CannonWelderItem.weldBlocks(level, root.relative(facing), root.relative(facing, 2), false), "weld second breech");
                    checkFails(level, root.relative(facing), "two closed ends");
                    Direction side = facing.getAxis() == Direction.Axis.Y ? Direction.EAST : Direction.UP;
                    put(level, root.relative(side), base + "barrel", side);
                    check(!CannonWelderItem.weldBlocks(level, root, root.relative(side), true), "reject branch");
                    var springLevel = new Fixture(original);
                    put(springLevel, root, base + "breech", facing);
                    put(springLevel, root.relative(facing), base + "recoil_spring", facing);
                    put(springLevel, root.relative(facing, 2), base + "barrel", facing);
                    check(CannonWelderItem.weldBlocks(springLevel, root, root.relative(facing), false), "weld recoil spring");
                    check(CannonWelderItem.weldBlocks(springLevel, root.relative(facing), root.relative(facing, 2), false), "weld spring to barrel");
                    for (int anchorIndex = 0; anchorIndex < 3; anchorIndex++) {
                        var cannon = new MountedTwinAutocannonContraption();
                        check(cannon.assemble(springLevel, root.relative(facing, anchorIndex)), "assemble recoil layout " + base + facing);
                        check(cannon.getBlocks().size() == 3 && cannon.initialOrientation() == facing, "recoil topology");
                    }
                }
            }
        }
        var level = new Fixture(original);
        BlockPos root = new BlockPos(0, 80, 0);
        put(level, root, "cbc_at:steel_twin_autocannon_breech", Direction.NORTH);
        put(level, root.north(), "cbc_at:steel_vert_twin_autocannon_barrel", Direction.NORTH);
        check(!CannonWelderItem.weldBlocks(level, root, root.north(), true), "reject mixed pair layout before welding");
    }

    private static void put(Fixture level, BlockPos pos, String id, Direction facing) {
        var block = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(id));
        level.setBlock(pos, block.defaultBlockState().setValue(BlockStateProperties.FACING, facing), 3);
        var be = (com.simibubi.create.foundation.blockEntity.SmartBlockEntity) level.getBlockEntity(pos);
        be.initialize();
    }

    private static void checkFails(Fixture level, BlockPos root, String label) throws Exception {
        try {
            boolean assembled = new MountedTwinAutocannonContraption().assemble(level, root);
            check(!assembled, label);
        } catch (AssemblyException expected) {
            check(expected.component.getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents text
                && text.getKey().equals("exception.createbigcannons.cannon_mount.invalidCannon"), "exact invalidCannon path: " + label);
        }
    }

    private static void checkRecipes(ServerLevel level) {
        ItemStack fuze = item("createbigcannons:timed_fuze");
        var fuzeTag = new net.minecraft.nbt.CompoundTag();
        fuzeTag.putInt("Timer", 73);
        fuze.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(fuzeTag));
        var fuzes = ItemContainerContents.fromItems(List.of(fuze));
        int recipes = 0;
        for (var holder : level.getRecipeManager().getRecipes()) {
            if (!(holder.value() instanceof MechanicalCraftingRecipe recipe)) continue;
            ItemStack template = recipe.getResultItem(level.registryAccess());
            var slots = new ArrayList<ItemStack>();
            for (var ingredient : recipe.getIngredients()) {
                slots.add(ingredient.isEmpty() ? ItemStack.EMPTY : ingredient.getItems()[0].copyWithCount(1));
            }
            var input = mechanicalInput(recipe.getWidth(), recipe.getHeight(), slots);
            if (List.of("cbcatfix:heavy_he_shell", "cbcatfix:heat_shell", "cbcatfix:flak_shell")
                .contains(BuiltInRegistries.ITEM.getKey(template.getItem()).toString())) {
                check(recipe.matches(input, level), "clean combined warhead " + holder.id());
                for (int slot = 0; slot < slots.size(); slot++) {
                    if (!(slots.get(slot).getItem() instanceof rbasamoyai.createbigcannons.munitions.big_cannon.ProjectileBlockItem)) continue;
                    slots.get(slot).set(CBCDataComponents.FUZE, fuzes);
                    var armedInput = mechanicalInput(recipe.getWidth(), recipe.getHeight(), slots);
                    check(!recipe.matches(armedInput, level), "no lost fuze in combined warhead");
                    check(recipe.assemble(armedInput, level.registryAccess()).isEmpty(), "guard direct warhead assembly");
                    slots.get(slot).remove(CBCDataComponents.FUZE);
                }
                continue;
            }
            if (com.cbcatfix.munitions.ClusterPayload.isAssembly(template)) {
                for (ItemStack stack : slots) {
                    if (stack.getItem() instanceof com.dsvv.cbcat.cannon.heavy_autocannon.munitions.AbstractFuzedHeavyAutocannonProjectileItem)
                        stack.set(CBCDataComponents.FUZE, fuzes);
                }
                var clusterInput = mechanicalInput(recipe.getWidth(), recipe.getHeight(), slots);
                check(recipe.matches(clusterInput, level), "cluster accepts installed submunition fuzes");
                var cluster = recipe.assemble(clusterInput, level.registryAccess());
                var retained = cluster.get(DataComponentRegistry.CLUSTER_FUZES);
                check(retained.getSlots() == com.cbcatfix.munitions.ClusterPayload.SUBMUNITION_COUNT, "all cluster fuzes retained");
                for (var stored : retained.nonEmptyItems()) check(ItemStack.isSameItemSameComponents(stored, fuze), "cluster fuze settings retained");
                check(!AmmunitionFuzes.isCleanPayload(cluster), "armed cluster cannot become clean rocket payload");
                continue;
            }
            if (!RocketGroundPlacement.isRocket(template) || !RocketStackFactory.usesConfiguredFuel(template)) continue;
            recipes++;
            check(recipe.matches(input, level), "clean recipe " + holder.id());
            ItemStack rocket = recipe.assemble(input, level.registryAccess());
            check(!rocket.isEmpty(), "clean output " + holder.id());
            var fuzing = new RocketFuzingRecipe();
            var fuzeInput = CraftingInput.of(2, 1, List.of(rocket, fuze));
            check(fuzing.matches(fuzeInput, level), "install rocket fuze " + holder.id());
            var armed = fuzing.assemble(fuzeInput, level.registryAccess());
            check(armed.get(CBCDataComponents.PROJECTILE).equals(rocket.get(CBCDataComponents.PROJECTILE)), "preserve rocket payload");
            check(RocketStackFactory.getFuelTicks(armed) == RocketStackFactory.getFuelTicks(rocket), "preserve rocket fuel");
            check(!fuzing.matches(CraftingInput.of(2, 1, List.of(armed, fuze)), level), "no fuze overwrite");
            ItemStack payload = template.get(CBCDataComponents.PROJECTILE).copyOne();
            for (int i = 0; i < slots.size(); i++) {
                if (!ItemStack.isSameItem(slots.get(i), payload)) continue;
                slots.get(i).set(CBCDataComponents.FUZE, fuzes);
                var bad = mechanicalInput(recipe.getWidth(), recipe.getHeight(), slots);
                check(!recipe.matches(bad, level), "reject fuzed recipe " + holder.id());
                check(recipe.assemble(bad, level.registryAccess()).isEmpty(), "guard direct assemble " + holder.id());
                check(AmmunitionFuzes.hasFuze(slots.get(i)), "input not stripped");
                slots.get(i).set(CBCDataComponents.FUZE, ItemContainerContents.EMPTY);
                check(recipe.matches(mechanicalInput(recipe.getWidth(), recipe.getHeight(), slots), level), "empty fuze component is clean");
                slots.get(i).remove(CBCDataComponents.FUZE);
            }
        }
        check(recipes == 33, "all 33 rocket recipes covered: " + recipes);
        ItemStack round = item("cbc_at:ha_he_item");
        round.set(CBCDataComponents.FUZE, fuzes);
        for (String powder : List.of("minecraft:gunpowder", "createbigcannons:nitropowder")) {
            var input = CraftingInput.of(2, 2, List.of(item("cbc_at:heavy_autocannon_empty_cartridge"), round, item(powder), item(powder)));
            var assembly = new HAMunitionAssemblyRecipe();
            check(assembly.matches(input, level), "medium cartridge input");
            var result = assembly.assemble(input, level.registryAccess());
            check(ItemStack.isSameItemSameComponents(HeavyAutocannonCartridgeItem.getProjectileStack(result), round), "preserve complete medium projectile");
            boolean strong = result.get(DataComponentRegistry.HA_STRONG_ROUND);
            var removal = new HAMunitionUnfuzingRecipe();
            var removalInput = CraftingInput.of(1, 1, List.of(result));
            check(removal.matches(removalInput, level), "medium remove fuze");
            check(ItemStack.isSameItemSameComponents(removal.assemble(removalInput, level.registryAccess()), fuze), "recover configured fuze once");
            var remainder = removal.getRemainingItems(removalInput).getFirst();
            check(remainder.get(DataComponentRegistry.HA_STRONG_ROUND) == strong, "preserve charge on unfuzing");
            check(!removal.matches(CraftingInput.of(1, 1, List.of(remainder)), level), "cannot remove twice");
            check(!AmmunitionFuzes.isCleanPayload(result) && AmmunitionFuzes.isCleanPayload(remainder), "nested payload detection");
            var traced = result.copy();
            var tracedRound = HeavyAutocannonCartridgeItem.getProjectileStack(traced);
            tracedRound.set(CBCDataComponents.AUTOCANNON_TRACER, true);
            HeavyAutocannonCartridgeItem.writeProjectile(tracedRound, traced);
            var tracerRemoval = new HAMunitionTracerRemovalRecipe();
            var tracerInput = CraftingInput.of(1, 1, List.of(traced));
            check(tracerRemoval.matches(tracerInput, level), "medium tracer removal matches");
            var untraced = tracerRemoval.getRemainingItems(tracerInput).getFirst();
            check(untraced.get(DataComponentRegistry.HA_STRONG_ROUND) == strong, "tracer removal preserves charge");
            check(HeavyAutocannonCartridgeItem.getProjectileStack(untraced).get(CBCDataComponents.FUZE).equals(fuzes), "tracer removal preserves fuze");
        }
        var caseless = new CaselessMunitionAssemblyRecipe();
        var shell = item("createbigcannons:he_shell");
        shell.set(CBCDataComponents.FUZE, fuzes);
        var caselessInput = CraftingInput.of(2, 2, List.of(shell, item("createbigcannons:powder_charge"), ItemStack.EMPTY, ItemStack.EMPTY));
        check(caseless.matches(caselessInput, level), "native caseless conversion matches");
        var converted = caseless.assemble(caselessInput, level.registryAccess());
        check(!converted.isEmpty() && fuzes.equals(converted.get(CBCDataComponents.FUZE)), "native caseless conversion preserves fuze");
        var armedInput = CraftingInput.of(1, 1, List.of(round));
        check(!new RocketRecipe().matches(armedInput, level), "legacy small recipe rejects armed ammunition");
        check(!new MediumRocketRecipe().matches(armedInput, level), "legacy medium recipe rejects armed ammunition");
        check(new RocketRecipe().assemble(armedInput, level.registryAccess()).isEmpty(), "legacy small assembly guarded");
        check(new MediumRocketRecipe().assemble(armedInput, level.registryAccess()).isEmpty(), "legacy medium assembly guarded");
        System.out.println("CBCAT AUDIT: rocket recipes checked=" + recipes);
    }

    private static CraftingInput mechanicalInput(int width, int height, List<ItemStack> slots) {
        try {
            var constructor = com.simibubi.create.content.kinetics.crafter.MechanicalCraftingInput.class
                .getDeclaredConstructor(int.class, int.class, List.class);
            constructor.setAccessible(true);
            return constructor.newInstance(width, height, slots);
        } catch (ReflectiveOperationException ex) { throw new AssertionError(ex); }
    }

    private static ItemStack item(String id) {
        var stack = BuiltInRegistries.ITEM.get(ResourceLocation.parse(id)).getDefaultInstance();
        check(!stack.isEmpty(), "registered item " + id);
        return stack;
    }
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
        checks++;
    }
    private static class Fixture extends SchematicLevel {
        Fixture(ServerLevel source) { super(source); renderMode = true; }
        @Override public void blockEntityChanged(BlockPos pos) {}
    }
}
