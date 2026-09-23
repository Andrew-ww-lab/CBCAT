# Optional Radars countermeasure compatibility — 2026-09-20

This update builds on the pre-existing uncommitted Radars 5 launcher work. It does not change steering, launch clearance, ammunition interchangeability, launcher serialization, dependency declarations, or third-party JARs.

## A. RADARS API INTEGRATION

Inspected `libs/create_radar-5.0-EA-2.0.2-1.21.1.jar`, SHA-256 `2E0220BB4A59E452345EBE81FAF6C1A0177337B38206F2A250B1BF7D5A9F27F1`. Checked its javap signatures against the existing decompiled reference under `build/radars5-reference`.

Exact upstream paths:

- `com.happysg.radar.chaff.ChaffLockRegistry.register(EntityType<T>, ChaffLockAdapter<? super T>)`: entity-type keyed registration; registration replaces an adapter for the same type. `ChaffLockAdapter.getTargetId`, `applySuppression(entity, targetId, untilTick)` and default `breakLock` (suppression until `Long.MAX_VALUE`). `ChaffManager.applyToRegisteredEntityLocks` performs the actual countermeasure rolls and calls the adapter. No second probability model.
- `com.happysg.radar.api.jamming.DirectionalJammingApi.reportedTracks(ServerLevel, EmitterContext, Collection<RadarTrack>)` applies the onboard emitter's jamming view. `resolveGuidance(RadarTrack, canonicalPosition, canonicalVelocity)` adds guidance offsets to physical observations and ignores physical observations for synthetic contacts. The service may omit real tracks and generate synthetic ones.
- `com.happysg.radar.block.arad.rwr.ExternalRwrEmitterRegistry.heartbeat(ServerLevel, EmitterState, ttlTicks)` and `remove(ServerLevel, sourceId)`. `EmitterState` includes a cone, range, threat stage, radar type and optional `SelectionMetadata`. Radars owns decay and level cleanup.
- `com.happysg.radar.block.radar.track.RadarTrack`: string ID, position/velocity, scan time, category, `isSynthetic()` and `JammingData`; a UUID is not guaranteed. `NetworkFiltererBlockEntity.activeTrackCache` remains the launch-designation source.
- `com.happysg.radar.registry.ModItems.GUIDED_FUZE` registers `create_radar:guided_fuze`; `GuidedFuzeItem` still stores `monitorPos` and extends CBC `FuzeItem`.

New shared-API calls live in `src/main/java/com/cbcatfix/compat/radars/RadarSeekerCompat.java`, reached through the existing mod-gated `RadarLauncherCompat.register`. Unlike the previous UUID-only assumption, contacts may now have no physical UUID. The installed guided fuze has no explicit active/passive selector: the mod's existing autonomous onboard radar seeker is classified as active. No passive seeker item was invented.

## B. GUIDED FUZE RESTORATION

The original `create_radar:guided_fuze` is added to the existing CBCAT creative tab by `CbcatFix.onBuildCreativeTabContents`, using `BuiltInRegistries.ITEM.getOptional` after the `create_radar` presence check. The tab's existing de-duplication preserves one original item entry. No item is registered, cloned or replaced.

Recipe ID: `cbcatfix:radars_guided_fuze`, file `src/main/resources/data/cbcatfix/recipe/radars_guided_fuze.json`:

```text
.E.
RPR
.R.
```

Result: one `create_radar:guided_fuze`. Ingredients: `E = create:electron_tube`, `P = create:precision_mechanism`, `R = minecraft:redstone`. The Create 6.0.10 `AllItems` registration bytecode (`electron_tube`, `precision_mechanism`), item assets, and Radars item registration/assets were inspected. The root `neoforge:conditions` uses `neoforge:mod_loaded` with `modid: create_radar`, so NeoForge skips decoding the missing result when Radars is absent. Base-only and Kaboom-only recipe reloads completed without a missing guided-fuze recipe error. Packaged JSON matches the source.

The existing class-name recognition in `SableGuidanceCompat.isGuidedFuze`, launch-only `monitorPos` binding and `GuidedFuzeItemMixin` suppression of duplicate controller steering remain intact.

## C. CHAFF / JAMMING / CONTACT HANDLING

Chaff adapters cover the eight CBCAT small/medium projectile types, three large rocket types and the transient block-projectile bridge. Their callbacks only expose a target when an actual active radar seeker is powered, enabled, alive and clear of its launcher. A bridge or unguided projectile has no eligible seeker and exposes no lock.

Temporary suppression stores the API's absolute expiry; it does not clear target identity or the frozen point. At expiry, ordinary onboard acquisition resumes. The API's default permanent break persists `Long.MAX_VALUE`, so that missile continues inertially toward its last-known point without reacquiring. The adapter introduces no random rolls or alternative chaff timers.

