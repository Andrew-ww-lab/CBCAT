# Balance baseline — before changes, 2026-09-15

Source snapshot: build/balance-baseline.json. Damage is health points; speed is blocks/tick. Penetration is the CBC coefficient, not guaranteed blocks of iron. Effective range depends on aim, thrust, drag, gravity, terrain and server chunkloading. Powered distance below is the straight-line ceiling v × fuel ticks, not a measured combat range.

## Runtime projectile configuration overrides

These override CBCAT/addon data through CbcatMunitionPropertiesMixin; original CBC ammunition is not overridden. Column order matches the actual config factory signatures.

| Config projectile family | Damage, knockback, gravity, drag, quadratic, mass, penetration, toughness, deflection; optional charge/recoil |
|---|---|
| AUTOCANNON_APDS / cbcAt.autocannon.apds | 12, 0.3, -0.02, 0.01, false, 1.75, 2.5, 1.25, 0.7 |
| AUTOCANNON_APDSFS / cbcAt.autocannon.apdsfs | 12, 0.3, -0.016, 0.00825, false, 1.66, 2.77, 1.3, 0.7 |
| AUTOCANNON_HE / cbcAt.autocannon.he | 9, 0.5, -0.025, 0.01, false, 1, 0.9, 0.4, 0.7 |
| AUTOCANNON_HEI / cbcAt.autocannon.hei | 9, 1, -0.025, 0.01, false, 1, 1, 0.4, 0.7 |
| HEAVY_AUTOCANNON_AP / cbcAt.heavyAutocannon.ap | 30, 1.825, -0.03, 0.013, true, 3.25, 2.75, 1.33, 0.66 |
| HEAVY_AUTOCANNON_APDS / cbcAt.heavyAutocannon.apds | 25, 1.3, -0.02, 0.011, true, 3, 3.1, 1.45, 0.66 |
| HEAVY_AUTOCANNON_APDSFS / cbcAt.heavyAutocannon.apdsfs | 24.5, 1.2, -0.0175, 0.008, true, 2.67, 3.33, 1.5, 0.66 |
| HEAVY_AUTOCANNON_HE / cbcAt.heavyAutocannon.he | 27, 1.825, -0.03, 0.013, true, 1.6, 2.3, 0.8, 0.66 |
| HEAVY_AUTOCANNON_HEF / cbcAt.heavyAutocannon.hef | 28, 1.8, -0.033, 0.013, true, 1.6, 2.125, 0.875, 0.66 |
| HEAVY_AUTOCANNON_HEAT_PROJECTILE / cbcAt.heavyAutocannon.heat | 28, 1.75, -0.035, 0.012, true, 1.55, 2.2, 0.85, 0.66 |
| HEAVY_AUTOCANNON_SMOKE / cbcAt.heavyAutocannon.smoke | 25, 1, -0.035, 0.005, true, 0.75, 0.5, 0.8, 0.7 |
| CLUSTER_SHELL / cbcAt.bigCannon.cluster | 28, 2.5, -0.05, 0.02, false, 1.75, 1, 1, 0.7, 0, 1, true, 1 |
| FLAK_SHELL / fix.bigCannon.flak | 10, 1, -0.05, 0.01, false, 2, 1, 1, 0.7, 0, 1, true, 1 |
| HEAVY_HE_SHELL / fix.bigCannon.heavyHe | 30, 3, -0.07, 0.015, false, 4, 1, 1, 0.7, 0, 1, true, 2 |
| HEAT_SHELL / fix.bigCannon.heat | 25, 1, -0.05, 0.01, false, 2, 2, 2, 0.5, 0, 1, true, 1.2 |
| HESH_SHELL / fix.bigCannon.hesh | 20, 1, -0.05, 0.01, false, 2.5, 3, 3, 0.3, 0, 1, true, 1.5 |

## Rocket families and launcher tradeoffs

All rocket ejection speed: 0.15. Thrust acceleration a=(targetSpeed-forwardSpeed)/responseTicks; crossflow damping=1/responseTicks. Native CBCAT spread=max(0, material.baseSpread - traversedBarrels × material.spreadReductionPerBarrel × 0.825 small / 0.9 medium+big). CBC material settings are authoritative. No fixed single spread number applies to every launcher.

Small native cooldown table (ticks): 80,60,48,40,30,24,20,15,12,10,8,6,5,4,3. Medium/large: 160,120,96,80,60,48,40,30,24,20,16,12,10,8,6. Default level 7: 20/40 ticks. Each attached lane grants +25% group rate (max 3 lanes). Native failures, material limits and loading remain applicable.

