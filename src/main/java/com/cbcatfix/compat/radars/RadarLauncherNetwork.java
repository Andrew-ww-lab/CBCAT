package com.cbcatfix.compat.radars;

import com.happysg.radar.block.behavior.networks.WeaponNetworkRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/** Approved internal bridge: public network API cannot resolve a mount back to its pitch endpoint. */
public final class RadarLauncherNetwork {
    private RadarLauncherNetwork() {}

    public static BlockPos endpoint(ServerLevel level, BlockPos mount) {
        var runtime = WeaponNetworkRuntime.peek(level);
        if (runtime == null) return null;
        var control = runtime.getWeaponControlViewForMount(mount);
        if (control == null || !control.validTopology() || control.channelForMount(mount) == null) return null;
        return control.pitchPos();
    }
}
