# Visual assets для Blockbench

Побайтові копії наявних вихідних ресурсів станом на 2026-10-07. Папка не підключена до збірки: редагування копій саме по собі не змінює гру.

## Структура і мапа

`art_reference/assets/<namespace>/<path>` відповідає `src/main/resources/assets/<namespace>/<path>`. Namespace і всі вкладені папки збережено, щоб уникнути колізій. Усередині є `models/block`, `models/item`, `textures/block` та всі інші наявні texture підпапки, `blockstates`.

`file_map.csv` містить повну пофайлову відповідність і SHA256. `references.csv` перелічує parent/model/texture посилання. `Local=False` означає відсутність відповідного JSON/PNG у вихідних assets проєкту: це можуть бути залежності, vanilla або генеровані моделі, а не обов'язково помилка гри.

## Основні групи

| Група | Копії та обмеження |
|---|---|
| Small launcher rail/breech | `cbc_at/models/block/rocket_pod/*_assembled_rocket_pod_rail.json`, `cbc_at/models/item/steel_rocket_pod_breech.json`. Rail JSON посилаються на parent з залежності. Базова геометрія Small `rocket_pod.json` та steel breech генерується; вихідних локальних PNG rail/breech немає. |
| Rapid Cycling Module | `cbcatfix/models/item/*rapid_cycling_module*.json`, відповідні blockstates і block/item mould JSON. Основна block-геометрія готового, unfinished і unbored модулів генерується. |
| Traverse Gear Module | `cbcatfix/models/item/*traverse_gear_module*.json`, відповідні blockstates і block/item mould JSON. Основна block-геометрія готового, unfinished і unbored модулів генерується. |
| Medium launcher | `cbc_at/blockstates/wrought_iron_medium_rocket_rail*.json`; їх `cbc_at:block/medium_rocket_rail/...` models відсутні у вихідних assets і належать залежності. Наявні medium rocket item/base JSON включено. |
| Large launcher | `cbcatfix/models/block/big_rocket_rail*.json`, `models/item/big_rocket_rail*.json`, відповідні blockstates; big rocket і fuel/payload variants також включено. |
| Cluster variants | `cbc_at/models/block/cluster_block.json`, `models/block/projectile_cartridge/cluster*.json`, `models/item/cluster*.json`; усі HE/HEAT/HEF/smoke та cartridge/caseless JSON у `cbcatfix/models/item/cluster*.json`. Існуючі source JSON включено навіть якщо раніше їх створював допоміжний скрипт. |
| Heavy autocannon projectile visuals | Окремого повного набору вихідних моделей heavy autocannon ammo залежності тут немає. `HeavyAutocannonProjectileRendererMixin.java` підбирає `AbstractHeavyAutocannonProjectileItem` за entity type та малює його item model, задаючи орієнтацію і зміщення кодом. Наявні Fix `heavy_he_shell`, `heat_shell`, `flak_shell` block/item JSON, fuze variants і projectile PNG включено; вони не є повним набором heavy autocannon ammo. |

Шляхи в таблиці наведено відносно `art_reference/assets/`.

## Генеровані та runtime visuals

`build.gradle`, задача `generateLauncherModel`, створює Small tube mesh, steel breech та Rapid/Traverse модулі з unfinished/unbored варіантами у `build/generated/launcherResources`. Small геометрія залежить від констант `src/main/java/com/cbcatfix/rocket/RocketMounts.java`. Генеровані файли не копіювалися і не відтворювалися. Текстури цих моделей також використовують ресурси CBC/CBCAT залежностей.

`UnifiedRocketRenderer`, `RocketModelRenderer`, `MountedRocketRenderer` та `RocketBlockEntityRenderer` використовують item models і задають runtime розташування, масштаб та орієнтацію. `ClusterVisuals` і `ClusterProjectileVisualMixin` використовують item models cluster variants. Blockbench не відтворює ці code-driven transforms, tracer effects чи динамічні fuzes.

## Як редагувати

Відкривай геометричні JSON у Blockbench. Якщо файл містить лише `parent`, шукай його через `references.csv`: за `Local=False` повного parent немає у копії. PNG можна редагувати окремо; може знадобитися вручну підключити текстуру, бо resource-location references збережено без змін. Blockstates є мапою станів і поворотів, а не геометрією.

Скопійовано лише models, textures і blockstates з `src/main/resources/assets`. Build/generated/cache, Java, рецепти, lang і sounds не копіювалися. Ресурси з JAR залежностей не витягувалися. Фейкових JSON/PNG немає. Оригінали не змінено; зворотне перенесення копій не виконувалося.
