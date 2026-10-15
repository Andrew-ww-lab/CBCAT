# Module marking provenance

Updated 2026-10-11.

The Rapid Cycling and Traverse Gear module markings are original procedural
geometry defined in the generateLauncherModel task in build.gradle.

- Traverse uses two radial gear symbols with eight and six teeth and circular
  hubs. Their outlines are calculated from radii and angles.
- Rapid uses a folded wire between two end bars, calculated from line segments.
- Neither symbol reads, traces, samples or extracts a Create/CBC image or model.
  The previous dependency-JAR silhouette extraction has been removed.
- Existing module bodies, tube apertures, UV mapping and symmetric panel lines
  are retained. The change affects only which flat surface cells carry a mark.
- Existing dependency textures are referenced by resource identifier at runtime;
  they are not copied into the generated module resources.

This resolves the identified copied-contour issue only. It does not establish
licensing provenance for other artwork, sounds, vendored dependencies or the
project as a whole. The subsequent CBCAT permission and source confirmations
are recorded in [THIRD_PARTY_NOTICES.md](../THIRD_PARTY_NOTICES.md).