Previously `RocketSeeker.observe` called `ServerLevel.getEntity(UUID)` and `SableSeekerTarget.observe` read physical sublevel bounds directly. Those remain *raw* sensor inputs, but the active-radar branch must now pass them through both `reportedTracks` and `resolveGuidance` before steering can consume them. A missing reported contact never falls back to its true position. Non-radar mode bypasses radar EW; missing Radars keeps saved radar seekers at their last-known point.

`RocketSeeker` retains string contact identity, optional UUID, synthetic flag, active-radar flag, last-known position and suppression expiry as plain NBT. Live state distinguishes `CONFIRMED`, `DEGRADED`, `LAST_KNOWN`, `FALSE_CONTACT` and `AMBIGUOUS`. Old UUID-only saves still load as radar seekers. Synthetic contacts never resolve through a UUID even if their ID happens to parse as one. Non-UUID designations retain their supplied point and identity; this does not invent a physical target resolver for non-entity objects.

The seeker remains designated-target-only: it accepts matching reported identities, not arbitrary jammer ghosts. An initially designated false return is retained as a false point; if no matching onboard observation exists it remains frozen. Lost/out-of-cone/occluded contacts retain the last observed point, with no unseen velocity extrapolation. Existing range, cone, chunk-load and line-of-sight gates remain. Reacquisition uses onboard observations only. The external controller is consulted once at launch; no controller position feed, target switching, steering rewrite or self-deletion was added.

## D. RWR

Source IDs are `cbcatfix:seeker:<projectile UUID>`, distinct from Kaboom. The optional post-entity-tick listener heartbeats `AIRBORNE` / `ENGAGED` emissions only for a live, powered, enabled, launcher-cleared active radar seeker with a confirmed or degraded current observation. The envelope uses existing seeker range and cone settings. Selection metadata exposes the projectile UUID through the shared RWR API.

Eligible guided-fuze small, medium and large rockets emit. Unguided rockets, non-radar/passive mode, launcher-clearance flight, exhausted motors, disabled/broken guidance, chaff-suppressed seekers and last-known/ambiguous/false-point-only flight do not. Current observation acquisition already checks target line of sight; the external registry's additional per-receiver LOS option is disabled (its installed implementation directly references Sable classes).

Suppression removes the emitter immediately. Entity removal/detonation/unload uses `EntityLeaveLevelEvent`; post-tick removes it on shutdown or guidance termination. A two-tick heartbeat TTL provides fallback expiration; Radars owns registry decay and dimension cleanup. No rocket or emitter objects are retained in a new global cache.

## E. KABOOM COEXISTENCE

Inspected `create_kaboom-1.21.1-1.0.0-EA.jar`, SHA-256 `35675343E7925EC9BFEEAE69FCE68FBF648467D39B1032AF7A57A21664412222`, matching `build/kaboom-reference`.

| Area | Kaboom behavior | Sable Rocket behavior | Conflict found? | Fix applied? |
|---|---|---|---|---|
| Radars adapters | `create_kaboom:rocket_pod`, accepts Kaboom `RocketPodContraption` | `cbcatfix:rocket_launchers`, accepts `LauncherShotView` | No overlapping key or accepted mount | None needed |
| Chaff | Registers missile and Kaboom unguided-rocket entity types | Registers CBCAT/owned rocket types; runtime seeker eligibility | No shared entity-type key | None needed |
| Jamming | Shared `reportedTracks`/`resolveGuidance`, own emitter identities | Same public API with `cbcatfix:seeker:` IDs | No overwritten registration | Shared API only |
| RWR | `create_kaboom:radar_guidance:` and guided-missile source IDs | `cbcatfix:seeker:` IDs, heartbeat/removal | No key collision | None needed |
| CBC fuzes | CBC fuze accessors/invokers; its own projectile logic | Existing CBC callbacks; guided-fuze override scoped to `RocketPayloadAccess` | No overwritten fuze item or callback found | None needed |
| Controllers | Own pod adapter and command/ARAD handlers | Existing mount adapter; launch-only designation | No adapter replacement; accepted mount classes differ | None needed |
| Launcher persistence | Own inventories/missile controllers | Existing lane/transfer state unchanged; seeker uses primitive NBT | No shared serialized keys/objects introduced | None needed |
| Optional loading | Optional Radars integration; bundles Sable Companion | Mod-gated Radars bootstrap and mixins; no Kaboom imports | Radars itself fails without full Sable, below | Documented; no mandatory dependency added |

Kaboom's CBC projectile mixin changes collision handling for its interceptable ordnance and bomb launch contexts; it does not claim our guidance state. Combined client/world/server startup succeeded. This is a source/registration and startup audit, not proof of every combat interaction.

