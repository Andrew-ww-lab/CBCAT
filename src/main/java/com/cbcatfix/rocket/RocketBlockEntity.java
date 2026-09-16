package com.cbcatfix.rocket;

import com.cbcatfix.munitions.CbcatFixMunitions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.state.BlockState;
import rbasamoyai.createbigcannons.index.CBCDataComponents;
import rbasamoyai.createbigcannons.munitions.big_cannon.FuzedBlockEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * CBC's normal fuzed projectile block entity with only the extra item state that
 * a generic rocket block needs. Minecraft still owns placement, selection and
 * breaking; this object is only the smallest storage adapter for rocket variants.
 */
public class RocketBlockEntity extends FuzedBlockEntity {
    private static final String ROCKETS_TAG = "Rockets";

    private final List<ItemStack> rockets = new ArrayList<>();
    private final List<ItemStack> rocketsView = Collections.unmodifiableList(rockets);
    private int selectedRocket = -1;
    private net.minecraft.world.phys.AABB renderBounds;
    private boolean splitFailed;

    public RocketBlockEntity(BlockPos pos, BlockState state) {
        super(CbcatFixMunitions.ROCKET_BLOCK_ENTITY.get(), pos, state);
    }

    public List<ItemStack> getRockets() {
        return rocketsView;
    }

    public ItemStack getPrimaryRocket() {
        return this.rockets.isEmpty() ? ItemStack.EMPTY : this.rockets.getFirst();
    }

    public net.minecraft.world.phys.AABB getRenderBoundingBox() {
        if (renderBounds != null) return renderBounds;
        var state = getBlockState();
        var tier = RocketBlock.tier(state);
        var facing = RocketGeometry.horizontalFacing(state.getValue(RocketBlock.FACING));
        return renderBounds = RocketGeometry.shapes(tier, facing, Math.max(1, rockets.size()), isIndependent()).bounds()
            .inflate(1.0 / 16.0).move(getBlockPos());
    }

    private void invalidateRenderBoundingBox() { renderBounds = null; }

    @Override public void setBlockState(BlockState state) {
        super.setBlockState(state);
        invalidateRenderBoundingBox();
    }

    public ItemStack getSelectedRocket() {
        int index = this.selectedIndex();
        return index < 0 ? ItemStack.EMPTY : this.rockets.get(index);
    }

    public void selectRocket(int slot) {
        this.selectedRocket = this.rockets.isEmpty() ? -1 : Math.clamp(slot, 0, this.rockets.size() - 1);
        this.syncInheritedInventory();
    }

    public void clearSelection() {
        this.selectedRocket = -1;
        this.syncInheritedInventory();
    }

    public boolean addRocket(ItemStack stack) {
        if (!RocketGroundPlacement.isRocket(stack)
            || (isIndependent() && !rockets.isEmpty())
            || this.rockets.size() >= RocketBlock.capacity(RocketBalance.tierForRocketItem(stack.getItem()))) {
            return false;
        }
        if (!this.rockets.isEmpty()
            && RocketBalance.tierForRocketItem(this.rockets.getFirst().getItem())
                != RocketBalance.tierForRocketItem(stack.getItem())) {
            return false;
        }

        ItemStack stored = stack.copyWithCount(1);
        this.rockets.add(stored);
        if (this.rockets.size() == 1) {
            super.setFuze(fuzeFrom(stored));
            super.setTracer(tracerFrom(stored));
        }
        this.setChangedAndSync();
        return true;
    }

    public ItemStack removeRocket(int slot) {
        if (slot < 0 || slot >= this.rockets.size()) {
            return ItemStack.EMPTY;
        }
        ItemStack removed = this.rockets.remove(slot);
        this.selectedRocket = -1;
        this.syncInheritedInventory();
        this.setChangedAndSync();
        return removed;
    }

    public List<ItemStack> copyRockets() {
        return this.rockets.stream().map(stack -> stack.copyWithCount(1)).toList();
    }

    public boolean isIndependent() {
        return getBlockState().getValue(RocketBlock.INDEPENDENT);
    }

    boolean splitFailed() { return splitFailed; }
    void stopSplittingAfterFailure() { splitFailed = true; setChanged(); }

    void replaceRockets(List<ItemStack> items, boolean independent) {
        rockets.clear();
        for (ItemStack item : items) if (!item.isEmpty()) rockets.add(item.copyWithCount(1));
        setBlockState(getBlockState().setValue(RocketBlock.INDEPENDENT, independent));
        selectedRocket = -1;
        syncInheritedInventory();
        setChangedAndSync();
    }

    public void replaceWithSingleRocket(ItemStack rocket, boolean independent) {
        this.rockets.clear();
        if (!rocket.isEmpty()) {
            this.rockets.add(rocket.copyWithCount(1));
        }
        setBlockState(getBlockState().setValue(RocketBlock.INDEPENDENT, independent));
        this.selectedRocket = -1;
        this.syncInheritedInventory();
        this.setChangedAndSync();
    }

