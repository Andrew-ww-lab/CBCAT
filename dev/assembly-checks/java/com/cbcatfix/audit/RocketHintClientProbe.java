package com.cbcatfix.audit;

import com.cbcatfix.rocket.*;
import com.cbcatfix.munitions.CbcatFixMunitions;
import net.createmod.catnip.placement.PlacementClient;
import net.createmod.catnip.placement.PlacementHelpers;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import java.util.LinkedHashMap;
import java.util.Map;

/** Opt-in visual probe inside the existing world. Fixtures are client-only and restored before exit. */
@EventBusSubscriber(modid = "cbcatfix", value = Dist.CLIENT)
public final class RocketHintClientProbe {
    private static boolean active;
    private static int phase, ticks, capture = -1;
    private static BlockPos root;
    private static Vec3 aim;
    private static ItemStack savedHand, held;
    private static float yaw, pitch;
    private static final Map<BlockPos, BlockState> saved = new LinkedHashMap<>();
    private static final String[] NAMES = {"small-slot", "medium-stack", "ground", "create-shaft", "occupied-slot"};

    static void start(Minecraft mc) {
        if (PlacementHelpers.getHelpersView().stream().noneMatch(h -> h instanceof com.cbcatfix.client.RocketPlacementHelper))
            throw new AssertionError("Rocket helper not registered");
        root = mc.player.blockPosition().offset(0, 1, 3);
        savedHand = mc.player.getMainHandItem().copy();
        yaw = mc.player.getYRot(); pitch = mc.player.getXRot();
        for (BlockPos p : BlockPos.betweenClosed(root.offset(-2,-1,-2), root.offset(2,2,2))) {
            if (mc.level.getBlockEntity(p) != null) throw new AssertionError("Visual fixture would overlap existing BE");
            saved.put(p.immutable(), mc.level.getBlockState(p));
        }
        active = true;
        setup(mc);
    }

    private static ItemStack item(String id) { return BuiltInRegistries.ITEM.get(ResourceLocation.parse(id)).getDefaultInstance(); }
    private static void put(Minecraft mc, BlockPos p, BlockState state) { mc.level.setBlock(p, state, 18); }

    private static void setup(Minecraft mc) {
        saved.keySet().forEach(p -> put(mc, p, Blocks.AIR.defaultBlockState()));
        for (BlockPos p : BlockPos.betweenClosed(root.offset(-2,-1,-2), root.offset(2,-1,2)))
            put(mc, p, Blocks.STONE.defaultBlockState());
        if (phase == 0 || phase == 4) {
            put(mc, root, BuiltInRegistries.BLOCK.get(ResourceLocation.parse("cbc_at:steel_rocket_pod_breech"))
                .defaultBlockState().setValue(BlockStateProperties.FACING, Direction.NORTH));
            put(mc, root.north(), BuiltInRegistries.BLOCK.get(ResourceLocation.parse("cbc_at:steel_rocket_pod_rail"))
                .defaultBlockState().setValue(BlockStateProperties.FACING, Direction.NORTH));
            var be = mc.level.getBlockEntity(root);
            aim = RocketMounts.transform(be, 6).center().add(Vec3.atLowerCornerOf(root));
            held = com.dsvv.cbcat.registry.ItemRegister.HE_ROCKET_ITEM.get().getDefaultInstance();
            if (phase == 4) ((MountedRocketStorage) be).cbcatfix$setRocketInSlot(6, held.copy());
        } else if (phase == 1) {
            var state = CbcatFixMunitions.ROCKET_BLOCK.get().defaultBlockState()
                .setValue(RocketBlock.TIER, RocketBalance.Tier.MEDIUM.ordinal()).setValue(RocketBlock.FACING, Direction.WEST);
            put(mc, root, state);
            var be = (RocketBlockEntity) mc.level.getBlockEntity(root);
            held = com.dsvv.cbcat.registry.ItemRegister.MEDIUM_HE_ROCKET_ITEM.get().getDefaultInstance();
            for (int i=0;i<3;i++) be.addRocket(held);
            aim = Vec3.atCenterOf(root).add(0, -.3, 0);
        } else if (phase == 2) {
            held = CbcatFixMunitions.BIG_HE_ROCKET_ITEM.get().getDefaultInstance();
            aim = Vec3.atCenterOf(root.below()).add(0,.5,0);
        } else {
            var shaft = BuiltInRegistries.BLOCK.get(ResourceLocation.parse("create:shaft"));
            put(mc, root, shaft.defaultBlockState().setValue(BlockStateProperties.AXIS, Direction.Axis.X));
            held = item("create:shaft");
            aim = Vec3.atCenterOf(root).add(.3,0,0);
        }
        if (held.isEmpty()) throw new AssertionError("Missing held item for " + NAMES[phase]);
        mc.player.setItemInHand(InteractionHand.MAIN_HAND, held);
        point(mc);
        ticks = 0;
    }

