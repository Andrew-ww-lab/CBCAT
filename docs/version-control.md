# Building and version control

Use the checked-in Gradle 8.9 wrapper with a Java 21 toolchain installed:

```powershell
.\gradlew.bat build --no-daemon
```

On Unix, use `./gradlew build --no-daemon`. Gradle needs access to the declared
remote repositories on a fresh machine. Third-party JARs in `libs/` are intentional
vendored dependencies; keep them tracked. `libs/cbcatfix-*.jar` is an old local
build, is not a dependency, and must not be committed. The build does not resolve
dependencies from `mavenLocal()`.

Launcher model resources are generated from `RocketMounts.java` into
`build/generated/launcherResources` before resource processing. Opt-in
`-ProcketTests` generates its structure under `build/generated/rocketTests`.
Neither output is committed. There is currently no `src/generated/` datagen
directory; review future datagen conventions before adding an ignore rule for it.
Source assets, recipes, mixins, and NeoForge metadata under `src/` stay tracked.

Keep runtime directories under `run/`, `runs/`, or `build/`, including custom
`-PrunDirectory` values. These locations, local worlds, logs, Gradle output, IDE
settings, Sable native caches, and disabled development mods are ignored. Do not
force-add them. The Gradle wrapper JAR and intentional `libs/` dependencies are
explicit exceptions to the JAR ignore rule.

## Versions

`gradle.properties` owns the release version through `mod_version` (currently
`1.1.0`, the live release). Gradle uses it for the project/artifact version and expands it into
`META-INF/neoforge.mods.toml`. Change only that property for a release.

Use `MAJOR.MINOR.PATCH`; pre-release values such as `1.1.0-dev` or `1.1.0-beta.1`
are supported by the same mechanism. Minecraft, NeoForge, and dependency versions are separate compatibility
settings, not the mod release version.

## Daily workflow

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

Review source deletions and new assets before committing. There are currently no
JUnit test sources, so `test NO-SOURCE` is not a gameplay test pass.

The live release is on `main`, which tracks `origin/main`. The previous main
commit is preserved unchanged on `codex/main-before-live-1.1.0`.
The existing `codex/fix-recipes-block-drops` branch tracks
`origin/beta/rocket_update`. Repository-local `push.default=upstream` makes plain
`git push` use that existing destination despite the different branch names.
Before pushing,
check `git branch -vv`; this local push setting is not transferred by cloning.
Without it, the explicit command when working on `codex/fix-recipes-block-drops` is:

```powershell
git push origin HEAD:beta/rocket_update
```

For a newly created branch that should have its own remote branch, explicitly
establish the intended upstream using `git push -u origin <branch>`.

Removing files from the index does not purge old commits or shrink existing
history. Previously committed runtime usernames, world data, and logs remain in
history. History cleanup would be a separate, coordinated operation.
