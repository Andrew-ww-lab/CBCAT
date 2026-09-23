package com.cbcatfix.rocket;

import com.happysg.radar.block.controller.networkcontroller.NetworkFiltererBlockEntity;
import com.happysg.radar.block.radar.track.TrackCategory;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import java.util.UUID;

/** Loaded only when Radar is present. Copies identity, never the controller itself. */
final class RadarDesignation {
    private RadarDesignation() {}

    static RocketSeeker capture(Level level, BlockPos monitor) {
        if (!(level.getBlockEntity(monitor) instanceof NetworkFiltererBlockEntity controller)) return null;
        var track = controller.activeTrackCache;
        if (track == null || !RocketSeeker.isFinite(track.getPosition())) return null;
        if (track.getId() == null || track.getId().isBlank()) return null;
        UUID targetId = null;
        if (!track.isSynthetic()) {
            try {
                targetId = UUID.fromString(track.getId());
            } catch (IllegalArgumentException invalidId) {
                // A radar contact need not be an entity; preserve its point and string identity.
            }
        }
        return new RocketSeeker(track.getId(), targetId, track.getTrackCategory() == TrackCategory.SABLE,
            track.getPosition(), true, track.isSynthetic());
    }
}
