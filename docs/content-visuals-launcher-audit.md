# Content, visuals, automation and launcher audit — 2026-09-30

Audited installed Create 6.0.10, CBC 5.11.7 and CBCA 0.1.4c, plus the current working tree. This pass preserves the earlier uncommitted assembly/fuze work. No launcher balance values were changed. No new machines, recipe serializers, projectile entities or texture/model assets were added.

## A. Large HE rocket

Previously the three recipes used `cbcatfix:heavy_he_shell`, and the registered long-range/double-payload defaults also named that item. `BigHERocketProjectile` constructed a CBCA `HA_HEProjectile` to obtain direct-hit damage and ballistics. Its explosion already used normal CBC HE properties; it did **not** use our heavy HE shell's 8/10 explosion powers. The mixed sources were the actual problem.

All three recipes now take **`createbigcannons:he_shell`**: one in long-range/lightweight, two in double-payload. The variant default components and lightweight recipe's explicit projectile component agree. Fuzed inputs remain rejected. This supersedes the heavy-ingredient alignment in the previous assembly audit.

`BigHERocketProjectile.getDamageProperties()` now reads `CBCMunitionPropertiesHandlers.COMMON_SHELL_BIG_CANNON_PROJECTILE.getPropertiesOf(CBCEntityTypes.HE_SHELL.get()).damage()`. The carrier no longer constructs any `HA_HEProjectile`; the superclass's optional payload entry is null. Its detonation continues to use CBC's shared **`ShellExplosion` + `CreateBigCannons.handleCustomExplosion`**, with the normal `HE_SHELL` block/entity powers and CBC damage-restriction setting, as `HEShellProjectile` does. The existing double-payload multiplier is retained. No explosion formula was added or copied.

The existing carrier ballistic settings are deliberately retained through the same configuration source, then `RocketBallistics.properties`: changing those would also change carrier mass/flight response. This retained carrier configuration is distinct from direct-hit damage and detonation. Propulsion, guidance, fuzes, geometry, range settings, launcher compatibility and Sable handling are unchanged. Neither heavy HE explosion settings nor heavy HE direct-hit damage are used.

Old saved stacks with an explicit heavy payload component are not destructively rewritten. Their large HE projectile still uses the corrected normal HE damage/explosion; new crafting/default stacks store normal HE.

## B. Projectile visual audit

Root cause: dependency `HeavyAutocannonProjectileRenderer.render` renders every non-tracer heavy round as a camera-facing, scale-2 `createbigcannons:textures/entity/shrapnel.png` quad. This is the apparent gray ball, not a missing-texture lookup. Cluster spawning explicitly disables the tracer, making this branch particularly visible. Sable Rocket did not introduce that renderer.

The client-only mixin renders the existing ammo item model oriented along the native projectile orientation. The mapping is derived from `AbstractHeavyAutocannonProjectileItem.getEntityType`, not a second handwritten registry table. Native tracer rendering still runs for tracer rounds. Unknown types without a matching ammunition item keep their dependency fallback.

| Projectile registry ID / actual class | Previous renderer | Correct renderer/model used | Root cause | Fixed? |
|---|---|---|---|---|
| `cbc_at:ha_ap_projectile` / `HA_APProjectile` | HeavyAutocannonProjectileRenderer sprite | Same renderer with `cbc_at:item/ha_ap_item` | Shared non-tracer sprite branch | Yes |
| `cbc_at:ha_apds_projectile` / `HA_APDSProjectile` | Same | `cbc_at:item/ha_apds_item` | Same | Yes |
| `cbc_at:ha_apdsfs_projectile` / `HA_APDSFSProjectile` | Same | `cbc_at:item/ha_apdsfs_item` | Same | Yes |
| `cbc_at:ha_he_projectile` / `HA_HEProjectile` | Same | `cbc_at:item/ha_he_item` | Same | Yes |
| `cbc_at:ha_hef_projectile` / `HA_HEFProjectile` | Same | `cbc_at:item/ha_hef_item` | Same | Yes |
| `cbc_at:ha_heat_projectile` / `HA_HEATProjectile` | Same | `cbc_at:item/ha_heat_item` | Same | Yes |
| `cbc_at:ha_smoke_projectile` / `HA_SmokeProjectile` | Same | `cbc_at:item/ha_smoke_item` | Same | Yes |
| Cluster heavy submunitions / the corresponding types above | Same, forced non-tracer | Same ammo model path as direct fire | `FuzedClusterProjectile.spawnClusterParts` calls the real ammo factory and `setTracer(false)` | Yes, same fix |
| `cbc_at:ha_heat_copper_ray` / `HA_CopperRay` | Heavy renderer | Dependency fallback retained | No ammo item/model; legacy jet type, not an assembled shell. Current HEAT adapter uses existing HeatEffects instead | No incorrect ammo mapping established |
| `createbigcannons:ap_autocannon`, `flak_autocannon`, `machine_gun_bullet` | CBC AutocannonProjectileRenderer | Native sprite/tracer path | Intentional small-round rendering, not a failed model lookup | Unchanged |
| `cbc_at:apds_projectile`, `apdsfs_projectile`, `he_projectile`, `hei_projectile` | CBC AutocannonProjectileRenderer | Same native small-round path | Explicit registration, no Sable fallback | Unchanged |
| `createbigcannons:shrapnel_burst`, `flak_burst`, `grapeshot_burst` | ShrapnelBurstRenderer / GrapeshotBurstRenderer | Native fragments | Actual fragment/burst sprites | Unchanged |
| CBC big shells/shots and `cbc_at:cluster_projectile` | BigCannonProjectileRenderer | Their `getRenderedBlockState()` | No gray-sphere branch | Correct already |
| `cbcatfix:heavy_he_shell`, `heat_shell`, `flak_shell`, `rocket` block projectile | BigCannonProjectileRenderer | Existing owned block models | No gray-sphere branch | Correct already |
| All 4 CBCA small, 4 CBCA medium and 3 owned large flying rocket types | UnifiedRocketRenderer | Existing rocket item models | No gray-sphere fallback; registry-derived item IDs | Correct already |
| CBC fluid burst, smoke emitter, gas cloud | NoopRenderer plus particles | Native effects | Deliberately no solid projectile body | Unchanged |

