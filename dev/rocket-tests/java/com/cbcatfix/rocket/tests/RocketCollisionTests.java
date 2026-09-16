package com.cbcatfix.rocket.tests;

import com.cbcatfix.munitions.CbcatFixMunitions;
import com.cbcatfix.rocket.*;
import com.dsvv.cbcat.registry.EntityRegister;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import rbasamoyai.createbigcannons.config.CBCCfgMunitions.GriefState;
import rbasamoyai.createbigcannons.munitions.AbstractCannonProjectile;
import rbasamoyai.createbigcannons.munitions.AbstractCannonProjectile.ImpactResult;
import rbasamoyai.createbigcannons.munitions.AbstractCannonProjectile.ImpactResult.KinematicOutcome;
import rbasamoyai.createbigcannons.munitions.ProjectileContext;

@GameTestHolder("cbcatfix")
@PrefixGameTestTemplate(false)
public class RocketCollisionTests {
    private static AbstractCannonProjectile rocket(GameTestHelper helper, int size) {
        AbstractCannonProjectile rocket = switch (size) {
            case 0 -> EntityRegister.AP_ROCKET.create(helper.getLevel());
            case 1 -> EntityRegister.MEDIUM_AP_ROCKET.create(helper.getLevel());
            default -> CbcatFixMunitions.BIG_AP_ROCKET_PROJECTILE.get().create(helper.getLevel());
        };
        prepare(helper, rocket, true, size);
        return rocket;
    }

    private static void prepare(GameTestHelper helper, AbstractCannonProjectile rocket, boolean ap, int size) {
        rocket.setPos(Vec3.atCenterOf(helper.absolutePos(new BlockPos(2, 3, 3))));
        rocket.setDeltaMovement(new Vec3(3, 0, 0));
        RocketFlightEffects.setForward(rocket, new Vec3(1, 0, 0));
        RocketPayloadAccess payload = (RocketPayloadAccess) rocket;
        payload.cbcatfix$configurePayload(1, ItemStack.EMPTY, ap);
        payload.cbcatfix$configureFlight(3, 3);
        payload.cbcatfix$configureDurability(RocketBalance.maximumDurability(RocketBalance.Tier.values()[size]));
        payload.cbcatfix$setPoweredFlightTicks(100);
    }

    private static ImpactResult impact(GameTestHelper helper, AbstractCannonProjectile rocket,
                                      net.minecraft.world.level.block.Block block, GriefState grief) {
        BlockPos pos = helper.absolutePos(new BlockPos(5, 3, 3));
        helper.getLevel().setBlockAndUpdate(pos, block.defaultBlockState());
        BlockHitResult hit = new BlockHitResult(new Vec3(pos.getX(), pos.getY() + 0.5, pos.getZ() + 0.5),
            Direction.WEST, pos, false);
        try {
            Class<?> base = rocket instanceof com.dsvv.cbcat.cannon.rocketpod.munitions.AbstractRocket<?>
                ? com.dsvv.cbcat.cannon.rocketpod.munitions.AbstractRocket.class
                : com.dsvv.cbcat.cannon.medium_rocketpod.munitions.AbstractMediumRocket.class;
            var method = base.getDeclaredMethod("calculateBlockPenetration", ProjectileContext.class,
                net.minecraft.world.level.block.state.BlockState.class, BlockHitResult.class);
            method.setAccessible(true);
            return (ImpactResult) method.invoke(rocket, new ProjectileContext(rocket, grief), block.defaultBlockState(), hit);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("Transformed penetration method failed", exception);
        }
    }

    @GameTest(template = "rocket_empty")
    public static void permeableBlocksAllSizes(GameTestHelper helper) {
        for (int size = 0; size < 3; size++) {
            var rocket = rocket(helper, size);
            var payload = (RocketPayloadAccess) rocket;
            float mass = rocket.getProjectileMass();
            float hp = payload.cbcatfix$getDurability();
            var result = impact(helper, rocket, Blocks.OAK_LEAVES, GriefState.ALL_DAMAGE);
            helper.assertTrue(result.kinematics() == KinematicOutcome.PENETRATE && !result.shouldRemove(), "Leaves must pass");
            helper.assertTrue(payload.cbcatfix$getDurability() == hp && rocket.getProjectileMass() == mass, "Leaves must not damage");
            helper.assertTrue(Math.abs(rocket.getDeltaMovement().x - 2.94) < 1e-6, "Leaves must slow by 2%");
            impact(helper, rocket, Blocks.GLASS_PANE, GriefState.ALL_DAMAGE);
            helper.assertTrue(payload.cbcatfix$getDurability() == hp - 1, "Glass costs one hull HP");
            impact(helper, rocket, Blocks.WHITE_WOOL, GriefState.ALL_DAMAGE);
            helper.assertTrue(payload.cbcatfix$getDurability() == hp - 3 && rocket.getProjectileMass() == mass, "Wool costs two hull HP, no penetrator mass");
            helper.assertTrue(!((RocketEngineAccess) rocket).cbcatfix$isEngineDisabled(), "Permeable impacts must preserve engine");
        }
        helper.succeed();
    }

