package com.cbcatfix.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rbasamoyai.createbigcannons.cannon_control.cannon_mount.CannonMountBlockEntity;
import rbasamoyai.createbigcannons.cannon_control.contraption.PitchOrientedContraptionEntity;

/** CBC's existing CannonYaw/CannonPitch remain the only saved angles. */
@Mixin(value = CannonMountBlockEntity.class, remap = false)
public abstract class CannonMountOrientationMixin {
    @Shadow protected float cannonYaw;
    @Shadow protected float cannonPitch;
    @Shadow private float prevYaw;
    @Shadow private float prevPitch;
    @Shadow protected PitchOrientedContraptionEntity mountedContraption;
    @Shadow private boolean running;
    @Shadow protected abstract void applyRotation();
    @Unique private String cbcatfix$aimFrame = "";

    @Unique
    private String cbcatfix$currentFrame() {
        var state = ((CannonMountBlockEntity) (Object) this).getBlockState();
        return state.getValue(BlockStateProperties.HORIZONTAL_FACING).getSerializedName() + "/"
            + state.getValue(BlockStateProperties.VERTICAL_DIRECTION).getSerializedName() + "/"
            + mountedContraption.getInitialOrientation().getSerializedName();
    }

    @WrapOperation(method = "disassemble", at = @At(value = "INVOKE",
        target = "Lrbasamoyai/createbigcannons/cannon_control/cannon_mount/CannonMountBlockEntity;resetContraptionToOffset()V"))
    private void cbcatfix$keepMountAim(CannonMountBlockEntity mount, Operation<Void> original) {
        // A hand-controlled cannon may own the latest rotation until this synchronization.
        if (mountedContraption.isAlive()) {
            applyRotation();
            cbcatfix$aimFrame = cbcatfix$currentFrame();
        }
        float yaw = cannonYaw;
        float pitch = cannonPitch;
        original.call(mount); // Restore world blocks on CBC's original grid, never at rounded aim angles.
        cannonYaw = prevYaw = yaw;
        cannonPitch = prevPitch = pitch;
        mount.setChanged();
    }

    @WrapOperation(method = "assemble", at = @At(value = "INVOKE",
        target = "Lrbasamoyai/createbigcannons/cannon_control/cannon_mount/CannonMountBlockEntity;resetContraptionToOffset()V"))
    private void cbcatfix$restoreMountAim(CannonMountBlockEntity mount, Operation<Void> original) {
        float yaw = cannonYaw;
        float pitch = cannonPitch;
        boolean restore = cbcatfix$aimFrame.equals(cbcatfix$currentFrame())
            && Float.isFinite(yaw) && Float.isFinite(pitch);
        original.call(mount);
        if (restore) {
            cannonYaw = prevYaw = yaw;
            cannonPitch = prevPitch = Mth.clamp(pitch, -mountedContraption.maximumDepression(), mountedContraption.maximumElevation());
            var direction = mountedContraption.getInitialOrientation();
            boolean positivePitch = (direction.getAxisDirection() == net.minecraft.core.Direction.AxisDirection.POSITIVE)
                == (direction.getAxis() == net.minecraft.core.Direction.Axis.X);
            mountedContraption.pitch = cannonPitch * (positivePitch ? 1 : -1);
            mountedContraption.yaw = cannonYaw;
            mountedContraption.prevPitch = mountedContraption.pitch;
            mountedContraption.prevYaw = mountedContraption.yaw;
            mountedContraption.setXRot(mountedContraption.pitch);
            mountedContraption.setYRot(mountedContraption.yaw);
            mountedContraption.xRotO = mountedContraption.getXRot();
            mountedContraption.yRotO = mountedContraption.getYRot();
        }
        cbcatfix$aimFrame = "";
        mount.setChanged();
    }

    @WrapOperation(method = "tick", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD,
        target = "Lrbasamoyai/createbigcannons/cannon_control/cannon_mount/CannonMountBlockEntity;cannonYaw:F"))
    private void cbcatfix$keepIdleYaw(CannonMountBlockEntity mount, float value, Operation<Void> original) {
        if (running || cbcatfix$aimFrame.isEmpty()) original.call(mount, value);
    }

    @WrapOperation(method = "tick", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD,
        target = "Lrbasamoyai/createbigcannons/cannon_control/cannon_mount/CannonMountBlockEntity;cannonPitch:F"))
    private void cbcatfix$keepIdlePitch(CannonMountBlockEntity mount, float value, Operation<Void> original) {
        if (running || cbcatfix$aimFrame.isEmpty()) original.call(mount, value);
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void cbcatfix$alignIdleInterpolation(CallbackInfo ci) {
        if (!running && !cbcatfix$aimFrame.isEmpty()) {
            prevPitch = cannonPitch;
            prevYaw = cannonYaw;
        }
    }

    @Inject(method = "write", at = @At("RETURN"))
    private void cbcatfix$writeFrame(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket, CallbackInfo ci) {
        if (!cbcatfix$aimFrame.isEmpty()) tag.putString("CbcatFixAimFrame", cbcatfix$aimFrame);
    }

    @Inject(method = "read", at = @At("RETURN"))
    private void cbcatfix$readFrame(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket, CallbackInfo ci) {
        cbcatfix$aimFrame = tag.getString("CbcatFixAimFrame");
        if (!running && !cbcatfix$aimFrame.isEmpty()) {
            prevPitch = cannonPitch;
            prevYaw = cannonYaw;
        }
    }
}
