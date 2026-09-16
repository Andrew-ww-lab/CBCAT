# Git hygiene audit — 2026-09-16

This records the hygiene audit before the subsequent 1.1.0 live release. Its
version, branch, staging, and publication status describe that audit snapshot.
For the published release, see [CHANGELOG.md](../CHANGELOG.md) and
[the current workflow](version-control.md).

## A. Repository status

Repository: `CBCAT` (the current Sable Rocket mod workspace).
Branch: `codex/fix-recipes-block-drops`.
Remote: `origin`, https://github.com/Andrew-ww-lab/CBCAT.git.

The repository initially tracked build/cache/runtime data and had extensive
uncommitted gameplay and resource changes, including new source files and source
deletions. The initial index had no staged changes. Existing changes were
preserved and staged together with this cleanup so the proposed index could be
built as a complete project. Nothing was committed or pushed. The final tree is
intentionally not clean: it contains staged changes, with no unstaged or untracked
files after validation.

An empty index lock dated September 14 blocked Git writes. It was removed after
checking its age and confirming no Git process was running. The sandbox account
needed command-scoped `safe.directory`; no global trust exception was installed.

## B. Ignore rules and attributes

`.gitignore` now covers Gradle/build output, Java crash/profiling output, root
Minecraft runtime/world directories, Sable native caches, IDE metadata, OS junk,
temporary files, Python caches, local properties/environment files, and built JARs.
Runtime rules are root-scoped to avoid hiding source fixtures or resources.
Custom run directories should live below `run/`, `runs/`, or `build/`.

Exceptions retain the Gradle wrapper JAR and intentional `libs/*.jar`
dependencies; `libs/cbcatfix-*.jar` remains ignored. The old blanket `*.gz` rule
was removed because runtime directories already cover compressed logs and a
blanket compression rule could hide source assets. `src/generated/` is not
ignored.

`.gitattributes` retains automatic text normalization, specifies LF for shell
scripts/`gradlew`, CRLF for batch files, and binary treatment for PNG/OGG/JAR.
`gradlew` now has executable mode `100755` in the index.

## C. Removed from tracking

| Path | Tracked files removed |
| --- | ---: |
| `.gradle/` | 16 |
| `.idea/` | 10 |
| `.sable/` | 1 |
| `build/` | 518 |
| `run/` | 669 |
| `libs/cbcatfix-1.0.0.jar` | 1 |
| **Total** | **1,215** |

Removal used `git rm --cached`; local copies were retained. Source deletions
already present before the audit are separately staged existing work, not part
of these 1,215 removals. Runtime data included worlds, screenshots, config,
user caches, logs, and exported mixin classes. Sable's native library is
recreated from the compressed native bundle inside its vendored dependency.

## D. Kept tracked and large-file review

The proposed index includes 127 Java source files and 505 main resource files,
including 60 model files, 9 textures, the current sound asset, 78 recipe files,
2 language files, mixin configuration, and NeoForge metadata. Build scripts,
Gradle properties, wrapper scripts/JAR/properties, dev tooling/tests, and project
documentation are included. No access transformer or CI configuration is present.
No README or LICENSE file was present; none was invented.

Eight intentional third-party JARs remain in `libs/`. The largest are Aeronautics
(33.1 MB), Create (19.1 MB), and Sable (12.9 MB). Generated NeoForge/Minecraft
JARs up to 32.4 MB and world region files up to 7.9 MB were removed from tracking.
The existing third-party Registrate JAR is retained as a vendored dependency.

No obvious credentials were found by the current text-file pattern scan.
Personal absolute paths in historical developer documentation were replaced
with relative links or placeholders. This was an obvious-secret check, not a
complete history/security scan. Previously committed runtime personal data
remains in history; no history rewrite was performed.

## E. Versioning and generated resources

`gradle.properties: mod_version=1.0.0` is now authoritative. Gradle project version
reads it and resource processing expands it into `neoforge.mods.toml`, with an
explicit task input so version changes invalidate resource processing. The
release version was not bumped. Future releases edit this one value using SemVer;
pre-release suffixes need no separate version system.

Launcher resources and opt-in test structures are generated under `build/`.
Their generating tasks are dependencies of resource processing. There is no
`src/generated/` or top-level `generated/` directory to classify. Existing source
assets remain tracked. `mavenLocal()` was removed to avoid machine-specific
dependency shadowing; declared remote repositories and vendored JARs suffice.

## F. Remote and branch status

The current branch tracks `origin/beta/rocket_update`; `main` tracks
`origin/main`. A read-only `git ls-remote` confirmed both branches exist and match
the locally recorded commits. The current branch and upstream point to
`5a48c4b818b7098119850912eefc9803f3926040` at audit time.

Repository-local `push.default=upstream` enables normal `git push` despite the
different local/upstream names. Remote URLs and branch names were unchanged.
Commit identity is configured. Push destination is ready; write authorization
and branch protections were not tested by pushing.

## G. Build validation

Both builds passed using `.\gradlew.bat build --no-daemon --console=plain`:

1. Normal workspace build: successful in 5 seconds.
2. Fresh export of the staged Git index under `build/git-audit/checkout`:
   successful in 11 seconds, all five project tasks executed, Java recompiled.

The second build had no copied workspace caches, output, IDE settings, runtime
directories, or ignored source files. It used the existing external Gradle
dependency cache (`GRADLE_USER_HOME`), so this proves tracked-input completeness,
not a fully cold network build or byte-for-byte reproducibility.

Both JARs contain expanded version `1.0.0` and the generated launcher model.
Compilation reported existing deprecated API usage. `test NO-SOURCE`: no JUnit
tests ran, and no gameplay/world tests were launched.

No ignored files remain tracked. Ignore probes preserve source/generated
resource paths, dependency JARs, and the wrapper. Build output did not add
untracked changes. Byte hashes confirmed that all existing Java and asset files
were unchanged by this audit; only NeoForge version metadata changed under
`src/`. Final staged whitespace validation passed after removing two extra EOF
blank lines in developer reports.

Local validation logs: `build/git-audit/build.log` and
`build/git-audit/index-build.log` (intentionally ignored).

## H. Daily workflow

```powershell
git status
git diff
.\gradlew.bat build --no-daemon
git add .
git diff --cached --stat
git diff --cached
git status
git commit -m "Describe the change"
git pull --rebase
git push
```

Review the existing staged gameplay changes as well as this hygiene work before
the first commit. See [version-control.md](version-control.md) for the versioning,
dependency, runtime-directory, and upstream conventions.
