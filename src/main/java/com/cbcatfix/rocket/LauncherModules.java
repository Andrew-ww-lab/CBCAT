package com.cbcatfix.rocket;

import com.cbcatfix.munitions.CbcatFixMunitions;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.bus.api.IEventBus;
import rbasamoyai.createbigcannons.base.CBCRegistries;
import rbasamoyai.createbigcannons.crafting.casting.CannonCastShape;
import rbasamoyai.createbigcannons.crafting.casting.CannonCastMouldBlock;
import rbasamoyai.createbigcannons.crafting.incomplete.IncompleteAutocannonBlock;
import rbasamoyai.createbigcannons.crafting.boring.UnboredAutocannonBlock;
import rbasamoyai.createbigcannons.index.CBCAutocannonMaterials;
import rbasamoyai.createbigcannons.index.CBCItems;
import com.simibubi.create.AllItems;

/** Module content uses the existing CBCA rail block entity; no additional inventory. */
public final class LauncherModules {
    private static final DeferredRegister<CannonCastShape> SHAPES = DeferredRegister.create(CBCRegistries.CANNON_CAST_SHAPES, "cbcatfix");
    public static final DeferredHolder<Block, LauncherModuleBlock> RAPID = block("rapid_cycling_module", LauncherModuleBlock.Module.RAPID, true);
    public static final DeferredHolder<Block, LauncherModuleBlock> TRAVERSE = block("traverse_gear_module", LauncherModuleBlock.Module.TRAVERSE, true);
    public static final DeferredHolder<Block, CannonCastMouldBlock> RAPID_MOULD = mould("rapid_cycling_module_mould", () -> rapidShape());
    public static final DeferredHolder<Block, CannonCastMouldBlock> TRAVERSE_MOULD = mould("traverse_gear_module_mould", () -> traverseShape());
    public static final DeferredHolder<CannonCastShape, CannonCastShape> RAPID_SHAPE = SHAPES.register("rapid_cycling_module",
        () -> moduleShape(RAPID_MOULD));
    public static final DeferredHolder<CannonCastShape, CannonCastShape> TRAVERSE_SHAPE = SHAPES.register("traverse_gear_module",
        () -> moduleShape(TRAVERSE_MOULD));
    public static final DeferredHolder<Block, IncompleteAutocannonBlock> UNFINISHED_RAPID = unfinished(
        "unfinished_rapid_cycling_module", RAPID, RAPID_SHAPE, () -> CBCItems.RECOIL_SPRING.get());
    public static final DeferredHolder<Block, IncompleteAutocannonBlock> UNFINISHED_TRAVERSE = unfinished(
        "unfinished_traverse_gear_module", TRAVERSE, TRAVERSE_SHAPE, () -> AllItems.PRECISION_MECHANISM.get());
    public static final DeferredHolder<Block, UnboredAutocannonBlock> UNBORED_RAPID = unbored("unbored_rapid_cycling_module", RAPID_SHAPE);
    public static final DeferredHolder<Block, UnboredAutocannonBlock> UNBORED_TRAVERSE = unbored("unbored_traverse_gear_module", TRAVERSE_SHAPE);

    private LauncherModules() {}

    private static CannonCastShape moduleShape(java.util.function.Supplier<? extends Block> mould) {
        return new CannonCastShape(com.cbcatfix.balance.BalanceDefaults.MODULE_CAST_STEEL_MB, 10,
            () -> mould.get(), false, false, rbasamoyai.createbigcannons.base.PropertySetter.of(
                net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING, net.minecraft.core.Direction.UP));
    }

    private static DeferredHolder<Block, LauncherModuleBlock> block(String id, LauncherModuleBlock.Module module, boolean complete) {
        var block = CbcatFixMunitions.BLOCKS.register(id, () -> new LauncherModuleBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5, 6).noOcclusion(), module, complete));
        CbcatFixMunitions.ITEMS.register(id, () -> new BlockItem(block.get(), new Item.Properties()));
        return block;
    }

    private static CannonCastShape rapidShape() { return RAPID_SHAPE.get(); }
    private static CannonCastShape traverseShape() { return TRAVERSE_SHAPE.get(); }

    private static DeferredHolder<Block, UnboredAutocannonBlock> unbored(String id, java.util.function.Supplier<CannonCastShape> shape) {
        var block = CbcatFixMunitions.BLOCKS.register(id, () -> new UnboredAutocannonBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5, 6).noOcclusion(),
            CBCAutocannonMaterials.STEEL, Block.box(0, 0, 1.5, 16, 16, 14.5), shape));
        CbcatFixMunitions.ITEMS.register(id, () -> new BlockItem(block.get(), new Item.Properties()));
        return block;
    }

    private static DeferredHolder<Block, CannonCastMouldBlock> mould(String id, java.util.function.Supplier<CannonCastShape> shape) {
        var block = CbcatFixMunitions.BLOCKS.register(id, () -> new CannonCastMouldBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2).noOcclusion(),
            Block.box(0, 0, 3, 16, 16, 13), shape));
        CbcatFixMunitions.ITEMS.register(id, () -> new BlockItem(block.get(), new Item.Properties()));
        return block;
    }

    private static DeferredHolder<Block, IncompleteAutocannonBlock> unfinished(String id,
        java.util.function.Supplier<? extends Block> result, java.util.function.Supplier<CannonCastShape> shape,
        com.tterrag.registrate.util.nullness.NonNullSupplier<? extends net.minecraft.world.level.ItemLike> part) {
        var block = CbcatFixMunitions.BLOCKS.register(id, () -> new IncompleteAutocannonBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5, 6).noOcclusion(),
            CBCAutocannonMaterials.STEEL, LauncherShapes.get(RocketBalance.Tier.SMALL, false, net.minecraft.core.Direction.UP),
            shape, () -> result.get(), part));
        CbcatFixMunitions.ITEMS.register(id, () -> new BlockItem(block.get(), new Item.Properties()));
        return block;
    }

    public static boolean isUnfinished(Block block) {
        return block == UNFINISHED_RAPID.get() || block == UNFINISHED_TRAVERSE.get()
            || block == UNBORED_RAPID.get() || block == UNBORED_TRAVERSE.get();
    }

    public static void register(IEventBus bus) {
        SHAPES.register(bus);
    }
}
