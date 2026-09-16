package com.cbcatfix.munitions;

import java.util.HashSet;
import java.util.Set;
import com.cbcatfix.config.CbcatFixConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import rbasamoyai.createbigcannons.CreateBigCannons;
import rbasamoyai.createbigcannons.block_armor_properties.BlockArmorPropertiesHandler;
import rbasamoyai.createbigcannons.block_armor_properties.BlockArmorPropertiesProvider;
import rbasamoyai.createbigcannons.config.CBCCfgMunitions;
import rbasamoyai.createbigcannons.config.CBCConfigs;
import rbasamoyai.createbigcannons.munitions.AbstractCannonProjectile;
import rbasamoyai.createbigcannons.munitions.ShellExplosion;
import rbasamoyai.createbigcannons.munitions.big_cannon.ProjectileBlock;

public final class HeatEffect {
    private static final double SAMPLE_INTERVAL = 0.25;

    private HeatEffect() {
    }

    public static void detonate(
        AbstractCannonProjectile source,
        Position position,
        Vec3 direction,
        float scale,
        CbcatFixConfig.HeatSettings settings
    ) {
        Level level = source.level();
        if (level.isClientSide()) {
            return;
        }

        float safeScale = Math.max(1.0f, scale);
        Vec3 origin = new Vec3(position.x(), position.y(), position.z());
        Vec3 jetDirection = resolveDirection(source, direction);

        Vec3 jetEnd = createJet(
            source,
            origin.add(jetDirection.scale(0.25)),
            jetDirection,
            settings.jetLength(),
            settings.jetEnergy() * safeScale,
            settings.jetPenetration() * safeScale,
            settings.jetEntityDamage() * safeScale
        );
        spawnJetEndParticles(level, jetEnd, jetDirection);
        playJetSounds(level, origin, jetEnd);

        ShellExplosion explosion = new ShellExplosion(
            level,
            source,
            level.damageSources().explosion(source, source),
            jetEnd.x,
            jetEnd.y,
            jetEnd.z,
            settings.blockExplosionPower() * safeScale,
            settings.entityExplosionPower() * safeScale,
            false,
            CBCConfigs.server().munitions.damageRestriction.get().explosiveInteraction()
        );
        CreateBigCannons.handleCustomExplosion(level, explosion);
    }

    private static Vec3 createJet(
        AbstractCannonProjectile source,
        Vec3 start,
        Vec3 direction,
        double maximumDistance,
        double initialEnergy,
        double penetration,
        float entityDamage
    ) {
        Level level = source.level();
        double remainingEnergy = initialEnergy;
        Set<BlockPos> visitedBlocks = new HashSet<>();
        Set<Integer> damagedEntities = new HashSet<>();
        boolean blockDamageAllowed = CBCConfigs.server().munitions.damageRestriction.get() != CBCCfgMunitions.GriefState.NO_DAMAGE;
        Vec3 jetEnd = start;

        for (double distance = 0.0; distance <= maximumDistance && remainingEnergy > 0.0; distance += SAMPLE_INTERVAL) {
            Vec3 point = start.add(direction.scale(distance));
            jetEnd = point;
            spawnJetParticles(level, point, direction, distance);
            BlockPos blockPos = BlockPos.containing(point);
            BlockState state = level.getBlockState(blockPos);

            if (!state.isAir() && visitedBlocks.add(blockPos.immutable())) {
                spawnArmorImpactParticles(level, point, direction);
                if (!blockDamageAllowed || state.getDestroySpeed(level, blockPos) < 0.0f) {
                    break;
                }

                BlockArmorPropertiesProvider armor = BlockArmorPropertiesHandler.getProperties(state);
                double toughness = sanitizeArmorValue(armor.toughness(level, state, blockPos, true), 1.0);
                double hardness = sanitizeArmorValue(armor.hardness(level, state, blockPos, true), 0.0);
                double hardnessFactor = Math.max(1.0, hardness / Math.max(0.01, penetration));
                double energyCost = Math.max(0.25, toughness * hardnessFactor);

                if (remainingEnergy + 1.0e-6 < energyCost) {
                    CreateBigCannons.BLOCK_DAMAGE.damageBlock(
                        blockPos.immutable(),
                        Math.max(1, Mth.ceil(remainingEnergy)),
                        state,
                        level
                    );
                    break;
                }

                remainingEnergy -= energyCost;
                level.setBlock(blockPos, Blocks.AIR.defaultBlockState(), ProjectileBlock.UPDATE_ALL_IMMEDIATE);
            }

            AABB damageArea = new AABB(point, point).inflate(0.4);
            for (Entity entity : level.getEntities(source, damageArea, entity -> entity.isAlive() && entity != source)) {
                if (damagedEntities.add(entity.getId())) {
                    entity.hurt(level.damageSources().magic(), entityDamage);
                }
            }
        }
        return jetEnd;
    }

