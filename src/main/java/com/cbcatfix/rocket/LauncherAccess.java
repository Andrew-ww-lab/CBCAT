package com.cbcatfix.rocket;

import net.minecraft.world.level.block.entity.BlockEntity;

/** A CBC contraption owns detached BEs; a Sable plot still owns real world BEs. */
public final class LauncherAccess {
    private LauncherAccess() {}

    public static boolean canTransfer(BlockEntity breech) {
        var level = breech.getLevel();
        return level != null && !breech.isRemoved() && !LauncherTransfer.isLocked(breech)
            && level.getBlockEntity(breech.getBlockPos()) == breech;
    }
}
