package com.cbcatfix.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import com.cbcatfix.CbcatFix;
import com.cbcatfix.munitions.CbcatFixMunitions;
import com.dsvv.cbcat.registry.BlockEntityRegister;

@Mixin(BlockEntityType.class)
public class BlockEntityTypeMixin {

    @Inject(method = "isValid", at = @At("HEAD"), cancellable = true)
    private void onIsValid(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (state == null) {
            return;
        }

        BlockEntityType<?> type = (BlockEntityType<?>) (Object) this;
        boolean isBigRail = state.is(CbcatFixMunitions.BIG_ROCKET_RAIL.get());
        boolean isBigRailBreech = state.is(CbcatFixMunitions.BIG_ROCKET_RAIL_BREECH.get());
        if (!isBigRail && !isBigRailBreech) {
            return;
        }

        BlockEntityType<?> expectedType = isBigRail
            ? BlockEntityRegister.MEDIUM_ROCKET_POD_BARREL_BLOCK_ENTITY.get()
            : BlockEntityRegister.MEDIUM_ROCKET_POD_BREECH_BLOCK_ENTITY.get();
        if (type == expectedType) {
            cir.setReturnValue(true);
            return;
        }

        ResourceLocation id = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(type);
        CbcatFix.LOGGER.warn("Invalid block entity type {} requested for {}; expected {}.",
            id, state.getBlock(), BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(expectedType));
    }
}
