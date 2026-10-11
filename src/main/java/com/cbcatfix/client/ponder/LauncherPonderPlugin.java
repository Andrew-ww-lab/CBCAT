package com.cbcatfix.client.ponder;

import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

/** Client-only entry point for the fix's interactive tutorials. */
public final class LauncherPonderPlugin implements PonderPlugin {
    @Override public String getModId() { return "cbcatfix"; }

    @Override public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        helper.forComponents(
            ResourceLocation.parse("cbc_at:steel_rocket_pod_breech"),
            ResourceLocation.parse("cbc_at:steel_rocket_pod_rail"))
            .addStoryBoard("small_launcher", SmallLauncherScenes::construction);
    }
}