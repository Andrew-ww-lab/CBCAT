# Radars 5.0-EA-3.1.1 compatibility — 2026-10-13

The supplied JAR replaces 3.1.0 in both Gradle compile and development runtime
dependencies. Local older JARs are preserved but not selected. The new SHA-512
is recorded in `libs/dependencies.json`. The optional minimum remains 3.1.0:
the adapter was already migrated and tested against that API generation.

## Findings

Direct JAR comparison found no added/removed classes and 74 changed class files
(including nested classes). WeaponShotContext, jamming contact API and RWR API
are byte-identical to 3.1.0. Our previous Java adaptations remain applicable;
no additional production Java changes were required for this update.

Changed internals include WeaponFiringControl, network coordination, monitor
rendering and pitch control. CannonMountPitch now maintains motion states per
block entity rather than one shared state. Our Traverse hook wraps native motion
steps and therefore retains native ownership of those states. The private
ballisticSidePathClear signature changed; this project does not target it.
Existing estimateSlewTicks, rotateCBC and speed-cap hooks still load.

## Kaboom conflict reproduced

Kaboom 1.0.0-EA still fails in
`com.happysg.kaboom.compat.radars.RocketPodWeaponShotAdapter.resolve`, line 29,
when Radars resolves the registered weapon adapters. Its expected descriptor is:

```
WeaponShotContext.weapon(): AbstractMountedCannonContraption
```

The actual return type is RadarWeaponAdapter. Inspection of Kaboom's same adapter
also finds calls to removed `WeaponShotContext.entity()` and the old
`WeaponShotContext.mount(): CannonMountContext`. The new mount() returns
RadarMountAdapter. Fixing only the first failing call would leave further binary
incompatibilities. Radars 3.1.1 does not restore these old descriptors.

Full-set server evidence: `build/radars-311-check/server-full.log` records
NoSuchMethodError from Kaboom. Gradle reports success despite Minecraft logging
a server crash, so that result is explicitly **FAIL**, not a passing runtime test.
The user's existing choice to supply newer Kaboom remains pending; Kaboom has
not been removed from production dependencies or patched with another Mixin.

## Recommended migration

1. **Update/recompile Kaboom's whole weapon adapter against Radars 3.1.1.**
   For CBC-specific behavior, narrow context.weapon() to CbcRadarWeaponAdapter,
   obtain legacyCbcContext(), then getContraption() and its native cannon. Handle
   null/unassembled mounts and non-CBC weapons. For generic mount operations use
   the new RadarMountAdapter interface. Preserve existing ammunition/fire rules.
2. **Keep CBCAT Fix's current native adapter approach.** Do not replace Radars'
   pitch state, acceleration or braking with a custom motion system. Its new
   per-mount state makes independent-mount behavior especially important.
3. **Retest the complete set with the replacement Kaboom.** Exercise the registry
   for empty/loaded/detached launchers, all three tiers, and Kaboom's own launchers.
   The existing regression tests cover the CBCAT side. A gameplay check with two
   mounts sharing control and a moving Sable vehicle would cover behavior beyond
   startup and the current in-memory fixtures.
4. **Avoid a rushed monitor API rewrite.** The current designation capture needs
   synthetic-contact identity and category. MonitorContactSnapshot exposes only
   id/position/selectability/source type; switching to that alone would lose data.
   Keep the working capture until an equivalent public contract is established.

No workaround that hides linkage errors, deletes registered adapters or changes
gameplay was applied. Newer Kaboom remains the preferred fix for the full set.

## Validation

Tests use existing worlds only, with the same temporary diagnostic exclusion of
Kaboom as the 3.1.0 investigation. Main build.gradle still includes Kaboom.

- Full dependency set: **FAIL**, reproduced Kaboom linkage error above.
- Diagnostic dedicated server without Kaboom: **PASS**, 2946 assertions,
  including 18 native weapon-registry and 27 Traverse assertions. Seeker
  persistence and Radars observation contract checks also passed.
- Diagnostic client without Kaboom: **PASS**, existing Architecture world
  loaded, module/ammunition models and tooltips checked, full Ponder UI playback
  completed, and client exited normally. No new test world was created.
- Ordinary production `build`: **PASS** (offline, without the audit init script).
  Gradle's ordinary test task has no sources; the assertions above run through
  the separate development audit harness.
- Supplied Radars JAR and selected local dependency have identical SHA-512.
- Artifact inspection: no audit/probe/check classes packaged; author DrLuckily,
  version 1.1.0, optional Radars minimum 3.1.0. `git diff --check` passed.

Commands used the Java 21 installation and existing Gradle cache:

```powershell
./gradlew.bat build --offline --console=plain --no-daemon --project-cache-dir build/architecture-cache -I build/architecture.init.gradle
```

Runtime audit tasks were `runServer checkSeekerState checkRadarObservation`
and `runClient`, with the development audit init scripts. The isolated runs also
used `build/radars-310-check/without-kaboom.init.gradle`; this exclusion is not
part of production dependencies. Evidence is in `build/radars-311-check/`:
`server-full.log`, `server-without-kaboom.log`, `client-without-kaboom.log`,
and `build.log`.

Artifact: `build/architecture-20260915/libs/cbcatfix-1.1.0.jar`.
SHA-256: `c529e972f0d4d1829e27327921eaed1bab20fd4176e681c0e422432b14a3e559`.

### Additional upstream resource failures

The passing isolated runs are not clean-log claims. Both server and client
reject two recipes contained in the supplied Radars JAR:

- `data/create_radar/recipe/crafting/radar_jammer.json`: result
  `create_radar:jammer` is not registered. Recommend correcting the result to
  the intended registered item and reviewing its other ingredient IDs.
- `data/create_radar/recipe/crafting/radar_warning_reciever.json`: ingredient C
  uses `"item": "c:plates/copper"`; this should be a tag ingredient
  (`"tag": "c:plates/copper"`). The recipe currently fails to load.

The client also reports a missing blockstate model variant for
`create_radar:sky_radar_sublevel_connector`, plus existing CBCAT model warnings.
These resources were not silently replaced as part of the API compatibility
check. Recommend fixing the Radars resources upstream before release.

Actual combat with multiple controlled mounts and a moving Sable vehicle remains
a manual gameplay check; startup and in-memory assertions do not establish it.

NOT RELEASE READY