| Original item / entity family | Payload / direct damage source | Speed; fuel s; response ticks; powered ceiling | HP; turn cap; seeker range | Slots; minimum length |
|---|---|---|---|---|
| cbc_at:ap_rocket_item / ap_rocket | CBC AP autocannon / 10 | 7; 3; 4; 420 | 10; 11°/tick before inertia; 256 | 12; 1 block |
| cbc_at:flak_rocket_item / flak_rocket | CBC flak autocannon / 10 | 7; 3; 4; 420 | 10; 11°/tick before inertia; 256 | 12; 1 block |
| cbc_at:he_rocket_item / he_rocket | CBCAT HE / 9 | 7; 3; 4; 420 | 10; 11°/tick before inertia; 256 | 12; 1 block |
| cbc_at:hei_rocket_item / hei_rocket | CBCAT HEI / 9 | 7; 3; 4; 420 | 10; 11°/tick before inertia; 256 | 12; 1 block |
| cbc_at:medium_ap_rocket_item / medium_ap_rocket | CBCAT HA ap / 30 | 5.5; 6; 5; 660 | 20; 8°/tick before inertia; 512 | 4; 2 blocks |
| cbc_at:medium_he_rocket_item / medium_he_rocket | CBCAT HA he / 27 | 5.5; 6; 5; 660 | 20; 8°/tick before inertia; 512 | 4; 2 blocks |
| cbc_at:medium_hef_rocket_item / medium_hef_rocket | CBCAT HA hef / 28 | 5.5; 6; 5; 660 | 20; 8°/tick before inertia; 512 | 4; 2 blocks |
| cbc_at:medium_heat_rocket_item / medium_heat_rocket | CBCAT HA heat / 28 | 5.5; 6; 5; 660 | 20; 8°/tick before inertia; 512 | 4; 2 blocks |
| cbcatfix:big_ap_rocket | Native HA carrier damage 30; AP mass8/penetration2; HE explosion from CBC HE shell; HEAT custom jet | 4; 12; 6; 960 | 40; 5°/tick before inertia; 1024 | 1; 3 blocks |
| cbcatfix:big_he_rocket | Native HA carrier damage 27; AP mass8/penetration2; HE explosion from CBC HE shell; HEAT custom jet | 4; 12; 6; 960 | 40; 5°/tick before inertia; 1024 | 1; 3 blocks |
| cbcatfix:big_heat_rocket | Native HA carrier damage 27; AP mass8/penetration2; HE explosion from CBC HE shell; HEAT custom jet | 4; 12; 6; 960 | 40; 5°/tick before inertia; 1024 | 1; 3 blocks |

Big AP/HEAT carrier damage is inherited from native HA_AP/HA_HE constructors, not inferred from the stored warhead label. AP remains unfuzed for impact detonation; HE/HEI/HEF/HEAT/flak use native fuzes. Guidance requires optional Radar designation. Conventional shells have charge/material/barrel-dependent initial speed, no cruise or powered acceleration; range is ballistic, not a fixed ID property.

## Variant costs and exact recipe ingredients

Existing double-payload: count2 → damage/mass/explosion scale1.5, fuel0.5; lightweight: mass0.85, fuel0.65, same HP and powered response; double-fuel: count1, full configured fuel. There is no reinforced variant. Recipe ingredients below are counted from actual pattern symbols. IDs use the corresponding *_double_fuel / *_double_payload aliases; lightweight uses the legacy component stack.

