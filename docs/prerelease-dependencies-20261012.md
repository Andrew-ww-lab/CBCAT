# Dependency review — 2026-10-12

Updated 2026-10-13 after the user-supplied Radars 3.1.1 update; see [compatibility findings](radars-311-compatibility-20261013.md).

Target: Minecraft 1.21.1, NeoForge, Java 21. “Latest compatible” below means
publisher-listed for that Minecraft/loader pair, **not proven compatible with
this complete modpack**. Dates are publisher release dates unless marked Maven
Last-Modified. Public and early-access version streams are not interchangeable.

| Dependency | Current | Latest Compatible | Official Source | API Change? | Safe to Update? | Recommendation |
|---|---|---|---|---|---|---|
| Minecraft | 1.21.1 | Fixed target | [Mojang](https://www.minecraft.net/en-us/article/minecraft-java-edition-1-21-1) | Other game versions require a port | No automatic port | Keep 1.21.1 |
| NeoForge | 21.1.228 | 21.1.259; 2026-10-10 Maven timestamp | [Changelog](https://maven.neoforged.net/releases/net/neoforged/neoforge/21.1.259/neoforge-21.1.259-changelog.txt) | Loader, Mixin and MixinExtras updates across this gap; slot API changes | Not yet qualified | Test separately before adoption |
| Create | 6.0.10 | 6.0.10; 2026-04-21 | [Publisher](https://modrinth.com/mod/create/versions), [repository](https://github.com/Creators-of-Create/Create) | No version change | Current tested | Keep |
| Create Big Cannons | 5.11.7 | 5.11.7; 2026-06-22 | [Release](https://modrinth.com/mod/create-big-cannons/version/5.11.7), [repository](https://github.com/Cannoneers-of-Create/CreateBigCannons) | Latest notes include Sable 2 support/impact fix | Current tested | Keep |
| CBCAT | 0.1.4c | 0.1.4c-1.21.1; 2026-05-02 | [Publisher](https://modrinth.com/mod/create-big-cannons-advanced-technologies/versions) | Notes target CBC 5.11.3; our compatibility hooks bridge current CBC | Current tested with CBC 5.11.7 | Keep; resource permission confirmed by DrLuckily |
| Sable | **2.0.6**, updated from 2.0.5 | 2.0.6; 2026-10-03 | [Release](https://github.com/ryanhcode/sable/releases/tag/mc1.21.1-2.0.6-neoforge), [publisher](https://modrinth.com/mod/sable/versions) | Only one top-level class changed in compared NeoForge JARs; companion API byte-identical | Approved update; full server/client passed | Adopted |
| Radars | 5.0-EA-3.1.1 (user-supplied update) | Latest EA unverified; supplied version inspected directly | [Repository](https://github.com/Arsenalists-of-Create/Create-Radar) | Breaking weapon context, contact snapshot and RWR types; adapters migrated | CBCAT Fix tests pass without Kaboom; Kaboom 1.0.0-EA incompatible | Await newer Kaboom; see upgrade report |
| Kaboom | 1.0.0-EA | EA successor **unverified**; public 1.0-9.3-dev dated 2026-08-09 | [Publisher](https://modrinth.com/mod/create-kaboom/versions) | Different channel; no sufficient migration notes | Unknown | Keep; no Kaboom-internal API dependency |
| Jade | 15.10.6+neoforge | Same; 2026-08-06 | [Publisher](https://modrinth.com/mod/jade/versions), [repository](https://github.com/Snownee/Jade) | Current notes include Aeronautics compatibility | Current tested | Keep |
| JEI | 19.27.0.340 | 19.57.0.451; 2026-10-05 | [Publisher](https://modrinth.com/mod/jei/versions), [repository](https://github.com/mezz/JustEnoughItems) | Whole version gap not binary-qualified; latest single changelog is CI-only | Not established; open user crash report below | Keep until separate upgrade validation |
| Aeronautics | 1.3.0 bundled | 1.3.2; 2026-08-29 | [Publisher](https://modrinth.com/mod/create-aeronautics/versions), [repository](https://github.com/Creators-of-Aeronautics/Simulated-Project) | Changes swivel-bearing mass and JEI/creative compatibility | Behavior changes need approval/testing | Candidate, not automatically adopted |
| RitchiesProjectileLib | 2.1.2 | Same; 2026-02-07 | [Publisher](https://modrinth.com/mod/rpl/versions), [repository](https://github.com/Wagers-of-Industrial-Warfare/RitchiesProjectileLib) | No version change | Current tested | Keep |
| Flywheel API + runtime | 1.0.6 / 1.0.6 | Stable 1.0.6; 2026-01-02 Maven timestamp; development 1.0.7-48 also listed | [Maven](https://maven.createmod.net/dev/engine-room/flywheel/flywheel-neoforge-1.21.1/maven-metadata.xml), [repository](https://github.com/Engine-Room/Flywheel) | Development build API not qualified | No reason to replace stable with development build | Keep paired API/runtime |
| Ponder / Catnip | 1.0.82+mc1.21.1 | 1.0.87+mc1.21.1; 2026-06-20 Maven timestamp | [Maven](https://maven.createmod.net/net/createmod/ponder/ponder-neoforge/maven-metadata.xml), [source branch](https://github.com/Creators-of-Create/Ponder/tree/mc1.21.1/dev) | Placement helper, ghost extension and tutorial rendering need requalification | Not established | Keep Create baseline pending separate test |
| Registrate | MC1.21-1.3.0+67 | Same documented Create baseline; newer compatible release/date unconfirmed | [Create developer guide](https://wiki.createmod.net/developers/depend-on-create/neoforge-1.21.1), [repository](https://github.com/tterrag1098/Registrate) | Maven listing at tterrag only exposes older MC branches | Current local artifact tested | Keep; do not substitute MC1.20 artifact |
| ModDevGradle | 2.0.78 | 2.0.148; Maven metadata updated 2026-09-29 (not a proven release date) | [Plugin Maven](https://plugins.gradle.org/m2/net/neoforged/moddev/net.neoforged.moddev.gradle.plugin/maven-metadata.xml), [repository](https://github.com/neoforged/ModDevGradle) | Build plugin changes across many releases | Not qualified | Separate tooling upgrade |
| Gradle wrapper | 8.9 | Current overall 9.8.1; build date 2026-10-07; compatibility with this plugin unverified | [Official version service](https://services.gradle.org/versions/current), [upgrade guide](https://docs.gradle.org/current/userguide/upgrading_version_8.html) | Major Gradle upgrade | Not established | Keep tested wrapper |
| Java toolchain | 21 | Java 21 remains the target; latest vendor patch not determined | [NeoForge development guide](https://docs.neoforged.net/docs/gettingstarted/) | No language/bytecode target change | Current Microsoft 21.0.12 tested | Keep Java 21 |

## Source and migration findings

- Sable's published fix addresses entities standing on fluids; Fabric networking
  changes are also mentioned. The main NeoForge class difference is
  `TheFasterEntityCollisionContext`; no top-level classes were added or removed.
  This is supporting evidence, not a promise of identical native physics behavior.
- NeoForge's cumulative changelog explicitly includes MixinExtras 0.5.5, Mixin
  0.16.4 and newer FML. Our 72 Mixins make a full runtime rerun necessary.
- [JEI issue 4547](https://github.com/mezz/JustEnoughItems/issues/4547) reports
  world-creation crashes in newer versions and a downgrade workaround. It is an
  unverified upstream user report, not a reproduced CBCAT Fix bug. It prevents
  treating the newest JEI as automatically safe.
- [Ponder issue 92](https://github.com/Creators-of-Create/Ponder/issues/92)
  discusses late sprite registration with the 1.0.82 baseline; issue 91 concerns
  Lithium/schematic chunk behavior. Neither was reproduced in this audit.
  Repository branches exist, but its releases/tags endpoints supplied no release
  entries; Maven availability alone is not proof of API compatibility.
- The public Radars notes themselves mention CBC/Aeronautics limitations. Do not
  extrapolate those notes to the separately supplied Radars 5 EA binary.
- No complete binary/API migration proof was obtained for unadopted upgrades.
  An update decision must be followed by compilation, minimal/full client and
  server, Ponder, placement hints and affected integration tests.

## Scope and packaging

Mandatory dependencies use implementation; optional Sable/Radars/Jade/JEI APIs
use compileOnly with explicit development runtimeOnly entries. Sable's nested
companion is exposed only to compilation. Flywheel uses its API at compile time
and implementation at development runtime. Dependencies are not shaded into the
release JAR. Minimal runtime excludes all optional integrations, including
Aeronautics and Kaboom. Exact local inputs are recorded in
[the manifest](../libs/dependencies.json).
