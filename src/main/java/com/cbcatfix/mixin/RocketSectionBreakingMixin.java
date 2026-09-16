package com.cbcatfix.mixin;

import com.cbcatfix.rocket.RocketTargetResolver;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayerGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public class RocketSectionBreakingMixin {
    @Shadow protected ServerLevel level;
    @Inject(method = "destroyBlock", at = @At("HEAD"), cancellable = true)
    private void cbcatfix$breakCanonicalRocket(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        var root = RocketTargetResolver.findRoot(level, pos).orElse(null);
        if (root != null && !root.pos().equals(pos)) {
            // Redirect only the completed break, BEFORE events/tool wear/drop handling.
            // The root then follows Minecraft/CBC's ordinary path exactly once.
            cir.setReturnValue(((ServerPlayerGameMode) (Object) this).destroyBlock(root.pos()));
        }
    }
}
