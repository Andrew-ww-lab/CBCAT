# Guidance architecture / Radars 3.1.1 refactor

Status: **IN PROGRESS — migration detail and RWR policy await user decisions**.
No new items, recipes, assets, balance defaults or installation interaction.

## A. Old path

`RocketBalance.configureProjectile` reads the CBC FUZE component, calls
`SableGuidanceCompat.initializeAtLaunch`, detects GuidedFuzeItem by class name,
reads `monitorPos` from the fuze's CUSTOM_DATA and captures a contact. This runs
in the projectile factory, before actual world insertion. `RocketSeeker` retains
only contact identity/position and autonomous observation state.
`GuidedFuzeItemMixin.onProjectileTick` suppresses the dependency's second steering
pass for RocketPayloadAccess projectiles. Both Small/Medium flight mixins retain
the special universal-fuze guard; Large derives from the Medium path.

Saved compatibility: individual item FUZE/CUSTOM_DATA, RocketBlockEntity `Rockets`
list, mounted slot ItemStacks, projectile `CbcatFixUniversalFuze`,
`CbcatFixSeeker` (identity, last-known position, synthetic, suppression deadline),
and `CbcatFixGuidanceBroken`. No concrete RocketInstance class exists here:
the canonical stored round is its ItemStack.

## B–G. New path and equipment

Implemented for explicitly equipped rounds:

```text
Actual fireShot, authoritative lane and slot selected
  -> copied fired ItemStack (same item used for spawn-failure restoration)
  -> RocketSeekerEquipment capability check
  -> CBC carrier's actual controller/mount
  -> RadarLauncherNetwork: existing Radars runtime mount -> pitch endpoint
  -> RadarNetworkApi.getSelectedContact
  -> RadarContactMetadata (synthetic metadata only)
  -> detached RocketSeeker launch snapshot
  -> spawned projectile
  -> existing RocketSteering after launcher clearance
  -> autonomous flight

Per-round ItemStack
  |- payload (existing CBC component)
  |- fuze (existing CBC component)
  |- CbcatFixSeekerEquipment in existing CUSTOM_DATA
  '- existing fuel/variant/components
```

The item holds equipment, never a target. `RocketFlightState.activeRadarEquipped`
is the serialization transfer to the projectile, stored as
`CbcatFixActiveRadarEquipped`; the projectile alone owns its live seeker snapshot.
Factories copy equipment but never designate targets for these rounds. No
public gameplay installation method, item or recipe is registered. The setter
is an internal preparation/migration seam.

`RocketMountLaunchMixin` calls the initializer immediately before addFreshEntity,
using the captured fired round. Existing round-robin, lane selection, geometry,
clearance, velocities, spawn rollback and fuze handling are unchanged. Manual
launch without a selected target remains allowed. After launch no network,
controller or mount reference is retained or polled.

`WeaponShotAdapterRegistry` remains the read-only integration. Reasons distinguish
empty, invalid, blocked, cooldown and arming states. Arming readiness now reflects
the lock already enforced by the actual fireShot wrapper. There is no second
automatic-fire controller and no target write during profile resolution.

The old source path temporarily remains for unequipped legacy rounds until the
required migration decision is resolved. It now shares the public-API snapshot
conversion; this is not yet the final single-path architecture.

## H. EW, chaff and false contacts

Production construction of internal RadarTrack was replaced with BasicRadarContact.
The installed API converts it to a native track before applying the existing
DirectionalJammingService. Identity, position, velocity and SABLE/MISC category
are preserved. Internal source-label text changes to Radars' external-contact
label; inspected jammer bytecode does not use that label for these observations.

Physical observations still seed the simulated onboard sensor. They are passed
through reportedTracks and resolveGuidance before steering can use them; no
fallback directly feeds true UUID positions into steering when EW produces no
observation. Existing null-contact, distortion and synthetic rules remain.
FOV/range/LOS, frozen last-known position and reacquisition remain in RocketSeeker.
ChaffLockRegistry registration/suppression are unchanged.

All native RadarTrack casts are isolated in RadarContactMetadata. Installed
getActiveRadarContact returns the original RadarTrack, so the false-contact flag
is preserved. ReportedContact also exposes that metadata publicly. Unknown
representations are not assumed real; designation is rejected rather than
inventing a UUID target. Future contact representations require review.

## I. RWR

RadarRwrApi exposes heartbeat only. Existing ExternalRwrEmitterRegistry provides
explicit remove and the directional/engaged emitter state with a two-tick lease.
Lifecycle parity has not been proven. Existing path remains unchanged pending
the user's RWR decision; no mechanical replacement or emitter timing changes.
Only active, powered, cleared, unsuppressed radar seekers emit; unavailable,
ambiguous, last-known and false contacts remove the emitter as before.

## J–M. Other APIs

