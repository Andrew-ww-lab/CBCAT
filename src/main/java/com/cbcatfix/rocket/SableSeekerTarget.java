package com.cbcatfix.rocket;

import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.mixinterface.clip_overwrite.ClipContextExtension;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import java.util.UUID;

/** Direct UUID lookup in Sable's world-space bounds; no entity scan or radar dependency. */
final class SableSeekerTarget {
    private SableSeekerTarget() {}

    static RocketSeeker.Observation observe(ServerLevel level, UUID id) {
        var container = SubLevelContainer.getContainer(level);
        var target = container == null ? null : container.getSubLevel(id);
        if (target == null || target.isRemoved()) return null;
        Vector3d position = target.boundingBox().center(new Vector3d());
        // Consecutive poses give blocks/game tick, including rotation, not physics metres/second.
        Vector3d local = target.logicalPose().transformPositionInverse(new Vector3d(position));
        Vector3d previous = target.lastPose().transformPosition(local);
        return new RocketSeeker.Observation(new Vec3(position.x, position.y, position.z),
            new Vec3(position.x - previous.x, position.y - previous.y, position.z - previous.z));
    }

    static void ignoreTargetHull(ServerLevel level, UUID id, ClipContext ray) {
        var container = SubLevelContainer.getContainer(level);
        var target = container == null ? null : container.getSubLevel(id);
        if (target != null && ray instanceof ClipContextExtension extension) {
            extension.sable$setIgnoredSubLevel(target);
        }
    }
}
