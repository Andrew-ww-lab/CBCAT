# Radars 3.1.3 overlap review — 2026-10-15

Compared the supplied Downloads JAR against selected project dependency 3.1.1.
Production dependency selection and production Java were not changed. Screenshot
notes distinguish CBC:AT fixes announced for 3.1.2 from further 3.1.3 changes;
binary comparison is cumulative 3.1.1 -> 3.1.3, not a per-release attribution.

## Confirmed changes

- CBCATCannonCompat.findLoadedAmmunition now prioritizes the actual breech at
  getStartPos. Twin/heavy autocannons inspect their next buffer/magazine round;
  Small rocket pods peek the input buffer, Medium rails scan getFromSlot in order.
  verifyNextRound validates ammunition kind and projectile construction. This is
  base CBC:AT support, not explicit CBCAT Fix lane/slot integration.
- CBCATRocketProjectileModel now samples through LoadedFluidDrag rather than
  directly querying Level.getFluidState. LoadedFluidDrag requires the server
  thread and uses getChunkNow; unavailable samples abort through its sentinel.
  This avoids loading chunks through that sampling path. No measured TPS claim.
- CBC More Shells received racked rocket/torpedo/special-bomb model additions.
  This is separate from our launcher state and steering.
- Network/controller targeting and Sable-related internals changed. New config
  options cover automatic target cycling, unloaded-target timeout and water-depth
  filtering. These influence selection for later shots, not our already-flying
  autonomous seeker snapshots.

## Overlap assessment

| Current integration | Assessment |
|---|---|
| RadarLauncherCompat / WeaponShotAdapterRegistry | Keep. Registry profile takes precedence over built-in CBCAT fallback. Our exact lane/slot, Large tier, module/readiness and muzzle geometry remain required. |
| RadarDirectLauncherAimMixin powered-rocket bypass | Keep for now. Both isPoweredRocket calls and the DIRECT/fallback branch remain in tick; native ballistic support does not prove this workaround obsolete. |
| Traverse controller/motion hooks | Keep. DirectCbcMountMotion is byte-identical; controller hooks loaded in runtime tests. No second multiplier found in the new dependency. |
| New equipment / launch snapshot path | No new upstream replacement. All existing api/ class bytes are unchanged, as are GuidedFuzeItem and chaff classes. |
| RWR | ExternalRwrEmitterRegistry and public API unchanged. Explicit-removal lifecycle gap remains. |
| RocketSteering / Sable launch physics | Keep. Upstream projectile prediction is not an equivalent simulation of our powered flight, slot offsets and inherited velocity. |
| RadarLauncherNetwork internal mount lookup | Runtime class changed; current code compiles, but real multi-controller/Sable topology lookup still needs gameplay coverage. |

hasResolvableShot consults our public profile first and recognizes DIRECT as
loaded without requiring canTriggerFire. Thus the new ammo-availability path does
not inherently equate our arming/cooldown flag with an empty launcher. End-to-end
automatic target timeout/cycling behavior still merits a normal-world test.

## Validation and limits

Temporary `build/radars-313-check/override.init.gradle` selects the Downloads
3.1.3 JAR for compile/runtime and excludes known-incompatible Kaboom only for
diagnostic execution. Production dependencies still select 3.1.1 and Kaboom.

Executed in pre-existing `build/architecture-server-minimal`:
`runServer checkSeekerState checkRadarObservation --offline`, with the existing
architecture and audit init scripts plus the temporary override.

- Compilation and dedicated server audit passed: **2964 assertions**.
- Native weapon adapter/readiness tests and Traverse checks passed.
- Seeker persistence and EW observation contract checks passed.
- Evidence: `build/radars-313-check/server.log` and javap dumps/diffs there.
- No new world created for this review. Client/real combat were not run.
- Existing Radars jammer/receiver recipe parse errors remain in this JAR.
- No full-pack compatibility claim: Kaboom was excluded. WeaponShotContext is
  unchanged, so the old Kaboom binary mismatch has no restored API here.

Recommendation: retain our exact-shot adapter and current Mixins, test target
cycling/ammo timeout plus moving/multiple mounts before declaring a production
upgrade. Do not switch our rockets to CBCATRocketProjectileModel or delete the
DIRECT workaround solely on the basis of the release announcement. Outstanding
guidance-migration decisions are unaffected by this dependency review.
