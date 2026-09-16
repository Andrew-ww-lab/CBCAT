# Серверні налаштування після уточнення фізики

Згенеровано з `config/cbcatfix-server.toml`, створеного NeoForge у локальному середовищі перевірки. Сторона усіх параметрів — SERVER; синхронізація штатна. Старі користувацькі значення не перезаписуються модом.

| Категорія | Параметр | Default | Межі | Сторона | Дія |
|---|---|---|---|---|---|
| rockets.small | maxSpeedBlocksPerTick | 9.0 | 0.1 ~ 20.0 | SERVER | Maximum powered speed in blocks per tick. |
| rockets.small | guidedTurnRateDegreesPerTick | 36.0 | 0.0 ~ 45.0 | SERVER | Maximum guided turn rate in degrees per tick. |
| rockets.small | poweredFlightSeconds | 1.5 | 0.05 ~ 120.0 | SERVER | Powered flight time in seconds for a long-range rocket. A double-payload rocket receives half of this duration. |
| rockets.small | durability | 10.0 | 1.0 ~ 1000.0 | SERVER | Damage required to shoot this rocket down. |
| rockets.small | maximumGuidanceRangeBlocks | 256.0 | 1.0 ~ 4096.0 | SERVER | Maximum distance in blocks at which a guided rocket can retain its target. |
| rockets.small.apWarhead | durabilityMass | 2.0 | 0.01 ~ 100.0 | SERVER | CBC penetrator mass consumed by impacts; distinct from rocket hull health. |
| rockets.small.apWarhead | penetration | 2.0 | 0.0 ~ 100.0 | SERVER | CBC armor penetration coefficient; not a fixed number of blocks. |
| rockets.small.apWarhead | toughness | 1.0 | 0.0 ~ 100.0 | SERVER | CBC resistance to projectile shatter. |
| rockets.small.apWarhead | deflection | 0.7 | 0.0 ~ 1.0 | SERVER | CBC angle coefficient used by the native ricochet calculation. |
| rockets.medium | maxSpeedBlocksPerTick | 6.0 | 0.1 ~ 20.0 | SERVER | Maximum powered speed in blocks per tick. |
| rockets.medium | guidedTurnRateDegreesPerTick | 18.0 | 0.0 ~ 45.0 | SERVER | Maximum guided turn rate in degrees per tick. |
| rockets.medium | poweredFlightSeconds | 6.0 | 0.05 ~ 120.0 | SERVER | Powered flight time in seconds for a long-range rocket. A double-payload rocket receives half of this duration. |
| rockets.medium | durability | 20.0 | 1.0 ~ 1000.0 | SERVER | Damage required to shoot this rocket down. |
| rockets.medium | maximumGuidanceRangeBlocks | 768.0 | 1.0 ~ 4096.0 | SERVER | Maximum distance in blocks at which a guided rocket can retain its target. |
| rockets.medium.apWarhead | durabilityMass | 3.25 | 0.01 ~ 100.0 | SERVER | CBC penetrator mass consumed by impacts; distinct from rocket hull health. |
| rockets.medium.apWarhead | penetration | 2.75 | 0.0 ~ 100.0 | SERVER | CBC armor penetration coefficient; not a fixed number of blocks. |
| rockets.medium.apWarhead | toughness | 1.33 | 0.0 ~ 100.0 | SERVER | CBC resistance to projectile shatter. |
| rockets.medium.apWarhead | deflection | 0.66 | 0.0 ~ 1.0 | SERVER | CBC angle coefficient used by the native ricochet calculation. |
| rockets.big | maxSpeedBlocksPerTick | 4.5 | 0.1 ~ 20.0 | SERVER | Maximum powered speed in blocks per tick. |
| rockets.big | guidedTurnRateDegreesPerTick | 4.0 | 0.0 ~ 45.0 | SERVER | Maximum guided turn rate in degrees per tick. |
| rockets.big | poweredFlightSeconds | 18.0 | 0.05 ~ 120.0 | SERVER | Powered flight time in seconds for a long-range rocket. A double-payload rocket receives half of this duration. |
| rockets.big | durability | 40.0 | 1.0 ~ 1000.0 | SERVER | Damage required to shoot this rocket down. |
| rockets.big | maximumGuidanceRangeBlocks | 1536.0 | 1.0 ~ 4096.0 | SERVER | Maximum distance in blocks at which a guided rocket can retain its target. |
| rockets.big.apWarhead | durabilityMass | 8.0 | 0.01 ~ 100.0 | SERVER | CBC penetrator mass consumed by impacts; distinct from rocket hull health. |
| rockets.big.apWarhead | penetration | 2.0 | 0.0 ~ 100.0 | SERVER | CBC armor penetration coefficient; not a fixed number of blocks. |
| rockets.big.apWarhead | toughness | 1.0 | 0.0 ~ 100.0 | SERVER | CBC resistance to projectile shatter. |
| rockets.big.apWarhead | deflection | 0.7 | 0.0 ~ 1.0 | SERVER | CBC angle coefficient used by the native ricochet calculation. |
| rockets.guidance | speedLossPerTurnDegree | 0.003 | 0.0 ~ 0.1 | SERVER | Fraction of speed lost for each degree actually turned in one tick. |
| rockets.guidance | maximumSpeedLossPerTick | 0.15 | 0.0 ~ 0.9 | SERVER | Maximum fraction of speed a rocket can lose to steering in one tick. |
| rockets.guidance | lightweightMassMultiplier | 0.6 | 0.1 ~ 1.0 | SERVER | Mass multiplier for the one-warhead, one-fuel lightweight recipe. |
| rockets.guidance | lightweightFuelMultiplier | 0.5 | 0.05 ~ 1.0 | SERVER | Powered-flight-time multiplier for the one-warhead, one-fuel lightweight recipe. |
| rockets.guidance | enabled | true | true / false | SERVER | Allow rocket steering and launch-time Radar designation. |
| rockets.guidance | seekerHalfAngleDegrees | 30.0 | 1.0 ~ 85.0 | SERVER | Half-angle of the seeker's forward detection cone. |
| rockets.guidance | turnRateMultiplier | 1.0 | 0.0 ~ 4.0 | SERVER | Multiplier before speed/mass steering inertia; effective cap 45 degrees/tick. |
| rockets.physics | velocityMultiplier | 1.0 | 0.1 ~ 4.0 | SERVER | Powered speed multiplier; effective speed is capped at 20 blocks/tick. |
| rockets.physics | accelerationMultiplier | 1.0 | 0.1 ~ 4.0 | SERVER | Motor response multiplier; variants retain their mass penalty. |
| rockets.physics | fuelMultiplier | 1.0 | 0.1 ~ 4.0 | SERVER | Configured fuel duration multiplier; total powered time is capped at 120 seconds. |
| rockets.physics | gravityMultiplier | 1.0 | 0.0 ~ 4.0 | SERVER | Ballistic rocket gravity multiplier. Does not add gravity during powered flight. |
| rockets.physics | lightweightSpeedMultiplier | 1.2 | 1.0 ~ 1.5 | SERVER | Lightweight powered speed advantage; same 20 blocks/tick powered ceiling applies. |
| rockets.payload | directDamageMultiplier | 1.0 | 0.0 ~ 10.0 | SERVER | Direct rocket impact damage multiplier; explosion and jet power have separate munition settings. |
| rockets.payload | penetrationMultiplier | 1.0 | 0.0 ~ 4.0 | SERVER | Rocket penetration coefficient multiplier; preserves CBC armor calculations. |
| rockets.payload | doublePayloadMultiplier | 1.75 | 1.0 ~ 2.0 | SERVER | Double-warhead damage/effect and penetrator mass scale; fuel remains halved. |
| rockets.payload | lightweightDurabilityMultiplier | 0.75 | 0.1 ~ 1.0 | SERVER | Lightweight hull health multiplier; does not weaken the AP penetrator. |
| munitions.heat.heavyAutocannon | blockExplosionPower | 1.5 | 0.0 ~ 32.0 | SERVER | HEAT explosion power against blocks. |
| munitions.heat.heavyAutocannon | entityExplosionPower | 3.5 | 0.0 ~ 32.0 | SERVER | HEAT explosion power against entities. |
| munitions.heat.heavyAutocannon | jetLengthBlocks | 6.0 | 0.25 ~ 64.0 | SERVER | Maximum HEAT jet length through air, in blocks. |
| munitions.heat.heavyAutocannon | jetEnergy | 20.0 | 0.0 ~ 1000.0 | SERVER | Energy available for penetrating CBC block armor. |
| munitions.heat.heavyAutocannon | jetPenetration | 5.0 | 0.01 ~ 100.0 | SERVER | Resistance to CBC armor hardness; higher values preserve more jet energy. |
| munitions.heat.heavyAutocannon | jetEntityDamage | 50.0 | 0.0 ~ 1000.0 | SERVER | Damage dealt once to each entity intersected by the HEAT jet. |
| munitions.heat.mediumRocket | blockExplosionPower | 1.5 | 0.0 ~ 32.0 | SERVER | HEAT explosion power against blocks. |
| munitions.heat.mediumRocket | entityExplosionPower | 3.5 | 0.0 ~ 32.0 | SERVER | HEAT explosion power against entities. |
| munitions.heat.mediumRocket | jetLengthBlocks | 8.0 | 0.25 ~ 64.0 | SERVER | Maximum HEAT jet length through air, in blocks. |
| munitions.heat.mediumRocket | jetEnergy | 20.0 | 0.0 ~ 1000.0 | SERVER | Energy available for penetrating CBC block armor. |
| munitions.heat.mediumRocket | jetPenetration | 5.0 | 0.01 ~ 100.0 | SERVER | Resistance to CBC armor hardness; higher values preserve more jet energy. |
| munitions.heat.mediumRocket | jetEntityDamage | 50.0 | 0.0 ~ 1000.0 | SERVER | Damage dealt once to each entity intersected by the HEAT jet. |
| munitions.heat.bigCannon | blockExplosionPower | 2.5 | 0.0 ~ 32.0 | SERVER | HEAT explosion power against blocks. |
| munitions.heat.bigCannon | entityExplosionPower | 2.5 | 0.0 ~ 32.0 | SERVER | HEAT explosion power against entities. |
| munitions.heat.bigCannon | jetLengthBlocks | 10.0 | 0.25 ~ 64.0 | SERVER | Maximum HEAT jet length through air, in blocks. |
| munitions.heat.bigCannon | jetEnergy | 32.0 | 0.0 ~ 1000.0 | SERVER | Energy available for penetrating CBC block armor. |
| munitions.heat.bigCannon | jetPenetration | 8.0 | 0.01 ~ 100.0 | SERVER | Resistance to CBC armor hardness; higher values preserve more jet energy. |
| munitions.heat.bigCannon | jetEntityDamage | 80.0 | 0.0 ~ 1000.0 | SERVER | Damage dealt once to each entity intersected by the HEAT jet. |
| munitions.heat.bigRocket | blockExplosionPower | 3.0 | 0.0 ~ 32.0 | SERVER | HEAT explosion power against blocks. |
| munitions.heat.bigRocket | entityExplosionPower | 4.0 | 0.0 ~ 32.0 | SERVER | HEAT explosion power against entities. |
| munitions.heat.bigRocket | jetLengthBlocks | 12.0 | 0.25 ~ 64.0 | SERVER | Maximum HEAT jet length through air, in blocks. |
| munitions.heat.bigRocket | jetEnergy | 40.0 | 0.0 ~ 1000.0 | SERVER | Energy available for penetrating CBC block armor. |
| munitions.heat.bigRocket | jetPenetration | 8.0 | 0.01 ~ 100.0 | SERVER | Resistance to CBC armor hardness; higher values preserve more jet energy. |
| munitions.heat.bigRocket | jetEntityDamage | 90.0 | 0.0 ~ 1000.0 | SERVER | Damage dealt once to each entity intersected by the HEAT jet. |
| munitions.cbcAt.autocannon.apds | entityDamage | 12.0 | 0.0 ~ 1000.0 | SERVER | Direct hit damage in health points, before armor and other native damage rules. |
| munitions.cbcAt.autocannon.apds | knockback | 0.3 | 0.0 ~ 1000.0 | SERVER | Native projectile knockback strength. |
| munitions.cbcAt.autocannon.apds | gravity | -0.02 | -1.0 ~ 0.0 | SERVER | Vertical ballistic acceleration in blocks/tick squared; zero disables gravity. |
| munitions.cbcAt.autocannon.apds | drag | 0.01 | 0.0 ~ 1.0 | SERVER | Native linear or quadratic drag coefficient. |
| munitions.cbcAt.autocannon.apds | quadraticDrag | false | true / false | SERVER | Quadratic native drag. |
| munitions.cbcAt.autocannon.apds | durabilityMass | 1.75 | 0.0 ~ 100.0 | SERVER | CBC penetrator mass consumed by impacts; distinct from rocket hull health. |
| munitions.cbcAt.autocannon.apds | penetration | 2.5 | 0.0 ~ 100.0 | SERVER | CBC armor penetration coefficient; not a fixed number of blocks. |
| munitions.cbcAt.autocannon.apds | toughness | 1.25 | 0.0 ~ 100.0 | SERVER | CBC resistance to projectile shatter. |
| munitions.cbcAt.autocannon.apds | deflection | 0.7 | 0.0 ~ 1.0 | SERVER | CBC angle coefficient used by the native ricochet calculation. |
| munitions.cbcAt.autocannon.apdsfs | entityDamage | 12.0 | 0.0 ~ 1000.0 | SERVER | Direct hit damage in health points, before armor and other native damage rules. |
| munitions.cbcAt.autocannon.apdsfs | knockback | 0.3 | 0.0 ~ 1000.0 | SERVER | Native projectile knockback strength. |
| munitions.cbcAt.autocannon.apdsfs | gravity | -0.016 | -1.0 ~ 0.0 | SERVER | Vertical ballistic acceleration in blocks/tick squared; zero disables gravity. |
| munitions.cbcAt.autocannon.apdsfs | drag | 0.00825 | 0.0 ~ 1.0 | SERVER | Native linear or quadratic drag coefficient. |
| munitions.cbcAt.autocannon.apdsfs | quadraticDrag | false | true / false | SERVER | Quadratic native drag. |
| munitions.cbcAt.autocannon.apdsfs | durabilityMass | 1.66 | 0.0 ~ 100.0 | SERVER | CBC penetrator mass consumed by impacts; distinct from rocket hull health. |
| munitions.cbcAt.autocannon.apdsfs | penetration | 2.77 | 0.0 ~ 100.0 | SERVER | CBC armor penetration coefficient; not a fixed number of blocks. |
| munitions.cbcAt.autocannon.apdsfs | toughness | 1.3 | 0.0 ~ 100.0 | SERVER | CBC resistance to projectile shatter. |
| munitions.cbcAt.autocannon.apdsfs | deflection | 0.7 | 0.0 ~ 1.0 | SERVER | CBC angle coefficient used by the native ricochet calculation. |
| munitions.cbcAt.autocannon.he | entityDamage | 9.0 | 0.0 ~ 1000.0 | SERVER | Direct hit damage in health points, before armor and other native damage rules. |
| munitions.cbcAt.autocannon.he | knockback | 0.5 | 0.0 ~ 1000.0 | SERVER | Native projectile knockback strength. |
| munitions.cbcAt.autocannon.he | gravity | -0.025 | -1.0 ~ 0.0 | SERVER | Vertical ballistic acceleration in blocks/tick squared; zero disables gravity. |
| munitions.cbcAt.autocannon.he | drag | 0.01 | 0.0 ~ 1.0 | SERVER | Native linear or quadratic drag coefficient. |
| munitions.cbcAt.autocannon.he | quadraticDrag | false | true / false | SERVER | Quadratic native drag. |
| munitions.cbcAt.autocannon.he | durabilityMass | 1.0 | 0.0 ~ 100.0 | SERVER | CBC penetrator mass consumed by impacts; distinct from rocket hull health. |
| munitions.cbcAt.autocannon.he | penetration | 0.9 | 0.0 ~ 100.0 | SERVER | CBC armor penetration coefficient; not a fixed number of blocks. |
| munitions.cbcAt.autocannon.he | toughness | 0.4 | 0.0 ~ 100.0 | SERVER | CBC resistance to projectile shatter. |
| munitions.cbcAt.autocannon.he | deflection | 0.7 | 0.0 ~ 1.0 | SERVER | CBC angle coefficient used by the native ricochet calculation. |
| munitions.cbcAt.autocannon.he | cbcFlakExplosionScale | 1.05 | 0.0 ~ 10.0 | SERVER | Multiplier applied to the corresponding CBC flak explosion property. |
| munitions.cbcAt.autocannon.hei | entityDamage | 9.0 | 0.0 ~ 1000.0 | SERVER | Direct hit damage in health points, before armor and other native damage rules. |
| munitions.cbcAt.autocannon.hei | knockback | 1.0 | 0.0 ~ 1000.0 | SERVER | Native projectile knockback strength. |
| munitions.cbcAt.autocannon.hei | gravity | -0.025 | -1.0 ~ 0.0 | SERVER | Vertical ballistic acceleration in blocks/tick squared; zero disables gravity. |
| munitions.cbcAt.autocannon.hei | drag | 0.01 | 0.0 ~ 1.0 | SERVER | Native linear or quadratic drag coefficient. |
| munitions.cbcAt.autocannon.hei | quadraticDrag | false | true / false | SERVER | Quadratic native drag. |
| munitions.cbcAt.autocannon.hei | durabilityMass | 1.0 | 0.0 ~ 100.0 | SERVER | CBC penetrator mass consumed by impacts; distinct from rocket hull health. |
| munitions.cbcAt.autocannon.hei | penetration | 1.0 | 0.0 ~ 100.0 | SERVER | CBC armor penetration coefficient; not a fixed number of blocks. |
| munitions.cbcAt.autocannon.hei | toughness | 0.4 | 0.0 ~ 100.0 | SERVER | CBC resistance to projectile shatter. |
| munitions.cbcAt.autocannon.hei | deflection | 0.7 | 0.0 ~ 1.0 | SERVER | CBC angle coefficient used by the native ricochet calculation. |
| munitions.cbcAt.heavyAutocannon.ap | entityDamage | 30.0 | 0.0 ~ 1000.0 | SERVER | Direct hit damage in health points, before armor and other native damage rules. |
| munitions.cbcAt.heavyAutocannon.ap | knockback | 1.825 | 0.0 ~ 1000.0 | SERVER | Native projectile knockback strength. |
| munitions.cbcAt.heavyAutocannon.ap | gravity | -0.03 | -1.0 ~ 0.0 | SERVER | Vertical ballistic acceleration in blocks/tick squared; zero disables gravity. |
| munitions.cbcAt.heavyAutocannon.ap | drag | 0.013 | 0.0 ~ 1.0 | SERVER | Native linear or quadratic drag coefficient. |
| munitions.cbcAt.heavyAutocannon.ap | quadraticDrag | true | true / false | SERVER | Quadratic native drag. |
| munitions.cbcAt.heavyAutocannon.ap | durabilityMass | 3.25 | 0.0 ~ 100.0 | SERVER | CBC penetrator mass consumed by impacts; distinct from rocket hull health. |
| munitions.cbcAt.heavyAutocannon.ap | penetration | 2.75 | 0.0 ~ 100.0 | SERVER | CBC armor penetration coefficient; not a fixed number of blocks. |
| munitions.cbcAt.heavyAutocannon.ap | toughness | 1.33 | 0.0 ~ 100.0 | SERVER | CBC resistance to projectile shatter. |
| munitions.cbcAt.heavyAutocannon.ap | deflection | 0.66 | 0.0 ~ 1.0 | SERVER | CBC angle coefficient used by the native ricochet calculation. |
| munitions.cbcAt.heavyAutocannon.apds | entityDamage | 25.0 | 0.0 ~ 1000.0 | SERVER | Direct hit damage in health points, before armor and other native damage rules. |
| munitions.cbcAt.heavyAutocannon.apds | knockback | 1.3 | 0.0 ~ 1000.0 | SERVER | Native projectile knockback strength. |
| munitions.cbcAt.heavyAutocannon.apds | gravity | -0.02 | -1.0 ~ 0.0 | SERVER | Vertical ballistic acceleration in blocks/tick squared; zero disables gravity. |
| munitions.cbcAt.heavyAutocannon.apds | drag | 0.011 | 0.0 ~ 1.0 | SERVER | Native linear or quadratic drag coefficient. |
| munitions.cbcAt.heavyAutocannon.apds | quadraticDrag | true | true / false | SERVER | Quadratic native drag. |
| munitions.cbcAt.heavyAutocannon.apds | durabilityMass | 3.0 | 0.0 ~ 100.0 | SERVER | CBC penetrator mass consumed by impacts; distinct from rocket hull health. |
| munitions.cbcAt.heavyAutocannon.apds | penetration | 3.1 | 0.0 ~ 100.0 | SERVER | CBC armor penetration coefficient; not a fixed number of blocks. |
| munitions.cbcAt.heavyAutocannon.apds | toughness | 1.45 | 0.0 ~ 100.0 | SERVER | CBC resistance to projectile shatter. |
| munitions.cbcAt.heavyAutocannon.apds | deflection | 0.66 | 0.0 ~ 1.0 | SERVER | CBC angle coefficient used by the native ricochet calculation. |
| munitions.cbcAt.heavyAutocannon.apdsfs | entityDamage | 24.5 | 0.0 ~ 1000.0 | SERVER | Direct hit damage in health points, before armor and other native damage rules. |
| munitions.cbcAt.heavyAutocannon.apdsfs | knockback | 1.2 | 0.0 ~ 1000.0 | SERVER | Native projectile knockback strength. |
| munitions.cbcAt.heavyAutocannon.apdsfs | gravity | -0.0175 | -1.0 ~ 0.0 | SERVER | Vertical ballistic acceleration in blocks/tick squared; zero disables gravity. |
| munitions.cbcAt.heavyAutocannon.apdsfs | drag | 0.008 | 0.0 ~ 1.0 | SERVER | Native linear or quadratic drag coefficient. |
| munitions.cbcAt.heavyAutocannon.apdsfs | quadraticDrag | true | true / false | SERVER | Quadratic native drag. |
| munitions.cbcAt.heavyAutocannon.apdsfs | durabilityMass | 2.67 | 0.0 ~ 100.0 | SERVER | CBC penetrator mass consumed by impacts; distinct from rocket hull health. |
| munitions.cbcAt.heavyAutocannon.apdsfs | penetration | 3.33 | 0.0 ~ 100.0 | SERVER | CBC armor penetration coefficient; not a fixed number of blocks. |
| munitions.cbcAt.heavyAutocannon.apdsfs | toughness | 1.5 | 0.0 ~ 100.0 | SERVER | CBC resistance to projectile shatter. |
| munitions.cbcAt.heavyAutocannon.apdsfs | deflection | 0.66 | 0.0 ~ 1.0 | SERVER | CBC angle coefficient used by the native ricochet calculation. |
| munitions.cbcAt.heavyAutocannon.he | entityDamage | 27.0 | 0.0 ~ 1000.0 | SERVER | Direct hit damage in health points, before armor and other native damage rules. |
| munitions.cbcAt.heavyAutocannon.he | knockback | 1.825 | 0.0 ~ 1000.0 | SERVER | Native projectile knockback strength. |
| munitions.cbcAt.heavyAutocannon.he | gravity | -0.03 | -1.0 ~ 0.0 | SERVER | Vertical ballistic acceleration in blocks/tick squared; zero disables gravity. |
| munitions.cbcAt.heavyAutocannon.he | drag | 0.013 | 0.0 ~ 1.0 | SERVER | Native linear or quadratic drag coefficient. |
| munitions.cbcAt.heavyAutocannon.he | quadraticDrag | true | true / false | SERVER | Quadratic native drag. |
| munitions.cbcAt.heavyAutocannon.he | durabilityMass | 1.6 | 0.0 ~ 100.0 | SERVER | CBC penetrator mass consumed by impacts; distinct from rocket hull health. |
| munitions.cbcAt.heavyAutocannon.he | penetration | 2.3 | 0.0 ~ 100.0 | SERVER | CBC armor penetration coefficient; not a fixed number of blocks. |
| munitions.cbcAt.heavyAutocannon.he | toughness | 0.8 | 0.0 ~ 100.0 | SERVER | CBC resistance to projectile shatter. |
| munitions.cbcAt.heavyAutocannon.he | deflection | 0.66 | 0.0 ~ 1.0 | SERVER | CBC angle coefficient used by the native ricochet calculation. |
| munitions.cbcAt.heavyAutocannon.he | cbcFlakExplosionScale | 1.66 | 0.0 ~ 10.0 | SERVER | Multiplier applied to the corresponding CBC flak explosion property. |
| munitions.cbcAt.heavyAutocannon.hef | entityDamage | 28.0 | 0.0 ~ 1000.0 | SERVER | Direct hit damage in health points, before armor and other native damage rules. |
| munitions.cbcAt.heavyAutocannon.hef | knockback | 1.8 | 0.0 ~ 1000.0 | SERVER | Native projectile knockback strength. |
| munitions.cbcAt.heavyAutocannon.hef | gravity | -0.033 | -1.0 ~ 0.0 | SERVER | Vertical ballistic acceleration in blocks/tick squared; zero disables gravity. |
| munitions.cbcAt.heavyAutocannon.hef | drag | 0.013 | 0.0 ~ 1.0 | SERVER | Native linear or quadratic drag coefficient. |
| munitions.cbcAt.heavyAutocannon.hef | quadraticDrag | true | true / false | SERVER | Quadratic native drag. |
| munitions.cbcAt.heavyAutocannon.hef | durabilityMass | 1.6 | 0.0 ~ 100.0 | SERVER | CBC penetrator mass consumed by impacts; distinct from rocket hull health. |
| munitions.cbcAt.heavyAutocannon.hef | penetration | 2.125 | 0.0 ~ 100.0 | SERVER | CBC armor penetration coefficient; not a fixed number of blocks. |
| munitions.cbcAt.heavyAutocannon.hef | toughness | 0.875 | 0.0 ~ 100.0 | SERVER | CBC resistance to projectile shatter. |
| munitions.cbcAt.heavyAutocannon.hef | deflection | 0.66 | 0.0 ~ 1.0 | SERVER | CBC angle coefficient used by the native ricochet calculation. |
| munitions.cbcAt.heavyAutocannon.hef | cbcFlakBlockExplosionScale | 1.3 | 0.0 ~ 10.0 | SERVER | Multiplier applied to the corresponding CBC flak explosion property. |
| munitions.cbcAt.heavyAutocannon.hef | cbcFlakEntityExplosionScale | 1.4 | 0.0 ~ 10.0 | SERVER | Multiplier applied to the corresponding CBC flak explosion property. |
| munitions.cbcAt.heavyAutocannon.hef | cbcFlakFragmentCountScale | 8.0 | 0.0 ~ 10.0 | SERVER | Multiplier applied to the CBC flak fragment count. |
| munitions.cbcAt.heavyAutocannon.hef | cbcFlakFragmentSpreadScale | 4.0 | 0.0 ~ 10.0 | SERVER | Multiplier applied to the CBC flak fragment spread. |
| munitions.cbcAt.heavyAutocannon.heat | entityDamage | 28.0 | 0.0 ~ 1000.0 | SERVER | Direct hit damage in health points, before armor and other native damage rules. |
| munitions.cbcAt.heavyAutocannon.heat | knockback | 1.75 | 0.0 ~ 1000.0 | SERVER | Native projectile knockback strength. |
| munitions.cbcAt.heavyAutocannon.heat | gravity | -0.035 | -1.0 ~ 0.0 | SERVER | Vertical ballistic acceleration in blocks/tick squared; zero disables gravity. |
| munitions.cbcAt.heavyAutocannon.heat | drag | 0.012 | 0.0 ~ 1.0 | SERVER | Native linear or quadratic drag coefficient. |
| munitions.cbcAt.heavyAutocannon.heat | quadraticDrag | true | true / false | SERVER | Quadratic native drag. |
| munitions.cbcAt.heavyAutocannon.heat | durabilityMass | 1.55 | 0.0 ~ 100.0 | SERVER | CBC penetrator mass consumed by impacts; distinct from rocket hull health. |
| munitions.cbcAt.heavyAutocannon.heat | penetration | 2.2 | 0.0 ~ 100.0 | SERVER | CBC armor penetration coefficient; not a fixed number of blocks. |
| munitions.cbcAt.heavyAutocannon.heat | toughness | 0.85 | 0.0 ~ 100.0 | SERVER | CBC resistance to projectile shatter. |
| munitions.cbcAt.heavyAutocannon.heat | deflection | 0.66 | 0.0 ~ 1.0 | SERVER | CBC angle coefficient used by the native ricochet calculation. |
| munitions.cbcAt.heavyAutocannon.smoke | entityDamage | 25.0 | 0.0 ~ 1000.0 | SERVER | Direct hit damage in health points, before armor and other native damage rules. |
| munitions.cbcAt.heavyAutocannon.smoke | knockback | 1.0 | 0.0 ~ 1000.0 | SERVER | Native projectile knockback strength. |
| munitions.cbcAt.heavyAutocannon.smoke | gravity | -0.035 | -1.0 ~ 0.0 | SERVER | Vertical ballistic acceleration in blocks/tick squared; zero disables gravity. |
| munitions.cbcAt.heavyAutocannon.smoke | drag | 0.005 | 0.0 ~ 1.0 | SERVER | Native linear or quadratic drag coefficient. |
| munitions.cbcAt.heavyAutocannon.smoke | quadraticDrag | true | true / false | SERVER | Quadratic native drag. |
| munitions.cbcAt.heavyAutocannon.smoke | durabilityMass | 0.75 | 0.0 ~ 100.0 | SERVER | CBC penetrator mass consumed by impacts; distinct from rocket hull health. |
| munitions.cbcAt.heavyAutocannon.smoke | penetration | 0.5 | 0.0 ~ 100.0 | SERVER | CBC armor penetration coefficient; not a fixed number of blocks. |
| munitions.cbcAt.heavyAutocannon.smoke | toughness | 0.8 | 0.0 ~ 100.0 | SERVER | CBC resistance to projectile shatter. |
| munitions.cbcAt.heavyAutocannon.smoke | deflection | 0.7 | 0.0 ~ 1.0 | SERVER | CBC angle coefficient used by the native ricochet calculation. |
| munitions.cbcAt.heavyAutocannon.smoke | blockExplosionPower | 1.66 | 0.0 ~ 32.0 | SERVER | Explosion strength against terrain, subject to CBC damage restrictions. |
| munitions.cbcAt.heavyAutocannon.smoke | entityExplosionPower | 2.0 | 0.0 ~ 32.0 | SERVER | Explosion strength against entities. |
| munitions.cbcAt.heavyAutocannon.smoke | smokeDurationScale | 0.75 | 0.0 ~ 10.0 | SERVER | Multiplier applied to native smoke lifetime. |
| munitions.cbcAt.heavyAutocannon.smoke | smokeSizeScale | 0.3 | 0.0 ~ 10.0 | SERVER | Multiplier applied to native smoke size. |
| munitions.cbcAt.bigCannon.cluster | entityDamage | 28.0 | 0.0 ~ 1000.0 | SERVER | Direct hit damage in health points, before armor and other native damage rules. |
| munitions.cbcAt.bigCannon.cluster | knockback | 2.5 | 0.0 ~ 1000.0 | SERVER | Native projectile knockback strength. |
| munitions.cbcAt.bigCannon.cluster | gravity | -0.05 | -1.0 ~ 0.0 | SERVER | Vertical ballistic acceleration in blocks/tick squared; zero disables gravity. |
| munitions.cbcAt.bigCannon.cluster | drag | 0.02 | 0.0 ~ 1.0 | SERVER | Native linear or quadratic drag coefficient. |
| munitions.cbcAt.bigCannon.cluster | quadraticDrag | false | true / false | SERVER | Quadratic native drag. |
| munitions.cbcAt.bigCannon.cluster | durabilityMass | 1.75 | 0.0 ~ 100.0 | SERVER | CBC penetrator mass consumed by impacts; distinct from rocket hull health. |
| munitions.cbcAt.bigCannon.cluster | penetration | 1.0 | 0.0 ~ 100.0 | SERVER | CBC armor penetration coefficient; not a fixed number of blocks. |
| munitions.cbcAt.bigCannon.cluster | toughness | 1.0 | 0.0 ~ 100.0 | SERVER | CBC resistance to projectile shatter. |
| munitions.cbcAt.bigCannon.cluster | deflection | 0.7 | 0.0 ~ 1.0 | SERVER | CBC angle coefficient used by the native ricochet calculation. |
| munitions.cbcAt.bigCannon.cluster | addedChargePower | 0.0 | 0.0 ~ 100.0 | SERVER | Additional native big-cannon charge power. |
| munitions.cbcAt.bigCannon.cluster | minimumChargePower | 1.0 | 0.0 ~ 1000.0 | SERVER | Minimum charge power used by native squib checks. |
| munitions.cbcAt.bigCannon.cluster | canSquib | true | true / false | SERVER | Native insufficient-charge squib checks. |
| munitions.cbcAt.bigCannon.cluster | addedRecoil | 1.0 | 0.0 ~ 100.0 | SERVER | Additional native big-cannon recoil. |
| munitions.cbcAt.bigCannon.cluster | submunitionLifetimeTicks | 50 | 1 ~ 12000 | SERVER | Maximum emitted submunition lifetime in ticks (20 ticks per second). |
| munitions.cbcAt.bigCannon.cluster | submunitionSpeedScale | 1.0 | 0.0 ~ 10.0 | SERVER | Multiplier applied to native cluster release speed. |
| munitions.cbcAt.bigCannon.cluster | submunitionInaccuracyDegrees | 25.0 | 0.0 ~ 180.0 | SERVER | Native shoot inaccuracy parameter for cluster release; larger values widen the spread. |
| munitions.cbcAt.bigCannon.cluster | submunitionCount | 8 | 1 ~ 64 | SERVER | Emitted cluster submunitions; crafting still consumes eight parts. Fuzes repeat in assembly order. |
| munitions.fix.bigCannon.flak | entityDamage | 10.0 | 0.0 ~ 1000.0 | SERVER | Direct hit damage in health points, before armor and other native damage rules. |
| munitions.fix.bigCannon.flak | knockback | 1.0 | 0.0 ~ 1000.0 | SERVER | Native projectile knockback strength. |
| munitions.fix.bigCannon.flak | gravity | -0.05 | -1.0 ~ 0.0 | SERVER | Vertical ballistic acceleration in blocks/tick squared; zero disables gravity. |
| munitions.fix.bigCannon.flak | drag | 0.01 | 0.0 ~ 1.0 | SERVER | Native linear or quadratic drag coefficient. |
| munitions.fix.bigCannon.flak | quadraticDrag | false | true / false | SERVER | Quadratic native drag. |
| munitions.fix.bigCannon.flak | durabilityMass | 2.0 | 0.0 ~ 100.0 | SERVER | CBC penetrator mass consumed by impacts; distinct from rocket hull health. |
| munitions.fix.bigCannon.flak | penetration | 1.0 | 0.0 ~ 100.0 | SERVER | CBC armor penetration coefficient; not a fixed number of blocks. |
| munitions.fix.bigCannon.flak | toughness | 1.0 | 0.0 ~ 100.0 | SERVER | CBC resistance to projectile shatter. |
| munitions.fix.bigCannon.flak | deflection | 0.7 | 0.0 ~ 1.0 | SERVER | CBC angle coefficient used by the native ricochet calculation. |
| munitions.fix.bigCannon.flak | addedChargePower | 0.0 | 0.0 ~ 100.0 | SERVER | Additional native big-cannon charge power. |
| munitions.fix.bigCannon.flak | minimumChargePower | 1.0 | 0.0 ~ 1000.0 | SERVER | Minimum charge power used by native squib checks. |
| munitions.fix.bigCannon.flak | canSquib | true | true / false | SERVER | Native insufficient-charge squib checks. |
| munitions.fix.bigCannon.flak | addedRecoil | 1.0 | 0.0 ~ 100.0 | SERVER | Additional native big-cannon recoil. |
| munitions.fix.bigCannon.flak | blockExplosionPower | 2.0 | 0.0 ~ 32.0 | SERVER | Explosion strength against terrain, subject to CBC damage restrictions. |
| munitions.fix.bigCannon.flak | entityExplosionPower | 2.0 | 0.0 ~ 32.0 | SERVER | Explosion strength against entities. |
| munitions.fix.bigCannon.flak | shrapnelCount | 45 | 0 ~ 256 | SERVER | Number of native fragment projectiles per detonation. |
| munitions.fix.bigCannon.flak | burstParticleProjectileCount | 30 | 0 ~ 256 | SERVER | Number of native fragment projectiles per detonation. |
| munitions.fix.bigCannon.heavyHe | entityDamage | 30.0 | 0.0 ~ 1000.0 | SERVER | Direct hit damage in health points, before armor and other native damage rules. |
| munitions.fix.bigCannon.heavyHe | knockback | 3.0 | 0.0 ~ 1000.0 | SERVER | Native projectile knockback strength. |
| munitions.fix.bigCannon.heavyHe | gravity | -0.07 | -1.0 ~ 0.0 | SERVER | Vertical ballistic acceleration in blocks/tick squared; zero disables gravity. |
| munitions.fix.bigCannon.heavyHe | drag | 0.015 | 0.0 ~ 1.0 | SERVER | Native linear or quadratic drag coefficient. |
| munitions.fix.bigCannon.heavyHe | quadraticDrag | false | true / false | SERVER | Quadratic native drag. |
| munitions.fix.bigCannon.heavyHe | durabilityMass | 4.0 | 0.0 ~ 100.0 | SERVER | CBC penetrator mass consumed by impacts; distinct from rocket hull health. |
| munitions.fix.bigCannon.heavyHe | penetration | 1.0 | 0.0 ~ 100.0 | SERVER | CBC armor penetration coefficient; not a fixed number of blocks. |
| munitions.fix.bigCannon.heavyHe | toughness | 1.0 | 0.0 ~ 100.0 | SERVER | CBC resistance to projectile shatter. |
| munitions.fix.bigCannon.heavyHe | deflection | 0.7 | 0.0 ~ 1.0 | SERVER | CBC angle coefficient used by the native ricochet calculation. |
| munitions.fix.bigCannon.heavyHe | addedChargePower | 0.0 | 0.0 ~ 100.0 | SERVER | Additional native big-cannon charge power. |
| munitions.fix.bigCannon.heavyHe | minimumChargePower | 1.0 | 0.0 ~ 1000.0 | SERVER | Minimum charge power used by native squib checks. |
| munitions.fix.bigCannon.heavyHe | canSquib | true | true / false | SERVER | Native insufficient-charge squib checks. |
| munitions.fix.bigCannon.heavyHe | addedRecoil | 2.0 | 0.0 ~ 100.0 | SERVER | Additional native big-cannon recoil. |
| munitions.fix.bigCannon.heavyHe | blockExplosionPower | 8.0 | 0.0 ~ 32.0 | SERVER | Explosion strength against terrain, subject to CBC damage restrictions. |
| munitions.fix.bigCannon.heavyHe | entityExplosionPower | 10.0 | 0.0 ~ 32.0 | SERVER | Explosion strength against entities. |
| munitions.fix.bigCannon.heavyHe | maximumSpeedBlocksPerTick | 4.0 | 0.1 ~ 20.0 | SERVER | Projectile speed cap in blocks per tick. |
| munitions.fix.bigCannon.heat | entityDamage | 25.0 | 0.0 ~ 1000.0 | SERVER | Direct hit damage in health points, before armor and other native damage rules. |
| munitions.fix.bigCannon.heat | knockback | 1.0 | 0.0 ~ 1000.0 | SERVER | Native projectile knockback strength. |
| munitions.fix.bigCannon.heat | gravity | -0.05 | -1.0 ~ 0.0 | SERVER | Vertical ballistic acceleration in blocks/tick squared; zero disables gravity. |
| munitions.fix.bigCannon.heat | drag | 0.01 | 0.0 ~ 1.0 | SERVER | Native linear or quadratic drag coefficient. |
| munitions.fix.bigCannon.heat | quadraticDrag | false | true / false | SERVER | Quadratic native drag. |
| munitions.fix.bigCannon.heat | durabilityMass | 2.0 | 0.0 ~ 100.0 | SERVER | CBC penetrator mass consumed by impacts; distinct from rocket hull health. |
| munitions.fix.bigCannon.heat | penetration | 2.0 | 0.0 ~ 100.0 | SERVER | CBC armor penetration coefficient; not a fixed number of blocks. |
| munitions.fix.bigCannon.heat | toughness | 2.0 | 0.0 ~ 100.0 | SERVER | CBC resistance to projectile shatter. |
| munitions.fix.bigCannon.heat | deflection | 0.5 | 0.0 ~ 1.0 | SERVER | CBC angle coefficient used by the native ricochet calculation. |
| munitions.fix.bigCannon.heat | addedChargePower | 0.0 | 0.0 ~ 100.0 | SERVER | Additional native big-cannon charge power. |
| munitions.fix.bigCannon.heat | minimumChargePower | 1.0 | 0.0 ~ 1000.0 | SERVER | Minimum charge power used by native squib checks. |
| munitions.fix.bigCannon.heat | canSquib | true | true / false | SERVER | Native insufficient-charge squib checks. |
| munitions.fix.bigCannon.heat | addedRecoil | 1.2 | 0.0 ~ 100.0 | SERVER | Additional native big-cannon recoil. |
| launchers | spreadMultiplier | 1.0 | 0.0 ~ 4.0 | SERVER | Multiplier after native CBCAT material/barrel-length spread. Native RNG is preserved. |
| launchers | fireRateMultiplier | 1.0 | 0.1 ~ 4.0 | SERVER | Firing-rate multiplier; minimum cooldown is one tick. Large rails retain their slower rate. |
| launchers | extraLaneFireRateBonus | 0.25 | 0.0 ~ 1.0 | SERVER | Additional firing-rate fraction per attached lane. |
| compat.sable | inheritedVelocityMultiplier | 1.0 | 0.0 ~ 2.0 | SERVER | Fraction of native Sable velocity inherited at rocket launch; Sable remains optional. |
