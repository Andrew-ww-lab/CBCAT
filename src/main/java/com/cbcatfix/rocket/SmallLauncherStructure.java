package com.cbcatfix.rocket;

import com.cbcatfix.balance.BalanceDefaults;
import com.dsvv.cbcat.cannon.rocketpod.RocketPodBlock;
import com.simibubi.create.content.contraptions.AssemblyException;
import net.minecraft.world.level.block.state.BlockState;
import rbasamoyai.createbigcannons.cannon_control.contraption.AbstractMountedCannonContraption;
import rbasamoyai.createbigcannons.index.CBCAutocannonMaterials;

import java.util.ArrayList;
import java.util.List;

/** Assembly roles deliberately exclude waterlogging and axial rail facing. */
public final class SmallLauncherStructure {
    private SmallLauncherStructure() {}

    public static boolean isSteel(BlockState state) {
        return state != null && state.getBlock() instanceof RocketPodBlock block
            && block.getAutocannonMaterial().equals(CBCAutocannonMaterials.STEEL);
    }

    public static List<String> validate(AbstractMountedCannonContraption cannon) throws AssemblyException {
        var root = cannon.getBlocks().get(cannon.getStartPos());
        if (root == null || !(root.state().getBlock() instanceof RocketPodBlock)) return List.of();
        boolean steel = isSteel(root.state());
        int length = RocketMounts.length(cannon);
        if (steel && (length < BalanceDefaults.SMALL_LAUNCHER_MIN_LENGTH
            || length > BalanceDefaults.SMALL_LAUNCHER_MAX_LENGTH || length != cannon.getBlocks().size()))
            throw AbstractMountedCannonContraption.invalidCannon();
        var roles = new ArrayList<String>();
        boolean hasModule = false;
        for (int index = 0; index < length; index++) {
            var info = cannon.getBlocks().get(cannon.getStartPos().relative(cannon.initialOrientation(), index));
            if (info == null || !(info.state().getBlock() instanceof RocketPodBlock block))
                throw AbstractMountedCannonContraption.invalidCannon();
            if (block instanceof LauncherModuleBlock module) {
                if (!steel || !block.isComplete(info.state()) || index != 1 || hasModule)
                    throw AbstractMountedCannonContraption.invalidCannon();
                hasModule = true;
                roles.add(module.module().name());
            } else {
                if (steel && (!isSteel(info.state()) || !block.isComplete(info.state())
                    || block.isBreechMechanism(info.state()) != (index == 0)))
                    throw AbstractMountedCannonContraption.invalidCannon();
                roles.add(index == 0 ? "BREECH" : "SECTION");
            }
        }
        return List.copyOf(roles);
    }

    public static LauncherModuleBlock.Module module(AbstractMountedCannonContraption cannon) {
        var root = cannon.getBlocks().get(cannon.getStartPos());
        if (root == null || !isSteel(root.state())) return null;
        var info = cannon.getBlocks().get(cannon.getStartPos().relative(cannon.initialOrientation()));
        return info != null && info.state().getBlock() instanceof LauncherModuleBlock block
            && block.isComplete(info.state()) ? block.module() : null;
    }

    public static float spread(int length) {
        return Math.max(0, BalanceDefaults.SMALL_LAUNCHER_SHORT_SPREAD
            * (BalanceDefaults.SMALL_LAUNCHER_SPREAD_REFERENCE_LENGTH - length)
            / (BalanceDefaults.SMALL_LAUNCHER_SPREAD_REFERENCE_LENGTH - BalanceDefaults.SMALL_LAUNCHER_MIN_LENGTH));
    }

    public static double rateMultiplier(AbstractMountedCannonContraption cannon) {
        return module(cannon) == LauncherModuleBlock.Module.RAPID ? BalanceDefaults.RAPID_CYCLING_RATE : 1;
    }
}
