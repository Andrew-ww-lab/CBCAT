# Changelog

## Unreleased — update branch

### Added

- Small launcher Ponder introduction and Shift summaries for launchers,
  modules and ammunition.
- Individual rocket slot loading, compact placed rocket stacks and Create
  placement assistance for existing launchers and piles.
- Rapid Cycling and Traverse modules with casting, drilling and finishing.
- Launcher side-lane assembly, shared arming and sequential firing.

### Changed

- Maximum Small / Medium / Large launcher lengths are 3 / 5 / 8 blocks,
  including the breech; preparation lasts 20 / 40 / 100 ticks.
- Medium and Large recipes use steel structural materials and mechanisms.
- Cluster recipes consume eight fuzed submunitions and retain their fuze data.
- Module markings use original procedural gear and spring symbols instead of
  contours extracted from dependency artwork.
- Placement hints no longer appear over empty ground.

### Fixed

- Ammunition component transfer, cluster payload visuals and ordinary HE
  rocket behavior.
- Exact slot rendering and interaction, launcher state preservation through
  disassembly, and invalid placement preview suppression.
- CBC dependency ordering that could close an optional-mod loading cycle.
- Corrected the steel launcher rail's missing particle texture reference.

### Compatibility

- Development Radars baseline updated to the user-supplied 5.0-EA-3.1.1.
  Adapted weapon context, contact snapshots and airborne RWR type identifiers.
  Older Radars versions no longer satisfy the optional dependency minimum.
- Radars 5 electronic-warfare integration uses its public chaff, jamming and
  RWR APIs; Traverse adapts its powered aiming speed cap.
- Sable remains optional. Development compile/runtime baseline updated from
  2.0.5 to 2.0.6 for NeoForge 1.21.1.
- Jade and JEI integrations remain optional; Kaboom internals are not used.

### Performance

- Launcher geometry, stack shapes and stable render bounds use shared/cached
  sources. Arming corrections use vanilla tracked data rather than full
  contraption snapshots each tick.
- Verbose Mixin diagnostics and class exports are now opt-in for development.

### Internal

- Removed unused Java imports and consolidated rocket-item detection.
- Replaced the Mixin into our own large-rocket item factories with direct
  configuration calls, preserving one configuration pass per projectile.
- NeoForge and Sable build versions come from Gradle properties.
- Updated project description and issue tracker metadata; added build and
  installation documentation.
- Recorded DrLuckily's permission for CBCAT material, including the older
  projectile PNGs and launch recording, closing the resource-provenance gate.

### Known Issues

- Kaboom 1.0.0-EA calls the old Radars weapon-context API and crashes during
  weapon profile resolution with Radars 3.1.x. A compatible Kaboom update is pending.
- Known upstream missing-texture warnings and optional manual checks are
  documented in the pre-release audit report.
- Newer dependency versions other than the explicitly approved Sable update
  have not been adopted or qualified by this changelog.
- The final audit report distinguishes current test results from earlier runs.

## 1.1.0 — Live release — 2026-09-16

This is the live version of the Sable Rocket mod, published on `main`.

- Includes the accumulated rocket implementation, compatibility, balance,
  recipe, resource, and documentation changes from the development branch.
- Cleans up Git tracking of generated builds, caches, IDE files, and local
  Minecraft runtime data while preserving source assets and build dependencies.
- Uses `mod_version` in `gradle.properties` as the authoritative release version.

The previous `main` is preserved unchanged on `codex/main-before-live-1.1.0`.
The release is identified by tag `v1.1.0`.
