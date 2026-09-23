# Radars 5 EA compatibility update — 2026-09-19

## Scope and branch

Started from clean `main`, created `codex/radars-5-ea-compat`. No reset, merge, push or dependency-JAR modification. Only local copies of the supplied Radars/Sable JARs were added to `libs`; older JARs remain but are not selected by Gradle.

Exact inspected inputs (SHA-256):

- Radars 5.0-EA-2.0.2-1.21.1: `2E0220BB4A59E452345EBE81FAF6C1A0177337B38206F2A250B1BF7D5A9F27F1`
- Sable 2.0.5: `C8710F85BC780BBF523E6726CEAAF76DD0B8C61479DB497B382C6BB34317E7AB`
- Kaboom 1.0.0-EA: `35675343E7925EC9BFEEAE69FCE68FBF648467D39B1032AF7A57A21664412222`

## Actual Radars path

`CannonMountContext.resolveEndpoint/of` resolves CBC mounts/extensions (plus its own optional compact-mount compatibility). Recognition is class-based, not a rocket tag/capability. `WeaponFiringControl.hasResolvableShot/tick` consults `WeaponShotAdapterRegistry` before the built-in projectile resolver. `WeaponShotContext` exposes the actual mounted contraption and entity.

Unlike the old 0.4.8 integration, 5 EA already recognizes native CBCAT small/medium rocket contraptions in `CBCATCannonCompat`. Its native powered-flight assumptions are not our modified flight system. Merely relying on those assumptions would not model our rockets correctly.

`RadarLauncherCompat` registers a DIRECT profile for our `LauncherShotView` contraptions. Large rails share the medium contraption but their tier/geometry comes from their real breech. The read-only view follows the existing round-robin cursor and loaded lanes, prefers a ready lane, and keeps tracking during cooldown. No ammunition is extracted or rewritten by the adapter. Readiness checks cooldown, assembled lifetime, transfer locks and rail length. Radars retains its safety/alignment/auto-fire gates.

### Coordinates

The adapter uses the same `RocketMounts.transform` slot center/direction as `RocketMountLaunchMixin`, then `PitchOrientedContraptionEntity.toGlobalVector`, then existing `RocketSableCompat.launchFrame`. This last step uses Sable's current `logicalPose().transformPosition/transformNormal` and native carrier velocity. The supplied profile position is the actual ejection center (not a guessed common barrel origin).

Radars computes the target-world minus ejection-world direction. `WeaponFiringControl.buildMountAimFrame` obtains the containing carrier through `SableCompanion.INSTANCE.getContaining` and transforms its right/up/forward basis with its native Sable helper. `MountAimFrame.evaluate` / `PitchConstraint` express that world direction in the current mount basis, respecting pitch/yaw limits. Radars' native pitch/yaw controllers issue mount commands. This handles translation, rotation and tilt without a second matrix/quaternion implementation. Unavailable native frames remain fail-closed.

One narrow upstream EA issue needed a mixin: in `tick`, `rejectCustomDirect` still rejects any built-in CBCAT powered rocket in the ordinary-world branch even when a DIRECT adapter exists. `RadarDirectLauncherAimMixin` bypasses only that fallback powered-rocket rejection for `LauncherShotView`. It does not alter global `CannonUtil`, registry IDs, projectile entities or ballistic properties. Sable already takes the explicit direct-profile branch.

The old launch-only `RadarDesignation` UUID/position bridge is retained: the exact new JAR still supplies `activeTrackCache`, `getId/getPosition/getTrackCategory` and guided-fuze `monitorPos`. No obsolete solver existed in our mod to remove. The old Gradle Radars dependency is replaced. Installed older Radars versions are now rejected by the optional minimum-version constraint instead of failing at API linkage.

Limits: direct aiming is not a powered-flight intercept solution. Native target visibility, controller setup, movement limits and target filtering still apply. No automatic linking of unlinked guided fuzes, new network protocol, fake cannon shells, replacement entities or full rocket lead solver was added.

## Fire-rate balance

At neutral server multipliers and maximum native rate (small interval 3 ticks, medium/large 6 ticks):

| Tier | Previous internal factor | New factor | Previous base maximum/min | New base maximum/min |
|---|---:|---:|---:|---:|
| Small | 0.9 | 0.675 | 360 | 270 |
| Medium | 1.0 | 0.75 | 200 | 150 |
| Large | 0.5 | 0.5 | 100 | 100 |

