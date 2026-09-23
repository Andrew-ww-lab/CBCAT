package com.cbcatfix.rocket;

import com.happysg.radar.api.jamming.DirectionalJammingApi;
import com.happysg.radar.block.radar.track.RadarTrack;
import com.happysg.radar.block.radar.track.TrackCategory;
import net.minecraft.world.phys.Vec3;

/** Pins the exact installed API contract that prevents canonical-position bypass. */
public final class RadarObservationCheck {
    public static void main(String[] args) {
        Vec3 physical = new Vec3(10, 20, 30);
        RadarTrack track = new RadarTrack("contact", Vec3.ZERO, Vec3.ZERO, 42,
            TrackCategory.MISC, "check", 1);
        track.setJammingData(new RadarTrack.JammingData("source", 1, 1, 0, 0,
            new Vec3(3, 0, 0), new Vec3(0, 2, 0), 7));
        var jammed = DirectionalJammingApi.resolveGuidance(track, physical, Vec3.ZERO);
        if (!jammed.position().equals(new Vec3(13, 20, 30))
            || !jammed.velocity().equals(new Vec3(0, 2, 0)) || !jammed.jammed())
            throw new AssertionError("Jamming must alter canonical observations");
        track.setSynthetic(true);
        var synthetic = DirectionalJammingApi.resolveGuidance(track, physical, Vec3.ZERO);
        if (!synthetic.synthetic() || !synthetic.position().equals(Vec3.ZERO))
            throw new AssertionError("Synthetic contacts must ignore physical UUID observations");
        if (DirectionalJammingApi.resolveGuidance(null, physical, Vec3.ZERO) != null)
            throw new AssertionError("Missing contact must not reveal canonical position");
        System.out.println("Radars observation contract checks passed");
    }
}