| Recipe | Output | Actual ingredient quantities |
|---|---|---|
| big_rocket_rail_breech | {'id': 'cbcatfix:big_rocket_rail_breech', 'count': 1} | 1× {'item': 'createbigcannons:cast_iron_ingot'}, 1× {'item': 'cbcatfix:big_rocket_rail'} |
| large_rocket_propellant | {} | [{'item': 'createbigcannons:packed_gunpowder'}, {'item': 'createbigcannons:packed_gunpowder'}, {'item': 'minecraft:paper'}, {'item': 'createbigcannons:steel_scrap'}, {'item': 'createbigcannons:steel_scrap'}] |
| medium_rocket_propellant | {} | [{'item': 'createbigcannons:packed_gunpowder'}, {'item': 'minecraft:paper'}, {'item': 'create:iron_sheet'}] |
| small_rocket_propellant | {} | [{'item': 'minecraft:gunpowder'}, {'item': 'minecraft:paper'}, {'item': 'minecraft:iron_nugget'}] |
| big_rocket_rail | {} | [{'item': 'cbc_at:wrought_iron_medium_rocket_rail'}] |
| flak_shell | {'id': 'cbcatfix:flak_shell', 'count': 1} | 2× {'item': 'minecraft:iron_ingot'}, 6× {'tag': 'c:plates/iron'}, 2× {'item': 'createbigcannons:shrapnel_shell'}, 1× {'item': 'createbigcannons:shot_balls'}, 1× {'item': 'createbigcannons:packed_guncotton'}, 1× {'tag': 'minecraft:wooden_slabs'} |
| heat_shell | {'id': 'cbcatfix:heat_shell', 'count': 1} | 2× {'item': 'createbigcannons:cast_iron_ingot'}, 6× {'tag': 'c:plates/iron'}, 2× {'item': 'createbigcannons:ap_shell'}, 1× {'item': 'createbigcannons:he_shell'}, 1× {'tag': 'c:plates/copper'}, 1× {'tag': 'minecraft:wooden_slabs'} |
| heavy_he_shell | {'id': 'cbcatfix:heavy_he_shell', 'count': 1} | 2× {'item': 'createbigcannons:cast_iron_ingot'}, 6× {'tag': 'c:plates/iron'}, 2× {'item': 'createbigcannons:he_shell'}, 1× {'tag': 'createbigcannons:high_explosive_materials'}, 1× {'item': 'createbigcannons:packed_guncotton'}, 1× {'tag': 'minecraft:wooden_slabs'} |
| hesh_shell | {'id': 'cbcatfix:hesh_shell', 'count': 1} | 2× {'item': 'createbigcannons:cast_iron_ingot'}, 6× {'tag': 'c:plates/iron'}, 2× {'item': 'minecraft:clay_ball'}, 1× {'item': 'createbigcannons:he_shell'}, 1× {'item': 'createbigcannons:packed_guncotton'}, 1× {'tag': 'minecraft:wooden_slabs'} |
| cluster_he | {'id': 'cbc_at:cluster', 'count': 1, 'components': {'cbc_at:cluster_projectile': 'tooltip.cbc_at.ha_he', 'cbc_at:cluster_fuzes': [{'slot': 0, 'item': {'id': 'createbigcannons:impact_fuze', 'count': 1}}, {'slot': 1, 'item': {'id': 'createbigcannons:impact_fuze', 'count': 1}}, {'slot': 2, 'item': {'id': 'createbigcannons:impact_fuze', 'count': 1}}, {'slot': 3, 'item': {'id': 'createbigcannons:impact_fuze', 'count': 1}}, {'slot': 4, 'item': {'id': 'createbigcannons:impact_fuze', 'count': 1}}, {'slot': 5, 'item': {'id': 'createbigcannons:impact_fuze', 'count': 1}}, {'slot': 6, 'item': {'id': 'createbigcannons:impact_fuze', 'count': 1}}, {'slot': 7, 'item': {'id': 'createbigcannons:impact_fuze', 'count': 1}}]}} | 8× {'item': 'create:iron_sheet'}, 3× {'item': 'createbigcannons:steel_ingot'}, 8× {'item': 'cbc_at:ha_he_item'}, 1× {'item': 'createbigcannons:packed_gunpowder'} |
| cluster_heat | {'id': 'cbc_at:cluster', 'count': 1, 'components': {'cbc_at:cluster_projectile': 'tooltip.cbc_at.ha_heat', 'cbc_at:cluster_fuzes': [{'slot': 0, 'item': {'id': 'createbigcannons:impact_fuze', 'count': 1}}, {'slot': 1, 'item': {'id': 'createbigcannons:impact_fuze', 'count': 1}}, {'slot': 2, 'item': {'id': 'createbigcannons:impact_fuze', 'count': 1}}, {'slot': 3, 'item': {'id': 'createbigcannons:impact_fuze', 'count': 1}}, {'slot': 4, 'item': {'id': 'createbigcannons:impact_fuze', 'count': 1}}, {'slot': 5, 'item': {'id': 'createbigcannons:impact_fuze', 'count': 1}}, {'slot': 6, 'item': {'id': 'createbigcannons:impact_fuze', 'count': 1}}, {'slot': 7, 'item': {'id': 'createbigcannons:impact_fuze', 'count': 1}}]}} | 8× {'item': 'create:iron_sheet'}, 3× {'item': 'createbigcannons:steel_ingot'}, 8× {'item': 'cbc_at:ha_heat_item'}, 1× {'item': 'createbigcannons:packed_gunpowder'} |
| cluster_hef | {'id': 'cbc_at:cluster', 'count': 1, 'components': {'cbc_at:cluster_projectile': 'tooltip.cbc_at.ha_hef', 'cbc_at:cluster_fuzes': [{'slot': 0, 'item': {'id': 'createbigcannons:impact_fuze', 'count': 1}}, {'slot': 1, 'item': {'id': 'createbigcannons:impact_fuze', 'count': 1}}, {'slot': 2, 'item': {'id': 'createbigcannons:impact_fuze', 'count': 1}}, {'slot': 3, 'item': {'id': 'createbigcannons:impact_fuze', 'count': 1}}, {'slot': 4, 'item': {'id': 'createbigcannons:impact_fuze', 'count': 1}}, {'slot': 5, 'item': {'id': 'createbigcannons:impact_fuze', 'count': 1}}, {'slot': 6, 'item': {'id': 'createbigcannons:impact_fuze', 'count': 1}}, {'slot': 7, 'item': {'id': 'createbigcannons:impact_fuze', 'count': 1}}]}} | 8× {'item': 'create:iron_sheet'}, 3× {'item': 'createbigcannons:steel_ingot'}, 8× {'item': 'cbc_at:ha_hef_item'}, 1× {'item': 'createbigcannons:packed_gunpowder'} |
| cluster_smoke | {'id': 'cbc_at:cluster', 'count': 1, 'components': {'cbc_at:cluster_projectile': 'tooltip.cbc_at.ha_smoke', 'cbc_at:cluster_fuzes': [{'slot': 0, 'item': {'id': 'createbigcannons:impact_fuze', 'count': 1}}, {'slot': 1, 'item': {'id': 'createbigcannons:impact_fuze', 'count': 1}}, {'slot': 2, 'item': {'id': 'createbigcannons:impact_fuze', 'count': 1}}, {'slot': 3, 'item': {'id': 'createbigcannons:impact_fuze', 'count': 1}}, {'slot': 4, 'item': {'id': 'createbigcannons:impact_fuze', 'count': 1}}, {'slot': 5, 'item': {'id': 'createbigcannons:impact_fuze', 'count': 1}}, {'slot': 6, 'item': {'id': 'createbigcannons:impact_fuze', 'count': 1}}, {'slot': 7, 'item': {'id': 'createbigcannons:impact_fuze', 'count': 1}}]}} | 8× {'item': 'create:iron_sheet'}, 3× {'item': 'createbigcannons:steel_ingot'}, 8× {'item': 'cbc_at:ha_smoke_item'}, 1× {'item': 'createbigcannons:packed_gunpowder'} |
| ap_rocket_item_double_payload | {'id': 'cbcatfix:ap_rocket_double_payload', 'count': 1} | 2× {'item': 'create:iron_sheet'}, 1× {'item': 'cbcatfix:small_rocket_propellant'}, 1× {'item': 'minecraft:iron_nugget'}, 2× {'item': 'createbigcannons:ap_autocannon_round'} |
| ap_rocket_item_lightweight | {'id': 'cbc_at:ap_rocket_item', 'count': 1, 'components': {'createbigcannons:projectile': [{'slot': 0, 'item': {'id': 'createbigcannons:ap_autocannon_round', 'count': 1}}], 'cbc_at:rocket_fuel': 127, 'minecraft:custom_data': {'CbcatFixAssemblyFuel': True, 'CbcatFixLightweight': True}}} | 2× {'item': 'create:iron_sheet'}, 1× {'item': 'cbcatfix:small_rocket_propellant'}, 1× {'item': 'minecraft:iron_nugget'}, 1× {'item': 'createbigcannons:ap_autocannon_round'} |
| ap_rocket_item_long_range | {'id': 'cbcatfix:ap_rocket_double_fuel', 'count': 1} | 2× {'item': 'create:iron_sheet'}, 2× {'item': 'cbcatfix:small_rocket_propellant'}, 1× {'item': 'minecraft:iron_nugget'}, 1× {'item': 'createbigcannons:ap_autocannon_round'} |
| big_ap_rocket_double_payload | {'id': 'cbcatfix:big_ap_rocket_double_payload', 'count': 1} | 3× {'item': 'create:iron_sheet'}, 4× {'item': 'createbigcannons:steel_ingot'}, 1× {'item': 'cbcatfix:large_rocket_propellant'}, 2× {'item': 'createbigcannons:ap_shot'} |
| big_ap_rocket_lightweight | {'id': 'cbcatfix:big_ap_rocket', 'count': 1, 'components': {'createbigcannons:projectile': [{'slot': 0, 'item': {'id': 'createbigcannons:ap_shot', 'count': 1}}], 'cbc_at:rocket_fuel': 127, 'minecraft:custom_data': {'CbcatFixAssemblyFuel': True, 'CbcatFixLightweight': True}}} | 3× {'item': 'create:iron_sheet'}, 4× {'item': 'createbigcannons:steel_ingot'}, 1× {'item': 'cbcatfix:large_rocket_propellant'}, 1× {'item': 'createbigcannons:ap_shot'} |
| big_ap_rocket_long_range | {'id': 'cbcatfix:big_ap_rocket_double_fuel', 'count': 1} | 3× {'item': 'create:iron_sheet'}, 4× {'item': 'createbigcannons:steel_ingot'}, 2× {'item': 'cbcatfix:large_rocket_propellant'}, 1× {'item': 'createbigcannons:ap_shot'} |
| big_he_rocket_double_payload | {'id': 'cbcatfix:big_he_rocket_double_payload', 'count': 1} | 3× {'item': 'create:iron_sheet'}, 4× {'item': 'createbigcannons:steel_ingot'}, 1× {'item': 'cbcatfix:large_rocket_propellant'}, 2× {'item': 'createbigcannons:he_shell'} |
| big_he_rocket_lightweight | {'id': 'cbcatfix:big_he_rocket', 'count': 1, 'components': {'createbigcannons:projectile': [{'slot': 0, 'item': {'id': 'createbigcannons:he_shell', 'count': 1}}], 'cbc_at:rocket_fuel': 127, 'minecraft:custom_data': {'CbcatFixAssemblyFuel': True, 'CbcatFixLightweight': True}}} | 3× {'item': 'create:iron_sheet'}, 4× {'item': 'createbigcannons:steel_ingot'}, 1× {'item': 'cbcatfix:large_rocket_propellant'}, 1× {'item': 'createbigcannons:he_shell'} |
| big_he_rocket_long_range | {'id': 'cbcatfix:big_he_rocket_double_fuel', 'count': 1} | 3× {'item': 'create:iron_sheet'}, 4× {'item': 'createbigcannons:steel_ingot'}, 2× {'item': 'cbcatfix:large_rocket_propellant'}, 1× {'item': 'createbigcannons:he_shell'} |
| big_heat_rocket_double_payload | {'id': 'cbcatfix:big_heat_rocket_double_payload', 'count': 1} | 3× {'item': 'create:iron_sheet'}, 4× {'item': 'createbigcannons:steel_ingot'}, 1× {'item': 'cbcatfix:large_rocket_propellant'}, 2× {'item': 'cbcatfix:heat_shell'} |
| big_heat_rocket_lightweight | {'id': 'cbcatfix:big_heat_rocket', 'count': 1, 'components': {'createbigcannons:projectile': [{'slot': 0, 'item': {'id': 'cbcatfix:heat_shell', 'count': 1}}], 'cbc_at:rocket_fuel': 127, 'minecraft:custom_data': {'CbcatFixAssemblyFuel': True, 'CbcatFixLightweight': True}}} | 3× {'item': 'create:iron_sheet'}, 4× {'item': 'createbigcannons:steel_ingot'}, 1× {'item': 'cbcatfix:large_rocket_propellant'}, 1× {'item': 'cbcatfix:heat_shell'} |
| big_heat_rocket_long_range | {'id': 'cbcatfix:big_heat_rocket_double_fuel', 'count': 1} | 3× {'item': 'create:iron_sheet'}, 4× {'item': 'createbigcannons:steel_ingot'}, 2× {'item': 'cbcatfix:large_rocket_propellant'}, 1× {'item': 'cbcatfix:heat_shell'} |
| flak_rocket_item_double_payload | {'id': 'cbcatfix:flak_rocket_double_payload', 'count': 1} | 2× {'item': 'create:iron_sheet'}, 1× {'item': 'cbcatfix:small_rocket_propellant'}, 1× {'item': 'minecraft:iron_nugget'}, 2× {'item': 'createbigcannons:flak_autocannon_round'} |
| flak_rocket_item_lightweight | {'id': 'cbc_at:flak_rocket_item', 'count': 1, 'components': {'createbigcannons:projectile': [{'slot': 0, 'item': {'id': 'createbigcannons:flak_autocannon_round', 'count': 1}}], 'cbc_at:rocket_fuel': 127, 'minecraft:custom_data': {'CbcatFixAssemblyFuel': True, 'CbcatFixLightweight': True}}} | 2× {'item': 'create:iron_sheet'}, 1× {'item': 'cbcatfix:small_rocket_propellant'}, 1× {'item': 'minecraft:iron_nugget'}, 1× {'item': 'createbigcannons:flak_autocannon_round'} |
| flak_rocket_item_long_range | {'id': 'cbcatfix:flak_rocket_double_fuel', 'count': 1} | 2× {'item': 'create:iron_sheet'}, 2× {'item': 'cbcatfix:small_rocket_propellant'}, 1× {'item': 'minecraft:iron_nugget'}, 1× {'item': 'createbigcannons:flak_autocannon_round'} |
| he_rocket_item_double_payload | {'id': 'cbcatfix:he_rocket_double_payload', 'count': 1} | 2× {'item': 'create:iron_sheet'}, 1× {'item': 'cbcatfix:small_rocket_propellant'}, 1× {'item': 'minecraft:iron_nugget'}, 2× {'item': 'cbc_at:he_item'} |
| he_rocket_item_lightweight | {'id': 'cbc_at:he_rocket_item', 'count': 1, 'components': {'createbigcannons:projectile': [{'slot': 0, 'item': {'id': 'cbc_at:he_item', 'count': 1}}], 'cbc_at:rocket_fuel': 127, 'minecraft:custom_data': {'CbcatFixAssemblyFuel': True, 'CbcatFixLightweight': True}}} | 2× {'item': 'create:iron_sheet'}, 1× {'item': 'cbcatfix:small_rocket_propellant'}, 1× {'item': 'minecraft:iron_nugget'}, 1× {'item': 'cbc_at:he_item'} |
| he_rocket_item_long_range | {'id': 'cbcatfix:he_rocket_double_fuel', 'count': 1} | 2× {'item': 'create:iron_sheet'}, 2× {'item': 'cbcatfix:small_rocket_propellant'}, 1× {'item': 'minecraft:iron_nugget'}, 1× {'item': 'cbc_at:he_item'} |
| hei_rocket_item_double_payload | {'id': 'cbcatfix:hei_rocket_double_payload', 'count': 1} | 2× {'item': 'create:iron_sheet'}, 1× {'item': 'cbcatfix:small_rocket_propellant'}, 1× {'item': 'minecraft:iron_nugget'}, 2× {'item': 'cbc_at:hei_item'} |
| hei_rocket_item_lightweight | {'id': 'cbc_at:hei_rocket_item', 'count': 1, 'components': {'createbigcannons:projectile': [{'slot': 0, 'item': {'id': 'cbc_at:hei_item', 'count': 1}}], 'cbc_at:rocket_fuel': 127, 'minecraft:custom_data': {'CbcatFixAssemblyFuel': True, 'CbcatFixLightweight': True}}} | 2× {'item': 'create:iron_sheet'}, 1× {'item': 'cbcatfix:small_rocket_propellant'}, 1× {'item': 'minecraft:iron_nugget'}, 1× {'item': 'cbc_at:hei_item'} |
| hei_rocket_item_long_range | {'id': 'cbcatfix:hei_rocket_double_fuel', 'count': 1} | 2× {'item': 'create:iron_sheet'}, 2× {'item': 'cbcatfix:small_rocket_propellant'}, 1× {'item': 'minecraft:iron_nugget'}, 1× {'item': 'cbc_at:hei_item'} |
| medium_ap_rocket_item_double_payload | {'id': 'cbcatfix:medium_ap_rocket_double_payload', 'count': 1} | 3× {'item': 'create:iron_sheet'}, 1× {'item': 'cbcatfix:medium_rocket_propellant'}, 1× {'item': 'createbigcannons:steel_ingot'}, 2× {'item': 'cbc_at:ha_ap_item'} |
| medium_ap_rocket_item_lightweight | {'id': 'cbc_at:medium_ap_rocket_item', 'count': 1, 'components': {'createbigcannons:projectile': [{'slot': 0, 'item': {'id': 'cbc_at:ha_ap_item', 'count': 1}}], 'cbc_at:rocket_fuel': 127, 'minecraft:custom_data': {'CbcatFixAssemblyFuel': True, 'CbcatFixLightweight': True}}} | 3× {'item': 'create:iron_sheet'}, 1× {'item': 'cbcatfix:medium_rocket_propellant'}, 1× {'item': 'createbigcannons:steel_ingot'}, 1× {'item': 'cbc_at:ha_ap_item'} |
| medium_ap_rocket_item_long_range | {'id': 'cbcatfix:medium_ap_rocket_double_fuel', 'count': 1} | 3× {'item': 'create:iron_sheet'}, 2× {'item': 'cbcatfix:medium_rocket_propellant'}, 1× {'item': 'createbigcannons:steel_ingot'}, 1× {'item': 'cbc_at:ha_ap_item'} |
| medium_he_rocket_item_double_payload | {'id': 'cbcatfix:medium_he_rocket_double_payload', 'count': 1} | 3× {'item': 'create:iron_sheet'}, 1× {'item': 'cbcatfix:medium_rocket_propellant'}, 1× {'item': 'createbigcannons:steel_ingot'}, 2× {'item': 'cbc_at:ha_he_item'} |
| medium_he_rocket_item_lightweight | {'id': 'cbc_at:medium_he_rocket_item', 'count': 1, 'components': {'createbigcannons:projectile': [{'slot': 0, 'item': {'id': 'cbc_at:ha_he_item', 'count': 1}}], 'cbc_at:rocket_fuel': 127, 'minecraft:custom_data': {'CbcatFixAssemblyFuel': True, 'CbcatFixLightweight': True}}} | 3× {'item': 'create:iron_sheet'}, 1× {'item': 'cbcatfix:medium_rocket_propellant'}, 1× {'item': 'createbigcannons:steel_ingot'}, 1× {'item': 'cbc_at:ha_he_item'} |
| medium_he_rocket_item_long_range | {'id': 'cbcatfix:medium_he_rocket_double_fuel', 'count': 1} | 3× {'item': 'create:iron_sheet'}, 2× {'item': 'cbcatfix:medium_rocket_propellant'}, 1× {'item': 'createbigcannons:steel_ingot'}, 1× {'item': 'cbc_at:ha_he_item'} |
| medium_heat_rocket_item_double_payload | {'id': 'cbcatfix:medium_heat_rocket_double_payload', 'count': 1} | 3× {'item': 'create:iron_sheet'}, 1× {'item': 'cbcatfix:medium_rocket_propellant'}, 1× {'item': 'createbigcannons:steel_ingot'}, 2× {'item': 'cbc_at:ha_heat_item'} |
| medium_heat_rocket_item_lightweight | {'id': 'cbc_at:medium_heat_rocket_item', 'count': 1, 'components': {'createbigcannons:projectile': [{'slot': 0, 'item': {'id': 'cbc_at:ha_heat_item', 'count': 1}}], 'cbc_at:rocket_fuel': 127, 'minecraft:custom_data': {'CbcatFixAssemblyFuel': True, 'CbcatFixLightweight': True}}} | 3× {'item': 'create:iron_sheet'}, 1× {'item': 'cbcatfix:medium_rocket_propellant'}, 1× {'item': 'createbigcannons:steel_ingot'}, 1× {'item': 'cbc_at:ha_heat_item'} |
| medium_heat_rocket_item_long_range | {'id': 'cbcatfix:medium_heat_rocket_double_fuel', 'count': 1} | 3× {'item': 'create:iron_sheet'}, 2× {'item': 'cbcatfix:medium_rocket_propellant'}, 1× {'item': 'createbigcannons:steel_ingot'}, 1× {'item': 'cbc_at:ha_heat_item'} |
| medium_hef_rocket_item_double_payload | {'id': 'cbcatfix:medium_hef_rocket_double_payload', 'count': 1} | 3× {'item': 'create:iron_sheet'}, 1× {'item': 'cbcatfix:medium_rocket_propellant'}, 1× {'item': 'createbigcannons:steel_ingot'}, 2× {'item': 'cbc_at:ha_hef_item'} |
| medium_hef_rocket_item_lightweight | {'id': 'cbc_at:medium_hef_rocket_item', 'count': 1, 'components': {'createbigcannons:projectile': [{'slot': 0, 'item': {'id': 'cbc_at:ha_hef_item', 'count': 1}}], 'cbc_at:rocket_fuel': 127, 'minecraft:custom_data': {'CbcatFixAssemblyFuel': True, 'CbcatFixLightweight': True}}} | 3× {'item': 'create:iron_sheet'}, 1× {'item': 'cbcatfix:medium_rocket_propellant'}, 1× {'item': 'createbigcannons:steel_ingot'}, 1× {'item': 'cbc_at:ha_hef_item'} |
| medium_hef_rocket_item_long_range | {'id': 'cbcatfix:medium_hef_rocket_double_fuel', 'count': 1} | 3× {'item': 'create:iron_sheet'}, 2× {'item': 'cbcatfix:medium_rocket_propellant'}, 1× {'item': 'createbigcannons:steel_ingot'}, 1× {'item': 'cbc_at:ha_hef_item'} |

