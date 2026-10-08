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
        append(event.getItemStack(), event.getToolTip(), Screen.hasShiftDown());
    }

    /** Same presentation path for normal hover and expanded summary. */
    public static void append(net.minecraft.world.item.ItemStack stack, List<Component> lines, boolean expanded) {
        if (!(stack.getItem() instanceof BlockItem item)) return;
        var block = item.getBlock();
        if (block instanceof rbasamoyai.createbigcannons.cannons.CannonContraptionProviderBlock cannon
            && !cannon.isComplete(block.defaultBlockState())) return;
        var module = block instanceof com.cbcatfix.rocket.LauncherModuleBlock special ? special : null;
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
        if (module != null) add(lines, module.module() == com.cbcatfix.rocket.LauncherModuleBlock.Module.RAPID ? "rapid" : "traverse");
        String[] prompt = com.simibubi.create.foundation.utility.CreateLang.translateDirect("tooltip.holdForDescription", "$").getString().split("\\$", 2);
        if (prompt.length == 2) lines.add(Component.literal(prompt[0]).withStyle(ChatFormatting.DARK_GRAY)
            .append(com.simibubi.create.foundation.utility.CreateLang.translateDirect("tooltip.keyShift").withStyle(expanded ? ChatFormatting.WHITE : ChatFormatting.GRAY))
            .append(Component.literal(prompt[1]).withStyle(ChatFormatting.DARK_GRAY)));
        if (!expanded) return;
        lines.add(Component.translatable("tooltip.cbcatfix.launcher.properties").withStyle(ChatFormatting.GRAY));
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
        var properties = material.properties();
        boolean steelSmall = tier == RocketBalance.Tier.SMALL && com.cbcatfix.rocket.SmallLauncherStructure.isSteel(state);
        int minimum = steelSmall ? com.cbcatfix.balance.BalanceDefaults.SMALL_LAUNCHER_MIN_LENGTH
            : Math.max(2, (int) Math.ceil(RocketGeometry.bodyLength(tier)));
        int maximum = Math.min(com.cbcatfix.rocket.LauncherAssembly.maximumLength(tier),
            rbasamoyai.createbigcannons.cannon_control.contraption.AbstractMountedCannonContraption.getMaxCannonLength());
        property(lines, "type", Component.translatable("tooltip.cbcatfix.launcher.type." + tier.name().toLowerCase(Locale.ROOT)));
        property(lines, "range", Component.translatable("tooltip.cbcatfix.launcher.blocks", minimum, maximum));
        property(lines, "capacity_label", Component.literal(Integer.toString(storage.cbcatfix$slotCount())));
        double moduleRate = module != null && module.module() == com.cbcatfix.rocket.LauncherModuleBlock.Module.RAPID
            ? com.cbcatfix.balance.BalanceDefaults.RAPID_CYCLING_RATE : 1;
        property(lines, "rate_label", Component.translatable("tooltip.cbcatfix.launcher.rpm",
            number(Math.min(1200, nativeMaxRate * RocketBalance.launcherRate(tier, 1) * moduleRate))));
        if (steelSmall) add(lines, "length_accuracy");
        else {
            float reduction = properties.spreadReductionPerBarrel() * RocketBalance.launcherSpreadFactor(tier);
            if (reduction > 0 && Math.ceil(properties.baseSpread() / reduction) < maximum)
                property(lines, "accuracy_label", Component.translatable("tooltip.cbcatfix.launcher.zero_spread_rails", (int) Math.ceil(properties.baseSpread() / reduction)));
            else add(lines, "length_accuracy");
        }
        property(lines, "weight_label", Component.translatable("block.createbigcannons.autocannon.tooltip.weightImpact.goggles", number(properties.weight() * 2)));
        property(lines, "arming_label", Component.translatable("tooltip.cbcatfix.launcher.seconds", number(com.cbcatfix.rocket.LauncherReadiness.duration(tier) / 20.0)));
        if (module != null) {
            add(lines, "module_position");
            add(lines, "one_module");
            add(lines, module.module() == com.cbcatfix.rocket.LauncherModuleBlock.Module.RAPID ? "rapid_detail" : "traverse_detail");
        } else if (steelSmall) add(lines, "module_position");
        add(lines, tier == RocketBalance.Tier.BIG ? "single" : steelSmall ? "matching_structure" : "sides");
    }

    private static void property(List<Component> lines, String key, Component value) {
        lines.add(Component.literal(" ").append(Component.translatable("tooltip.cbcatfix.launcher." + key)).withStyle(ChatFormatting.GRAY));
        var palette = rbasamoyai.createbigcannons.base.CBCTooltip.getPalette();
        lines.addAll(com.simibubi.create.foundation.item.TooltipHelper.cutStringTextComponent(value.getString(), palette.primary(), palette.highlight(), 2));
    }
    private static String number(double value) {
        return String.format(Locale.ROOT, "%.2f", value).replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    private static void add(List<Component> lines, String key, Object... args) {
        lines.add(Component.translatable("tooltip.cbcatfix.launcher." + key, args).withStyle(ChatFormatting.GRAY));
    }
}
