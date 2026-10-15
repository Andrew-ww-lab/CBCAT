package com.cbcatfix.audit;

import com.cbcatfix.rocket.MountedRocketStorage;
import com.happysg.radar.api.weapon.WeaponShotAdapterRegistry;
import com.happysg.radar.api.weapon.WeaponShotContext;
import com.happysg.radar.api.weapon.WeaponShotProfile;
import com.happysg.radar.compat.cbc.CannonMountContext;
import com.happysg.radar.compat.cbc.CbcRadarWeaponAdapter;
import net.createmod.catnip.levelWrappers.SchematicLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import rbasamoyai.createbigcannons.cannon_control.cannon_mount.CannonMountBlockEntity;
import rbasamoyai.createbigcannons.cannon_control.contraption.PitchOrientedContraptionEntity;
import rbasamoyai.createbigcannons.cannons.CannonContraptionProviderBlock;
import rbasamoyai.createbigcannons.index.CBCBlocks;

/** Exercises the real 3.1.0 adapter registry with in-memory CBC mounts. */
final class OptionalRadarWeaponChecks {
    static int run(ServerLevel world) throws Exception {
        int checks = 0;
        String[][] tiers = {
            {"cbc_at:steel_rocket_pod_breech", "cbc_at:steel_rocket_pod_rail", "cbc_at:he_rocket_item"},
            {"cbc_at:wrought_iron_medium_rocket_rail_breech", "cbc_at:wrought_iron_medium_rocket_rail", "cbc_at:medium_he_rocket_item"},
            {"cbcatfix:big_rocket_rail_breech", "cbcatfix:big_rocket_rail", "cbcatfix:big_he_rocket"}
        };
        for (int tier = 0; tier < tiers.length; tier++) {
            var fixture = new SchematicLevel(world) {
                @Override public void blockEntityChanged(BlockPos pos) {}
            };
            var root = new BlockPos(0, 81, 0);
            for (int i = 0; i < (tier == 2 ? 3 : 2); i++) {
                var block = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(tiers[tier][i == 0 ? 0 : 1]));
                var pos = root.north(i);
                fixture.setBlock(pos, block.defaultBlockState().setValue(BlockStateProperties.FACING, Direction.NORTH), 3);
                if (fixture.getBlockEntity(pos) instanceof com.simibubi.create.foundation.blockEntity.SmartBlockEntity be)
                    be.initialize();
            }
            var provider = (CannonContraptionProviderBlock) fixture.getBlockState(root).getBlock();
            var cannon = provider.getCannonContraption();
            require(cannon.assemble(fixture, root), "assembly");
            fixture.setBlock(root.below(), CBCBlocks.CANNON_MOUNT.getDefaultState(), 3);
            var mount = (CannonMountBlockEntity) fixture.getBlockEntity(root.below());
            mount.initialize();
            var entity = PitchOrientedContraptionEntity.create(world, cannon, Direction.NORTH, mount);
            var field = CannonMountBlockEntity.class.getDeclaredField("mountedContraption");
            field.setAccessible(true);
            field.set(mount, entity);
            var adapter = new CbcRadarWeaponAdapter(world, CannonMountContext.of(mount));
            var context = new WeaponShotContext(world, adapter.createContext(world));
            var empty = WeaponShotAdapterRegistry.resolve(context);
            require(empty != null && !empty.canTriggerFire() && empty.fingerprint().equals("cbcatfix:empty"), "empty launcher disabled");
            var storage = (MountedRocketStorage) cannon.presentBlockEntities.get(cannon.getStartPos());
            var rocket = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(tiers[tier][2])));
            require(!rocket.isEmpty(), "registered rocket");
            storage.cbcatfix$setRocketInSlot(0, rocket);
            var before = cannon.writeNBT(world.registryAccess(), false);
            var loaded = WeaponShotAdapterRegistry.resolve(context);
            require(loaded != null && loaded.aimMode() == WeaponShotProfile.AimMode.DIRECT
                && loaded.fingerprint().startsWith("cbcatfix:"), "native registry selects rocket direct adapter");
            require(before.equals(cannon.writeNBT(world.registryAccess(), false)), "profile lookup is read-only");
            require(!loaded.canTriggerFire() && "rocket_launcher_arming".equals(loaded.diagnosticReason()),
                "arming reports the existing firing lock");
            com.cbcatfix.rocket.RocketSeekerEquipment.setActiveRadar(rocket);
            var projectile = com.cbcatfix.rocket.RocketProjectileFactory.create(rocket, world);
            com.cbcatfix.rocket.RocketLaunchGuidance.initialize(rocket, projectile, entity);
            var payload = (com.cbcatfix.rocket.RocketPayloadAccess) projectile;
            require(payload.cbcatfix$flightState().activeRadarEquipped && !payload.cbcatfix$hasGuidance(),
                "launch without network has equipment but no homing");
            field.set(mount, null);
            require(WeaponShotAdapterRegistry.resolve(context) == null, "detached mount ignored");
            checks += 8;
        }
        System.out.println("CBCAT RADARS WEAPON: " + checks + " assertions passed; native 3.1.0 registry");
        return checks;
    }

    private static void require(boolean value, String label) {
        if (!value) throw new AssertionError(label);
    }
}