## Native CBC / CBCAT property comparison

Exact installed data; omitted fields use native defaults. Original source implementations and launch materials were inspected separately. This table intentionally retains explicit property keys to distinguish explosion power from direct damage and durability mass.

| Owner / projectile ID | Actual property data |
|---|---|
| data/createbigcannons/munition_properties/projectiles/ap_autocannon.json | {"entity_damage": 14.0, "durability_mass": 2.0, "renders_invulnerable": false, "ignores_invulnerability": true, "ignores_entity_armor": false, "gravity": -0.025, "drag": 0.01, "quadratic_drag": false, "knockback": 0.5, "added_recoil": 1, "can_squib": true, "penetration": 2, "toughness": 1, "deflection": 0.7} |
| data/createbigcannons/munition_properties/projectiles/ap_shot.json | {"entity_damage": 50.0, "durability_mass": 8.0, "renders_invulnerable": false, "ignores_invulnerability": true, "ignores_entity_armor": true, "gravity": -0.05, "drag": 0.01, "quadratic_drag": false, "knockback": 2, "added_charge_power": 0, "minimum_charge_power": 1.0, "can_squib": true, "added_recoil": 1.0, "penetration": 2, "toughness": 1, "deflection": 0.7} |
| data/createbigcannons/munition_properties/projectiles/flak_autocannon.json | {"entity_damage": 10.0, "durability_mass": 1.0, "renders_invulnerable": false, "ignores_invulnerability": true, "ignores_entity_armor": false, "base_fuze": false, "gravity": -0.025, "drag": 0.01, "quadratic_drag": false, "knockback": 0.5, "added_recoil": 1, "can_squib": true, "shrapnel_spread": 0.25, "shrapnel_count": 15, "entity_damaging_explosive_power": 2.5, "block_damaging_explosive_power": 1, "penetration": 1, "toughness": 0.5, "deflection": 0.7} |
| data/createbigcannons/munition_properties/projectiles/fluid_shell.json | {"entity_damage": 30.0, "durability_mass": 2.0, "renders_invulnerable": false, "ignores_invulnerability": true, "ignores_entity_armor": true, "gravity": -0.05, "drag": 0.01, "quadratic_drag": false, "knockback": 2, "added_charge_power": 0, "minimum_charge_power": 1.0, "can_squib": true, "added_recoil": 1.0, "base_fuze": false, "entity_damaging_explosive_power": 3, "block_damaging_explosive_power": 2, "fluid_shell_capacity": 2000, "millibuckets_per_fluid_blob": 500, "millibuckets_per_area_of_effect_radius": 125, "fluid_blob_spread": 1, "penetration": 1, "toughness": 1, "deflection": 0.7} |
| data/createbigcannons/munition_properties/projectiles/he_shell.json | {"entity_damage": 30.0, "durability_mass": 2.0, "renders_invulnerable": false, "ignores_invulnerability": true, "ignores_entity_armor": true, "gravity": -0.05, "drag": 0.01, "quadratic_drag": false, "knockback": 2, "added_charge_power": 0, "minimum_charge_power": 1.0, "can_squib": true, "added_recoil": 1.0, "base_fuze": false, "entity_damaging_explosive_power": 12, "block_damaging_explosive_power": 8, "penetration": 1, "toughness": 1, "deflection": 0.7} |
| data/createbigcannons/munition_properties/projectiles/shrapnel_shell.json | {"entity_damage": 30.0, "durability_mass": 2.0, "renders_invulnerable": false, "ignores_invulnerability": true, "ignores_entity_armor": true, "gravity": -0.05, "drag": 0.01, "quadratic_drag": false, "added_charge_power": 0, "minimum_charge_power": 1.0, "can_squib": true, "added_recoil": 1.0, "base_fuze": false, "shrapnel_spread": 0.6, "shrapnel_count": 50, "entity_damaging_explosive_power": 3, "block_damaging_explosive_power": 2, "penetration": 1, "toughness": 1, "deflection": 0.7} |

