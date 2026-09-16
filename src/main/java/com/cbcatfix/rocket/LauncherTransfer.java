package com.cbcatfix.rocket;

import com.dsvv.cbcat.cannon.medium_rocketpod.IMediumRocketPodBlockEntity;
import com.dsvv.cbcat.cannon.rocketpod.IRocketPodBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Clearable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

/** One synchronous native Sable move. No persistent inventory mirror or global lock. */
public final class LauncherTransfer implements AutoCloseable {
    private static final ThreadLocal<LauncherTransfer> CURRENT = new ThreadLocal<>();
    private final LauncherTransfer parent;
    private final List<Entry> entries = new ArrayList<>();
    private final Set<Location> locked = new HashSet<>();
    private final IdentityHashMap<CompoundTag, Entry> snapshots = new IdentityHashMap<>();

    private LauncherTransfer() {
        parent = CURRENT.get();
        CURRENT.set(this);
    }

    public static LauncherTransfer begin() { return new LauncherTransfer(); }

    /** Fail during mod loading, not on the first inventory move, if a required hook cannot apply. */
    public static void verifyIntegration() {
        if (!Clearable.class.isAssignableFrom(com.dsvv.cbcat.cannon.rocketpod.breech.RocketPodBreechBlockEntity.class)
            || !Clearable.class.isAssignableFrom(com.dsvv.cbcat.cannon.medium_rocketpod.breech.MediumRocketPodBreechBlockEntity.class))
            throw new IllegalStateException("CBCAT launcher inventory removal contract is missing");
        if (!net.neoforged.fml.ModList.get().isLoaded("sable")) return;
        try {
            // Optional linkage; class loading applies/verifies mixins without moving blocks or creating a world.
            Class.forName("dev.ryanhcode.sable.api.SubLevelAssemblyHelper", false, LauncherTransfer.class.getClassLoader());
            Class.forName("net.minecraft.world.Containers", false, LauncherTransfer.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Sable launcher transfer API is unavailable", e);
        }
        com.cbcatfix.CbcatFix.LOGGER.info("[CBCAT Fix] Launcher inventory transfer hooks loaded (Sable/Create).");
    }

    public static boolean isLauncher(BlockEntity be) {
        return be instanceof IRocketPodBlockEntity || be instanceof IMediumRocketPodBlockEntity;
    }

    public void include(BlockEntity source, ServerLevel destination, BlockPos target) {
        if (!isLauncher(source)) return;
        Location from = new Location(source.getLevel(), source.getBlockPos());
        Location to = new Location(destination, target.immutable());
        if (isLocked(from.level(), from.pos()) || isLocked(to.level(), to.pos()))
            throw new IllegalStateException("Reentrant launcher ownership transfer at " + from.pos());
        entries.add(new Entry(source, from, to));
        locked.add(from);
        locked.add(to);
    }

    public static boolean isLocked(BlockEntity be) {
        return be != null && isLocked(be.getLevel(), be.getBlockPos());
    }

    public static boolean isLocked(Level level, BlockPos pos) {
        for (var scope = CURRENT.get(); scope != null; scope = scope.parent)
            if (scope.locked.contains(new Location(level, pos))) return true;
        return false;
    }

    public static boolean defersClear(BlockEntity be) { return sourceEntry(be) != null; }

    private static Entry sourceEntry(BlockEntity source) {
        var scope = CURRENT.get();
        if (scope != null)
            for (var entry : scope.entries) if (entry.source == source) return entry;
        return null;
    }

    public static void snapshot(BlockEntity source, CompoundTag tag) {
        var entry = sourceEntry(source);
        if (entry != null) CURRENT.get().snapshots.put(tag, entry);
    }

    /** Native deserialization replaces the target buffers. A successful receipt consumes the source. */
    public static void load(BlockEntity target, CompoundTag tag, Runnable nativeLoad) {
        var scope = CURRENT.get();
        var entry = scope == null ? null : scope.snapshots.get(tag);
        if (entry == null) { nativeLoad.run(); return; }
        // A receipt consumes this snapshot. Replaying it must not replenish a fired/removed round.
        if (entry.committed) return;
        try {
            if (target == entry.source || !isLauncher(target))
                throw new IllegalStateException("Invalid launcher transfer destination");
            nativeLoad.run();
            if (!sameContents(contents(entry.source), contents(target)))
                throw new IllegalStateException("Launcher inventory did not survive native NBT restoration");
        } catch (RuntimeException failure) {
            // Never keep a partial destination AND let native removal drop the full source.
            if (target != entry.source && target instanceof Clearable inventory) inventory.clearContent();
            throw failure;
        }
        ((Clearable) entry.source).clearContent();
        entry.source.setChanged();
        entry.committed = true;
    }

    private static List<ItemStack> contents(BlockEntity be) {
        List<ItemStack> nativeDrops = be instanceof IRocketPodBlockEntity small ? small.getDrops()
            : ((IMediumRocketPodBlockEntity) be).getDrops();
        return nativeDrops.stream().filter(s -> !s.isEmpty()).map(ItemStack::copy).toList();
    }

    private static boolean sameContents(List<ItemStack> source, List<ItemStack> target) {
        // Compare occurrences, not unique item IDs: two identical rounds are two legitimate rounds.
        var remaining = new ArrayList<>(target);
        for (var stack : source) {
            int match = -1;
            for (int i = 0; i < remaining.size(); i++)
                if (ItemStack.matches(stack, remaining.get(i))) { match = i; break; }
            if (match < 0) return false;
            remaining.remove(match);
        }
        return remaining.isEmpty();
    }

    /** On a native per-block load failure, keep its ordinary drops OUT of the retiring plot. */
    public static Location failedDropDestination(Level level, double x, double y, double z) {
        var scope = CURRENT.get();
        if (scope == null) return null;
        BlockPos pos = BlockPos.containing(x, y, z);
        for (var entry : scope.entries)
            if (!entry.committed && entry.from.level() == level && entry.from.pos().equals(pos)) return entry.to;
        return null;
    }

    @Override public void close() {
        if (parent == null) CURRENT.remove(); else CURRENT.set(parent);
    }

    public record Location(Level level, BlockPos pos) {}

    private static final class Entry {
        private final BlockEntity source;
        private final Location from;
        private final Location to;
        private boolean committed;

        private Entry(BlockEntity source, Location from, Location to) {
            this.source = source;
            this.from = from;
            this.to = to;
        }
    }
}
