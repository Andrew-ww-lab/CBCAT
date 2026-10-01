package com.cbcatfix.client;

import com.cbcatfix.munitions.BigRocketRailBlock;
import com.cbcatfix.munitions.BigRocketRailBreechBlock;
import com.cbcatfix.munitions.CbcatFixMunitions;
import com.cbcatfix.rocket.MountedRocketStorage;
import com.cbcatfix.rocket.RocketBalance;
import com.cbcatfix.rocket.RocketGeometry;
import com.dsvv.cbcat.cannon.medium_rocketpod.MediumRocketPodBlock;
import com.dsvv.cbcat.cannon.medium_rocketpod.breech.MediumRocketPodBreechBlockEntity;
import com.dsvv.cbcat.cannon.rocketpod.RocketPodBlock;
import com.dsvv.cbcat.cannon.rocketpod.breech.RocketPodBreechBlockEntity;
import com.dsvv.cbcat.registry.BlockRegister;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import rbasamoyai.createbigcannons.cannons.autocannon.material.AutocannonMaterial;

import java.util.List;
import java.util.Locale;

/** Standard client tooltip event; values are read from native material/breech gameplay APIs. */
@EventBusSubscriber(modid = "cbcatfix", value = Dist.CLIENT)
public final class LauncherTooltips {
    private LauncherTooltips() {}

    @SubscribeEvent
    public static void tooltip(ItemTooltipEvent event) {
        if (!(event.getItemStack().getItem() instanceof BlockItem item)) return;
        var block = item.getBlock();
        var state = block.defaultBlockState();
        RocketBalance.Tier tier;
        AutocannonMaterial material;
        if (block instanceof RocketPodBlock small && small.isComplete(state)) {
            tier = RocketBalance.Tier.SMALL;
            material = small.getAutocannonMaterial();
        } else if (block instanceof MediumRocketPodBlock medium && medium.isComplete(state)) {
            tier = block instanceof BigRocketRailBlock || block instanceof BigRocketRailBreechBlock
                ? RocketBalance.Tier.BIG : RocketBalance.Tier.MEDIUM;
            material = medium.getAutocannonMaterial();
        } else return;
        List<Component> lines = event.getToolTip();
        if (!Screen.hasShiftDown()) {
            add(lines, "hint");
            return;
        }
        var breechBlock = switch (tier) {
            case SMALL -> BlockRegister.STEEL_ROCKET_POD_BREECH.get();
            case MEDIUM -> BlockRegister.WROUGHT_IRON_MEDIUM_ROCKET_RAIL_BREECH.get();
            case BIG -> CbcatFixMunitions.BIG_ROCKET_RAIL_BREECH.get();
        };
        BlockEntity breech = ((EntityBlock) breechBlock).newBlockEntity(BlockPos.ZERO, breechBlock.defaultBlockState());
        if (!(breech instanceof MountedRocketStorage storage)) return;
        int nativeMaxRate;
        if (breech instanceof RocketPodBreechBlockEntity small) {
            small.setFireRate(15);
            nativeMaxRate = small.getActualFireRate();
        } else if (breech instanceof MediumRocketPodBreechBlockEntity medium) {
            medium.setFireRate(15);
            nativeMaxRate = medium.getActualFireRate();
        } else return;
        add(lines, "capacity", storage.cbcatfix$slotCount());
        add(lines, "length", Math.max(2, (int) Math.ceil(RocketGeometry.bodyLength(tier))));
        var properties = material.properties();
        float reduction = properties.spreadReductionPerBarrel() * RocketBalance.launcherSpreadFactor(tier);
        if (reduction > 0) add(lines, "accuracy", (int) Math.ceil(properties.baseSpread() / reduction));
        add(lines, "rate", number(Math.min(1200, nativeMaxRate * RocketBalance.launcherRate(tier, 1))));
        add(lines, "load", number(properties.weight() * 2));
        add(lines, tier == RocketBalance.Tier.BIG ? "single" : "sides");
        if (tier == RocketBalance.Tier.BIG) add(lines, "above");
        add(lines, "loading");
    }

    private static String number(double value) {
        return String.format(Locale.ROOT, "%.2f", value).replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    private static void add(List<Component> lines, String key, Object... args) {
        lines.add(Component.translatable("tooltip.cbcatfix.launcher." + key, args).withStyle(ChatFormatting.GRAY));
    }
}
