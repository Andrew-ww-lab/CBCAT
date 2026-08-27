package com.cbcatfix.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(Player.class)
public abstract class PlayerMiningMixin {

    @Unique
    private static final float CBCATFIX$SILK_TOUCH_MINING_SPEED_MULTIPLIER = 2.0f;

    @Unique
    private static final TagKey<Block> CBCATFIX$SILK_TOUCH_MINEABLE = TagKey.create(
        Registries.BLOCK,
        ResourceLocation.fromNamespaceAndPath("cbcatfix", "silk_touch_mineable")
    );

    @Inject(method = "getDestroySpeed", at = @At("RETURN"), cancellable = true)
    private void cbcatfix$speedUpSilkTouchMining(
        BlockState state,
        CallbackInfoReturnable<Float> cir
    ) {
        if (!state.is(CBCATFIX$SILK_TOUCH_MINEABLE)) {
            return;
        }

        Player player = (Player) (Object) this;
        if (cbcatfix$hasSilkTouch(player.getMainHandItem())) {
            cir.setReturnValue(cir.getReturnValue() * CBCATFIX$SILK_TOUCH_MINING_SPEED_MULTIPLIER);
        }
    }

    @Unique
    private static boolean cbcatfix$hasSilkTouch(ItemStack stack) {
        ItemEnchantments enchantments = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        return enchantments.entrySet().stream().anyMatch(entry ->
            entry.getIntValue() > 0 && entry.getKey().is(Enchantments.SILK_TOUCH)
        );
    }
}