Future opportunities, not implemented: shared-API gameplay testing of Kaboom ARAD against these selectable seeker emissions and mixed-controller batteries. No ammunition, motor, warhead, physics, conversion or registry-remapping interoperability was added. Kaboom's missing heatseeker loot item, malformed texture paths and missing sound assets are upstream content issues, not evidence of an integration conflict; they were not patched.

## F. OPTIONAL DEPENDENCY SAFETY

| Runtime configuration | Dedicated startup result |
|---|---|
| A. Base only | Passed; `Done`, recipe load, clean save/stop on repeat |
| B. Base + Radars | Failed inside Radars during mod construction: missing Sable `SubLevelContainer` |
| C. Base + Kaboom | Passed; `Done`, recipe load, clean save/stop; bundled Sable Companion is not full Sable |
| D. Base + Radars + Kaboom | Same upstream Radars construction failure as B |
| E. Base + Sable + Radars + Kaboom | Passed; `Done`, adapter setup, recipe load, clean save/stop |

B/D fail at `com.happysg.radar.registry.ModItems.<clinit>(ModItems.java:34)` while linking its `AdvancedProximityFuze`, with `NoClassDefFoundError: dev/ryanhcode/sable/api/sublevel/SubLevelContainer`, before this mod's common-setup registration. This is an accidental hard class reference in the installed Radars EA, despite optional metadata. It is **not fixed** by this update: replacing upstream fuze registration/classes or shipping Sable stubs would exceed this small sensor-layer change. Consequently this exact upstream build cannot be claimed to support B/D. No dependency was made mandatory to hide the failure.

New Radars imports are confined to `compat/radars/RadarSeekerCompat`; its signatures used from common code contain only common project/Minecraft types. The existing designation/launcher bridges remain behind Radars checks. Common serialized state contains no Radars/Kaboom classes. No new Kaboom compile or packaged dependency; its local JAR was added only by the temporary validation init script. Base-only JVM checks additionally loaded and round-tripped `RocketSeeker` with neither Radars nor Sable present.

## G. VALIDATION

- `./gradlew.bat build --no-daemon --console=plain`: **passed**, including final packaging. Normal Gradle `test` has no sources; the focused checks below are separate and are not packaged.
- `./gradlew.bat checkSeekerState -PminimalRuntime -I dev/compat-checks/checks.init.gradle --no-daemon --console=plain`: **passed**. Covers legacy saves, UUID-free synthetic saves, unrelated chaff IDs, suppression extension/expiry, permanent break persistence, non-radar immunity and malformed state.
- `./gradlew.bat checkRadarObservation -I dev/compat-checks/checks.init.gradle --no-daemon --console=plain`: **passed** against installed Radars. Verifies canonical position/velocity are offset, synthetic observations ignore canonical data, and missing contacts return no observation.
- Conditional recipe/packaged JSON/installed ingredient assets/test-class exclusion checks: **passed**. No new item registry entry or changed dependency metadata was introduced in this turn.
- Client: `./gradlew.bat runClient -I build/optional-ew-validation.init.gradle -PewKaboom --no-daemon --console=plain`. Default runtime supplies Radars/Sable/Aeronautics/JEI/Jade; the init script adds Kaboom. **Main menu and existing normal `New World (2)` were visually verified**, world loaded without immediate crash, saved back to title, and client stopped. No special world was created. UI shutdown returned a foreground-process error after closing; window inventory and `Stopping!` log confirmed exit.
- Servers: `./gradlew.bat runServer -PminimalRuntime -PrunDirectory=build/architecture-server-minimal -I build/optional-ew-validation.init.gradle --no-daemon --console=plain`, with `-PewRadars` for B, `-PewKaboom` for C and both for D. E adds all three `-PewRadars -PewKaboom -PewSable` and uses existing `build/architecture-server-dev`. Results are in F. The first A run had closed stdin and was terminated after startup; the repeat A run saved/stopped cleanly. An initial B attempt selected no Radars due to development dependency coordinates and was counted as that repeat A, not a Radars pass; corrected B/D loaded the verified JAR and exposed the upstream failure.
- Local evidence: `build/optional-ew-server-A.log`, `build/optional-ew-server-A-clean.log`, `build/optional-ew-server-B.log` through `-E.log`, and `build/optional-ew-client-E.log`. No new compatibility classloading errors appeared in passing configurations. Logs include pre-existing optional-mixin warnings, Kaboom content errors and unavailable Mojang authentication/update endpoints; they are not clean-error-free logs.
- `git diff --check`: passed. Existing unrelated working-tree edits were preserved.

Remaining manual testing: actual chaff rolls and lock suppression/break, expiry/reacquisition, jammer corruption/dropout and false designation, RWR activation/removal, save/reload of airborne suppressed rockets, moving/tilted Sable targets, and mixed Kaboom/CBC fuze/controller operation. Automated checks do not simulate combat or validate RWR receivers in-game.
