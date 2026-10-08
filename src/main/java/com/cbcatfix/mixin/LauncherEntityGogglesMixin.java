package com.cbcatfix.mixin;

import com.cbcatfix.rocket.LauncherReadiness;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import rbasamoyai.createbigcannons.base.goggles.IHaveEntityGoggleInformation;
import rbasamoyai.createbigcannons.cannon_control.contraption.PitchOrientedContraptionEntity;

import java.util.List;
import java.util.Locale;

/** CBC already renders goggles information for a looked-at contraption entity. */
@Mixin(value = PitchOrientedContraptionEntity.class, remap = false)
public abstract class LauncherEntityGogglesMixin implements IHaveEntityGoggleInformation {
    @Override public boolean addToGoggleTooltip(List<Component> tooltip, boolean sneaking) {
        var entity = (PitchOrientedContraptionEntity) (Object) this;
        if (!(entity.getContraption() instanceof LauncherReadiness ready)) return false;
        Component status;
        if (!ready.cbcatfix$hasRockets()) status = Component.translatable("tooltip.cbcatfix.launcher.empty");
        else if (ready.cbcatfix$armingTicksRemaining() > 0) {
            double seconds = Math.ceil(ready.cbcatfix$armingTicksRemaining() / 2.0) / 10.0;
            Locale locale = Locale.forLanguageTag(Minecraft.getInstance().getLanguageManager().getSelected().replace('_', '-'));
            status = Component.translatable("tooltip.cbcatfix.launcher.arming", String.format(locale, "%.1f", seconds));
        } else status = Component.translatable("tooltip.cbcatfix.launcher.armed");
        tooltip.add(Component.literal("    ").append(status).withStyle(ChatFormatting.GRAY));
        return true;
    }
}
