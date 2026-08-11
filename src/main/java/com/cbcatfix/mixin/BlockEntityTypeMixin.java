package com.cbcatfix.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import com.cbcatfix.munitions.CbcatFixMunitions;

@Mixin(BlockEntityType.class)
public class BlockEntityTypeMixin {

    @Inject(method = "isValid", at = @At("HEAD"), cancellable = true)
    private void onIsValid(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        BlockEntityType<?> type = (BlockEntityType<?>) (Object) this;
        ResourceLocation id = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(type);
        if (id != null && "cbc_at".equals(id.getNamespace())) {
            if ("medium_rocket_rail_be".equals(id.getPath())) {
                if (state.getBlock() == CbcatFixMunitions.BIG_ROCKET_RAIL.get()) {
                    cir.setReturnValue(true);
                }
            } else if ("medium_rocket_rail_breech_be".equals(id.getPath())) {
                if (state.getBlock() == CbcatFixMunitions.BIG_ROCKET_RAIL_BREECH.get()) {
                    cir.setReturnValue(true);
                }
            }
        }
    }
}
