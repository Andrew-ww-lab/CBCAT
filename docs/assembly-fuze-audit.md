# Assembly, orientation and fuze audit — 2026-09-26

Baseline: `2d6eaf4` on `codex/radars-5-ea-compat`. The previous dependency-cycle fix was committed before this work. This audit's changes are not committed or pushed.

Dependencies inspected: the actual local Create 6.0.10, CBC 5.11.7, CBCA 0.1.4c, Minecraft 1.21.1 / NeoForge 21.1.228 classes, and the current mod. Existing extracted dependency sources under `build/reference-*` were cross-checked by executing the loaded, transformed classes. Runtime includes Sable 2.0.5, Radars 5.0-EA-2.0.2 and Kaboom 1.0.0-EA.

## A. Double-cannon assembly

### Exact failure path

`CannonMountBlockEntity.onRedstoneUpdate` → `assemble` → `getContraption` → `TwinAutocannonBlock.getCannonContraption` → `MountedTwinAutocannonContraption.assemble` → `collectCannonBlocks` → `AbstractMountedCannonContraption.invalidCannon`.

The factory creates `exception.createbigcannons.cannon_mount.invalidCannon`, whose English text is “Cannon must have exactly one opening and exactly one closed end”. The mount catches the `AssemblyException` and stores it as its assembly error.

CBCA discovers a line along the starting segment's `FACING` axis in both positive and negative directions. `isConnectedToCannon` requires matching material, reciprocal CBC `ItemCannonBehavior` connections, and matching `isVertical()` values. It rejects a chain shorter than two blocks, two breeches, or failure to resolve the actual breech. Incomplete parts, wrong breech direction and excessive length have their own checks. This is not a general graph algorithm counting the two visible bores as two independent input/output pairs.

| Structure | Native topology |
| --- | --- |
| CBC single autocannon | One linear row, one breech, one open end, one controller |
| CBCA horizontal twin | One linear row of paired-bore blocks, one shared breech and controller; two physical muzzles |
| CBCA vertical twin | The same topology; the bore arrangement within each segment is vertical |

Two adjacent independent CBC cannons are not automatically one CBCA twin. Vertical bore arrangement is also distinct from pointing the whole weapon upward.

### What was reproduced and changed

All three default CBC autocannon materials (`cast_iron`, `bronze`, `steel`) have `connectsInSurvival(false)` and are weldable. `TwinAutocannonBlockItem.place` automatically connects them in creative, but survival requires CBC's Cannon Welder. An unwelded row reproduces the exact reported `invalidCannon` error. Welding the correct row makes the native validator accept it.

**A valid, uniformly oriented, welded twin failing native assembly was not reproduced.** The original report cannot be attributed to an incorrect multi-input count from the evidence available. No validator bypass or unconditional survival auto-connection was added.

A real inconsistency was found: `TwinAutocannonBlock.canWeldSide` previously permitted welding a horizontal segment to a vertical segment, while `isConnectedToCannon` refused to traverse that connection during assembly. `TwinAutocannonConnectivityMixin` now rejects that incompatible weld at validation time. Material, weldability, reciprocal connections and local facing checks remain CBC/CBCA-owned. Existing mixed layouts must be rebuilt with consistent parts; their stale connection flags are not silently rewritten.

Validation executed native welding and assembly in Create's in-memory `SchematicLevel` fixtures, without placing blocks in a saved world:

- Three materials × two bore arrangements × six facing directions.
- Two-block breech/barrel assemblies collected from either anchor.
- Three-block breech/recoil-spring/barrel assemblies collected from all three anchors.
- Unwelded rows and two closed ends rejected with the exact `invalidCannon` translation key.
- Side branches rejected by native welding; mixed horizontal/vertical welding rejected.
- No multiple-input relaxation: only axial connections are allowed, so normal welding cannot construct a loop or branched cannon graph.

The native collector, block-entity NBT and recoil-spring collection remain intact; there is no new graph or pair cache.

## B. Orientation persistence

While assembled, the kinetic CBC mount owns `cannonYaw/cannonPitch`; `applyRotation` synchronizes them with `PitchOrientedContraptionEntity.yaw/pitch`, accounting for CBC's local pitch sign. Hand-controlled cannons can supply the entity's latest rotation back to the mount. CBC already serializes these fields as `CannonYaw/CannonPitch` and synchronizes them to clients.

The loss occurred in two places:

1. Both assembly and disassembly called `resetContraptionToOffset`, resetting mount yaw/pitch.
2. The unassembled branch of `tick` reset the mount's angles again every tick.

