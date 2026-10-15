# Local development dependencies

JARs are intentionally ignored and are not redistributed by this repository.
Obtain the exact versions listed in [dependencies.json](dependencies.json) from
their publishers and place them here. SHA-512 hashes identify the audited inputs.
Early-access Radars/Kaboom artifacts require access through their publisher;
public releases with different version schemes are not drop-in replacements.

Create, CBC, CBCAT and RPL are mandatory local inputs. Sable, Radars and JEI are
also required to compile their optional adapters; this does not make them runtime
requirements for players. Aeronautics and Kaboom are full development runtime
inputs. Registrate's local JAR supplies the declared coordinate through flatDir.
Other library coordinates are resolved through the repositories in build.gradle.

The old Sable 2.0.5 JAR may remain as a local backup, but the build selects only
2.0.6 via sable_version. No wildcard imports all JARs from this directory.
