package com.cbcatfix.rocket;

import net.minecraft.core.BlockPos;

/** Read-only view of the existing round-robin sequence. Never consumes ammunition. */
public interface LauncherShotView {
    BlockPos cbcatfix$nextLoadedLane();
}
