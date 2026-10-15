# Pre-release audit — 2026-10-12

Scope: the complete current CBCAT Fix / Sable Rocket branch, including launcher
progression, flight, guidance, recipes, placed ammunition, Ponder and placement
assistance. This report supersedes September inventory counts, not historical
design decisions. Gameplay and save-format policy were preserved.

**Subsequent dependency update (2026-10-13):** Radars was changed to the user-supplied
5.0-EA-3.1.1 after the baseline audit. The old Kaboom 1.0.0-EA adapter now fails
with NoSuchMethodError during weapon profile resolution. The baseline results
below remain historical evidence, not validation of that incompatible pair.
See [the latest Radars upgrade report](radars-311-compatibility-20261013.md).

## A. Branch status

- Branch: `codex/radars-5-ea-compat`, tracking the same branch on origin.
- User-requested pre-audit checkpoint: `bebc2f8` — Add Small launcher Ponder and
  native Create rocket placement hints. Previous work is preserved there.
- Audit changes remain in the working tree. Ten dependency JAR removals are
  staged as index-only removals; all local JARs remain on disk.
- No push, merge, reset or history rewrite was performed. Old commits still
  contain their previously committed dependencies.
- `mod_version` remains 1.1.0 as requested by the no-automatic-bump rule.
  **Recommend 1.2.0** for the feature update, after publication blockers close.

## B. Full structure map

Current inventory: **163 production Java files, 72 active Mixins: 57 common and
15 client**, plus the Mixin configuration plugin. The
[complete class/Mixin map](prerelease-class-map-20261012.md) records declarations,
parents, interfaces, responsibilities, dependency APIs, state and hook risks.

| Package / area | Responsibility and important owners | Side / dependencies | Assessment |
|---|---|---|---|
| `com.cbcatfix` | Bootstrap, event/config/sound registration; scoped detonation context | Common; NeoForge/CBC/CBCAT | Necessary; duplicate rocket predicate removed |
| `balance`, `config` | Fixed defaults and synchronized server controls | Common; NeoForge ModConfigSpec | One source for values; fixed 20/40/100 arming remains fixed |
| `rocket` structure/storage | LauncherAssembly, SmallLauncherStructure, RocketMounts, MountedRocketStorage, LauncherTransfer | Common/server mutations; CBCAT/CBC/Create | Native contraption and breech storage retained |
| `rocket` placed ammunition | RocketBlock/Entity, Footprint, Geometry, GroundPlacement, TargetResolver | Common; CBC ammunition inheritance | Canonical placed ItemStacks; shared geometry for render/pick/place |
| `rocket` flight/guidance | RocketFlightState, Steering, Ballistics, Seeker, Sable adapters | Common/server guidance; native projectile and optional APIs | No second orientation or EW framework |
| `munitions` | Registries, large rocket factories/entities, CBC-derived ammunition, cluster payload/fuzes | Common; CBC/CBCAT | Direct owned factories now replace own-class Mixin |
| `crafting` | Component-aware native recipe support | Common/server crafting; Create/CBC | Actual input fuzes/payloads retained |
| `client` | Shared model rendering, sounds, tooltips, cluster predicates, placement adapter | Client; Minecraft/Create/Catnip | Existing renderer/ghost lifecycle reused |
| `client.ponder` | Small launcher construction tutorial and registration | Client; Ponder | Native scene API, introductory scope only |
| `compat.jade`, JEI plugin | Dependency-discovered presentation/recipe integration | Optional client | No common bootstrap load of optional UI classes |
| `compat.radars`, rocket Radars adapters | Observation, jamming, chaff, RWR, native mount control | Optional; public Radars 5 APIs | EA version-sensitive; no Kaboom internals |
| `mixin` | Narrow lifecycle/ABI/renderer/recipe hooks and optional gating | Common or explicit client list | 72 retained; one unnecessary owned-target Mixin removed |
| Resource JSON | Registries' models, recipes, tags, loot, language, projectile properties | Data/resources | JSON parses; ID wrappers intentionally retained |
| `build.gradle` | Dependencies, resource expansion, model generation, dev runs | Build | Version properties authoritative; verbose exports opt-in |
| `dev/` | Opt-in in-memory/server/client/compat checks and asset conversion tools | Development only | Excluded from release artifact |
| `docs`, `art_reference` | Design history and deliberate Blockbench/reference exports | Documentation only | Retained user work; not runtime assets |
| `libs` | Local publisher binaries | Development inputs | Git tracking removed; hashes/source manifest retained |

