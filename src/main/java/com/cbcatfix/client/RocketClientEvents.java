package com.cbcatfix.client;

import com.dsvv.cbcat.cannon.medium_rocketpod.munitions.AbstractMediumRocket;
import com.dsvv.cbcat.cannon.rocketpod.munitions.AbstractRocket;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import com.cbcatfix.rocket.RocketPayloadAccess;
import rbasamoyai.createbigcannons.munitions.AbstractCannonProjectile;

import java.util.Map;
import java.util.IdentityHashMap;

@net.neoforged.fml.common.EventBusSubscriber(modid = "cbcatfix", value = net.neoforged.api.distmarker.Dist.CLIENT)
public final class RocketClientEvents {
    private static final Map<AbstractCannonProjectile, RocketEngineSound> ACTIVE_ENGINE_SOUNDS = new IdentityHashMap<>();

    private RocketClientEvents() {
    }

    @net.neoforged.bus.api.SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        // NeoForge applies additional spawn data AFTER EntityJoinLevelEvent.
        // Start once fuel is available, not before the payload packet arrives.
        if (!event.getEntity().level().isClientSide()
            || (!(event.getEntity() instanceof AbstractRocket<?>)
                && !(event.getEntity() instanceof AbstractMediumRocket<?>))
            || !(event.getEntity() instanceof AbstractCannonProjectile rocket)
            || !(rocket instanceof RocketPayloadAccess payload)
            || payload.cbcatfix$getPoweredFlightTicks() <= 0 || rocket.isInGround()
            || ACTIVE_ENGINE_SOUNDS.containsKey(rocket)) {
            return;
        }
        RocketEngineSound engine = new RocketEngineSound(rocket);
        ACTIVE_ENGINE_SOUNDS.put(rocket, engine);
        Minecraft.getInstance().getSoundManager().play(engine);
    }

    static void release(AbstractCannonProjectile rocket, RocketEngineSound sound) {
        if (ACTIVE_ENGINE_SOUNDS.remove(rocket, sound)) {
            Minecraft.getInstance().getSoundManager().stop(sound);
        }
    }

    @net.neoforged.bus.api.SubscribeEvent
    public static void onEntityLeave(EntityLeaveLevelEvent event) {
        if (!event.getLevel().isClientSide()) return;
        RocketEngineSound engine = ACTIVE_ENGINE_SOUNDS.remove(event.getEntity());
        if (engine != null) Minecraft.getInstance().getSoundManager().stop(engine);
    }

    @net.neoforged.bus.api.SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (!event.getLevel().isClientSide()) return;
        ACTIVE_ENGINE_SOUNDS.values().forEach(Minecraft.getInstance().getSoundManager()::stop);
        ACTIVE_ENGINE_SOUNDS.clear();
    }
}
