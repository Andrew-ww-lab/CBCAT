# CRITICAL DUPLICATION FIX — 2026-09-10

## Scope and evidence

This change fixes launcher inventory ownership during native Sable moves. It does
not implement bomb-like falling, build a demonstration world, or change projectile physics.
Inspected the installed CBCAT 0.1.4c, Sable 2.0.3, Simulated 1.3.0 and Create 6.0.10 code.

1. **Physicalization root cause:** `SubLevelAssemblyHelper.moveBlocks` saves source
   BE NBT, calls `Clearable.tryClear`, loads the destination, then removes the source
   block. CBCAT rocket BEs did not implement Clearable. Both `RocketPodBlock` and
   `MediumRocketPodBlock.onRemoveCannon` unconditionally drop `getDrops()` on replacement.
   Thus the destination owned the saved rockets while source removal spawned copies.
2. **Dephysicalization root cause:** `SimAssemblyHelper.disassembleSubLevel` uses
   exactly the same Sable `moveBlocks` path. The same copy-plus-source-drop defect
   applies in reverse; it is not a second inventory format or a radar issue.
3. **Previous ownership:** native per-breech Input/Output buffers plus rail
   `ItemCannonBehavior` were serialized correctly, but remained nonempty when
   source removal treated transport as destruction.
4. **New ownership:** source stays authoritative and locked during native capture
   and destination load. Successful inventory verification consumes its native
   buffers before source removal. The destination then owns the inventory.
   NBT is a transport snapshot, not a second live inventory. There is no new UUID
   registry, RocketInstance list, or persistent inventory mirror.
5. **Main/left/right:** each actual breech is transferred separately by position.
   `LauncherAssembly` stores lanes as distinct entries in native blocks/presentBEs;
   it does not merge their contents. Round-robin state is only lane index/fraction.
6. **Duplicate paths removed:** successful native source removal can no longer
   drop the copied inventory. Duplicate gathered block coordinates are collapsed
   before movement; item stacks are never deduplicated. There was no extra custom
   Sable launcher restore-insert pass to remove. Existing native NBT replacement
   is retained rather than followed by a manual inventory insertion pass.
7. **Repeated restoration:** a consumed snapshot cannot be received again inside
   a move. Repeated source coordinates are processed once; now-air sources are
   not replayed. Create's existing `Contraption.disassembled` and entity `isAlive`
   guards protect its separate restoration path. Simulated recollects non-air
   plot blocks on restoration rather than keeping an inventory backup.
8. **Mutation guards:** a thread-local scope locks only source/destination launcher
   positions for the synchronous native move, including reentrant callbacks.
   Player component edits, item-handler insertion/extraction, native extraction
   and launcher firing reject transfer-time mutations. Firing also rejects
   Create's native disassembled state. The scope is always released in finally;
   Sable physicalized but CBC-unassembled launchers remain loadable afterward.
9. **Save/load/chunks:** no new persistent ownership flags or replay queues are
   introduced. Native Input/Output/ContainedStack serialization is retained;
   cleared sources are marked changed and then removed. Destination reads replace
   native buffers, not append. Existing identical rounds and legacy overflow are
   preserved. We do not guess which items in an already-duplicated old save are
   legitimate, and therefore do not automatically delete them.
10. **Duplication and loss:** the identified successful-move duplicate drop is
    eliminated at its lifecycle boundary. Source clearing is deferred until the
    destination's item counts/components match; both input, output and rail items
    are included. A failed load clears partial destination contents, retaining
    source contents for native removal drops. Those failure drops are redirected
    to the destination, not left in a retiring Sable plot. This is a code-path
    guarantee for the inspected native flow, **not a claim of completed gameplay
    testing or crash-atomic disk storage**. Forced process termination, externally
    cancelled item spawning, and arbitrary third-party failures were not tested.
11. **Files changed for this fix:**
    - `src/main/java/com/cbcatfix/CbcatFix.java`
    - `src/main/java/com/cbcatfix/rocket/LauncherTransfer.java`
    - `src/main/java/com/cbcatfix/rocket/LauncherAccess.java`
    - `src/main/java/com/cbcatfix/rocket/MountedRocketInteraction.java`
    - `src/main/java/com/cbcatfix/mixin/RocketLauncherClearableMixin.java`
    - `src/main/java/com/cbcatfix/mixin/SmallRocketBreechSlotsMixin.java`
    - `src/main/java/com/cbcatfix/mixin/MediumRocketPodBreechBlockEntityMixin.java`
    - `src/main/java/com/cbcatfix/mixin/SableLauncherTransferMixin.java`
    - `src/main/java/com/cbcatfix/mixin/LauncherTransferDropsMixin.java`
    - `src/main/java/com/cbcatfix/mixin/RocketMountLaunchMixin.java`
    - `src/main/resources/cbcatfix.mixins.json`

## Technical validation

- Gradle: `.\gradlew.bat build --no-daemon`, then final
  `.\gradlew.bat build runClient --no-daemon` with the existing user Gradle cache.
- Build/compile succeeded. The project currently has no Java test source set contents;
  Gradle's `test NO-SOURCE` is not a unit-test pass.
- Corrected a remappable multi-target Shadow declaration found during the first launch.
- Final startup log at 17:48:45 confirms SableLauncherTransferMixin and
  LauncherTransferDropsMixin applied, followed by the transfer-hooks-loaded message.
- Create Mechanical Arm/Sable integration, launcher shape and assembly mixins loaded.
- Main menu reached. Existing ordinary `New World` (`New World (2)`) entered;
  final log records player joined at 17:49:47. No immediate world-load crash.
- World saved and exited normally at 17:51:27; client closed through Quit Game at
  17:51:39. Final combined Gradle invocation exited 0: BUILD SUCCESSFUL in 3m 22s.
- No gameplay commands, prepared structures, launcher demonstrations, or special worlds.
- Nonfatal resource errors remain for Create Radar models/blockstates, and CBCAT
  reports missing textures. Resource loading is not completely clean; these are
  outside this ownership fix.

## Manual gameplay checks remaining

Count rockets before/after repeated physicalization and dephysicalization for small,
medium and large launchers; repeat for main/left/right with different slot contents,
identical rockets, and distinct fuze data. Check no extra item entities appear.
Fire/remove some rounds before restoring; confirm they do not return. Save/reopen
while physicalized, unload/reload chunks, then restore. Check Mechanical Arm and
empty-hand insertion/removal work on CBC-unassembled Sable launchers and remain
blocked on CBC-assembled launchers. Include old saves with legacy overflow slots.

Bomb-like falling and the final optimization pass remain untouched, pending the
user's gameplay confirmation of this critical fix.
