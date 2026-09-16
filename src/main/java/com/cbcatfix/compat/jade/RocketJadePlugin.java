package com.cbcatfix.compat.jade;

import com.cbcatfix.rocket.RocketBlock;
import com.cbcatfix.rocket.RocketBodyExtensionBlock;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/** Optional client bridge; this class is discovered only when Jade is installed. */
@WailaPlugin("cbcatfix")
public final class RocketJadePlugin implements IWailaPlugin {
    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(RocketJadeProvider.INSTANCE, RocketBlock.class);
        registration.registerBlockComponent(RocketJadeProvider.INSTANCE, RocketBodyExtensionBlock.class);
        registration.registerBlockIcon(RocketJadeProvider.INSTANCE, RocketBlock.class);
        registration.registerBlockIcon(RocketJadeProvider.INSTANCE, RocketBodyExtensionBlock.class);
        registration.registerBlockComponent(RocketJadeProvider.INSTANCE, com.dsvv.cbcat.cannon.rocketpod.RocketPodBarrelBlock.class);
        registration.registerBlockIcon(RocketJadeProvider.INSTANCE, com.dsvv.cbcat.cannon.rocketpod.RocketPodBarrelBlock.class);
        registration.registerBlockComponent(RocketJadeProvider.INSTANCE, com.dsvv.cbcat.cannon.medium_rocketpod.MediumRocketPodBarrelBlock.class);
        registration.registerBlockIcon(RocketJadeProvider.INSTANCE, com.dsvv.cbcat.cannon.medium_rocketpod.MediumRocketPodBarrelBlock.class);
    }
}
