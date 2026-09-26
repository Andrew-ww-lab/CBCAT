# Modpack loading-cycle fix — 2026-09-23

Inspected the supplied `Modpack 1.0.0.mrpack`: 223 enabled downloadable mod JARs (SHA-512 verified or matched to local JAR hashes), three embedded override JARs, and their declared nested JarJar metadata. Disabled JAR entries were excluded. No third-party code was executed during metadata inspection. The metadata scan collected 308 records / 279 distinct mod IDs; duplicate bundled-library versions were conservatively included for ordering analysis.

Confirmed cycle (arrows mean must load before):

`cbcatfix -> createbigcannons -> cbcmodernwarfare -> create_radar -> cbcatfix`

Evidence from exact JAR manifests:

- CBCAT Fix 1.1.0: `createbigcannons`, ordering `BEFORE`.
- CBC Neo Warfare 0.0.6v: `createbigcannons`, ordering `AFTER`.
- Radars 5.0-EA-2.0.2: `cbcmodernwarfare`, ordering `AFTER`.
- CBCAT Fix 1.1.0: `create_radar`, ordering `AFTER`.

Fix: change only CBCAT Fix's required CBC dependency ordering from `BEFORE` to `AFTER`. CBC remains required; no optional dependencies become required. The recoil compatibility patch uses Mixin class transformation, not early mod construction. Munition handlers register during common setup after registries, so they do not need CBCAT Fix constructed before CBC.

Validation:

- Original export metadata: one cyclic strongly connected group, exactly the four IDs above.
- Substitute the fixed CBCAT Fix manifest: zero cyclic groups in the inspected graph.
- `gradlew.bat build --no-daemon --console=plain`: passed.
- `gradlew.bat runServer -PrunDirectory=build/architecture-server-dev -I build/modpack-audit/validate.init.gradle --no-daemon --console=plain`: reached `Done` at 22:22:37 with the exact embedded CBC Neo Warfare JAR, Radars, CBCAT Fix and the normal development dependencies. Existing development world used; no new world created. Stopped with the server `stop` command.
- Packaged fixed JAR: `build/libs/cbcatfix-1.1.0-load-order-fix.jar`; same internal mod ID/version. Replace the old CBCAT Fix JAR rather than installing both.

Limits: runtime smoke test uses the project's NeoForge 21.1.228 and a focused mod set, not the entire supplied pack on NeoForge 21.1.248. The earlier log lists six cycle groups; this export's manifests reproduce one group. Thus the confirmed cycle is fixed, but this does not certify every earlier modpack configuration or all gameplay. Existing unrelated Kaboom loot and unavailable authentication-service errors appear in the successful server log.

Local audit data and scripts are under `build/modpack-audit/` (not packaged or committed).