`CannonMountOrientationMixin` retains the existing mount angles across those calls. During disassembly, the entity still receives CBC's canonical reset so Create restores blocks to the original grid. During reassembly, the mount's saved angles are applied to the new entity, including previous/render rotation fields. Restored pitch is bounded by the newly assembled weapon's native elevation limits.

The only new persisted value is `CbcatFixAimFrame`: the mount facing, mounting direction and cannon initial direction. It is not a second angle store. A changed assembly frame discards the pending aim rather than applying stale angles to a differently rebuilt weapon. A repeated disassembly of an already removed entity does not capture its reset pose. Idle ticks retain the mount angles, and CBC's normal NBT/client packets carry them.

Normal cannons, twins, small/medium rocket launchers and large rails all use this same mount hook. Launcher lane offsets and projectile launch transforms were not replaced. Sable world pose is not saved as another angle: the stored aim remains local to the mount, and existing Sable/contraption transforms apply when rendering and launching.

Fixed mounts already retain their user-adjustable angles in the native `pitchSlot/yawSlot` behaviors and reapply them in `applyRotation`; no duplicate storage was added there. A destroyed/removed mount or a discarded cannon carriage is not the same persistent mount lifecycle; recovery of its angles after replacement is not implemented by this patch.

Forty transformed-hook cases exercised ordinary autocannons, vertical twins and all three launcher sizes, four horizontal facings and both mounting directions. Checks covered native reset, idle ticking, NBT save/load, restored yaw, signed pitch and interpolation. These checks invoke the actual woven hooks/native reset, but **do not simulate the complete gameplay block removal/restoration or moving Sable carrier cycle**. Those remain manual checks; arbitrary altered-frame rebuilds intentionally do not restore old aim.

## C. Fuze inheritance

| Ammunition / recipe | Input fuze | Previous output | New output | Should preserve? |
| --- | --- | --- | --- | --- |
| CBC autocannon cartridge assembly / deployer | Configured fuze on projectile | Native copy of projectile, including components | Unchanged | Yes |
| CBC fuze application/removal | Native main fuze | Copies projectile; removal returns fuze and unfuzed projectile | Unchanged | Application/removal semantics |
| CBCA medium/heavy autocannon cartridge assembly, weak and strong | Configured fuze on medium projectile | Creative-template projectile discarded input fuze/tracer/other projectile data | Actual projectile copied once via `HeavyAutocannonCartridgeItem.writeProjectile`; native charge choice retained | Yes — fixed |
| CBCA medium fuze application | Fuze item plus shell/cartridge | Native copies preserve other projectile components | Unchanged | Yes |
| CBCA medium fuze removal | Fuzed shell/cartridge | Left an empty FUZE component; recreated cartridge without charge metadata; could match an empty container again | Removes FUZE, preserves original cartridge metadata and charge, requires a real installed fuze | Return exactly one fuze; preserve everything else — fixed |
| CBCA medium tracer removal | Traced and possibly fuzed cartridge | Nested fuze retained, outer cartridge metadata/charge lost | Retains nested fuze and original cartridge metadata | Yes — fixed |
| CBCA caseless conversion | Fuzed shell | Copies main fuze, fluid content and cluster components | Unchanged | Yes |
| Cluster mechanical assembly | Individually fuzed medium submunitions | Collects each configured fuze in `CLUSTER_FUZES` | Unchanged and tested | Yes, one per submunition |
| Heavy HE / HEAT / flak warhead recombination | Several input shells may carry fuzes | Static result discarded all input fuzes | Armed inputs rejected; clean inputs still work | Cannot merge several main fuzes into one; no silent loss |
| Rocket assembly | Armed payload | Could copy payload fuze into payload and rocket root | Recipe does not match, direct assembly returns empty | No — clean input required |
| Installing a fuze on a finished rocket | Rocket plus configured fuze | Payload/fuel preserved, but an existing root fuze could be overwritten | Payload/fuel preserved; already armed rocket rejected | Yes; no silent replacement |

There is no replacement fuze data model. `CBCDataComponents.FUZE` and `PROJECTILE`, CBCA `CLUSTER_FUZES`, native item-stack copies and cartridge APIs are reused. The redundant rocket-assembly copy of the payload fuze into the rocket root was removed.

Some root-level CBCA recipe overrides target CBC's cartridge serializer; the active nested CBCA heavy-ammunition recipe files still use `HAMunitionAssemblyRecipe`. The fix targets that serializer implementation, so both active aliases and data-pack uses get the corrected behavior.

## D. Rocket crafting restriction

All 33 current mechanical rocket recipes consume projectile inputs:

