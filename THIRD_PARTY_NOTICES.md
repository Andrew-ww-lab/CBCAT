# Third-party notices and resource provenance

Audited 2026-10-12. The root MIT license applies to original CBCAT Fix code
owned by DrLuckily. It does not relicense dependencies or pre-existing artwork.
This file records provenance findings and permission confirmed by the project
owner. The resource-provenance release gate is closed on that confirmation.

## Dependencies

Dependencies are loaded separately, not bundled into the CBCAT Fix release JAR.
Local `libs/*.jar` files are developer inputs; obtain them from their publishers.
See [the dependency manifest](libs/dependencies.json) for exact hashes and sources.

| Project | License/provenance observation | Official source |
|---|---|---|
| Create | Code MIT; supplied license reserves rights to assets. No extracted gear contour remains in the module generator. | [Create](https://github.com/Creators-of-Create/Create) |
| Create Big Cannons | Code MIT; assets have separate CC BY-NC-SA terms. Runtime texture references do not embed those texture files. | [CBC](https://github.com/Cannoneers-of-Create/CreateBigCannons) |
| CBC: Advanced Technologies | DrLuckily confirmed permission for all CBCAT-origin material used by this project on 2026-10-12. The conflicting JAR/page license labels are therefore not a release blocker for that material. This confirmation does not relicense CBCAT under this project's MIT license. | [CBCAT](https://modrinth.com/mod/create-big-cannons-advanced-technologies) |
| Sable | PolyForm Shield; separately installed optional dependency. | [Sable](https://github.com/ryanhcode/sable) |
| Create: Radars | Publisher-specific source license; separately installed optional dependency. | [Radars](https://github.com/Arsenalists-of-Create/Create-Radar) |
| Create: Kaboom | Early-access distribution has publisher-specific terms; public page uses ARR. No dependency code is bundled. | [Kaboom](https://modrinth.com/mod/create-kaboom) |
| Create Aeronautics | Publisher-specific license, separate asset restrictions; development runtime only. | [Aeronautics](https://github.com/Creators-of-Aeronautics/Simulated-Project) |
| JEI | MIT; optional compile/runtime integration. | [JEI](https://github.com/mezz/JustEnoughItems) |
| Jade | Publisher metadata: CC BY-NC-SA 4.0; separately installed optional integration. | [Jade](https://modrinth.com/mod/jade) |
| RitchiesProjectileLib | MIT; separately installed CBC dependency. | [RPL](https://github.com/Wagers-of-Industrial-Warfare/RitchiesProjectileLib) |

## Project resources

- Module gear and spring markings are original mathematical geometry, with no
  traced dependency silhouettes. [Generation details](docs/module-marking-provenance.md).
- Models reference textures in `create`, `createbigcannons` and `cbc_at` at runtime.
  Referencing a texture is different from redistributing its PNG; model geometry
  and derived artwork retain their own provenance. CBCAT-origin material is
  covered by the project owner's permission confirmation above.
- The nine PNGs in `src/main/resources/assets/cbcatfix/textures/block/projectile/`
  originate from CBCAT, as confirmed by DrLuckily on 2026-10-12, and are covered
  by the confirmed permission. They are attributed to CBCAT, not claimed as
  original CBCAT Fix artwork.
- `src/main/resources/assets/cbcatfix/sounds/missile_launch_mono.ogg` is a downmix
  of a CBCAT recording, as confirmed by DrLuckily on 2026-10-12. Its use is covered
  by the same permission. The development conversion changes the channel layout
  for positional playback; it does not transfer authorship of the recording.
- CBCAT-derived launcher/cluster model overrides and reference exports under
  `art_reference/` are covered by DrLuckily's permission confirmation.
  Reference exports are not packaged into the release JAR.

These permissions and source attributions are recorded from DrLuckily's explicit
confirmation. No permission has been inferred merely from an MIT label in this
project's loader metadata. Third-party materials retain their own terms.