    @Override
    public ItemStack getFuze() {
        ItemStack rocket = this.getSelectedRocket();
        return rocket.isEmpty() ? super.getFuze() : fuzeFrom(rocket);
    }

    @Override
    public void setFuze(ItemStack fuze) {
        super.setFuze(fuze);
        ItemStack selected = this.getSelectedRocket();
        if (!selected.isEmpty()) {
            selected.set(
                CBCDataComponents.FUZE,
                fuze.isEmpty()
                    ? ItemContainerContents.EMPTY
                    : ItemContainerContents.fromItems(List.of(fuze.copyWithCount(1)))
            );
        }
        this.setChanged();
    }

    @Override
    public ItemStack getTracer() {
        ItemStack rocket = this.getSelectedRocket();
        return rocket.isEmpty() ? super.getTracer() : tracerFrom(rocket);
    }

    @Override
    public void setTracer(ItemStack tracer) {
        super.setTracer(tracer);
        ItemStack selected = this.getSelectedRocket();
        if (!selected.isEmpty()) {
            selected.set(
                CBCDataComponents.TRACER,
                tracer.isEmpty()
                    ? ItemContainerContents.EMPTY
                    : ItemContainerContents.fromItems(List.of(tracer.copyWithCount(1)))
            );
        }
        this.setChanged();
    }

    @Override
    public void tick() {
        if (this.normalizeLegacyVerticalState()) {
            return;
        }
        if (RocketSableCompat.tryDetachUnsupported(this)) {
            return;
        }
        if (this.rockets.isEmpty()) {
            super.tick();
            return;
        }
        for (int slot = 0; slot < this.rockets.size() && !this.isRemoved(); slot++) {
            this.selectRocket(slot);
            super.tick();
        }
        if (!this.isRemoved()) {
            this.clearSelection();
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        this.saveRockets(tag, registries);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        this.invalidateRenderBoundingBox();
        this.rockets.clear();
        this.selectedRocket = -1;
        super.loadAdditional(tag, registries);
        ListTag list = tag.getList(ROCKETS_TAG, CompoundTag.TAG_COMPOUND);
        for (int index = 0; index < list.size(); index++) {
            ItemStack stack = ItemStack.parseOptional(registries, list.getCompound(index));
            if (!stack.isEmpty()) {
                this.rockets.add(stack.copyWithCount(1));
            }
        }
        this.splitFailed = tag.getBoolean("RocketSplitFailed");
        this.syncInheritedInventory();
    }

    @Override
    public void writeSafe(CompoundTag tag, HolderLookup.Provider registries) {
        super.writeSafe(tag, registries);
        this.saveRockets(tag, registries);
    }

    private void saveRockets(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (ItemStack rocket : this.rockets) {
            list.add(rocket.saveOptional(registries));
        }
        tag.put(ROCKETS_TAG, list);
        if (splitFailed) tag.putBoolean("RocketSplitFailed", true);
    }

    private void setChangedAndSync() {
        this.invalidateRenderBoundingBox();
        this.setChanged();
        if (this.getLevel() != null) {
            this.notifyUpdate();
        }
    }

    private boolean normalizeLegacyVerticalState() {
        if (this.getLevel() == null || this.getLevel().isClientSide()) {
            return false;
        }
        BlockState state = this.getBlockState();
        Direction oldFacing = state.getValue(RocketBlock.FACING);
        if (oldFacing.getAxis().isHorizontal()) {
            return false;
        }

        RocketBalance.Tier tier = RocketBlock.tier(state);
        for (Direction candidate : Direction.Plane.HORIZONTAL) {
            if (!RocketFootprint.canPlace(this.getLevel(), this.getBlockPos(), candidate, tier)) {
                continue;
            }
            RocketFootprint.remove(this.getLevel(), this.getBlockPos(), oldFacing, tier);
            this.getLevel().setBlock(
                this.getBlockPos(), state.setValue(RocketBlock.FACING, candidate), 11
            );
            RocketFootprint.place(this.getLevel(), this.getBlockPos(), candidate, tier);
            this.notifyUpdate();
            return true;
        }
        return false;
    }

    private int selectedIndex() {
        if (this.rockets.isEmpty()) {
            return -1;
        }
        return this.selectedRocket >= 0 && this.selectedRocket < this.rockets.size() ? this.selectedRocket : 0;
    }

    private void syncInheritedInventory() {
        ItemStack selected = this.getSelectedRocket();
        super.setFuze(selected.isEmpty() ? ItemStack.EMPTY : fuzeFrom(selected));
        super.setTracer(selected.isEmpty() ? ItemStack.EMPTY : tracerFrom(selected));
    }

    private static ItemStack fuzeFrom(ItemStack rocket) {
        return rocket.getOrDefault(CBCDataComponents.FUZE, ItemContainerContents.EMPTY).copyOne();
    }

    private static ItemStack tracerFrom(ItemStack rocket) {
        return rocket.getOrDefault(CBCDataComponents.TRACER, ItemContainerContents.EMPTY).copyOne();
    }
}