Only `BalanceDefaults` changed. The existing cooldown division/fractional remainder applies the change once; the display scales the reported rate independently, not the firing timer a second time. Existing user-config global rate multiplier remains relative to the new baseline. Existing default group multipliers remain 1 / 1.25 / 1.5; custom group-bonus overrides are preserved. Tick scheduling means instantaneous gaps are rounded, with the existing remainder preserving the intended average.

## Kaboom research (no integration installed)

Inspected executable implementations, not just class names. In particular `HighExplosiveMissileWarhead`, `APMissileWarhead`, `FragMissileWarhead` and `ClusterMissileWarhead` are empty classes in this EA; actual warhead behavior is in `MissileWarheadProjectile` and `AerialBombProjectile`.

| System | Actual behavior | Relevance | Possible compatibility | Priority | Notes |
|---|---|---|---|---|---|
| Rocket pod + Radars adapter | `RocketPodWeaponShotAdapter` registers separate direct/ballistic shot profiles | Same controller API | Coexist through distinct registry adapter IDs | Now: existing API approach | Our adapter only accepts our own view; it does not claim Kaboom pods |
| Modular missiles | Fuel tanks, thrusters and guidance providers feed its missile system | Similar purpose, different state/assembly | Keep independent; optional adapters only after a concrete use case | Not needed now | Replacing our entities would lose our fuel/payload/fuze state |
| Guidance | GPS, radar, command and ARAD implementations; Radars jamming/chaff hooks | Future target/countermeasure interoperability | Use Radars public designation, jamming and chaff APIs, not Kaboom private state | Later | Changes seeker balance and target-loss rules; requires gameplay tests |
| Rocket payloads | `RocketPayload` implements HE, AP, frag, smoke and fluid payload paths using CBC effects | Potential new ammo recipes | Explicit optional recipes/conversion with lossless component mapping | Later | Do not accept every Kaboom round in our rails by a broad tag |
| Bombs and missile warheads | `AerialBombProjectile` implements HE/AP/fluid/frag/incendiary/cluster; `MissileWarheadProjectile` delegates detonation | Alternate payload behavior | Opt-in new payload type, not replacement of current HEAT/HE | Later/low | Explosion size, penetration, fluids and fragmentation require independent balancing |
| CBC mounting | `MountedMissileController` handles mounted missile release | Shared CBC ecosystem | Native mount coexistence; no shared release implementation | Not needed | Kaboom missile release is not our lane firing/serialization |
| Sable launch kinematics | `SableUtils.getLaunchKinematics` uses native pose, direction and carrier velocity | Same physics boundary | Continue using Sable APIs directly | Not needed | Importing Kaboom's helper duplicates an existing optional bridge |

Recommendation: now keep independent ammunition and integrate each mod with the common Radars API. Later consider shared countermeasure APIs and narrowly defined component recipes. Do not merge flight controllers, inventories, warhead entities or HEAT mechanics in this update. Kaboom is not a compile/runtime dependency of this project.

## Validation

- `./gradlew.bat build --no-daemon`: successful; no test sources, so this is compilation/resource packaging, not gameplay test coverage.
- `./gradlew.bat runClient --no-daemon`: successful process exit. Radars adapter registration, `RadarDirectLauncherAimMixin` and Sable transfer mixin application confirmed in logs.
- UI automation could not initialize (`failed to write kernel assets`). User performed the UI steps. Log confirms `Dev joined the game` at 10:14:04, ordinary existing `New World (2)` saved at 10:14:27, client exited at 10:14:30. No immediate crash or ERROR/FATAL in that run. Main menu was not independently visually observed by the agent.
- `./gradlew.bat runServer -PminimalRuntime -PrunDirectory=build/architecture-server-minimal --no-daemon`: existing world reached `Done` without Radars, Sable, Aeronautics, Jade or Kaboom. This first process had no writable console and was terminated after startup; do not treat that as a clean-shutdown test.
- `./gradlew.bat runServer -PrunDirectory=build/architecture-server-dev --no-daemon`: existing world reached `Done` with exact Radars 5 EA and Sable 2.0.5; no dedicated-server classloading crash. Gradle did not forward `stop` to the server; the validation process was ended with Ctrl+C. Clean server shutdown was not validated.
- Existing CBCAT missing-model warnings and Radars `sky_radar_sublevel_connector` missing-variant warning remain, outside this focused update.
- No special world, datapack, combat scene or launcher structure was created. No Kaboom runtime/gameplay compatibility claim is made.

Manual checks still required: all launcher sizes on ground and translated/rotated/tilted/moving Sable carriers; target changes and loss; controller limits; auto-fire with several targets; one/two/three-lane cadence and next-slot offsets; guidance-fuze identity and payload preservation; save/reload with existing assemblies. Precise rocket interception is explicitly deferred.
