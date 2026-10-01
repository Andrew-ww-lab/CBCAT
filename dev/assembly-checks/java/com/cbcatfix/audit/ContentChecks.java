package com.cbcatfix.audit;

import com.cbcatfix.munitions.CbcatFixMunitions;
import com.cbcatfix.rocket.RocketProjectileFactory;
import com.dsvv.cbcat.cannon.heavy_autocannon.munitions.AbstractHeavyAutocannonProjectileItem;
import com.dsvv.cbcat.cannon.heavy_autocannon.munitions.HeavyAutocannonCartridgeItem;
import com.dsvv.cbcat.registry.DataComponentRegistry;
import com.simibubi.create.content.kinetics.crafter.RecipeGridHandler;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import org.apache.commons.lang3.tuple.Pair;
import rbasamoyai.createbigcannons.index.CBCDataComponents;
import rbasamoyai.createbigcannons.index.CBCEntityTypes;
import rbasamoyai.createbigcannons.index.CBCMunitionPropertiesHandlers;

import java.util.List;
import java.util.Map;

final class ContentChecks {
    static int run(ServerLevel level) throws Exception {
        int checks = 0;
        var normal = CBCMunitionPropertiesHandlers.COMMON_SHELL_BIG_CANNON_PROJECTILE
            .getPropertiesOf(CBCEntityTypes.HE_SHELL.get());
        for (String suffix : List.of("", "_double_payload", "_double_fuel")) {
            ItemStack rocket = item("cbcatfix:big_he_rocket" + suffix);
            require(suffix.isEmpty() || rocket.get(CBCDataComponents.PROJECTILE).copyOne().is(item("createbigcannons:he_shell").getItem()), "normal HE variant default");
            var projectile = RocketProjectileFactory.create(rocket, level);
            require(projectile.getDamageProperties().equals(normal.damage()), "normal HE damage");
            var entry = com.dsvv.cbcat.cannon.medium_rocketpod.munitions.AbstractMediumRocket.class.getDeclaredField("autocannonProjectileEntry");
            entry.setAccessible(true);
            require(entry.get(projectile) == null, "no heavy payload entity");
            checks += 3;
        }
        for (var item : BuiltInRegistries.ITEM) {
            if (!(item instanceof AbstractHeavyAutocannonProjectileItem)) continue;
            for (String powder : List.of("minecraft:gunpowder", "createbigcannons:nitropowder")) {
                ItemStack round = item.getDefaultInstance();
                round.set(CBCDataComponents.AUTOCANNON_TRACER, true);
                round.set(DataComponents.CUSTOM_NAME, Component.literal("Component preservation"));
                round.set(CBCDataComponents.FUZE, ItemContainerContents.fromItems(List.of(item("createbigcannons:timed_fuze"))));
                ItemStack casing = item("cbc_at:heavy_autocannon_empty_cartridge");
                casing.set(DataComponents.CUSTOM_NAME, Component.literal("Casing metadata"));
                var grouped = new RecipeGridHandler.GroupedItems();
                var field = RecipeGridHandler.GroupedItems.class.getDeclaredField("grid");
                field.setAccessible(true);
                @SuppressWarnings("unchecked") var grid = (Map<Pair<Integer, Integer>, ItemStack>) field.get(grouped);
                grid.put(Pair.of(0, 0), casing);
                grid.put(Pair.of(1, 0), round);
                grid.put(Pair.of(0, 1), item(powder));
                grid.put(Pair.of(1, 1), item(powder));
                var output = RecipeGridHandler.tryToApplyRecipe(level, grouped);
                require(output != null && output.is(item("cbc_at:heavy_autocannon_cartridge").getItem()) && output.getCount() == 1, "native crafter output " + item);
                require(ItemStack.isSameItemSameComponents(round, HeavyAutocannonCartridgeItem.getProjectileStack(output)), "preserve fuze/tracer/payload");
                require(output.get(DataComponents.CUSTOM_NAME).equals(casing.get(DataComponents.CUSTOM_NAME)), "preserve casing data");
                require(output.get(DataComponentRegistry.HA_STRONG_ROUND) == powder.endsWith("nitropowder"), "correct charge");
                require(!HeavyAutocannonCartridgeItem.hasProjectile(casing) && round.getCount() == 1, "inputs unchanged by lookup");
                grid.put(Pair.of(0, 0), output);
                require(RecipeGridHandler.tryToApplyRecipe(level, grouped) == null, "reject already loaded cartridge");
                checks += 6;
            }
        }
        System.out.println("CBCAT CONTENT CHECKS PASSED: " + checks + " assertions; native Create crafting path");
        for (String id : List.of("cbc_at:cast_iron_rocket_pod_rail", "cbc_at:bronze_rocket_pod_rail",
            "cbc_at:steel_rocket_pod_rail", "cbc_at:wrought_iron_medium_rocket_rail", "cbcatfix:big_rocket_rail")) {
            var block = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(id));
            var material = block instanceof com.dsvv.cbcat.cannon.rocketpod.RocketPodBlock small
                ? small.getAutocannonMaterial() : ((com.dsvv.cbcat.cannon.medium_rocketpod.MediumRocketPodBlock) block).getAutocannonMaterial();
            System.out.println("CBCAT LAUNCHER: " + id + " " + material.name() + " " + material.properties());
            if (net.neoforged.fml.ModList.get().isLoaded("sable")) {
                var method = Class.forName("dev.ryanhcode.sable.physics.config.block_properties.PhysicsBlockPropertyHelper")
                    .getMethod("getMass", net.minecraft.world.level.BlockGetter.class, net.minecraft.core.BlockPos.class,
                        net.minecraft.world.level.block.state.BlockState.class);
                System.out.println("CBCAT SABLE MASS: " + id + " = " + method.invoke(null, level, net.minecraft.core.BlockPos.ZERO, block.defaultBlockState()));
            }
        }
        return checks;
    }

    private static ItemStack item(String id) { return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(id))); }
    private static void require(boolean result, String message) {
        if (!result) throw new AssertionError(message);
    }
}