- Small AP, HE, flak and HEI × long-range, double-payload and lightweight.
- Medium AP, HE, HEF and HEAT × the same three variants.
- Large AP, HE and HEAT × the same three variants.

`AmmunitionFuzes.isCleanPayload` checks nonempty native main-fuze containers, nested `PROJECTILE` containers and cluster submunition fuzes. Empty FUZE containers count as unfuzed. For double-payload matching, an empty container is normalized only on a copy so two otherwise identical clean inputs remain compatible. Installed fuzes are rejected before any normalization; original stacks are never changed.

The existing `RocketMechanicalCraftingMixin` already adapts Create's matching and result transfer. The restriction is applied there at both matching and assembly, rather than creating another recipe type or framework. `LegacyRocketRecipeMixin` additionally guards CBCA's old small/medium recipe classes if a data pack restores those disabled recipes. Cluster rockets are not a supported current recipe; an armed cluster payload is nevertheless rejected by the shared predicate.

Two large HE variants (long-range and double-payload) had a pre-existing mismatch: JSON required `createbigcannons:he_shell`, but their default result's payload was `cbcatfix:heavy_he_shell`, so the payload-transfer validator rejected even clean inputs. This audit initially aligned all three recipes to the heavy shell (the lightweight variant had already matched). **Superseded on 2026-09-30 by the explicitly requested normal-HE restoration:** all three recipes and variant payload defaults now use `createbigcannons:he_shell`; see [the content audit](content-visuals-launcher-audit.md). The mismatch is resolved at the intended normal-HE source, with the original lightweight crafting cost restored.

Checks verified clean inputs work, each armed payload slot fails matching, direct assembly produces no output for rejected inputs, input fuze data remains unchanged, empty-container clean inputs work, and installing a fuze preserves rocket payload and fuel. No inventory fuze duplication was found in the audited recipe paths; this is not a claim about arbitrary third-party data-pack recipes.

## E. Validation

No new saved world or GameTest world was created. The dedicated server used its existing `build/architecture-server-dev/world`; the client used existing `build/architecture-client-dev/saves/Architecture`. Assembly fixtures exist only in memory and use Create's `SchematicLevel`, not gameplay blocks.

Commands:

```text
gradlew.bat build --no-daemon --console=plain
gradlew.bat runServer -PrunDirectory=build/architecture-server-dev -I dev/assembly-checks/checks.init.gradle --no-daemon --console=plain
gradlew.bat runClient -PrunDirectory=build/architecture-client-dev -I dev/assembly-checks/checks.init.gradle --no-daemon --console=plain
gradlew.bat runClient -PrunDirectory=build/architecture-client-dev -PquickPlayWorld=Architecture -I dev/assembly-checks/checks.init.gradle --no-daemon --console=plain
```

- Gradle build: passed (standard `test` task has no tests; this is not counted as test coverage).
- Dedicated server: reached `Done`; 1,125 assertions passed against transformed classes, including 33 rocket recipes and 40 orientation-hook cases. Clean shutdown and world save completed.
- Main menu: opt-in client probe observed the title screen rendered for 120 frames with no loading overlay; normal shutdown. This is programmatic observation, not visual screenshot inspection.
- Existing normal world: client probe observed the loaded world and player active for 100 ticks; normal shutdown and save.
- Logs: `build/assembly-audit-server.log`, `build/assembly-audit-client-menu.log`, `build/assembly-audit-client-world.log`.
- Earlier audit iterations caught a fixture using the wrong Create input class, an uninitialized fixture bounding box, the empty-versus-absent fuze mismatch, and the broken large HE recipes. Only the final passing run is counted.
- Existing third-party model/loot warnings and unavailable optional integration classes appeared; no new mixin application failure occurred. This is not a clean-up of upstream assets or the earlier Iris/Sodium modpack crash.

Checks and startup probes under `dev/assembly-checks` are opt-in and excluded from the distributable JAR. Gameplay is intentionally left for manual validation:

1. Survival placement and welding of complete horizontal/vertical twins, including springs, all materials and supported mount positions; capture a concrete layout if the reported failure remains on a correctly welded homogeneous row.
2. Repeated disassemble/reassemble at nonzero yaw/pitch, including save/reload while unassembled, ceiling mounting and hand-controlled cannons.
3. Actual block restoration and aim after Sable movement/rotation, including multi-lane launchers and radar aiming.
4. Medium ammunition crafting, fuze removal, tracer removal, cluster and caseless conversions using configured timed/guided fuzes.
5. Mechanical-crafter automation for clean and armed payloads, including double-payload recipes and the changed large HE ingredient.