| Integration | Classification | Reason |
|---|---|---|
| Selected contact cache | REPLACE WITH PUBLIC API, done | RadarNetworkApi replaces activeTrackCache field access |
| Internal raw-track construction | REPLACE WITH PUBLIC API, done | BasicRadarContact accepted by jamming API |
| Synthetic flag | REQUIRED FOR SYNTHETIC FLAG | One isolated native cast, allowed by part 6 |
| Mount -> pitch endpoint | KEEP, user approved A | WeaponNetworkRuntime supplies relation missing from public network API |
| CbcRadarWeaponAdapter.legacyCbcContext | KEEP | Actual CBC slot storage/contraption geometry still required |
| Native direct-aim Mixin | KEEP TEMPORARILY | Targeting solution API does not prove parity with powered-rocket rejection branch |
| Traverse Mixins / DirectCbcMountMotion | KEEP | Approved multiplier on native cap and slew estimate, native per-mount state/cache retained |
| GuidedFuzeItemMixin and fuze guard | KEEP TEMPORARILY | Legacy migration unresolved; prevents dual control |
| ChaffLockRegistry | KEEP | Existing registration API; no second countermeasure simulation |
| ExternalRwrEmitterRegistry | REQUIRED FOR LIFECYCLE GAP | Explicit removal; approval pending |
| RadarTargetingRegistry | FUTURE CLEANUP | Position/velocity/acceleration solution hook, not flight or firing-state API |
| RadarPhysics | KEEP existing Sable bridge | Public coordinate helpers do not replace muzzle geometry or inherited/angular velocity |
| Monitor APIs | FUTURE UI ONLY | Snapshot lacks sufficient deception/category data for launch guidance |
| RadarDataLinkInteractionRegistry | NOT NEEDED for approved approach | User selected existing-controller adapter instead of a new binding interaction |

## N. Migration status and decisions

User selected **B: convert Guided Fuze equipment into independent seeker state**.
User also approved **A: isolated adapter of the existing Radars controller**.

Still awaiting explicit decision on whether old monitorPos is discarded in favor
of the launcher's network (recommended, disconnected legacy launchers then fire
unguided), or preserved as a migration binding. Also pending: retain the existing
isolated internal RWR lifecycle path, or defer that migration until a public API
exists. Conversion is not silently enabled while those details are unresolved.
Already-flying target snapshots must survive either migration.

## O. Optional dependency

Common equipment and launch entry points expose only Minecraft/CBC types.
Radars classes are reached after ModList checks. Existing conditional mixin
selection remains. Missing Radars means no new target; manual launch/physics
continue. No unconditional Radars subscriber was added.

## P. Validation

Evidence under ignored `build/radars-311-check/guidance-*.log`.
Completed initial server audit: 2958 assertions; seeker persistence and EW
contract checks passed. Equipment roundtrips and no-target factory behavior
tested for Small/Medium/Large. Base dedicated server without optional mods passed.
Final rerun in the pre-existing server directory: **2964 assertions passed**,
including 24 weapon-adapter assertions. Seeker persistence and Radars observation
contracts passed, including detached snapshot and false-contact metadata checks.
Base/no-Radars dedicated server: **2913 assertions passed**. No-Radars client
loaded existing Architecture world and completed full Ponder UI playback.
Radars client loaded the existing world and verified Ponder registration/timeline;
its log does not confirm full playback completion, so that is not claimed.
No-Radars client/server runs preceded the final Radars-only diagnostic/loaded-chunk
guard changes; no common production changes followed them.

Ordinary offline `build` passed without the audit init script; `test` has no
sources (the validation above is the separate development harness). Packaged
JAR contains the new guidance classes and zero audit classes. `git diff --check`
passed. Artifact SHA-256:
`a149f954bcb912e591c329495c6efd14c5bf1dbad6003c07996fdd1dccfb8395`.

An invocation mistakenly combined server and client tasks with the client run
directory; it generated `build/architecture-client-minimal/world`. This violates
the requested no-new-world test procedure and is not counted as compliant server
validation. Subsequent server/client invocations are separated and use existing
worlds. No user world was deleted. Client quick-play used existing `Architecture`.

Full live network launch with real controllers, switching/destroying controller,
Sable moving carrier, chaff and RWR lifecycle need end-to-end gameplay checks;
unit/fixture checks do not establish these. Existing Kaboom 1.0.0-EA still blocks
full-stack registry execution, so diagnostic Radars runs exclude it only via an
ignored init script. Production dependencies are unchanged.

## Q. Sources and remaining work

Exact signatures and behaviors inspected from supplied
`create_radar-5.0-EA-3.1.1-1.21.1.jar`, including network, jamming, targeting,
weapon, RWR, physics, monitor and Data Link APIs. Official project/source and
changelog checked:
[project](https://modrinth.com/mod/create-radars),
[repository](https://github.com/Arsenalists-of-Create/Create-Radar),
[changelog](https://raw.githubusercontent.com/Arsenalists-of-Create/Create-Radar/master/changelog.txt).
The public changelog only describes early .1/.2 releases; the requested API source
path was unavailable through web retrieval. It is not treated as evidence for
3.1.1 signatures; the exact installed binary is authoritative for this review.

Remaining: resolve migration detail/RWR decisions, implement per-round migration
without another target source, remove obsolete legacy initialization only then,
verify actual controller topology and launch behavior, repeat validation for the
completed migration. No FPS/TPS improvements claimed.
