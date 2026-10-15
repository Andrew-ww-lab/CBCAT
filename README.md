# CBCAT Fix / Sable Rocket

Rocket launcher progression, ammunition, loading assistance and compatibility
fixes for Create Big Cannons: Advanced Technologies on Minecraft 1.21.1 NeoForge.

## Installation

Install the mod on both client and server, together with Create, Create Big
Cannons, CBC: Advanced Technologies and their required libraries. Use Java 21.
Do not install a second CBCAT Fix JAR alongside an older version.

The development baseline is NeoForge 21.1.228, Create 6.0.10, CBC 5.11.7,
CBCAT 0.1.4c and RitchiesProjectileLib 2.1.2. Create also requires Flywheel,
Ponder and Registrate. The loader's accepted version ranges are in
`src/main/resources/META-INF/neoforge.mods.toml`; broader ranges do not mean
every version combination has been tested.

Sable, Create: Radars, Create: Kaboom, Jade and JEI are optional integrations.
The development build uses Sable 2.0.6, Radars 5.0-EA-3.1.3, Kaboom 1.0.0-EA,
Jade 15.10.6 and JEI 19.27.0.340. Public Radars 0.4.x is not a substitute for
the Radars 5 API used here. Aeronautics is included in the full development
profile but is not a required dependency of this mod.

## Launchers and ammunition

- Maximum launcher length, including the breech: Small 3, Medium 5, Large 8 blocks.
- Assembly prepares the launcher for 20 / 40 / 100 ticks respectively.
- Insert rockets into individual compatible empty tubes while unassembled.
  Removal and fuze editing preserve individual rocket data.
- Hold a rocket and aim at an existing launcher or rocket pile to preview the
  next valid position. Empty ground intentionally has no placement hint.
- Hold Shift over items for Create-style summaries. The Small launcher has
  an introductory Ponder scene.
- Rapid Cycling and Traverse modules are produced through casting, drilling
  and finishing. Traverse affects powered aiming; mouse aiming remains direct.
- Cluster assembly requires eight fuzed submunitions. Each secondary fuze is
  retained separately from the main fuze. Consult JEI for the current recipes.

## Building

Use the committed Gradle wrapper with Java 21. Supply the exact local dependency
JARs declared by `build.gradle` in `libs/`, obtained from their publishers.
Some development integrations are early-access distributions. Do not substitute
similarly named public releases without checking their APIs.

```powershell
./gradlew.bat build
./gradlew.bat runClient -PminimalRuntime
```

The release version has one source: `mod_version` in `gradle.properties`.
The Sable development version is controlled by `sable_version`.
Use `-PverboseDiagnostics` only when detailed Mixin logs/exports are needed.
Optional audit harnesses under `dev/` are not part of the release JAR.

## Release status and licensing

The pre-release audit passed for its earlier dependency baseline; the subsequent
Radars 3.1.1 upgrade exposed an incompatibility with Kaboom 1.0.0-EA during weapon
profile resolution. A compatible Kaboom update is pending. See the
[audit report](docs/prerelease-audit-20261012.md) for runtime results, known
upstream warnings and remaining optional manual checks. This does not establish
compatibility with untested newer dependency versions.

Module gear/spring markings now use original procedural geometry; see
[their provenance](docs/module-marking-provenance.md). DrLuckily has confirmed
that the older projectile PNGs and launch recording also originate from CBCAT
and are covered by the permission for CBCAT material used here. Original project code is licensed
under MIT, copyright 2026 DrLuckily; see [LICENSE](LICENSE) and
[third-party notices](THIRD_PARTY_NOTICES.md).
Dependency licenses are not covered by this project's MIT metadata label.

Report reproducible issues at [the project issue tracker](https://github.com/Andrew-ww-lab/CBCAT/issues).
