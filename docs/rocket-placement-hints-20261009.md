# Rocket placement assistance — 2026-10-09

## A. Installed Create system

Inspected Create 1.21.1-6.0.10 and Catnip sources bundled with Ponder 1.0.82+mc1.21.1.
- CogwheelBlockItem registers SmallCogHelper/LargeCogHelper and integrated cog helpers in its constructor via PlacementHelpers.register.
- ShaftBlock registers ShaftBlock.PlacementHelper in its static initializer.
- IPlacementHelper filters held item and hit block, then returns PlacementOffset. PlacementClient calls getOffset, skips unsuccessful candidates, calls renderAt, and owns the directional indicator and fade.
- PlacementHelpers/IPlacementHelper/PlacementOffset are the shared abstraction; PlacementClient and ghost rendering run on the client. Native cog/shaft interactions resolve the helper again and call PlacementOffset.placeInWorld for block placement.
- Native placeInWorld validates replaceability, server interaction permission and platform placement hooks. Rockets retain their own existing insertion/ground paths instead of calling that block-placement method.
- Default GhostBlockRenderer consumes only BlockState and integer BlockPos. It cannot represent a rocket ItemStack, fuze, or sub-block slot transform.
- The user approved a small renderer adapter (option B) through the public IPlacementHelper.renderAt and GhostBlocks.showGhost extension points. No Create mixin, copied renderer, independent HUD, tick handler or arrow implementation was added.
- PonderClient.onTick calls PlacementClient.tick BEFORE GhostBlocks.tickGhosts. Ghost entries must use TTL 1, matching the standard API; TTL 0 expires before rendering.

## B. Launcher candidates

RocketPlacementCandidate uses MountedRocketInteraction.findBreech and viewed(..., true), which resolves the same nearest exact slot and Sable-aware input ray as actual right click.
MountedRocketItemHandler.insertItem(slot, held, true) performs the existing read-only validation: compatible tier, sufficient tube length, capacity/occupied slot, live world-owned BE and transfer lock.
No fallback to another free slot occurs when the selected slot is occupied.
The preview uses RocketMounts.Transform (also used by mounted rendering, selection and launch geometry). No new slot table.
Assembled CBC launchers are not represented as loadable block targets; detached/removed BEs fail existing admission rules.
Invalid candidates return PlacementOffset.fail. Ghost rendering also revalidates the current hit each frame so a filled/changed target is not shown as valid.

## C. Ground and stack candidates

Existing roots and extension blocks resolve through RocketTargetResolver.findRoot.
RocketBlockEntity.canAddRocket is the original admission predicate extracted for shared use by addRocket and the preview: tier consistency, capacity and independent-rocket restriction.
The next stack center is RocketGeometry.bodyCenter(tier, facing, storedCount, independent), exactly the same source as RocketBlockEntityRenderer. Medium preserves its compact 2x2 layout.
New ground placement requires the existing top-face interaction, replaceable root and RocketFootprint.canPlace checks, shared through RocketGroundPlacement.canPlaceNew.
The existing rules are preserved, including their existing support/footprint semantics; this feature adds no new physics rule.

## D. Client/server responsibilities

Only ClientSetup registers RocketPlacementHelper. Create controls discovery and rendering.
No preview packet or client-selected authoritative slot was introduced.
MountedRocketInteraction, MountedRocketItemHandler and RocketGroundPlacement still validate and mutate on the server through their original paths.
Item identities/components, serialization, mechanical-arm loading, fuze handling and removal restrictions are unchanged.
Create's native Shift-to-disable-assistance behavior is retained.
The native arrow targets the block origin; the rocket model itself marks the exact fractional slot. Rendering uses normal depth occlusion, not an X-ray overlay.

## E. Reuse and adapter

Reused: PlacementHelpers, IPlacementHelper, PlacementOffset, PlacementClient animation/indicator, GhostBlocks lifecycle/breathing opacity, Catnip ColoringVertexConsumer, existing RocketModelRenderer, RocketMounts and RocketGeometry.
Custom code is limited to candidate resolution, helper registration and an ItemStack render adapter. It preserves the full model scale rather than applying the native block ghost's 0.85 scale.
Sable remains optional behind an isolated class. Its SublevelRenderOffsetHelper supplies the moving structure render pose; existing RocketViewRay supplies input transforms.
Jade, Radars and Kaboom are not involved.

## F. Validation

Java 21; offline Gradle with the repository's existing architecture/init fixtures.

Server/build:
    ./gradlew.bat runServer build -PminimalRuntime -PrunDirectory=build/architecture-server-minimal --offline --console=plain --no-daemon --project-cache-dir build/architecture-cache -I build/architecture.init.gradle -I dev/assembly-checks/checks.init.gradle

Result: BUILD SUCCESSFUL; 902 new rocket hint assertions; 2850 total assembly/audit assertions.
Checks cover all three sizes and six launcher directions, every slot, real insertion following simulation, unchanged NBT/item counts, occupied/wrong-size/wrong-item/removed cases, stack limits, all horizontal stack directions and blocked footprints.

Client/build:
    ./gradlew.bat runClient build -PminimalRuntime -PrunDirectory=build/architecture-client-minimal -PquickPlayWorld=Architecture --offline --console=plain --no-daemon --project-cache-dir build/architecture-cache -I build/architecture.init.gradle -I dev/assembly-checks/checks.init.gradle -I build/rocket-hints.init.gradle

Client result: BUILD SUCCESSFUL; all five probe phases passed, including native ghost lifetime and invalid-slot suppression. Screenshots were inspected after correcting ghost TTL.

For reproduction, the same hint-probe init script is saved at dev/assembly-checks/hints.init.gradle.

The existing Architecture world is used. No new world is created. The opt-in RocketHintClientProbe creates temporary client-only block fixtures, restores them and shuts down. It does not send block edits to the server.
Screenshots: build/architecture-client-minimal/rocket-hint-{small-slot,medium-stack,ground,create-shaft,occupied-slot}.png.
Audit sources are excluded from the distributable jar.

Remaining manual coverage: moving Sable structures, multiplayer latency, additional resource packs/rendering mods. The minimal client run does not establish those integrations visually.

## 2026-10-11 scope correction

Per user request, empty-ground hints are disabled. Candidate resolution now stops unless the hit resolves to an existing launcher or rocket pile. Actual first-rocket ground placement remains unchanged.
The existing client probe now requires no candidate and zero indicator alpha for its ground phase, while retaining valid launcher/pile/native-shaft checks.
