package com.cbcatfix.client.ponder;

import com.cbcatfix.rocket.LauncherModules;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/** Introductory construction scene; live firing and automation belong to later tutorials. */
public final class SmallLauncherScenes {
    private SmallLauncherScenes() {}

    public static void construction(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("small_launcher", "Building a Small Rocket Launcher");
        scene.configureBasePlate(0, 0, 5);
        scene.scaleSceneView(0.85f);
        scene.setSceneOffsetY(-0.75f);
        scene.showBasePlate();
        scene.world().modifyBlockEntityNBT(util.select().position(3, 1, 2),
            rbasamoyai.createbigcannons.cannon_control.cannon_mount.CannonMountBlockEntity.class, tag -> {
                tag.putFloat("CannonYaw", Direction.WEST.toYRot());
                tag.putFloat("CannonPitch", 0);
            });
        scene.idle(10);
        scene.world().showSection(util.select().position(3, 1, 2), Direction.DOWN);
        scene.overlay().showText(80).attachKeyFrame()
            .text("Start with a Cannon Mount. Place the breech in its upper support.")
            .pointAt(util.vector().centerOf(3, 1, 2));
        scene.idle(90);
        scene.world().showSection(util.select().position(3, 3, 2), Direction.DOWN);
        scene.idle(15);
        scene.world().showSection(util.select().position(2, 3, 2), Direction.EAST);
        scene.overlay().showText(90).attachKeyFrame()
            .text("One steel breech and one steel rail form the shortest Small launcher. Face both in the firing direction.")
            .pointAt(util.vector().centerOf(2, 3, 2));
        scene.idle(100);
        scene.world().showSection(util.select().position(1, 3, 2), Direction.EAST);
        scene.overlay().showText(80).attachKeyFrame()
            .text("Small launchers can be 2 or 3 blocks long in total, including the breech.")
            .pointAt(util.vector().centerOf(1, 3, 2));
        scene.idle(90);
        scene.world().setBlock(util.grid().at(2, 3, 2), LauncherModules.RAPID.get().defaultBlockState()
            .setValue(BlockStateProperties.FACING, Direction.WEST), true);
        scene.overlay().showText(100).attachKeyFrame()
            .text("An optional module replaces the rail directly in front of the breech. Use at most one module; it counts towards the length limit.")
            .pointAt(util.vector().centerOf(2, 3, 2));
        scene.idle(110);
        scene.overlay().showText(100).attachKeyFrame()
            .text("Rapid Cycling doubles the firing rate. Traverse improves powered aiming instead. Choose the module to suit your installation.")
            .pointAt(util.vector().centerOf(2, 3, 2));
        scene.idle(110);
        scene.world().setBlock(util.grid().at(2, 3, 2), LauncherModules.TRAVERSE.get().defaultBlockState()
            .setValue(BlockStateProperties.FACING, Direction.WEST), true);
        scene.idle(20);
        scene.markAsFinished();
    }
}