    @GameTest(template = "rocket_empty")
    public static void bigApUsesCbcBudget(GameTestHelper helper) {
        var rocket = rocket(helper, 2);
        helper.assertTrue(rocket.getProjectileMass() == 8, "Big AP needs its own mass of eight");
        helper.assertTrue(((RocketPayloadAccess) rocket).cbcatfix$getMaximumDurability() == 40, "No AP hull bonus");
        var first = impact(helper, rocket, Blocks.IRON_BLOCK, GriefState.ALL_DAMAGE);
        helper.assertTrue(first.kinematics() == KinematicOutcome.PENETRATE && !first.shouldRemove(), "First iron block");
        helper.assertTrue(Math.abs(rocket.getProjectileMass() - 5) < 0.001, "CBC must subtract three mass");
        var second = impact(helper, rocket, Blocks.IRON_BLOCK, GriefState.ALL_DAMAGE);
        helper.assertTrue(second.kinematics() == KinematicOutcome.PENETRATE, "Second iron block");
        var third = impact(helper, rocket, Blocks.IRON_BLOCK, GriefState.ALL_DAMAGE);
        helper.assertTrue(third.kinematics() == KinematicOutcome.STOP, "CBC must still stop insufficient mass");
        helper.assertTrue(((RocketEngineAccess) rocket).cbcatfix$isEngineDisabled(), "Solid impact disables engine");
        helper.succeed();
    }

    @GameTest(template = "rocket_empty")
    public static void hullFailureAndSave(GameTestHelper helper) {
        var rocket = rocket(helper, 2);
        float mass = rocket.getProjectileMass();
        rocket.hurt(helper.getLevel().damageSources().generic(), 40);
        helper.assertTrue(!rocket.isRemoved() && rocket.getProjectileMass() == mass, "Shot-down AP remains a penetrator");
        helper.assertTrue(((RocketPayloadAccess) rocket).cbcatfix$getPoweredFlightTicks() == 0, "No engine after hull loss");
        CompoundTag save = new CompoundTag();
        rocket.saveWithoutId(save);
        var restored = rocket(helper, 2);
        restored.load(save);
        helper.assertTrue(((RocketEngineAccess) restored).cbcatfix$isEngineDisabled(), "Engine failure survives save");
        helper.assertTrue(((RocketPayloadAccess) restored).cbcatfix$getPoweredFlightTicks() == 0, "Fuel must not restore engine");
        helper.succeed();
    }

    @GameTest(template = "rocket_empty")
    public static void explosiveHullFailure(GameTestHelper helper) {
        var rocket = CbcatFixMunitions.BIG_HE_ROCKET_PROJECTILE.get().create(helper.getLevel());
        prepare(helper, rocket, false, 2);
        rocket.hurt(helper.getLevel().damageSources().generic(), 40);
        helper.assertTrue(rocket.isRemoved(), "Destroyed explosive rocket is removed");
        helper.assertTrue(((com.cbcatfix.mixin.AbstractCannonProjectileAccess) rocket).cbcatfix$isPendingRemoval(), "Destroyed explosive rocket is spent");
        helper.succeed();
    }

    @GameTest(template = "rocket_empty")
    public static void protectedBlocksStaySolid(GameTestHelper helper) {
        var rocket = rocket(helper, 2);
        var result = impact(helper, rocket, Blocks.OAK_LEAVES, GriefState.NO_DAMAGE);
        helper.assertTrue(result.kinematics() == KinematicOutcome.STOP, "No terrain damage must not bypass CBC");
        helper.assertTrue(helper.getLevel().getBlockState(helper.absolutePos(new BlockPos(5, 3, 3))).is(Blocks.OAK_LEAVES),
            "Protected leaves must remain");
        helper.succeed();
    }