CBC big-shell coverage includes `shot`, `he_shell`, `shrapnel_shell`, `bag_of_grapeshot`, `ap_shot`, `ap_shell`, `fluid_shell`, `smoke_shell`, `mortar_stone`, `traffic_cone`; `drop_mortar_shell` has its dedicated renderer. Cartridge/caseless conversions resolve to those real native projectile types, not replacement visual entities.

All seven item meshes and their `cbc_at:item/ha_rounds` texture are reused from CBCA; resource packs can still override them. No new assets. The native `getTextureLocation() == null` is not itself the bug: these renderers choose render types/atlas textures directly. Gameplay entity identities, collision, damage, fuze and cluster spawn parameters were preserved.

## C. Large/heavy autocannon cartridge automation

Manual path: `HAMunitionAssemblyRecipe`, serializer `cbc_at:ha_munition_assembly`, loaded recipe `cbc_at:munition/heavy_autocannon_munition/ha_munition_assembly`:

1. Empty `cbc_at:heavy_autocannon_empty_cartridge` (two inexpensive-cartridge sheets craft two casings).
2. One `AbstractHeavyAutocannonProjectileItem`.
3. Two separate gunpowder slots for a weak round, or two nitropowder slots for a strong round. Mixed powders are rejected.
4. One `cbc_at:heavy_autocannon_cartridge` with nested `CBCDataComponents.PROJECTILE` and `HA_STRONG_ROUND`.

**Mechanical Crafting already supports this.** `RecipeGridHandler.tryToApplyRecipe` first queries regular `RecipeType.CRAFTING` when Create's `allowRegularCraftingInCrafter` is enabled. It calls the original recipe's `assemble` with the actual inputs. This recipe is neither tagged automation-ignore nor suffixed `_manual_only`. Arrange four connected Mechanical Crafters as a 2×2, feed one casing, one projectile and one powder into each of the two remaining crafters. Create's existing transport handles consumption/output; the adapter never inserts or returns extra items.

The prior fuze pass already replaced CBCA's creative-default projectile output with the actual input projectile. This pass additionally applies the casing's component patch to the native loaded-cartridge result, then restores the recipe's selected charge and writes the real projectile. The output keeps the **loaded cartridge item identity**, count one, complete nested fuze/tracer/custom data, and casing metadata. Neither input stack is mutated. Already loaded cartridges are rejected by the native matcher.

CBC's `CartridgeAssemblyDeployerRecipe` is restricted to `CBCItems.FILLED_AUTOCANNON_CARTRIDGE` + `AutocannonRoundItem`. `BigCartridgeFillingDeployerRecipe`, `MunitionFuzingDeployerRecipe` and `TracerApplicationDeployerRecipe` serve their native CBC categories. CBCA heavy ammo is a different class; simply adding a static deploying JSON would lose component information or not match. There is no reason to add a second automation path now: the native Mechanical Crafter path works. If a pack disables regular crafting in Mechanical Crafters, that configuration is the blocker; no bypass was installed.

Runtime checks use Create's actual `RecipeGridHandler`, all seven heavy ammo types and both powders: output identity/count, charge, full nested data, casing name, unchanged lookup inputs, and rejection of a loaded casing. Physical belt throughput/repeated machine cycles remain a manual check.