No custom access-transformer or packet transport framework was introduced.
Generated launcher resources remain build outputs; source resources are not
blanket-ignored. Gradle wrapper remains tracked.

## C. Dead code removed

- Removed `BigRocketPayloadMixin` and its configuration entry. Its three targets
  belong to this project, so ordinary factory calls provide the same behavior.
- Removed 16 unused imports across seven Java files.
- Removed obsolete dependency-image/model extraction from the module generator,
  including its ZIP/image parsing and unused spring texture binding.
- Kept legacy launcher registry IDs and necessary recipe/resource entry points;
  no migration support was removed based on a text-only usage search.
- Kept old CBC constructor ABI bridges: some normal detonation paths are
  intercepted earlier, but deleting a binary-compatibility bridge without full
  alternate-path evidence would be unsafe.

## D. Duplicate logic removed

- Bootstrap rocket detection now delegates to `RocketGroundPlacement.isRocket`.
- Large factories call the existing `RocketBalance.configureProjectile` exactly
  once. Fifty-four new assertions exercise AP/HE/HEAT base, double-fuel and
  double-payload variants, including mass to detect accidental double scaling.
- Existing shared sources remain: RocketMounts/Geometry for transforms,
  RocketBalance for tier calculations, LauncherAssembly/SmallLauncherStructure
  for topology/roles, AmmunitionFuzes and ClusterPayload for native components.
- The 50 byte-identical resource groups found during inventory mostly represent
  required registry IDs, empty loot or parent wrappers. Equality of file bytes
  alone did not justify deleting their resource locations.

## E. Dependency API reuse

Placement uses Catnip `IPlacementHelper`, `PlacementOffset`, `PlacementClient`,
`GhostBlocks`, `GhostBlockParams` and its `GhostBlockRenderer` extension. Create
owns discovery, alpha/timing and ghost lifetime. The user-approved renderer
adapter calls the existing rocket item-model renderer for sub-block positions.
No Create renderer source was copied.

Placed rockets extend CBC `FuzedProjectileBlock` / `FuzedBlockEntity`; native
fuze components and callbacks remain in use. CBCAT owns launcher storage and
projectile lifecycle. Native CBC ballistic behavior is used after fuel depletion.
Create mechanical crafting performs recipe matching/assembly, with scoped
component-preservation hooks. Minecraft tracked entity data carries readiness.
Sable public pose/velocity APIs and Radars observation/EW APIs remain the bridges.
The direct factory simplification removes interception, not dependency behavior.

## F. Remaining custom systems

| System | Why retained / native API boundary | Possible later simplification |
|---|---|---|
| RocketGeometry, RocketMounts, Footprint | Dependencies do not define this branch's compact stacks, exact launcher tubes or multi-cell placed rocket footprint. One source feeds render/placement/interaction. | Upstream geometry contract could replace adapters if equivalent |
| MountedRocketStorage / transfer ownership | Native buffers exist, but per-slot component-preserving views and cross-sublevel ownership protection are needed. Storage is not duplicated. | Prefer upstream slot/transaction API if added |
| LauncherAssembly / readiness / modules | Side lanes, group arming, module rules and round-robin scheduling are this update's approved mechanics. | Keep derived lane cache; avoid another manager |
| Rocket flight/clearance/seeker | Powered flight, launch clearance and autonomous rocket guidance are project behavior; native projectile angles, ballistics and sensor APIs remain owners where applicable. | Small/medium hooks share algorithms despite different upstream parents |
| Component-aware crafting / cluster payload | Recipes must transfer eight actual secondary fuzes and keep the main fuze separate. Generic ingredient matching cannot do this alone. | Adopt upstream data-transfer hooks if they preserve identical contracts |
| RocketGhostRenderer | Stock BlockState ghost cannot represent the held rocket model at each tube/stack offset. The approved adapter extends native ghost rendering and reuses RocketModelRenderer. | Replace only if Catnip gains an equivalent item-instance preview |
| Tooltips / goggles adapters | Project-specific stats/arming need presentation; numeric data comes from gameplay APIs/config, not copied prose constants. | Continue using native tooltip/goggles entry points |
| Optional Radars Traverse adapter | Native DirectCbcMountMotion bypasses CBC drive speed; approved integration scopes its cap multiplier while keeping acceleration/braking. | Public per-mount speed-cap hook would remove two version-sensitive Mixins |

## G. Performance and authoritative state

