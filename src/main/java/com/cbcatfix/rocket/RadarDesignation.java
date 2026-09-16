package com.cbcatfix.rocket;

import com.cbcatfix.CbcatFix;
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
        try {
            return new RocketSeeker(UUID.fromString(track.getId()), track.getTrackCategory() == TrackCategory.SABLE,
                track.getPosition());
        } catch (IllegalArgumentException invalidId) {
            CbcatFix.LOGGER.debug("Radar target has no physical UUID: {}", track.getId());
            return null;
        }
    }
}
