package com.cbcatfix.rocket;

import com.cbcatfix.compat.radars.RadarContactMetadata;
import com.happysg.radar.api.network.RadarNetworkApi;
import com.happysg.radar.api.tracking.RadarContactCategory;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import java.util.UUID;

/** Loaded only when Radar is present. Copies identity, never the controller itself. */
final class RadarDesignation {
    private RadarDesignation() {}

    static RocketSeeker capture(Level level, BlockPos monitor) {
        if (!(level instanceof ServerLevel server) || !loaded(server, monitor)) return null;
        // Preserve legacy monitorPos semantics until the migration policy is applied:
        // it identified a controller, not an arbitrary connected endpoint.
        if (!RadarNetworkApi.getNetworkController(server, monitor).filter(monitor::equals).isPresent()) return null;
        return captureNetwork(server, monitor);
    }

    static RocketSeeker captureNetwork(ServerLevel level, BlockPos endpoint) {
        if (!loaded(level, endpoint)) return null;
        var controller = RadarNetworkApi.getNetworkController(level, endpoint).orElse(null);
        if (controller == null || !loaded(level, controller)) return null;
        return snapshot(RadarNetworkApi.getSelectedContact(level, endpoint).orElse(null));
    }

    private static boolean loaded(ServerLevel level, BlockPos pos) {
        return level.hasChunk(pos.getX() >> 4, pos.getZ() >> 4);
    }

    static RocketSeeker snapshot(com.happysg.radar.api.tracking.RadarContact track) {
        if (track == null || !RocketSeeker.isFinite(track.getPosition())) return null;
        if (track.getId() == null || track.getId().isBlank()) return null;
        var synthetic = RadarContactMetadata.synthetic(track);
        if (synthetic.isEmpty()) return null;
        UUID targetId = null;
        if (!synthetic.get()) {
            try {
                targetId = UUID.fromString(track.getId());
            } catch (IllegalArgumentException invalidId) {
                // A radar contact need not be an entity; preserve its point and string identity.
            }
        }
        return new RocketSeeker(track.getId(), targetId, track.getCategory() == RadarContactCategory.SABLE,
            track.getPosition(), true, synthetic.get());
    }
}
