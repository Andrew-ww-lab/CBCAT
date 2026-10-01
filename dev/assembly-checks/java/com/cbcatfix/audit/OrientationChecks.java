package com.cbcatfix.audit;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.createmod.catnip.levelWrappers.SchematicLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import rbasamoyai.createbigcannons.cannon_control.cannon_mount.CannonMountBlockEntity;
import rbasamoyai.createbigcannons.cannon_control.contraption.PitchOrientedContraptionEntity;
import rbasamoyai.createbigcannons.cannons.CannonContraptionProviderBlock;
import rbasamoyai.createbigcannons.index.CBCBlocks;
import java.lang.reflect.Method;

/** Exercises the woven lifecycle hooks and native reset/serialization without placing gameplay blocks. */
final class OrientationChecks {
    static int run(ServerLevel original) throws Exception {
        int cases = 0;
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            for (Direction mounting : new Direction[] {Direction.UP, Direction.DOWN}) {
                for (String block : new String[] {"createbigcannons:cast_iron_autocannon_breech",
                    "cbc_at:steel_vert_twin_autocannon_breech", "cbc_at:steel_rocket_pod_breech",
                    "cbc_at:wrought_iron_medium_rocket_rail_breech", "cbcatfix:big_rocket_rail_breech"}) {
                    var fixture = new SchematicLevel(original) {
                        @Override public void blockEntityChanged(BlockPos pos) {}
                    };
                    var state = CBCBlocks.CANNON_MOUNT.getDefaultState()
                        .setValue(BlockStateProperties.HORIZONTAL_FACING, facing)
                        .setValue(BlockStateProperties.VERTICAL_DIRECTION, mounting);
                    BlockPos pos = new BlockPos(0, 80, 0);
                    fixture.setBlock(pos, state, 3);
                    var mount = (CannonMountBlockEntity) fixture.getBlockEntity(pos);
                    mount.initialize();
                    var provider = (CannonContraptionProviderBlock) BuiltInRegistries.BLOCK.get(ResourceLocation.parse(block));
                    var cannon = provider.getCannonContraption();
                    cannon.bounds = new net.minecraft.world.phys.AABB(BlockPos.ZERO);
                    // Assembly collection is checked separately; these are canonical local axes for the lifecycle.
                    var directionField = rbasamoyai.createbigcannons.cannon_control.contraption.AbstractMountedCannonContraption.class
                        .getDeclaredField("initialOrientation");
                    directionField.setAccessible(true);
                    directionField.set(cannon, facing);
                    var entity = PitchOrientedContraptionEntity.create(fixture, cannon, facing, mount);
                    set(mount, "mountedContraption", entity);
                    set(mount, "running", true);
                    mount.setYaw(facing.toYRot() + 23.5f);
                    mount.setPitch(12.25f);
                    float expectedYaw = facing.toYRot() + 23.5f;
                    Method reset = CannonMountBlockEntity.class.getDeclaredMethod("resetContraptionToOffset");
                    reset.setAccessible(true);
                    Operation<Void> nativeReset = args -> {
                        try { reset.invoke(args[0]); } catch (ReflectiveOperationException ex) { throw new AssertionError(ex); }
                        return null;
                    };
                    hook("cbcatfix$keepMountAim").invoke(mount, mount, nativeReset);
                    assertAngle(get(mount, "cannonYaw"), expectedYaw, "saved yaw");
                    assertAngle(get(mount, "cannonPitch"), 12.25f, "saved pitch");
                    assertAngle(entity.pitch, 0, "grid restoration pitch");
                    set(mount, "mountedContraption", null);
                    set(mount, "running", false);
                    mount.tick();
                    assertAngle(get(mount, "cannonYaw"), expectedYaw, "idle yaw");
                    assertAngle(get(mount, "cannonPitch"), 12.25f, "idle pitch");
                    var tag = mount.saveWithFullMetadata(original.registryAccess());
                    mount.loadWithComponents(tag, original.registryAccess());
                    entity = PitchOrientedContraptionEntity.create(fixture, cannon, facing, mount);
                    set(mount, "mountedContraption", entity);
                    set(mount, "running", true);
                    hook("cbcatfix$restoreMountAim").invoke(mount, mount, nativeReset);
                    assertAngle(entity.yaw, expectedYaw, "restored yaw");
                    boolean positive = (facing.getAxisDirection() == Direction.AxisDirection.POSITIVE)
                        == (facing.getAxis() == Direction.Axis.X);
                    assertAngle(entity.pitch, positive ? 12.25f : -12.25f, "restored local pitch");
                    assertAngle(entity.prevPitch, entity.pitch, "no interpolation jump");
                    cases++;
                }
            }
        }
        System.out.println("CBCAT AUDIT: orientation lifecycle hook cases=" + cases);
        return cases;
    }
    private static Method hook(String name) {
        for (var method : CannonMountBlockEntity.class.getDeclaredMethods()) {
            if (method.getName().contains(name)) { method.setAccessible(true); return method; }
        }
        throw new AssertionError("Missing transformed hook " + name);
    }
    private static void set(Object target, String name, Object value) throws Exception {
        var field = CannonMountBlockEntity.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
    private static float get(Object target, String name) throws Exception {
        var field = CannonMountBlockEntity.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.getFloat(target);
    }
    private static void assertAngle(float actual, float expected, String label) {
        if (Math.abs(actual - expected) > 0.0001f) throw new AssertionError(label + ": " + actual + " != " + expected);
    }
}
