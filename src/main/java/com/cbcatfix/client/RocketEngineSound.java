package com.cbcatfix.client;

import com.cbcatfix.CbcatFix;
import com.cbcatfix.rocket.RocketBalance;
import com.cbcatfix.rocket.RocketFlightEffects;
import com.cbcatfix.rocket.RocketPayloadAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import rbasamoyai.createbigcannons.munitions.AbstractCannonProjectile;

final class RocketEngineSound extends AbstractTickableSoundInstance {
    private static final ResourceLocation BURNER_LOOP = ResourceLocation.fromNamespaceAndPath(
        "aeronautics", "block.hot_air_burner.idle");
    private static final int ENGINE_ATTENUATION_DISTANCE = 48;
    private final AbstractCannonProjectile rocket;
    private final float targetVolume;
    private final float enginePitch;

    RocketEngineSound(AbstractCannonProjectile rocket) {
        super(engineEvent(), SoundSource.NEUTRAL, rocket.level().random);
        this.rocket = rocket;
        this.looping = true;
        this.delay = 0;
        this.attenuation = Attenuation.LINEAR;
        this.relative = false;
        RocketBalance.Tier tier = RocketBalance.tierForProjectile(rocket);
        this.targetVolume = switch (tier) {
            case SMALL -> 0.9f;
            case MEDIUM -> 1.3f;
            case BIG -> 1.8f;
        };
        this.enginePitch = switch (tier) {
            case SMALL -> 1.25f;
            case MEDIUM -> 1.0f;
            case BIG -> 0.75f;
        };
        this.volume = this.targetVolume * 0.25f;
        this.pitch = this.enginePitch;
        this.updatePosition();
    }

    private static SoundEvent engineEvent() {
        // Optional resource reuse only: no Aeronautics classes or hard dependency.
        if (Minecraft.getInstance().getSoundManager().getSoundEvent(BURNER_LOOP) != null) {
            return SoundEvent.createVariableRangeEvent(BURNER_LOOP);
        }
        return CbcatFix.ROCKET_ENGINE.get();
    }

    @Override
    public WeighedSoundEvents resolve(SoundManager manager) {
        WeighedSoundEvents events = super.resolve(manager);
        if (events != null && this.sound != SoundManager.EMPTY_SOUND
            && this.sound != SoundManager.INTENTIONALLY_EMPTY_SOUND) {
            // The stationary burner asset normally has an 8-block range. Adapt only this
            // rocket instance's metadata; OpenAL/Minecraft still owns distance attenuation.
            Sound original = this.sound;
            this.sound = new Sound(original.getLocation(), original.getVolume(), original.getPitch(),
                original.getWeight(), original.getType(), original.shouldStream(),
                original.shouldPreload(), ENGINE_ATTENUATION_DISTANCE);
        }
        return events;
    }

    @Override
    public boolean canStartSilent() {
        return true; // Remain tracked when spawned outside hearing range, then become audible on approach.
    }

    @Override
    public void tick() {
        if (this.rocket.isRemoved() || this.rocket.isInGround()
            || !(this.rocket instanceof RocketPayloadAccess payload)
            || payload.cbcatfix$getPoweredFlightTicks() <= 0) {
            this.stop();
            RocketClientEvents.release(this.rocket, this);
            return;
        }
        this.volume += (this.targetVolume - this.volume) * 0.25f;
        this.pitch = this.enginePitch * (1.0f + 0.015f
            * (float) Math.sin(this.rocket.tickCount * 0.16f + this.rocket.getId()));
        this.updatePosition();
    }

    private void updatePosition() {
        Vec3 rear = RocketFlightEffects.rearPosition(this.rocket);
        this.x = rear.x;
        this.y = rear.y;
        this.z = rear.z;
    }
}