The audit did not claim unmeasured FPS gains. It removed unnecessary image
extraction during generation and made verbose Mixin logging/export opt-in.
Existing hot-path design was reviewed: cached shapes/render bounds, derived
assembly lanes, bounded seeker visibility/range checks and shared geometry.
No full-world scan or per-tick full-contraption snapshot was introduced.

Canonical owners: placed rocket list in RocketBlockEntity; launcher inventory in
CBCAT breech buffers; arming and scheduling in the assembled contraption;
cooldown in native breeches; yaw/pitch in native entities; fuze/payload data in
native item components. Client snapshots, render interpolation and scoped
ThreadLocals are not competing save owners. Arming uses tracked-data equality
and periodic corrections; clients do not decide readiness.

NBT review retained clamped arming/scheduling state, component payloads, optional
seeker state and legacy reads. Derived lane geometry is reconstructed. Native
BE state is flushed before disassembly. No save migration was introduced.
Fuel exhaustion retains ballistic flight; launch clearance gates maneuvering;
carrier velocity and Sable recoil suppression were not rebalanced.

## H. Client/server separation

Client renderers, UI and model work remain in the client package or the 15-entry
client Mixin list. Optional Jade/JEI callbacks are dependency-discovered.
Server insertion resolves and validates its own target through existing storage
and placement logic; preview candidates cannot authorize placement. Fuzes,
mechanical-arm loading, per-rocket identity and serialization remain on their
existing paths. Both real dedicated-server profiles reached a running world.

## I. Optional dependency safety

`CbcatFixMixinPlugin` gates Sable transfer, four Radars hooks and the CBCAT JEI
hook before target resolution. Sable adapters isolate optional classes behind
presence checks. Kaboom internals are not imported. Minimal runtime excludes
Sable, Radars, Kaboom, Aeronautics, Jade and JEI; full runtime includes them.

Tested combinations: **A (base)** and **G (full set)** on real client/server.
Individual B–F combinations were not separately launched. Static presence gates
and full/minimal runs support optional safety for these inputs, but do not prove
all mixed versions or interactions. No wider compatibility guarantee is made.

## J. Dependency version matrix

See the [complete version/date/source/API/migration matrix](prerelease-dependencies-20261012.md).
It includes every explicitly declared mod/library, build plugin, wrapper and
toolchain. Raw official query evidence is retained locally under
`build/prerelease-20261011/`; dependency JAR hashes are in the tracked manifest.

## K. Dependency recommendations

Approved Sable 2.0.5 → **2.0.6** is applied and tested. Keep Create 6.0.10,
CBC 5.11.7, CBCAT 0.1.4c, Jade 15.10.6 and RPL 2.1.2. Public Radars 0.4.x
is not an upgrade to the required Radars 5 EA API.

NeoForge 21.1.259, Ponder 1.0.87, JEI 19.57.0.451 and Aeronautics 1.3.2 are
separate upgrade candidates. They were not silently adopted. NeoForge changes
Mixin/loader versions; Aeronautics changes mass behavior; JEI has an open
upstream crash report. Latest EA Radars/Kaboom and a newer Registrate baseline
could not be established publicly. These limitations are recorded explicitly.

## L. File, resource and license cleanup

- Ten tracked dependency JARs removed from the index; local copies preserved.
  `.gitignore` now permits only the wrapper JAR. Added `libs/README.md` and
  exact SHA-512 manifest for the ten current local dependency inputs.
- New MIT LICENSE names **DrLuckily**. LICENSE and THIRD_PARTY_NOTICES are
  packaged in the release JAR; third-party resources are not relicensed.
- New original procedural module marks replace extracted dependency contours.
  Bodies, apertures and symmetric seams remain unchanged.
- Fixed the active steel rail's missing particle texture through two small
  model wrappers, using its existing steel face texture.
- Preserved intentional reference exports, historical docs and legacy IDs.
- Removed a personal absolute filesystem path from a historical document.
- Source/JSON scan found no credential-pattern matches or invalid JSON.
  This is a scoped secret-pattern check, not a claim of a complete security audit.

## M. Build, client and server validation

All runs used Java 21.0.12 and the existing worlds. **No new test world was
created.** Runtime fixtures were in-memory or client-only and restored by the
harness. Tests are opt-in development sources, not release content.

Commands used the wrapper with `--offline --console=plain --no-daemon
--project-cache-dir build/architecture-cache -I build/architecture.init.gradle`.
The local init file isolates build output under `build/architecture-20260915`
and enables compiler warnings. This keeps `clean` away from existing worlds.