    private static void point(Minecraft mc) {
        Vec3 d = aim.subtract(mc.player.getEyePosition());
        mc.player.setYRot((float)Math.toDegrees(Math.atan2(-d.x, d.z)));
        mc.player.setXRot((float)-Math.toDegrees(Math.atan2(d.y, Math.sqrt(d.x*d.x+d.z*d.z))));
        mc.player.yRotO = mc.player.getYRot(); mc.player.xRotO = mc.player.getXRot();
    }

    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        if (!active) return;
        var mc = Minecraft.getInstance();
        point(mc);
        if (++ticks == 80) {
            var hit = mc.hitResult;
            var candidate = hit instanceof net.minecraft.world.phys.BlockHitResult block
                ? RocketPlacementCandidate.resolve(mc.level, mc.player, block, held) : null;
            boolean expected = phase < 2;
            if (phase != 3 && (candidate != null) != expected)
                throw new AssertionError("Hint candidate mismatch " + NAMES[phase] + " hit=" + hit);
            if ((phase < 2 || phase == 3) && PlacementClient.getCurrentAlpha() <= 0)
                throw new AssertionError("Native placement helper did not activate: " + NAMES[phase]);
            if ((phase == 2 || phase == 4) && PlacementClient.getCurrentAlpha() != 0)
                throw new AssertionError("Invalid target still shows an indicator: " + NAMES[phase]);
            if (phase < 2) {
                try {
                    var field = net.createmod.catnip.ghostblock.GhostBlocks.class.getDeclaredField("ghosts");
                    field.setAccessible(true);
                    var ghosts = (java.util.Map<?, ?>) field.get(net.createmod.catnip.ghostblock.GhostBlocks.getInstance());
                    if (ghosts.keySet().stream().noneMatch(h -> h instanceof com.cbcatfix.client.RocketPlacementHelper))
                        throw new AssertionError("Ghost expired before rendering");
                } catch (ReflectiveOperationException e) { throw new RuntimeException(e); }
            }
            System.out.println("CBCAT HINT CLIENT: " + NAMES[phase] + " candidate=" + candidate + " alpha=" + PlacementClient.getCurrentAlpha());
            capture = phase;
        }
        if (ticks > 100 && capture == -1) {
            if (++phase < NAMES.length) setup(mc);
            else {
                saved.forEach((p,state) -> put(mc,p,state));
                mc.player.setItemInHand(InteractionHand.MAIN_HAND,savedHand);
                mc.player.setYRot(yaw); mc.player.setXRot(pitch);
                active = false;
                System.out.println("CBCAT HINT CLIENT PASSED: native shaft, launcher, medium stack, ground, invalid slot; client-only fixtures restored");
                mc.stop();
            }
        }
    }

    @SubscribeEvent public static void rendered(RenderGuiEvent.Post event) {
        if (capture < 0) return;
        try (var image = net.minecraft.client.Screenshot.takeScreenshot(Minecraft.getInstance().getMainRenderTarget())) {
            image.writeToFile(java.nio.file.Path.of("rocket-hint-" + NAMES[capture] + ".png"));
        } catch (java.io.IOException e) { throw new RuntimeException(e); }
        capture = -1;
    }
}
