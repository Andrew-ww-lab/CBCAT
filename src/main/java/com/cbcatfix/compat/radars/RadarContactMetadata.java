package com.cbcatfix.compat.radars;

import com.happysg.radar.api.jamming.DirectionalJammingApi;
import com.happysg.radar.api.tracking.RadarContact;
import com.happysg.radar.block.radar.track.RadarTrack;
import java.util.Optional;

/** The sole internal contact dependency: the public contact interface lacks deception metadata. */
public final class RadarContactMetadata {
    private RadarContactMetadata() {}

    public static Optional<Boolean> synthetic(RadarContact contact) {
        if (contact instanceof DirectionalJammingApi.ReportedContact reported)
            return Optional.of(reported.synthetic());
        // Verified 3.1.1 getActiveRadarContact returns the original RadarTrack.
        if (contact instanceof RadarTrack track) return Optional.of(track.isSynthetic());
        // An unknown contact representation must never silently become a real UUID target.
        return Optional.empty();
    }
}