    private static void spawnJetParticles(Level level, Vec3 point, Vec3 direction, double distance) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        // A dense white-hot core makes the penetration path readable even at rocket speed.
        serverLevel.sendParticles(
            ParticleTypes.END_ROD,
            point.x, point.y, point.z,
            2,
            0.012, 0.012, 0.012,
            0.002
        );
        serverLevel.sendParticles(
            ParticleTypes.ELECTRIC_SPARK,
            point.x, point.y, point.z,
            3,
            0.025, 0.025, 0.025,
            0.035
        );

        if (((int) Math.round(distance / SAMPLE_INTERVAL) & 1) != 0) {
            return;
        }
        serverLevel.sendParticles(
            ParticleTypes.SMALL_FLAME,
            point.x, point.y, point.z,
            3,
            direction.x * 0.04,
            direction.y * 0.04,
            direction.z * 0.04,
            0.035
        );
    }

    private static void spawnArmorImpactParticles(Level level, Vec3 point, Vec3 direction) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        serverLevel.sendParticles(
            ParticleTypes.CRIT,
            point.x, point.y, point.z,
            10,
            0.16, 0.16, 0.16,
            0.22
        );
        serverLevel.sendParticles(
            ParticleTypes.ELECTRIC_SPARK,
            point.x, point.y, point.z,
            14,
            Math.abs(direction.x) * 0.12 + 0.05,
            Math.abs(direction.y) * 0.12 + 0.05,
            Math.abs(direction.z) * 0.12 + 0.05,
            0.16
        );
    }

    private static void spawnJetEndParticles(Level level, Vec3 jetEnd, Vec3 direction) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        serverLevel.sendParticles(ParticleTypes.FLASH, jetEnd.x, jetEnd.y, jetEnd.z, 1, 0.0, 0.0, 0.0, 0.0);
        serverLevel.sendParticles(
            ParticleTypes.END_ROD,
            jetEnd.x, jetEnd.y, jetEnd.z,
            18,
            0.18, 0.18, 0.18,
            0.12
        );
        serverLevel.sendParticles(
            ParticleTypes.FLAME,
            jetEnd.x, jetEnd.y, jetEnd.z,
            12,
            Math.abs(direction.x) * 0.12 + 0.08,
            Math.abs(direction.y) * 0.12 + 0.08,
            Math.abs(direction.z) * 0.12 + 0.08,
            0.09
        );
    }

    private static void playJetSounds(Level level, Vec3 origin, Vec3 jetEnd) {
        // Two bright, high-pitched layers produce a distinct metallic "pierce" before the HEAT blast.
        level.playSound(
            null,
            origin.x, origin.y, origin.z,
            SoundEvents.BEACON_ACTIVATE,
            SoundSource.BLOCKS,
            2.4f,
            2.0f
        );
        level.playSound(
            null,
            jetEnd.x, jetEnd.y, jetEnd.z,
            SoundEvents.TRIDENT_RIPTIDE_3,
            SoundSource.BLOCKS,
            2.8f,
            1.75f
        );
        level.playSound(
            null,
            jetEnd.x, jetEnd.y, jetEnd.z,
            SoundEvents.AMETHYST_BLOCK_CHIME,
            SoundSource.BLOCKS,
            2.0f,
            2.0f
        );
    }

    private static Vec3 resolveDirection(AbstractCannonProjectile source, Vec3 requestedDirection) {
        if (requestedDirection.lengthSqr() > 1.0e-8) {
            return requestedDirection.normalize();
        }
        Vec3 velocity = source.getDeltaMovement();
        if (velocity.lengthSqr() > 1.0e-8) {
            return velocity.normalize();
        }
        Vec3 orientation = source.getOrientation();
        return orientation.lengthSqr() > 1.0e-8 ? orientation.normalize() : new Vec3(0.0, -1.0, 0.0);
    }

    private static double sanitizeArmorValue(double value, double fallback) {
        return Double.isFinite(value) && value >= 0.0 ? value : fallback;
    }
}
