package com.cbcatfix.rocket;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;
import java.util.Optional;

/** Shared local geometry for placed, rendered and physical rockets. */
public final class RocketGeometry {
    private static final double STORAGE_WIDTH = 0.94;

    private RocketGeometry() {
    }

    public static double bodyWidth(RocketBalance.Tier tier) {
        return (tier == RocketBalance.Tier.SMALL ? 2.0 : 4.0) / 16.0;
    }

    public static double bodyLength(RocketBalance.Tier tier) {
        return switch (tier) {
            case SMALL -> 1.0;
            case MEDIUM -> 2.0;
            case BIG -> 3.0;
        };
    }

    public static SlotOffset slotOffset(RocketBalance.Tier tier, int slot) {
        if (tier == RocketBalance.Tier.BIG) {
            // The main body rests on the supporting block.
            return new SlotOffset(0.0, bodyWidth(tier) * 0.5 - 0.5);
        }
        if (tier == RocketBalance.Tier.MEDIUM) {
            double diameter = bodyWidth(tier);
            double pitch = diameter + 0.5 / 16.0;
            return new SlotOffset((slot % 2 - 0.5) * pitch,
                diameter * 0.5 + pitch * (slot / 2) - 0.5);
        }
        int lowerCount = 5;
        int layer = 0;
        int indexInLayer = Math.max(0, slot);
        int countInLayer = lowerCount;
        while (countInLayer > 0 && indexInLayer >= countInLayer) {
            indexInLayer -= countInLayer;
            layer++;
            countInLayer--;
        }

        double diameter = bodyWidth(tier);
        double spacing = lowerCount <= 1 ? 0.0 : (STORAGE_WIDTH - diameter) / (lowerCount - 1);
        double firstCenter = -(STORAGE_WIDTH - diameter) * 0.5;
        double right = firstCenter + indexInLayer * spacing + layer * spacing * 0.5;
        double naturalLayerStep = Math.sqrt(3.0) * diameter * 0.5;
        double layerStep = Math.min(naturalLayerStep, (1.0 - diameter) / 2.0);
        double up = diameter * 0.5 + layer * layerStep - 0.5;
        return new SlotOffset(right, up);
    }

    public static Basis basis(Direction facing) {
        facing = horizontalFacing(facing);
        Vec3 forward = Vec3.atLowerCornerOf(facing.getNormal());
        return new Basis(new Vec3(-forward.z, 0.0, forward.x), new Vec3(0.0, 1.0, 0.0), forward);
    }

    /** Defensive normalization for legacy worlds that still contain vertical rocket states. */
    public static Direction horizontalFacing(Direction facing) {
        return facing.getAxis().isHorizontal() ? facing : Direction.NORTH;
    }

    /** Item-model pivot correction; physical geometry remains centred at {@link #bodyCenter}. */
    public static double modelPivotOffset(RocketBalance.Tier tier) {
        return tier == RocketBalance.Tier.BIG ? -5.0 / 16.0 : 0.0;
    }

    /** Source item meshes use 16 px length, except the 26 px large mesh. */
    public static float modelLengthScale(RocketBalance.Tier tier) {
        return (float) (bodyLength(tier) * 16.0 / (tier == RocketBalance.Tier.BIG ? 26.0 : 16.0));
    }

    public static Vec3 slotCrossOffset(RocketBalance.Tier tier, Direction facing, int slot) {
        SlotOffset offset = slotOffset(tier, slot);
        Basis basis = basis(facing);
        return basis.right().scale(offset.right()).add(basis.up().scale(offset.up()));
    }

    public static Vec3 bodyCenter(RocketBalance.Tier tier, Direction facing, int slot, boolean centered) {
        Basis basis = basis(facing);
        return new Vec3(0.5, 0.5, 0.5)
            .add(centered ? Vec3.ZERO : slotCrossOffset(tier, facing, slot))
            .add(basis.forward().scale((bodyLength(tier) - 1.0) * 0.5));
    }

    public static Vec3 nosePoint(RocketBalance.Tier tier, Direction facing, int slot, boolean independent) {
        return bodyCenter(tier, facing, slot, independent)
            .add(basis(facing).forward().scale(bodyLength(tier) * 0.5));
    }

    public static AABB bodyBox(RocketBalance.Tier tier, Direction facing, int slot) {
        return bodyBox(tier, facing, slot, false);
    }

