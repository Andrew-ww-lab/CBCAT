package com.cbcatfix.munitions;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import rbasamoyai.createbigcannons.munitions.big_cannon.BigCannonProjectileRenderer;
import rbasamoyai.createbigcannons.munitions.big_cannon.FuzedBlockEntityRenderer;
import rbasamoyai.createbigcannons.munitions.FuzedProjectileBlockItem;
import rbasamoyai.createbigcannons.munitions.config.MunitionPropertiesHandler;
import rbasamoyai.createbigcannons.index.CBCMunitionPropertiesHandlers;

public class CbcatFixMunitions {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, com.cbcatfix.CbcatFix.MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, com.cbcatfix.CbcatFix.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, com.cbcatfix.CbcatFix.MOD_ID);
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, com.cbcatfix.CbcatFix.MOD_ID);
    public static final DeferredHolder<Block, FlakShellBlock> FLAK_SHELL = BLOCKS.register("flak_shell",
        () -> new FlakShellBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0f))
    );
    public static final DeferredHolder<Block, HeavyHEShellBlock> HEAVY_HE_SHELL = BLOCKS.register("heavy_he_shell",
        () -> new HeavyHEShellBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0f))
    );
    public static final DeferredHolder<Block, HEATShellBlock> HEAT_SHELL = BLOCKS.register("heat_shell",
        () -> new HEATShellBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0f))
    );
    public static final DeferredHolder<Block, HESHShellBlock> HESH_SHELL = BLOCKS.register("hesh_shell",
        () -> new HESHShellBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0f))
    );
    public static final DeferredHolder<Block, BigRocketRailBlock> BIG_ROCKET_RAIL = BLOCKS.register("big_rocket_rail",
        () -> new BigRocketRailBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0f),
            rbasamoyai.createbigcannons.index.CBCAutocannonMaterials.STEEL)
    );
    public static final DeferredHolder<Block, BigRocketRailBreechBlock> BIG_ROCKET_RAIL_BREECH = BLOCKS.register("big_rocket_rail_breech",
        () -> new BigRocketRailBreechBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0f),
            rbasamoyai.createbigcannons.index.CBCAutocannonMaterials.STEEL)
    );
    public static final DeferredHolder<Block, com.cbcatfix.rocket.RocketBlock> ROCKET_BLOCK = BLOCKS.register("rocket",
        () -> new com.cbcatfix.rocket.RocketBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0f).noOcclusion()
        )
    );
    public static final DeferredHolder<Block, com.cbcatfix.rocket.RocketBodyExtensionBlock> ROCKET_BODY_EXTENSION = BLOCKS.register("rocket_body_extension",
        () -> new com.cbcatfix.rocket.RocketBodyExtensionBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.NONE).strength(2.0f).noOcclusion()
        )
    );
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CbcatFixFuzedBlockEntity>> FUZED_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("fuzed_block_entity",
        () -> BlockEntityType.Builder.of(
            (pos, state) -> new CbcatFixFuzedBlockEntity(pos, state),
            FLAK_SHELL.get(),
            HEAVY_HE_SHELL.get(),
            HEAT_SHELL.get(),
            HESH_SHELL.get()
        ).build(null)
    );
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.cbcatfix.rocket.RocketBlockEntity>> ROCKET_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("rocket",
        () -> BlockEntityType.Builder.of(
            (pos, state) -> new com.cbcatfix.rocket.RocketBlockEntity(pos, state),
            ROCKET_BLOCK.get()
        ).build(null)
    );
    public static final DeferredHolder<EntityType<?>, EntityType<FlakShellProjectile>> FLAK_SHELL_PROJECTILE = ENTITY_TYPES.register("flak_shell",
        () -> EntityType.Builder.<FlakShellProjectile>of(FlakShellProjectile::new, MobCategory.MISC)
            .sized(0.8f, 0.8f)
            .fireImmune()
            .clientTrackingRange(16)
            .updateInterval(1)
            .build("flak_shell")
    );
    public static final DeferredHolder<EntityType<?>, EntityType<HeavyHEShellProjectile>> HEAVY_HE_SHELL_PROJECTILE = ENTITY_TYPES.register("heavy_he_shell",
        () -> EntityType.Builder.<HeavyHEShellProjectile>of(HeavyHEShellProjectile::new, MobCategory.MISC)
            .sized(0.8f, 0.8f)
            .fireImmune()
            .clientTrackingRange(16)
            .updateInterval(1)
            .build("heavy_he_shell")
    );
    public static final DeferredHolder<EntityType<?>, EntityType<HEATShellProjectile>> HEAT_SHELL_PROJECTILE = ENTITY_TYPES.register("heat_shell",
        () -> EntityType.Builder.<HEATShellProjectile>of(HEATShellProjectile::new, MobCategory.MISC)
            .sized(0.8f, 0.8f)
            .fireImmune()
            .clientTrackingRange(16)
            .updateInterval(1)
            .build("heat_shell")
    );
    public static final DeferredHolder<EntityType<?>, EntityType<HESHShellProjectile>> HESH_SHELL_PROJECTILE = ENTITY_TYPES.register("hesh_shell",
        () -> EntityType.Builder.<HESHShellProjectile>of(HESHShellProjectile::new, MobCategory.MISC)
            .sized(0.8f, 0.8f)
            .fireImmune()
            .clientTrackingRange(16)
            .updateInterval(1)
            .build("hesh_shell")
    );
    public static final DeferredHolder<EntityType<?>, EntityType<BigHERocketProjectile>> BIG_HE_ROCKET_PROJECTILE = ENTITY_TYPES.register("big_he_rocket",
        () -> EntityType.Builder.<BigHERocketProjectile>of(BigHERocketProjectile::new, MobCategory.MISC)
            .sized(0.8f, 0.8f)
            .fireImmune()
            .clientTrackingRange(16)
            .updateInterval(1)
            .build("big_he_rocket")
    );
    public static final DeferredHolder<EntityType<?>, EntityType<BigAPRocketProjectile>> BIG_AP_ROCKET_PROJECTILE = ENTITY_TYPES.register("big_ap_rocket",
        () -> EntityType.Builder.<BigAPRocketProjectile>of(BigAPRocketProjectile::new, MobCategory.MISC)
            .sized(0.8f, 0.8f)
            .fireImmune()
            .clientTrackingRange(16)
            .updateInterval(1)
            .build("big_ap_rocket")
    );
    public static final DeferredHolder<EntityType<?>, EntityType<BigHEATRocketProjectile>> BIG_HEAT_ROCKET_PROJECTILE = ENTITY_TYPES.register("big_heat_rocket",
        () -> EntityType.Builder.<BigHEATRocketProjectile>of(BigHEATRocketProjectile::new, MobCategory.MISC)
            .sized(0.8f, 0.8f)
            .fireImmune()
            .clientTrackingRange(16)
            .updateInterval(1)
            .build("big_heat_rocket")
    );
    public static final DeferredHolder<EntityType<?>, EntityType<com.cbcatfix.rocket.RocketBlockProjectile>> ROCKET_BLOCK_PROJECTILE = ENTITY_TYPES.register("rocket_block_projectile",
        () -> EntityType.Builder.<com.cbcatfix.rocket.RocketBlockProjectile>of(com.cbcatfix.rocket.RocketBlockProjectile::new, MobCategory.MISC)
            .sized(0.25f, 0.25f)
            .fireImmune()
            .clientTrackingRange(16)
            .updateInterval(1)
            .build("rocket_block_projectile")
    );
    public static final DeferredHolder<Item, Item> SMALL_ROCKET_PROPELLANT = ITEMS.register("small_rocket_propellant",
        () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> MEDIUM_ROCKET_PROPELLANT = ITEMS.register("medium_rocket_propellant",
        () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> LARGE_ROCKET_PROPELLANT = ITEMS.register("large_rocket_propellant",
        () -> new Item(new Item.Properties()));

    public static final DeferredHolder<Item, FuzedProjectileBlockItem> FLAK_SHELL_ITEM = ITEMS.register("flak_shell",
        () -> new FuzedProjectileBlockItem(FLAK_SHELL.get(), new Item.Properties())
    );
    public static final DeferredHolder<Item, FuzedProjectileBlockItem> HEAVY_HE_SHELL_ITEM = ITEMS.register("heavy_he_shell",
        () -> new FuzedProjectileBlockItem(HEAVY_HE_SHELL.get(), new Item.Properties())
    );
    public static final DeferredHolder<Item, FuzedProjectileBlockItem> HEAT_SHELL_ITEM = ITEMS.register("heat_shell",
        () -> new FuzedProjectileBlockItem(HEAT_SHELL.get(), new Item.Properties())
    );
    public static final DeferredHolder<Item, FuzedProjectileBlockItem> HESH_SHELL_ITEM = ITEMS.register("hesh_shell",
        () -> new FuzedProjectileBlockItem(HESH_SHELL.get(), new Item.Properties())
    );
    public static final DeferredHolder<Item, net.minecraft.world.item.BlockItem> BIG_ROCKET_RAIL_ITEM = ITEMS.register("big_rocket_rail",
        () -> new net.minecraft.world.item.BlockItem(BIG_ROCKET_RAIL.get(), new Item.Properties())
    );
    public static final DeferredHolder<Item, net.minecraft.world.item.BlockItem> BIG_ROCKET_RAIL_BREECH_ITEM = ITEMS.register("big_rocket_rail_breech",
        () -> new net.minecraft.world.item.BlockItem(BIG_ROCKET_RAIL_BREECH.get(), new Item.Properties())
    );
    public static final DeferredHolder<Item, BigHERocketItem> BIG_HE_ROCKET_ITEM = ITEMS.register("big_he_rocket",
        () -> new BigHERocketItem(new Item.Properties())
    );
    public static final DeferredHolder<Item, BigAPRocketItem> BIG_AP_ROCKET_ITEM = ITEMS.register("big_ap_rocket",
        () -> new BigAPRocketItem(new Item.Properties())
    );
    public static final DeferredHolder<Item, BigHEATRocketItem> BIG_HEAT_ROCKET_ITEM = ITEMS.register("big_heat_rocket",
        () -> new BigHEATRocketItem(new Item.Properties())
    );

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
        ENTITY_TYPES.register(modEventBus);
        modEventBus.addListener(CbcatFixMunitions::registerRenderers);
        modEventBus.addListener(CbcatFixMunitions::commonSetup);
    }

    private static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            MunitionPropertiesHandler.registerProjectileHandler(FLAK_SHELL_PROJECTILE.get(), CBCMunitionPropertiesHandlers.COMMON_SHELL_BIG_CANNON_PROJECTILE);
            MunitionPropertiesHandler.registerProjectileHandler(HEAVY_HE_SHELL_PROJECTILE.get(), CBCMunitionPropertiesHandlers.COMMON_SHELL_BIG_CANNON_PROJECTILE);
            MunitionPropertiesHandler.registerProjectileHandler(HEAT_SHELL_PROJECTILE.get(), CBCMunitionPropertiesHandlers.COMMON_SHELL_BIG_CANNON_PROJECTILE);
            MunitionPropertiesHandler.registerProjectileHandler(HESH_SHELL_PROJECTILE.get(), CBCMunitionPropertiesHandlers.COMMON_SHELL_BIG_CANNON_PROJECTILE);
            MunitionPropertiesHandler.registerProjectileHandler(ROCKET_BLOCK_PROJECTILE.get(), CBCMunitionPropertiesHandlers.COMMON_SHELL_BIG_CANNON_PROJECTILE);
        });
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(com.dsvv.cbcat.registry.EntityRegister.AP_ROCKET.get(), com.cbcatfix.client.UnifiedRocketRenderer::new);
        event.registerEntityRenderer(com.dsvv.cbcat.registry.EntityRegister.FLAK_ROCKET.get(), com.cbcatfix.client.UnifiedRocketRenderer::new);
        event.registerEntityRenderer(com.dsvv.cbcat.registry.EntityRegister.HE_ROCKET.get(), com.cbcatfix.client.UnifiedRocketRenderer::new);
        event.registerEntityRenderer(com.dsvv.cbcat.registry.EntityRegister.HEI_ROCKET.get(), com.cbcatfix.client.UnifiedRocketRenderer::new);
        event.registerEntityRenderer(com.dsvv.cbcat.registry.EntityRegister.MEDIUM_AP_ROCKET.get(), com.cbcatfix.client.UnifiedRocketRenderer::new);
        event.registerEntityRenderer(com.dsvv.cbcat.registry.EntityRegister.MEDIUM_HE_ROCKET.get(), com.cbcatfix.client.UnifiedRocketRenderer::new);
        event.registerEntityRenderer(com.dsvv.cbcat.registry.EntityRegister.MEDIUM_HEF_ROCKET.get(), com.cbcatfix.client.UnifiedRocketRenderer::new);
        event.registerEntityRenderer(com.dsvv.cbcat.registry.EntityRegister.MEDIUM_HEAT_ROCKET.get(), com.cbcatfix.client.UnifiedRocketRenderer::new);
        event.registerEntityRenderer(FLAK_SHELL_PROJECTILE.get(), BigCannonProjectileRenderer::new);
        event.registerEntityRenderer(HEAVY_HE_SHELL_PROJECTILE.get(), BigCannonProjectileRenderer::new);
        event.registerEntityRenderer(HEAT_SHELL_PROJECTILE.get(), BigCannonProjectileRenderer::new);
        event.registerEntityRenderer(HESH_SHELL_PROJECTILE.get(), BigCannonProjectileRenderer::new);
        event.registerEntityRenderer(BIG_HE_ROCKET_PROJECTILE.get(), com.cbcatfix.client.UnifiedRocketRenderer::new);
        event.registerEntityRenderer(BIG_AP_ROCKET_PROJECTILE.get(), com.cbcatfix.client.UnifiedRocketRenderer::new);
        event.registerEntityRenderer(BIG_HEAT_ROCKET_PROJECTILE.get(), com.cbcatfix.client.UnifiedRocketRenderer::new);
        event.registerEntityRenderer(ROCKET_BLOCK_PROJECTILE.get(), BigCannonProjectileRenderer::new);

        event.registerBlockEntityRenderer(FUZED_BLOCK_ENTITY.get(), FuzedBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ROCKET_BLOCK_ENTITY.get(), com.cbcatfix.client.RocketBlockEntityRenderer::new);
    }
}
