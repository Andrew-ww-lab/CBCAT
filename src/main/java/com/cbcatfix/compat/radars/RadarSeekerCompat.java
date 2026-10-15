package com.cbcatfix.compat.radars;

import com.cbcatfix.config.CbcatFixConfig;
import com.cbcatfix.rocket.*;
import com.happysg.radar.api.jamming.DirectionalJammingApi;
import com.happysg.radar.block.arad.rwr.ExternalRwrEmitterRegistry;
import com.happysg.radar.api.radar.rwr.RadarRwrTypes;
import com.happysg.radar.api.tracking.BasicRadarContact;
import com.happysg.radar.api.tracking.RadarContact;
import com.happysg.radar.api.tracking.RadarContactCategory;
import com.happysg.radar.chaff.ChaffLockAdapter;
import com.happysg.radar.chaff.ChaffLockRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import rbasamoyai.createbigcannons.munitions.AbstractCannonProjectile;

import java.util.List;

/** Optional sensor adapter. No controller lookup, global target cache or custom EW rolls. */
public final class RadarSeekerCompat {
    private RadarSeekerCompat() {}

    public static void register() {
        registerType(com.dsvv.cbcat.registry.EntityRegister.AP_ROCKET.get());
        registerType(com.dsvv.cbcat.registry.EntityRegister.FLAK_ROCKET.get());
        registerType(com.dsvv.cbcat.registry.EntityRegister.HE_ROCKET.get());
        registerType(com.dsvv.cbcat.registry.EntityRegister.HEI_ROCKET.get());
        registerType(com.dsvv.cbcat.registry.EntityRegister.MEDIUM_AP_ROCKET.get());
        registerType(com.dsvv.cbcat.registry.EntityRegister.MEDIUM_HE_ROCKET.get());
        registerType(com.dsvv.cbcat.registry.EntityRegister.MEDIUM_HEF_ROCKET.get());
        registerType(com.dsvv.cbcat.registry.EntityRegister.MEDIUM_HEAT_ROCKET.get());
        registerType(com.cbcatfix.munitions.CbcatFixMunitions.BIG_AP_ROCKET_PROJECTILE.get());
        registerType(com.cbcatfix.munitions.CbcatFixMunitions.BIG_HE_ROCKET_PROJECTILE.get());
        registerType(com.cbcatfix.munitions.CbcatFixMunitions.BIG_HEAT_ROCKET_PROJECTILE.get());
        registerType(com.cbcatfix.munitions.CbcatFixMunitions.ROCKET_BLOCK_PROJECTILE.get());
        NeoForge.EVENT_BUS.addListener(RadarSeekerCompat::afterTick);
        NeoForge.EVENT_BUS.addListener(RadarSeekerCompat::onLeave);
    }

    private static <T extends Entity> void registerType(EntityType<T> type) {
        ChaffLockRegistry.register(type, new ChaffLockAdapter<T>() {
            @Override public String getTargetId(T entity) {
                RocketSeeker seeker = activeSeeker(entity);
                return seeker == null ? null : seeker.contactId();
            }
            @Override public void applySuppression(T entity, String targetId, long untilTick) {
                if (entity instanceof RocketPayloadAccess payload && payload.cbcatfix$flightState().seeker != null) {
                    payload.cbcatfix$flightState().seeker.suppress(targetId, untilTick);
                    removeEmitter(entity);
                }
            }
        });
    }

    private static RocketSeeker activeSeeker(Entity entity) {
        if (!entity.isAlive() || entity.isRemoved()
            || !CbcatFixConfig.value(CbcatFixConfig.GUIDANCE_ENABLED)
            || !(entity instanceof RocketPayloadAccess payload) || !payload.cbcatfix$hasGuidance()
            || payload.cbcatfix$getPoweredFlightTicks() <= 0 || !payload.cbcatfix$flightState().hasClearedLauncher()) return null;
        RocketSeeker seeker = payload.cbcatfix$flightState().seeker;
        return seeker != null && seeker.activeRadar() && !seeker.suppressed(entity.level().getGameTime()) ? seeker : null;
    }

    public static RocketSeeker.Observation observe(AbstractCannonProjectile rocket, RocketSeeker seeker,
                                                   RocketSeeker.Observation canonical) {
        if (!(rocket.level() instanceof ServerLevel level)) return null;
        var raw = canonical == null ? List.<RadarContact>of() : List.of(new BasicRadarContact(seeker.contactId(),
            canonical.position(), canonical.velocity(),
            seeker.subLevelTarget() ? RadarContactCategory.SABLE : RadarContactCategory.MISC));
        var emitter = new DirectionalJammingApi.EmitterContext(sourceId(rocket), rocket.getUUID(),
            rocket.position(), RocketFlightEffects.forward(rocket), range(rocket), halfAngle());
        for (var reported : DirectionalJammingApi.reportedTracks(level, emitter, raw)) {
            // Designated-target-only acquisition: never promote an unrelated ghost into a real entity.
            if (!seeker.contactId().equals(reported.getId())) continue;
            var observation = DirectionalJammingApi.resolveGuidance(reported,
                canonical == null ? null : canonical.position(), canonical == null ? null : canonical.velocity());
            if (observation != null) return new RocketSeeker.Observation(observation.position(),
                observation.velocity(), observation.jammed(), observation.synthetic());
        }
        return null;
    }

    private static void afterTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        if (!(entity.level() instanceof ServerLevel level) || !(entity instanceof RocketPayloadAccess)) return;
        RocketSeeker seeker = activeSeeker(entity);
        if (seeker == null || seeker.contactState() == RocketSeeker.ContactState.LAST_KNOWN
            || seeker.contactState() == RocketSeeker.ContactState.AMBIGUOUS
            || seeker.contactState() == RocketSeeker.ContactState.FALSE_CONTACT) {
            removeEmitter(entity);
            return;
        }
        AbstractCannonProjectile rocket = (AbstractCannonProjectile) entity;
        ExternalRwrEmitterRegistry.heartbeat(level, new ExternalRwrEmitterRegistry.EmitterState(
            sourceId(entity), entity.position(), RocketFlightEffects.forward(rocket), range(rocket), halfAngle(),
            null, ExternalRwrEmitterRegistry.ThreatStage.ENGAGED, RadarRwrTypes.AIRBORNE, false,
            new ExternalRwrEmitterRegistry.SelectionMetadata(entity.getUUID(), 0, 0)), 2);
    }

    private static void onLeave(EntityLeaveLevelEvent event) { removeEmitter(event.getEntity()); }

    private static void removeEmitter(Entity entity) {
        if (entity.level() instanceof ServerLevel level && entity instanceof RocketPayloadAccess)
            ExternalRwrEmitterRegistry.remove(level, sourceId(entity));
    }

    private static String sourceId(Entity entity) { return "cbcatfix:seeker:" + entity.getUUID(); }
    private static double range(AbstractCannonProjectile rocket) {
        return CbcatFixConfig.settings(RocketBalance.tierForProjectile(rocket)).maximumGuidanceRange();
    }
    private static float halfAngle() { return CbcatFixConfig.value(CbcatFixConfig.SEEKER_HALF_ANGLE).floatValue(); }
}