    public static AABB bodyBox(RocketBalance.Tier tier, Direction facing, int slot, boolean centered) {
        Vec3 center = bodyCenter(tier, facing, slot, centered);
        Basis basis = basis(facing);
        double halfLength = bodyLength(tier) * 0.5;
        double halfWidth = bodyWidth(tier) * 0.5;
        double extentX = Math.abs(basis.forward().x) * halfLength
            + (Math.abs(basis.right().x) + Math.abs(basis.up().x)) * halfWidth;
        double extentY = Math.abs(basis.forward().y) * halfLength
            + (Math.abs(basis.right().y) + Math.abs(basis.up().y)) * halfWidth;
        double extentZ = Math.abs(basis.forward().z) * halfLength
            + (Math.abs(basis.right().z) + Math.abs(basis.up().z)) * halfWidth;
        return new AABB(
            center.x - extentX, center.y - extentY, center.z - extentZ,
            center.x + extentX, center.y + extentY, center.z + extentZ
        );
    }

    public static VoxelShape bodyShape(RocketBalance.Tier tier, Direction facing, int slot, boolean centered) {
        AABB box = bodyBox(tier, facing, slot, centered);
        return Shapes.box(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ);
    }

    public static VoxelShape segmentShape(RocketBalance.Tier tier, Direction facing) {
        return segmentShape(tier, facing, 0, true);
    }

    public static VoxelShape segmentShape(
        RocketBalance.Tier tier, Direction facing, int slot, boolean centered
    ) {
        Basis basis = basis(facing);
        Vec3 center = new Vec3(0.5, 0.5, 0.5)
            .add(centered ? Vec3.ZERO : slotCrossOffset(tier, facing, slot));
        double halfLength = 0.5;
        double halfWidth = bodyWidth(tier) * 0.5;
        double extentX = Math.abs(basis.forward().x) * halfLength
            + (Math.abs(basis.right().x) + Math.abs(basis.up().x)) * halfWidth;
        double extentY = Math.abs(basis.forward().y) * halfLength
            + (Math.abs(basis.right().y) + Math.abs(basis.up().y)) * halfWidth;
        double extentZ = Math.abs(basis.forward().z) * halfLength
            + (Math.abs(basis.right().z) + Math.abs(basis.up().z)) * halfWidth;
        return Shapes.box(
            center.x - extentX, center.y - extentY, center.z - extentZ,
            center.x + extentX, center.y + extentY, center.z + extentZ
        );
    }

    public static VoxelShape segmentShapes(
        RocketBalance.Tier tier, Direction facing, int count, boolean centered
    ) {
        VoxelShape result = Shapes.empty();
        for (int slot = 0; slot < count; slot++) {
            result = Shapes.or(result, segmentShape(tier, facing, slot, centered));
        }
        return result.isEmpty() ? segmentShape(tier, facing) : result;
    }

    public static VoxelShape shapes(RocketBalance.Tier tier, Direction facing, int count, boolean centered) {
        VoxelShape result = Shapes.empty();
        for (int slot = 0; slot < count; slot++) {
            result = Shapes.or(result, bodyShape(tier, facing, slot, centered));
        }
        return result.isEmpty() ? bodyShape(tier, facing, 0, centered) : result;
    }

    public static int closestSlot(
        RocketBalance.Tier tier,
        Direction facing,
        int count,
        Vec3 localHit
    ) {
        int closest = 0;
        double closestDistance = Double.POSITIVE_INFINITY;
        for (int slot = 0; slot < count; slot++) {
            AABB box = bodyBox(tier, facing, slot);
            double dx = Math.max(Math.max(box.minX - localHit.x, 0.0), localHit.x - box.maxX);
            double dy = Math.max(Math.max(box.minY - localHit.y, 0.0), localHit.y - box.maxY);
            double dz = Math.max(Math.max(box.minZ - localHit.z, 0.0), localHit.z - box.maxZ);
            double distance = dx * dx + dy * dy + dz * dz;
            if (distance < closestDistance) {
                closestDistance = distance;
                closest = slot;
            }
        }
        return closest;
    }

    public static Optional<RaycastSlot> raycastSlot(
        RocketBalance.Tier tier,
        Direction facing,
        int count,
        boolean centered,
        Vec3 localStart,
        Vec3 localEnd
    ) {
        RaycastSlot closest = null;
        double closestDistance = Double.POSITIVE_INFINITY;
        for (int slot = 0; slot < count; slot++) {
            Optional<Vec3> intersection = bodyBox(tier, facing, slot, centered).clip(localStart, localEnd);
            if (intersection.isEmpty()) {
                continue;
            }
            double distance = localStart.distanceToSqr(intersection.get());
            if (distance < closestDistance) {
                closestDistance = distance;
                closest = new RaycastSlot(slot, intersection.get());
            }
        }
        return Optional.ofNullable(closest);
    }

    public static int closestSlot(
        RocketBalance.Tier tier,
        Direction facing,
        List<?> rockets,
        Vec3 localHit
    ) {
        return closestSlot(tier, facing, Math.max(1, rockets.size()), localHit);
    }

    public record SlotOffset(double right, double up) {
    }

    public record RaycastSlot(int slot, Vec3 location) {
    }

    public record Basis(Vec3 right, Vec3 up, Vec3 forward) {
    }
}