## D. Small launcher differences

Registry pairs are `cbc_at:<material>_rocket_pod_rail` and `cbc_at:<material>_rocket_pod_breech`. There are three finished material families, plus their six **unbored manufacturing intermediates**, not additional working variants.

Values below are installed defaults, confirmed against runtime material properties. `n` means rails ahead of the breech; total length is `n + 1`. Spread is the native `shoot` spread parameter, **not degrees**. Global launcher spread configuration multiplies it.

| Launcher | Capacity | Length | Spread/accuracy | Fire rate | Material / mass | Special behavior | Crafting difference |
|---|---|---|---|---|---|---|---|
| Cast iron | 12 per lane; 36 with both sides | At least 2 total; zero spread at 3 total | `max(0, 2 − 1.2375n)`; 0.7625 at one rail | Same small rate: ~40.5/min at default setting 7; ~270/min at 15 | CBC weight 1.5; mount load 3/block; measured Sable block mass 1 | Up to two side lanes; native overlength squib threshold 4 rails | Matching molten cast iron casting + drilling |
| Bronze | 12 / 36 | At least 2; zero spread at 3 | `max(0, 2.5 − 1.65n)`; 0.85 at one rail | Same | CBC weight 1; mount load 2/block; Sable mass 1 | Same sides; squib threshold 7 rails | Matching molten bronze casting + drilling |
| Steel | 12 / 36 | At least 2; zero spread at 4 | `max(0, 3 − 1.2375n)`; 1.7625 at one rail | Same | CBC weight 2.5; mount load 5/block; Sable mass 1 | Same sides; squib threshold 10 rails | Matching molten steel casting + drilling |

Sources and consequences:

- Capacity: `RocketMounts.SMALL_COLUMNS × SMALL_ROWS`, used by `SmallRocketBreechSlotsMixin`, gives 12 actual stored slots. Length never adds slots.
- Accuracy: native `MountedRocketPodContraption.fireShot`, `AutocannonMaterial.properties().baseSpread/spreadReductionPerBarrel`, factor 0.825. `RocketBalance.launcherSpreadFactor` now supplies that same unchanged factor to firing and tooltip calculations. Longer rails reduce initial spread, not rocket motor range.
- Fire rate: native `RocketPodBreechBlockEntity.FIRE_RATES`, default setting 7 (20 ticks) and highest setting 15 (3 ticks), multiplied by `RocketBalance.launcherRate(SMALL, lanes)`. Small base multiplier 0.675; each side lane adds 25% to group rate, not an independent simultaneous volley. Two sides give ~405/min maximum with default configuration. Tick rounding and controller triggers affect observed throughput.
- Native squib condition uses `floor(maxBarrelLength × 1.5)` and its native failure/ammunition conditions. It is not an advertised recommended length. CBC's global maximum cannon length also applies.
- CBC mount load is `2 × material.weight` per launcher block; it is **not Sable mass**. The optional installed Sable `PhysicsBlockPropertyHelper.getMass` returned 1.0 for each rail default state. Sable's data-driven block properties can override this; no material-dependent Sable bonus exists here.
- Native speed/recoil/lifetime material fields exist, but this mod replaces ejection speed with the shared 0.15, suppresses launcher recoil and uses its own existing fuel/flight lifecycle. Do not advertise those fields as material-specific rocket speed/range bonuses.
- All three accept the same small AP/HE/flak/HEI rocket variants. Manual and item-handler loading, mount controls, Sable transfer and attachments are shared. Side lanes must have the same tier, direction and total length; each lane's own material is retained. No per-material reload mechanic or launcher hit-point system exists. Finished small/medium blocks share CBCA hardness 5 / explosion resistance 6; this is not projectile durability.

No pair is globally identical: the material properties differ. At sufficiently long rails all reach zero spread, and their capacity/rate/accepted ammo are identical; mount load, cost and native length/failure limits still differ. No arbitrary steel buff was added.

Final SHIFT tooltip lines, English (Ukrainian equivalents included):

| Line | Cast iron | Bronze | Steel |
|---|---|---|---|
| Capacity per launcher | 12 rockets | 12 rockets | 12 rockets |
| Minimum length, including breech | 2 blocks | 2 blocks | 2 blocks |
| Rails for zero launch spread, excluding breech | 2 | 2 | 3 |
| Maximum single-launcher rate | ~270 rockets/min | ~270 rockets/min | ~270 rockets/min |
| Cannon mount load per block | 3 | 2 | 5 |

