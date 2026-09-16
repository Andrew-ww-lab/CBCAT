package com.cbcatfix.compat.jade;

import com.cbcatfix.CbcatFix;
import com.cbcatfix.rocket.RocketTargetResolver;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.util.ModIdentification;

import java.util.Optional;

enum RocketJadeProvider implements IBlockComponentProvider {
    INSTANCE;

    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(
        CbcatFix.MOD_ID, "individual_rocket"
    );

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        target(accessor).ifPresent(rocket -> {
            tooltip.replace(JadeIds.CORE_OBJECT_NAME, rocket.getHoverName());
            tooltip.replace(JadeIds.CORE_MOD_NAME,
                IThemeHelper.get().modName(ModIdentification.getModName(rocket)));
        });
    }

    @Override
    public IElement getIcon(BlockAccessor accessor, IPluginConfig config, IElement defaultIcon) {
        return target(accessor)
            .<IElement>map(rocket -> IElementHelper.get().item(rocket.copyWithCount(1)))
            .orElse(defaultIcon);
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public int getDefaultPriority() {
        return 10000; // Jade's mod-name provider runs at 9999; replace its adapter label afterwards.
    }

    private static Optional<ItemStack> target(BlockAccessor accessor) {
        if (accessor.getHitResult() == null) {
            return Optional.empty();
        }
        var placed = RocketTargetResolver.resolveInteraction(
            accessor.getLevel(), accessor.getHitResult(), accessor.getPlayer()
        ).map(RocketTargetResolver.Target::rocket);
        if (placed.isPresent()) return placed;
        var breech = com.cbcatfix.rocket.LauncherAssembly.findBreech(accessor.getLevel(), accessor.getPosition());
        if (!(breech instanceof com.cbcatfix.rocket.MountedRocketStorage storage)) return Optional.empty();
        var selected = com.cbcatfix.rocket.MountedRocketInteraction.viewed(breech, accessor.getPlayer(), false);
        return selected == null ? Optional.empty() : Optional.of(storage.cbcatfix$rocketInSlot(selected.slot()));
    }
}