    @GameTest(template = "rocket_empty")
    public static void actualTickPassesLeaves(GameTestHelper helper) {
        var rocket = rocket(helper, 2);
        helper.setBlock(new BlockPos(3, 3, 3), Blocks.OAK_LEAVES);
        helper.setBlock(new BlockPos(4, 3, 3), Blocks.OAK_LEAVES);
        float mass = rocket.getProjectileMass();
        rocket.tick();
        helper.assertTrue(rocket.getProjectileMass() == mass, "Actual sweep must preserve mass in leaves");
        helper.assertTrue(!((RocketEngineAccess) rocket).cbcatfix$isEngineDisabled(), "Actual leaf sweep must preserve engine");
        helper.assertTrue(rocket.getDeltaMovement().length() < 3, "Actual sweep slows rocket despite thrust");
        helper.assertTrue(helper.getLevel().getBlockState(helper.absolutePos(new BlockPos(3, 3, 3))).isAir(), "Leaf ray must make progress");
        helper.succeed();
    }

    @GameTest(template = "rocket_empty")
    public static void timedFuzesExplodeOrDisable(GameTestHelper helper) {
        ItemStack timer = rbasamoyai.createbigcannons.index.CBCItems.TIMED_FUZE.asStack();
        timer.set(rbasamoyai.createbigcannons.index.CBCDataComponents.FUZE_TIMER, 1);
        var ap = rocket(helper, 2);
        ((RocketPayloadAccess) ap).cbcatfix$configurePayload(1, timer, true);
        float mass = ap.getProjectileMass();
        ap.tick();
        helper.assertTrue(!ap.isRemoved() && ap.getProjectileMass() == mass, "AP timer preserves kinetic projectile");
        helper.assertTrue(((RocketEngineAccess) ap).cbcatfix$isEngineDisabled(), "AP timer disables engine");

        var he = CbcatFixMunitions.BIG_HE_ROCKET_PROJECTILE.get().create(helper.getLevel());
        prepare(helper, he, false, 2);
        he.setFuze(timer);
        he.tick();
        helper.assertTrue(((com.cbcatfix.mixin.AbstractCannonProjectileAccess) he).cbcatfix$isPendingRemoval(), "HE timer must detonate");
        he.tick();
        helper.assertTrue(he.isRemoved(), "HE timer removes spent rocket");
        helper.succeed();
    }

    @GameTest(template = "rocket_empty")
    public static void explosiveLeavesDoNotTriggerImpactFuze(GameTestHelper helper) {
        var he = CbcatFixMunitions.BIG_HE_ROCKET_PROJECTILE.get().create(helper.getLevel());
        prepare(helper, he, false, 2);
        he.setFuze(rbasamoyai.createbigcannons.index.CBCItems.IMPACT_FUZE.asStack());
        helper.setBlock(new BlockPos(3, 3, 3), Blocks.OAK_LEAVES);
        he.tick();
        helper.assertTrue(!((com.cbcatfix.mixin.AbstractCannonProjectileAccess) he).cbcatfix$isPendingRemoval(), "Leaves do not activate contact fuze");
        helper.assertTrue(((RocketPayloadAccess) he).cbcatfix$getDurability() == 40, "Explosive leaves cost no hull HP");
        helper.assertTrue(!((RocketEngineAccess) he).cbcatfix$isEngineDisabled(), "Explosive rocket keeps engine through leaves");
        helper.succeed();
    }

    @GameTest(template = "rocket_empty")
    public static void actualTickIronStopsEngine(GameTestHelper helper) {
        var ap = rocket(helper, 2);
        helper.setBlock(new BlockPos(3, 3, 3), Blocks.IRON_BLOCK);
        helper.setBlock(new BlockPos(4, 3, 3), Blocks.IRON_BLOCK);
        ap.tick();
        helper.assertTrue(((RocketEngineAccess) ap).cbcatfix$isEngineDisabled(), "Actual iron sweep disables engine");
        helper.assertTrue(helper.getLevel().getBlockState(helper.absolutePos(new BlockPos(4, 3, 3))).isAir(), "Actual sweep penetrates second iron");
        helper.assertTrue(ap.getProjectileMass() > 0 && ap.getProjectileMass() < 3, "Remaining CBC mass is spent normally");
        helper.succeed();
    }

    @GameTest(template = "rocket_empty")
    public static void legacyHullBonusIsRemoved(GameTestHelper helper) {
        var ap = rocket(helper, 2);
        CompoundTag legacy = new CompoundTag();
        ap.saveWithoutId(legacy);
        legacy.remove("CbcatFixMechanicsVersion");
        legacy.putFloat("CbcatFixMaximumDurability", 60);
        legacy.putFloat("CbcatFixDurability", 30);
        ap.load(legacy);
        helper.assertTrue(((RocketPayloadAccess) ap).cbcatfix$getMaximumDurability() == 40, "Legacy max bonus removed");
        helper.assertTrue(((RocketPayloadAccess) ap).cbcatfix$getDurability() == 20, "Legacy damage fraction preserved");
        helper.succeed();
    }
}
