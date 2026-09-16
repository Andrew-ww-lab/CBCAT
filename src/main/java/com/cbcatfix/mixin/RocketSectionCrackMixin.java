package com.cbcatfix.mixin;

import com.cbcatfix.rocket.RocketTargetResolver;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LevelRenderer.class)
public class RocketSectionCrackMixin {
    @ModifyVariable(method = "destroyBlockProgress", at = @At("HEAD"), argsOnly = true)
    private BlockPos cbcatfix$cracksOnWholeRocket(BlockPos pos) {
        var level = Minecraft.getInstance().level;
        return level == null ? pos : RocketTargetResolver.findRoot(level, pos).map(RocketTargetResolver.Root::pos).orElse(pos);
    }
}