All add “Supports one matching-length launcher on each side” and “Load through the breech by hand or item transport”. Normal hover adds only “Hold Shift for launcher details”. Values are derived from native storage/breech APIs, live material properties, geometry and the existing rate configuration, not separately fixed tooltip numbers. Details describe one complete launcher, including when hovering a rail component.

## E. Medium/large launcher audit

| Launcher | Current role | Functional differences | Problem | Action |
|---|---|---|---|---|
| `cbc_at:wrought_iron_medium_rocket_rail` + `_breech` | Four medium rockets per lane, up to 12 with two sides | Minimum 2 blocks; 2-block rocket; 4 exposed slots; accepts medium AP/HE/HEF/HEAT; rate multiplier 0.75; ~22.5/min default, ~150/min max single lane, ~225/min with both sides | Name suggests wrought-iron behavior, but registration explicitly supplies CBC **steel** properties | TOOLTIP ONLY |
| `cbcatfix:big_rocket_rail` + `_breech` | One large AP/HE/HEAT rocket | Minimum 3 blocks; 3-block rocket above rail; one slot; no sides; rate multiplier 0.5; ~15/min default and ~100/min max | Shared steel accuracy is not a unique material identity, but capacity, geometry, payload role and rate already distinguish it | TOOLTIP ONLY |

There is one finished medium family and one large family, not hidden alternate materials. Rails and breeches are complementary components, not redundant variants. Medium's name does not justify silently changing its material or inventing a second family.

Both use native medium spread `max(0, 3 − 1.35n)` (three rails reach zero); CBC weight 2.5 and mount load 5/block. Runtime Sable rail mass is 1 for both. Native medium `FIRE_RATES` is twice the small cooldown table. The large rocket centre is at 11/16 block height above the horizontal rail base (rail top 9/16, rocket radius 2/16), from `RocketMounts.transform`; this existing geometry is unchanged.

Medium recipe: rail = four iron ingots + iron bars; breech adds four iron nuggets. Large rail: Create cutting of the medium rail; large breech = large rail + cast iron ingot. Large blocks have existing hardness/resistance 2/2, unlike CBCA's medium 5/6. No extra durability mechanic was introduced.

SHIFT details for medium: capacity **4**, minimum length **2**, zero-spread rails **3**, max single rate **~150/min**, mount load **5**, matching-length side launchers, manual/item-transport loading. Large: **1**, **3**, **3**, **~100/min**, **5**, “Single launcher; no side launchers”, “Large rocket rests above the rail”, same loading text.

**No balance adjustment**: previous and new gameplay values are identical. The shared spread-factor extraction only prevents tooltips drifting away from native firing.

## F. Validation

All runs use Java 21 and Gradle 8.9. Existing `Architecture` client save and existing server worlds were reused; no special test world was created. Opt-in audit classes are excluded from the shipped jar.

Commands:

```text
gradlew.bat build --no-daemon --console=plain
gradlew.bat runClient -PrunDirectory=build/architecture-client-dev -I dev/assembly-checks/checks.init.gradle --no-daemon --console=plain
gradlew.bat runClient -PrunDirectory=build/architecture-client-dev -PquickPlayWorld=Architecture -I dev/assembly-checks/checks.init.gradle --no-daemon --console=plain
gradlew.bat runServer -PrunDirectory=build/architecture-server-dev -I dev/assembly-checks/checks.init.gradle --no-daemon --console=plain
gradlew.bat runServer -PminimalRuntime -PrunDirectory=build/architecture-server-minimal -I dev/assembly-checks/checks.init.gradle --no-daemon --console=plain
```

- Build: successful. Gradle's standard test source set is empty; runtime audit checks are separate.
- Client startup/main menu: 120 rendered title-screen frames, normal shutdown.
- Existing normal world: player active for 100 ticks; seven heavy ammo models present; 14 native renderer calls (tracer/non-tracer) passed; normal save/shutdown.
- Full dedicated server: 1218 assertions passed, including 33 rocket recipes and 93 new content/automation assertions. No client-only load failure.
- Minimal dedicated server: 1218 assertions passed without Sable, Radars, Kaboom, Jade or JEI; normal save/shutdown.
- Earlier test attempt exposed an incorrect test assumption that the legacy base rocket item has variant components; the fixture was corrected. No production change was needed for that failure.
- Existing optional Kaboom/model warnings and unreachable Mojang network endpoints occur in the development environment; they did not prevent the above checks.

Still manual: visual appearance/orientation of direct shots and cluster bursts in motion, tracer visibility/resource-pack interaction, SHIFT layout in inventory, sustained physical crafting cycles, actual grouped launcher firing and moving Sable gameplay. Renderer calls were exercised off-screen with native objects; they are not a claim of screenshot-based visual approval.

Production changes remain local and uncommitted in this pass.
