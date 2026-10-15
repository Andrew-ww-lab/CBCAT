# Radars 5.0-EA-3.1.0 compatibility — 2026-10-12

Input: user-supplied `create_radar-5.0-EA-3.1.0-1.21.1.jar`, copied into ignored
local `libs/`; its SHA-512 is in `libs/dependencies.json`. Old local JAR retained
but no longer selected by Gradle. No claim is made that this is the newest public
or early-access release. Inspection was against the actual supplied bytecode.

## API migration

- `WeaponShotContext.weapon()` now returns RadarWeaponAdapter; entity() was
  removed. Our adapter narrows the native CbcRadarWeaponAdapter and uses its
  public legacyCbcContext() bridge to the existing CBC contraption. Exact slot,
  launch position, storage and firing eligibility calculations stay unchanged.
- DirectionalJammingApi now exposes RadarContact / ReportedContact snapshots.
  Production consumes the returned snapshot directly. Contract tests use the
  new snapshot type, preserving position/velocity degradation and synthetic
  contact isolation instead of assuming internal RadarTrack carries that data.
- External RWR emitter construction now takes a resource ID. Replaced the old
  RadarType.AIRBORNE enum with native RadarRwrTypes.AIRBORNE.
- Required optional Radars minimum is now 5.0-EA-3.1.0-1.21.1 because the new
  compiled adapter cannot run against the old API. Radars remains optional.
- Existing Traverse/controller Mixin targets still load; the native cap,
  acceleration, low-RPM behavior and scope restoration checks pass.

## Incompatible optional mod

The full-set registry test reproduces:

```
java.lang.NoSuchMethodError:
AbstractMountedCannonContraption WeaponShotContext.weapon()
at com.happysg.kaboom.compat.radars.RocketPodWeaponShotAdapter.resolve(...:29)
```

Kaboom 1.0.0-EA was compiled against the old method descriptor. This is not fixed
by changing CBCAT Fix's adapter. The error occurs while Radars iterates its
registered adapters, including Kaboom's adapter, even for a CBCAT launcher.
Startup alone therefore cannot establish compatibility.

The user chose to provide a newer Kaboom JAR. No production exclusion, silent
rollback or workaround Mixin into Kaboom was introduced. A temporary ignored
Gradle init script excludes Kaboom only for diagnostic isolation.

Evidence is under `build/radars-310-check/`: binary/API diffs, runtime logs and
contract tests. `server-contracts.log` contains the reproduced failure despite
Gradle reporting BUILD SUCCESSFUL: the server crash was logged by Minecraft.
Pass decisions must check the harness success marker as well as process exit.

## Validation results

- Without Kaboom: real dedicated server passed **2946 assertions**, including
  18 new weapon-registry checks across Small, Medium and Large, and 27 Traverse
  checks. Seeker persistence and degraded/synthetic observation contracts passed.
- Without Kaboom: client opened the existing Architecture world and completed
  model/tooltip checks and full Ponder playback. No new test world was created.
- `build --offline` passed without audit-source injection. Release JAR inspection
  confirms the new Radars minimum and absence of test classes/embedded JARs.
- Artifact: `build/architecture-20260915/libs/cbcatfix-1.1.0.jar`;
  SHA-256 `c529e972f0d4d1829e27327921eaed1bab20fd4176e681c0e422432b14a3e559`.
- The former full-set pre-release result does not apply to the upgraded pair.
  Kaboom's registry crash remains reproduced and unresolved pending its update.
  No general guarantee is made for combat behavior or other untested EA builds.

Full-set compatibility remains pending the replacement Kaboom and its tests.

NOT RELEASE READY