## Other effects before changes

| Configuration | Original arguments |
|---|---|
| GUIDANCE_SPEED_LOSS_PER_DEGREE | "speedLossPerTurnDegree", 0.003, 0.0, 0.1, "Fraction of speed lost for each degree actually turned in one tick." |
| GUIDANCE_MAX_SPEED_LOSS | "maximumSpeedLossPerTick", 0.15, 0.0, 0.9, "Maximum fraction of speed a rocket can lose to steering in one tick." |
| LIGHTWEIGHT_MASS_MULTIPLIER | "lightweightMassMultiplier", 0.85, 0.1, 1.0, "Mass multiplier for the one-warhead, one-fuel lightweight recipe." |
| LIGHTWEIGHT_FUEL_MULTIPLIER | "lightweightFuelMultiplier", 0.65, 0.05, 1.0, "Powered-flight-time multiplier for the one-warhead, one-fuel lightweight recipe." |
| HEAVY_AUTOCANNON_HEAT | "heavyAutocannon", 1.5, 3.5, 6.0, 20.0, 5.0, 50.0 |
| MEDIUM_ROCKET_HEAT | "mediumRocket", 1.5, 3.5, 8.0, 20.0, 5.0, 50.0 |
| BIG_CANNON_HEAT | "bigCannon", 2.5, 2.5, 10.0, 32.0, 8.0, 80.0 |
| BIG_ROCKET_HEAT | "bigRocket", 8.0, 10.0, 14.0, 64.0, 12.0, 120.0 |
| HEAVY_HE_BLOCK_POWER | "fix.bigCannon.heavyHe", "blockExplosionPower", 16, 0, 1000 |
| HEAVY_HE_ENTITY_POWER | "fix.bigCannon.heavyHe", "entityExplosionPower", 24, 0, 1000 |
| FLAK_BLOCK_POWER | "fix.bigCannon.flak", "blockExplosionPower", 2, 0, 1000 |
| FLAK_ENTITY_POWER | "fix.bigCannon.flak", "entityExplosionPower", 2, 0, 1000 |
| FLAK_SHRAPNEL_COUNT | "fix.bigCannon.flak", "shrapnelCount", 45, 0, 1000 |
| FLAK_BURST_COUNT | "fix.bigCannon.flak", "burstParticleProjectileCount", 30, 0, 1000 |
| HEAVY_HE_MAX_SPEED | "fix.bigCannon.heavyHe", "maximumSpeedBlocksPerTick", 4, 0.1, 100 |
| HESH_SPALL_DISTANCE | "fix.bigCannon.hesh", "spallDistanceBlocks", 4, 0, 64 |
| HESH_SPALL_RADIUS | "fix.bigCannon.hesh", "spallRadiusBlocks", 2, 0, 64 |
| HESH_BLOCKS_DESTROYED | "fix.bigCannon.hesh", "maximumBlocksDestroyed", 2, 0, 64 |
| HESH_SPALL_DAMAGE | "fix.bigCannon.hesh", "spallEntityDamage", 35, 0, 100000 |
| AUTOCANNON_HE_EXPLOSION_SCALE | "cbcAt.autocannon.he", "cbcFlakExplosionScale", 1.05, 0, 100 |
| HEAVY_AUTOCANNON_HE_EXPLOSION_SCALE | "cbcAt.heavyAutocannon.he", "cbcFlakExplosionScale", 1.66, 0, 100 |
| HEAVY_AUTOCANNON_HEF_BLOCK_EXPLOSION_SCALE | "cbcAt.heavyAutocannon.hef", "cbcFlakBlockExplosionScale", 1.3, 0, 100 |
| HEAVY_AUTOCANNON_HEF_ENTITY_EXPLOSION_SCALE | "cbcAt.heavyAutocannon.hef", "cbcFlakEntityExplosionScale", 1.4, 0, 100 |
| HEAVY_AUTOCANNON_HEF_FRAGMENT_COUNT_SCALE | "cbcAt.heavyAutocannon.hef", "cbcFlakFragmentCountScale", 8, 0, 100 |
| HEAVY_AUTOCANNON_HEF_FRAGMENT_SPREAD_SCALE | "cbcAt.heavyAutocannon.hef", "cbcFlakFragmentSpreadScale", 4, 0, 100 |
| HEAVY_AUTOCANNON_SMOKE_EXPLOSION | "cbcAt.heavyAutocannon.smoke", 1.66, 2 |
| HEAVY_AUTOCANNON_SMOKE_DURATION_SCALE | "cbcAt.heavyAutocannon.smoke", "smokeDurationScale", 0.75, 0, 100 |
| HEAVY_AUTOCANNON_SMOKE_SIZE_SCALE | "cbcAt.heavyAutocannon.smoke", "smokeSizeScale", 0.3, 0, 100 |
| CLUSTER_SUBMUNITION_LIFETIME | "cbcAt.bigCannon.cluster", "submunitionLifetimeTicks", 50, 1, 12000 |
| CLUSTER_SUBMUNITION_SPEED_SCALE | "cbcAt.bigCannon.cluster", "submunitionSpeedScale", 1, 0, 100 |
| CLUSTER_SUBMUNITION_INACCURACY | "cbcAt.bigCannon.cluster", "submunitionInaccuracyDegrees", 25, 0, 180 |
| SMALL_ROCKET_AP | "small", 2, 2, 1, 0.70 |
| MEDIUM_ROCKET_AP | "medium", 3.25, 2.75, 1.33, 0.66 |
| BIG_ROCKET_AP | "big", 8, 2, 1, 0.70 |