| Check | Result / evidence log in `build/prerelease-20261011` |
|---|---|
| `clean build` without audit source injection | PASS, `final-release-build.log`, 10 seconds; Gradle `test` has NO-SOURCE, so this alone is not the runtime-test result |
| Full `runServer checkSeekerState checkRadarObservation` | PASS, `final-full-server.log`; **2931 assertions**, plus seeker persistence and Radars observation checks |
| Minimal `runServer -PminimalRuntime` | PASS, `final-minimal-server.log`; **2904 assertions**, no optional mods |
| Minimal menu | PASS, `audit-minimal-menu.log`; title screen rendered 120 frames before latest factory-only simplification |
| Minimal existing world and Ponder | PASS, `audit-minimal-world.log`; complete Ponder playback |
| Full existing world with Sable 2.0.6 | PASS, `final-full-world.log`; complete Ponder playback and model/tooltip checks after factory change |
| Latest minimal world + placement assistance | PASS, `final-minimal-hints.log`; exact small tube, medium stack, normal Create shaft, no empty-ground hint, no occupied-slot hint |
| JSON / diff whitespace / artifact inspection | PASS; `git diff --check`, parsed resources, release ZIP checks |

Client checks covered eight module/mould models, seven native ammunition models,
14 renderer calls, 102 unfinished tooltip suppressions, 12 launcher summaries,
12 cluster variant/roundtrips, 53 ammunition descriptions and seven cartridge
forwarding paths. The native model review image was inspected visually.

Historical baseline artifact (superseded by the Radars update; current hash is in
the upgrade report): `build/architecture-20260915/libs/cbcatfix-1.1.0.jar`,
961086 bytes, 868 ZIP entries. SHA-256:
`7810d1c075126296dfb3a6156680a122da16086c3538f9dfa67b7148e59ef1f8`.
The JAR was repackaged after the CBCAT permission confirmation to update its
notice; `confirmed-provenance-jar.log` records the successful packaging run.
No audit/test classes, embedded dependency JARs, reference exports or removed
BigRocketPayloadMixin are present. Metadata resolves version 1.1.0 and DrLuckily.

Warnings retained: five compiler deprecation/removal warnings: two `setBlockState`
uses for render-cache invalidation, the explicit mod-bus annotation and its enum,
and `hasChunkAt` in launch-time designation capture. These still compile and run
on the pinned versions; warning suppression was not used. Also retained:
development refmap warnings; CBC's absent optional
FramedBlocks target; expected filtered legacy recipes; offline authentication
key retrieval and vanilla development asset/sound messages. Full client also
logs upstream missing textures for legacy bronze/cast-iron launcher models,
built-up nethersteel barrel, twin recoil spring and Kaboom fluid warhead.
The active steel rail particle reference fixed here no longer appears in that
missing-texture set. This report does **not** describe the runtime logs as clean.
Moving-Sable placement visuals and extended multiplayer/load testing remain
manual checks; startup/contract tests do not replace them.

## N. Release changelog

[CHANGELOG.md](../CHANGELOG.md) has an Unreleased section with Added, Changed,
Fixed, Compatibility, Performance, Internal and Known Issues. README documents
installation, tested baselines, optional integrations, length/arming rules,
existing-pile hints, Ponder and build requirements. Version recommendation is
1.2.0; no version bump or publication was performed.

## O. Concrete release blockers

The earlier baseline has no outstanding provenance blockers. The subsequently
updated full development set has a concrete blocker: **Kaboom 1.0.0-EA is
binary-incompatible with Radars 3.1.1**. The user chose to supply newer Kaboom.

On 2026-10-12 DrLuckily confirmed permission for all CBCAT-origin material used
in this project, and then confirmed that the nine older projectile PNGs and
missile recording also originate from CBCAT. Both the model/reference permission
and the remaining PNG/audio attribution blockers are closed on those explicit
confirmations. See [THIRD_PARTY_NOTICES](../THIRD_PARTY_NOTICES.md).

Known upstream missing-texture warnings remain documented in section M. Core
branch model checks passed; these warnings are not presented as a new crash or
as proof that this branch caused the affected upstream assets to be missing.

Unrelated textures and legacy compatibility remain unchanged. Newer dependency
availability alone is not a blocker for the tested baseline.

## P. Final release-readiness status

The earlier baseline passed its listed tests. Resource provenance and permission
are confirmed by DrLuckily. After the Radars upgrade, full-set readiness awaits
a compatible Kaboom JAR and a new combined runtime test. Version 1.2.0 remains a
recommendation, not an applied bump. No merge or push has occurred.

NOT RELEASE READY
