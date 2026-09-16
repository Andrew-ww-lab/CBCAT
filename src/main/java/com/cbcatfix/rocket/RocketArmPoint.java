package com.cbcatfix.rocket;

import com.cbcatfix.CbcatFix;
import com.simibubi.create.api.registry.CreateRegistries;
import com.simibubi.create.content.kinetics.mechanicalArm.AllArmInteractionPointTypes.DepositOnlyArmInteractionPoint;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/** Create owns arm selection, animation, simulation, capability caching and transfer. */
public final class RocketArmPoint extends DepositOnlyArmInteractionPoint {
    private RocketArmPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
        super(type, level, pos, state);
    }

    public static void register(net.neoforged.bus.api.IEventBus bus) {
        var types = net.neoforged.neoforge.registries.DeferredRegister.create(
            CreateRegistries.ARM_INTERACTION_POINT_TYPE, CbcatFix.MOD_ID);
        types.register("rocket_launcher", () -> new ArmInteractionPointType() {
                @Override public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
                    return level.getBlockEntity(pos) instanceof MountedRocketStorage;
                }
                @Override public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
                    return new RocketArmPoint(this, level, pos, state);
                }
            });
        types.register(bus);
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK,
            com.dsvv.cbcat.registry.BlockEntityRegister.ROCKET_POD_BREECH_BLOCK_ENTITY.get(),
            (breech, side) -> breech.createItemHandler());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK,
            com.dsvv.cbcat.registry.BlockEntityRegister.MEDIUM_ROCKET_POD_BREECH_BLOCK_ENTITY.get(),
            (breech, side) -> breech.createItemHandler());
    }

    @Override protected Direction getInteractionDirection() {
        return cachedState.getValue(BlockStateProperties.FACING).getOpposite();
    }
    @Override protected Vec3 getInteractionPositionVector() {
        return Vec3.atCenterOf(pos).add(Vec3.atLowerCornerOf(getInteractionDirection().getNormal()).scale(0.5));
    }
    @Override public ItemStack insert(ArmBlockEntity arm, ItemStack stack, boolean simulate) {
        var breech = level.getBlockEntity(pos);
        if (!(breech instanceof MountedRocketStorage) || breech.isRemoved()) return stack;
        return super.insert(arm, stack, simulate);
    }
}
