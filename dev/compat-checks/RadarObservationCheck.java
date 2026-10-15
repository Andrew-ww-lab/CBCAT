package com.cbcatfix.rocket;

import com.happysg.radar.api.jamming.DirectionalJammingApi;
import com.happysg.radar.api.tracking.RadarContactCategory;
import net.minecraft.world.phys.Vec3;

/** Pins the exact installed API contract that prevents canonical-position bypass. */
public final class RadarObservationCheck {
    public static void main(String[] args) {
        Vec3 physical = new Vec3(10, 20, 30);
        // 3.1.0 returns this public snapshot from reportedTracks; a raw internal
        // RadarTrack no longer carries jamming/synthetic data through resolveGuidance.
        var state = new DirectionalJammingApi.JammingState("source", 1, 1, 0, 0,
            new Vec3(3, 0, 0), new Vec3(0, 2, 0), 7);
        var track = new DirectionalJammingApi.ReportedContact("contact", Vec3.ZERO, Vec3.ZERO,
            RadarContactCategory.MISC, 42, "check", 1, false, false, state);
        var jammed = DirectionalJammingApi.resolveGuidance(track, physical, Vec3.ZERO);
        if (!jammed.position().equals(new Vec3(13, 20, 30))
            || !jammed.velocity().equals(new Vec3(0, 2, 0)) || !jammed.jammed())
            throw new AssertionError("Jamming must alter canonical observations");
        track = new DirectionalJammingApi.ReportedContact("contact", Vec3.ZERO, Vec3.ZERO,
            RadarContactCategory.MISC, 42, "check", 1, false, true, state);
        var synthetic = DirectionalJammingApi.resolveGuidance(track, physical, Vec3.ZERO);
        if (!synthetic.synthetic() || !synthetic.position().equals(Vec3.ZERO))
            throw new AssertionError("Synthetic contacts must ignore physical UUID observations");
        if (!com.cbcatfix.compat.radars.RadarContactMetadata.synthetic(track).orElseThrow())
            throw new AssertionError("Public reported contact must preserve synthetic metadata");
        var internal = new com.happysg.radar.block.radar.track.RadarTrack("contact", physical, Vec3.ZERO, 42,
            com.happysg.radar.block.radar.track.TrackCategory.SABLE, "check", 1);
        internal.setSynthetic(true);
        if (!com.cbcatfix.compat.radars.RadarContactMetadata.synthetic(internal).orElseThrow())
            throw new AssertionError("Native selected contact must preserve synthetic metadata");
        var snapshot = RadarDesignation.snapshot(internal);
        internal.setPosition(new Vec3(99, 99, 99));
        if (snapshot == null || !snapshot.synthetic() || snapshot.save().hasUUID("Target")
            || snapshot.save().getDouble("X") != physical.x)
            throw new AssertionError("Launch snapshot must detach from controller contact and preserve false identity");
        var basic = new com.happysg.radar.api.tracking.BasicRadarContact("contact", physical, Vec3.ZERO,
            RadarContactCategory.MISC);
        if (com.cbcatfix.compat.radars.RadarContactMetadata.synthetic(basic).isPresent())
            throw new AssertionError("Missing metadata must not silently mean a real target");
        if (DirectionalJammingApi.resolveGuidance(null, physical, Vec3.ZERO) != null)
            throw new AssertionError("Missing contact must not reveal canonical position");
        System.out.println("Radars observation contract checks passed");
    }
}
