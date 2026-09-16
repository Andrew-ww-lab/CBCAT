# Architecture / dependency / optimization audit

Дата: 2026-09-15. Проєкт: CBCAT Fix (`cbcatfix`, у запиті — Sable Rocket). Перевірено весь production source, а не лише останні ракетні зміни. Базовий знімок зроблено до очищення: `build/architecture-baseline.json`. Повний поіменний перелік класів, вкладених типів, батьків, інтерфейсів, залежностей, сторін і mixin-цілей: [architecture-class-map.md](architecture-class-map.md).

Результат: **133 → 128 Java-файлів; 9216 → 8864 рядки; 59 → 55 активних mixin; 517 → 493 вихідні ресурси**. Видалено шість класів, додано один необхідний `IMixinConfigPlugin` для необов'язкових інтеграцій. Нові gameplay-механіки не додавалися. `LauncherTransfer.java` побайтово збігається з початковим знімком; виправлення дюпу не перероблялося. Наявні зміни налаштувань польоту у робочому дереві збережено; вони не є оптимізаціями цього аудиту.

**Sable — OPTIONAL.** Мінімальні клієнт і dedicated server справді запущено без Sable/Aeronautics/Radar/Jade/JEI. Обидва клієнти відкрили звичайний світ; обидва dedicated server дійшли до `Done`. Повний набір інтеграцій має сторонні попередження/помилки журналу, описані нижче: результат не означає «всі залежності без попереджень».

## A. FULL MOD STRUCTURE

Повний перелік **кожного** production-файлу з реальною декларацією й обґрунтуванням збереження наведено у [карті класів](architecture-class-map.md). Логічна структура:

```text
CBCAT Fix
├── bootstrap / registry
│   ├── CbcatFix: NeoForge events, sounds, config, creative tab
│   ├── CbcatFixMunitions: blocks, items, entity types, BE types
│   └── CbcatFixConfig: native ModConfigSpec, tier/munition records
├── placed ammunition
│   ├── RocketBlock → CBC FuzedProjectileBlock
│   ├── RocketBlockEntity → CBC FuzedBlockEntity; Rockets is authoritative
│   ├── RocketBodyExtensionBlock + RocketFootprint: occupied cells/root
│   └── RocketGroundPlacement / RocketTargetResolver / RocketGeometry
├── launcher adapters
│   ├── BigRocketRailBlock / BigRocketRailBreechBlock → CBCAT medium blocks
│   ├── native CBCAT breech buffers → MountedRocketStorage / ItemHandler
│   ├── RocketMounts: slot/launch transform and mesh dimensions
│   ├── LauncherAssembly: attached lanes; original contraption owns blocks
│   ├── LauncherCooldown: original timer access
│   └── LauncherTransfer: existing transfer ownership transaction
├── flight / impact
│   ├── native CBCAT small/medium projectile lifecycle
│   ├── RocketFlightState + RocketSteering: fuel, thrust, angular response
│   ├── RocketBallistics / RocketPenetrationBalance → CBC penetration
│   └── RocketDamage / RocketDurabilityMixin: health and engine failure
├── payload / fuze
│   ├── native item factory → RocketProjectileFactory
│   ├── RocketStackFactory / RocketFuzingSupport: native components
│   ├── RocketDetonationContext / guard / invoker → native detonate
│   └── ClusterPayload, HeatEffect and custom registered shell types
├── guidance
│   ├── RocketSeeker: designated UUID, last known point, native LOS
│   └── optional RadarDesignation / SableSeekerTarget
├── Sable compatibility (optional)
│   ├── RocketSableCompat / RocketViewRay: native transforms/velocity
│   └── SableRocketDetacher: native assembly/rigid-body API
├── client
│   ├── ClientSetup, UnifiedRocketRenderer, placed/mounted renderers
│   ├── RocketClientEvents + RocketEngineSound
│   └── client mixins, optional Jade provider and JEI adapter
└── compatibility / resources
    ├── CBCAT → current CBC ABI bridges
    ├── native recipes, loot tables, tags, models, languages and sounds
    └── no custom packet channel, network framework or access transformer
```

Перевірено `build.gradle`, wrapper/run-конфігурації, `META-INF/neoforge.mods.toml`, `cbcatfix.mixins.json`, `pack.mcmeta`, генерацію launcher mesh, opt-in dev GameTests. Власних service loader-файлів, access transformer, production datagen-класів або окремого мережевого протоколу немає. Тестові Java/structure включаються лише властивістю `rocketTests`; її під час аудиту не використовували, JAR явно їх виключає.

Ресурси після очищення: 299 CBCAT loot tables, 33 CBCAT recipes, 22 CBCAT munition properties, 46 власних recipes, 6 власних loot tables, 5 власних munition properties, 11 tags, 42 вихідні models, 10 blockstates, 12 textures, 2 мови, 1 OGG, sounds/config/metadata. Launcher model додатково генерується Gradle з `RocketMounts`. Усі JSON розібрані; прямі model/parent/texture/blockstate/sound посилання власних ресурсів знайдено у поточному наборі ресурсів та обов'язкових JAR. Це не перевірка кожної чужої моделі або візуальної якості всіх предметів.

Loot tables/recipes/tags використовуються resource loader за ID, тому відсутність Java-викликів не робить їх мертвими. Мови, моделі інвентарю й munition properties також завантажуються непрямо. Збережено всі 12 texture: це бокові/верхні/нижні грані чотирьох зареєстрованих оболонок. Єдиний залишений OGG прямо використовується `sounds.json`.

## B. INHERITANCE TREE

| Класи | Реальний батько / контракт | Власник | Висновок |
|---|---|---|---|
| RocketBlock | FuzedProjectileBlock<RocketBlockEntity, RocketBlockProjectile> | CBC | Правильно: reuse placed ammunition, fuze/block lifecycle. |
| RocketBlockEntity | FuzedBlockEntity | CBC | Правильно: додано лише stack/slot дані; native fuze/container callbacks збережено. |
| RocketBlockProjectile | FuzedBigCannonProjectile | CBC | Адаптер обмеження generic CBC: ENTITY має бути FuzedBigCannonProjectile, а CBCAT rockets — інша гілка AbstractCannonProjectile. Одразу делегує native rocket factory; не є другим польотним двигуном. |
| RocketBodyExtensionBlock | DirectionalBlock | Minecraft | Коректний службовий блок зайнятого об'єму; payload зберігає тільки root BE. |
| BigRocketRailBlock | MediumRocketPodBarrelBlock | CBCAT | Це справді рейка того самого типу; штатний barrel BE з доданим valid block. |
| BigRocketRailBreechBlock | MediumRocketPodBreechBlock | CBCAT | Це справді breech; використовується штатний medium BE. |
| BigAPRocketItem / BigHERocketItem / BigHEATRocketItem | APMediumRocketItem / HEMediumRocketItem / HEATMediumRocketItem | CBCAT | Правильна спеціалізація фабрики предмета; зареєстровані типи потрібні. |
| BigAPRocketProjectile | AbstractMediumRocket | CBCAT → CBC AbstractCannonProjectile | Правильна AP-гілка без ф'юзу. |
| BigHERocketProjectile / BigHEATRocketProjectile | AbstractMediumFuzedRocket | CBCAT → AbstractMediumRocket → CBC | Правильна fuzed-гілка; native lifecycle збережено. |
| Flak / HeavyHE / HEAT / HESH ShellBlock | SimpleShellBlock відповідного projectile | CBC | Правильна оболонка штатного боєприпасу. |
| Flak / HeavyHE / HEAT / HESH ShellProjectile | FuzedBigCannonProjectile | CBC | Native tick, fuze, damage lifecycle; власний payload effect. |
| CbcatFixFuzedBlockEntity | FuzedBlockEntity | CBC | Потрібен власний зареєстрований BE type для цих shell blocks. |
| RocketArmPoint | DepositOnlyArmInteractionPoint | Create | Native insertion simulation, capability cache, transfer, animation. |
| MountedRocketItemHandler | IItemHandlerModifiable | NeoForge | View над буфером CBCAT, без другого сховища. |
| UnifiedRocketRenderer | EntityRenderer<T> | Minecraft | Native ItemRenderer малює існуючу модель із ракетною орієнтацією. |
| RocketBlockEntityRenderer / MountedRocketRenderer | BlockEntityRenderer<T> | Minecraft | Native renderer contract; спеціалізація кількох слотів. |
| RocketEngineSound | AbstractTickableSoundInstance | Minecraft | Native sound engine/attenuation. |
| RocketFuzingSupport / recipe mixins | делегування існуючим CBCAT/Create recipe types | CBCAT / Create | Новий recipe type/serializer framework не створювався. |

Власних зайвих abstract base class не виявлено. Видалено непотрібний `BigRocketRenderer extends MediumRocketRenderer`, який не був зареєстрований. Інші наведені наслідування не замінювалися композицією: вони відповідають предметній моделі. `RocketBlockProjectile` — свідомо вузький адаптер CBC generic-контракту, а не твердження, що ракета є суцільним артилерійським снарядом.

## C. DEPENDENCY MATRIX

| Dependency / перевірена версія | Required? / причина | Compile scope | Runtime scope | Optional? |
|---|---|---|---|---|
| Java 21, Minecraft 1.21.1, NeoForge 21.1.228 | Так, loader/API | java toolchain / moddev | loader | Ні |
| Create 6.0.10 | Так, contraptions/arm/render/recipes | implementation | required | Ні |
| CBC 5.11.7 | Так, ammunition/penetration/fuzes | implementation | required | Ні |
| CBCAT 0.1.4c (cbc_at_Neoforge_1.21.1_0.1.4c.jar) | Так, native rockets/launchers | explicit implementation | required | Ні |
| Ritchie's Projectile Library 2.1.2 | Так, обов'язкова залежність CBC | implementation | required | Ні |
| Ponder 1.0.82+mc1.21.1 | Обов'язкова залежність Create | implementation | required | Ні |
| Registrate MC1.21-1.3.0+67 | Обов'язкова бібліотека Create/CBCAT | implementation | required / JarJar library | Ні |
| Flywheel 1.0.6 | Потрібний Create на клієнті; є у перевіреному dev server classpath | API compileOnly | runtimeOnly | Не довільна інтеграція |
| Sable 2.0.3 + embedded companion 1.6.0 | Sublevel physics/pose/velocity | compileOnly; companion only for compilation | runtimeOnly у повному dev-профілі | **Так** |
| Aeronautics bundle 1.3.0 та його embedded mods | Наявне dev-середовище/інтеграція Sable | немає прямого compile dependency | runtimeOnly у повному профілі | Так |
| Create Radar 0.4.8-1.21.1 | Designation/guided fuze integration | compileOnly | runtimeOnly у повному профілі | Так |
| Jade 15.10.6+neoforge | Overlay plugin | compileOnly | runtimeOnly у повному профілі | Так |
| JEI 19.27.0.340 | Recipe display compatibility | немає власного compile dependency | runtimeOnly у повному профілі | Так |
| libs/cbcatfix-1.0.0.jar | Стара копія самого мода | **вилучена з classpath** | немає | Не залежність |

Прибрано `implementation fileTree(... *.jar)`: довільні файли `libs` більше не потрапляють у runtime і не додають стару копію cbcatfix. JAR залежностей не перепаковуються всередину cbcatfix. ModDevGradle використовує звичайні Java configurations; `modImplementation` іншого toolchain тут не потрібен.

**Точний перевірений мінімальний набір:** Minecraft 1.21.1, NeoForge 21.1.228, Create 6.0.10, CBC 5.11.7, CBCAT 0.1.4c, RPL 2.1.2, Ponder 1.0.82, Flywheel 1.0.6, Registrate 1.3.0+67 і cbcatfix 1.0.0. Loader перелічує 9 модів; Registrate є бібліотекою. Це набір обов'язкових модів та їхніх runtime-бібліотек, не доказ мінімальності кожного loader/library JAR окремо.

Metadata тепер явно вимагає Minecraft 1.21.1, NeoForge >=21.1.228, Create [6.0.9,6.1.0), CBC [5.11.7,6.0), CBCAT >=0.1.4. **Фактично перевірено лише зазначені встановлені версії**, а не весь інтервал metadata. Sable/Radar/Jade залишилися optional. `-PminimalRuntime` прибирає optional runtime configurations; compileOnly інтеграції лишаються доступні компілятору.

Sable API calls ізольовані у guard/lazy adapters; mixin-plugin перевіряє наявність Sable/Radar/JEI до вирішення цілі. Нативна фізика Sable збережена, коли він встановлений. Без нього працює звичайне ядро Minecraft/CBCAT; sublevel-only можливості потребують саме Sable.

## D. REMOVED DEAD CODE

| Видалено | Доказ / причина |
|---|---|
| CbcatFixHelper | Два rail scan methods без викликів; єдиний потрібний виклик був перевіркою count > 0 і замінений еквівалентною перевіркою сусідів. |
| AbstractMountedCannonContraptionAccessor | Getter не викликався ні production, ні dev-кодом; видалено й config entry. |
| BigRocketRenderer | Не реєструвався, усі rocket entity types використовують UnifiedRocketRenderer. |
| RocketRendererMixin / MediumRocketRendererMixin | Змінювали замінені native renderers; активні registry bindings йдуть у UnifiedRocketRenderer. |
| BlockEntityTypeMixin | Заміна офіційною подією NeoForge, тому hook глобального isValid більше не потрібний. |
| IMediumRocketPodBreechBlockEntity.cbcatfix$getRailLength | Не реалізовувався і не викликався. |
| RocketBlock.bodyWidthPixels / bodyLengthPixels | Немає зовнішніх або callback-викликів; geometry належить RocketGeometry. |
| RocketBlockProjectile.getRocket | Невживаний getter. |
| HeatEffect.detonateHeavyAutocannon | Однорядковий wrapper; єдиний caller тепер викликає detonate прямо. |
| SableGuidanceCompat.rememberGuidanceSource + useOn injection | Порожній метод і hook без ефекту. Designation capture при запуску збережено. |
| RocketGeometry.bodyCenter/nosePoint/bodyShape/shapes короткі overloads; rearPoint | Немає викликів; використовувані overloads із centered збережено. |
| RocketSeeker.lastDetectionTick / LastDetection | Поле лише записувалось/читалось; поведінка його не використовувала. Старий NBT безпечно ігнорується. |
| RocketBlockEntity.INDEPENDENT_TAG / запис IndependentRocket | Вже не читалося; authoritative значення — BlockState. |
| duplicate FUZED_BLOCK_ENTITY renderer registration | Та сама native реєстрація вже є в ClientSetup. |
| missile_launch.ogg | sounds.json використовує missile_launch_mono.ogg; невживана stereo-копія. |
| 23 recipe JSON | Побайтово тотожні required CBCAT JAR за тим самим resource ID; loader тепер бере оригінал. Поіменний перелік і SHA: build/architecture-removed-resources.json. |
| Неіснуюча ціль AutocannonFlakProjectile у FlakExplosionRedirectMixin | Такого класу немає у встановленому CBCAT; справжню HA_HEF ціль залишено. |

Пакети мережі/packet classes не видалялися: їх не було. Config keys не видалялися. Зареєстровані type IDs, компоненти payload/fuze/fuel, launcher slots/cooldown, cluster `SecondaryFuzes`/`Projectile` збережено. Для inherited CBC component collection лишено необхідне узгодження native fuze-поля, а не видалено його як «дубль».

## E. REPLACED CUSTOM MECHANICS

Лише фактично зроблені заміни:

| Removed custom implementation | Replaced by | Dependency |
|---|---|---|
| Global BlockEntityType.isValid injection для великих рейок | BlockEntityTypeAddBlocksEvent.modify для двох native BE types | NeoForge |
| Окрема capability реєстрація big breech через registerBlock | Native registerBlockEntity бачить розширений validBlocks | NeoForge |
| Reflective Field lookup/setAccessible для cluster save/load, hook усіх fuzed big projectiles | Targeted mixin declaring class, @Shadow native fields, inherited serialization callbacks | CBCAT + CBC/Mixin |
| Common bootstrap із ручною рефлексивною перевіркою client dist | Dist.CLIENT event subscribers і native event dispatch | NeoForge |
| Per-tick refreshDimensions після старої custom hitbox-системи | Штатні CBC dimensions/bounding-box/movement lifecycle | CBC / Minecraft |
| 23 власні копії native recipe JSON | Ті самі resource IDs із required CBCAT pack | CBCAT / Minecraft |

Arm/collision/network/audio already використовували native APIs до цього аудиту; їх не приписано як нові заміни.

## F. CLIENT/SERVER SEPARATION

`client/` містить сім класів. Усі вісім client mixins перелічені тільки у `client` секції config. `FuzedBlockEntityRendererMixin` перенесено туди із common. Renderer registration перенесено з `CbcatFixMunitions` у `ClientSetup`; common bootstrap більше прямо не реєструє client class. `ClientSetup` і `RocketClientEvents` обмежені `Dist.CLIENT`.

Jade plugin може бути discovered серверним Jade, але його client provider викликається лише через registerClient. Відсутність Jade не завантажує plugin. JEI і Radar/Sable mixins gated до resolution. Common-код не імпортує Minecraft renderer/sound engine/GUI поза client-mixins та client package.

Мінімальний dedicated server: немає RuntimeDistCleaner errors або crash через client classes. Повний optional-набір: є спроби вирішення `ClientLevel` під час mixin metadata loading; вони відсутні у мінімальному запуску. Цього недостатньо, щоб чесно назвати конкретний чужий mixin винним без окремої бісекції; джерело локалізовано до підключення optional-набору. Власні client references/config перевірено окремо. Чужі JAR не патчилися.

Мережа: власних packet registrations немає. Entity data використовує SynchedEntityData і CBC spawn hooks; BE — native notifyUpdate/NBT, contraption blocks — Create writeAndSyncSingleBlockData. Додатковий `engineDisabled` accessor визначений **для кожного AbstractCannonProjectile**, оскільки slot зареєстрований на цьому base class: це зберігає попереднє виправлення crash `SynchedEntityData`.

## G. DEDICATED SERVER RESULTS

Використано Microsoft OpenJDK 21.0.12, Gradle wrapper 8.9, NeoForge `forgeserverdev`, `--nogui`. Це справжні окремі dedicated-server JVM, не integrated singleplayer. EULA=true встановлено після явного прийняття користувачем. Сервери прив'язані до 127.0.0.1:25575/25576; development launch використовує offline mode.

```powershell
$env:JAVA_HOME='<path-to-jdk-21>'
./gradlew.bat runServer -PrunDirectory=build/architecture-server-dev --offline --console=plain --no-daemon --project-cache-dir build/architecture-cache -I build/architecture.init.gradle
./gradlew.bat runServer -PminimalRuntime -PrunDirectory=build/architecture-server-minimal --offline --console=plain --no-daemon --project-cache-dir build/architecture-cache -I build/architecture.init.gradle
```

| Запуск | Результат / доказ |
|---|---|
| Development dedicated, фінальна Java-версія | `20:09:36 Done (2.441s)!`; world/overworld/nether/end, Sable physics initialized. `stop` → усі dimensions saved, BUILD SUCCESSFUL. [Log](../build/architecture-server-dev-final.log). |
| Minimal dedicated, фінальна Java-версія | `20:10:58 Done (0.696s)!`; registry/common setup/world loaded, без optional mods. `stop` → save + BUILD SUCCESSFUL. [Log](../build/architecture-server-minimal-final.log). |
| Minimal після видалення 23 identical recipes | `20:16:52 Done (0.685s)!`; знову 3083 recipes, як до очищення. [Log](../build/architecture-server-recipes.log). |

Виправлено власне missing mixin target, common renderer leakage, optional target gating. Registry, recipe serializer, datapack і network-channel startup errors у фінальному мінімальному запуску не виявлено. Ініціалізовано 10 CBCAT serializers. Немає Mixin apply failure або нового crash report у чотирьох audit runtime dirs.

Залишені попередження: optional `ClientLevel` resolution у development-наборі; missing FramedBlocks accessor із CBC; відсутня JetBrains ScheduledForRemoval annotation у dependency metadata; development refmap warnings; JarJar вибирає вже явно наявні Ponder/Flywheel/Registrate; vanilla command ambiguity та `union:` resource URL. Config correction у першому повторному запуску змінила коментарі default після наявних змін config defaults, зберігши встановлені значення. Ці повідомлення не приховувалися.

## H. PERFORMANCE OPTIMIZATIONS

| Фактична зміна | Межа ефекту |
|---|---|
| Прибрано refreshDimensions з кожного tick обох rocket bases | Менше зайвого перерахунку dimensions/bbox; нативний CBC bbox не залежить від ракетної орієнтації. |
| Один unmodifiable view для RocketBlockEntity.rockets | Прибрано створення wrapper кожного getRockets; payload list лишається єдиним власником. |
| Перевірка максимум двох прилеглих рейок замість повного scan заради count > 0 | Обмежено block reads у classification path, семантика позитивної довжини збережена. |
| Cluster serialization на actual target без reflection | Немає getDeclaredField/setAccessible при save/load; hook не перевіряє кожен сторонній FuzedBigCannonProjectile. |
| Native validBlocks event замість global isValid hook | Прибрано registry-ID lookup і branching із кожного isValid виклику. |
| Видалено порожній useOn hook, dead render hooks, duplicate registration | Менше непотрібних injections/registration work. |

TPS/FPS/наносекунди не вимірювалися — числового приросту не заявляємо. Guidance вже робить lookup однієї UUID-цілі й LOS без force loading; новою оптимізацією це не є. Immutable voxel-shape cache та item model stack cache не додавалися: без профілювання це додаткова invalidation/identity-складність. Геометричні запити обмежені місткістю rocket stack; launcher graph обчислюється на assembly/read, не кожен tick.

## I. REMAINING CUSTOM SYSTEMS

| Subsystem | Native reuse / чому адаптер ще потрібний |
|---|---|
| Placed rocket stack і довге тіло | CBC ammo block підтримує один боєприпас, CBCAT rocket item не надає потрібного placed-stack/root-extension представлення. Використано CBC base + native block lifecycle; payload тільки root. |
| Launcher slot view | CBCAT deque/array не надає потрібної індексації всіх слотів IItemHandler. View працює над ними; власної паралельної inventory немає. |
| Attached launcher lanes / round robin | Оригінальний CBCAT launcher не моделює потрібні додаткові lanes. Native contraption owns blocks/transforms, native breech owns cooldown. |
| LauncherTransfer | Native Sable move/clear/drop callbacks не гарантували потрібну transactional ownership послідовність. Збережено існуюче виправлення; фізику/assembly виконує Sable. |
| Shared geometry / targeting / transforms | Minecraft AABB/Shapes/clip використовуються для реального clipping. Власні дані описують розміри та слоти ракети, яких native dependency не знає. Sable pose math делегується Sable. |
| Powered flight / steering / spread | CBC ballistic shell не має ракетного двигуна/палива/керування, CBCAT fixed thrust не надає поточної fuel/attitude моделі. Спільний FlightState/Steering адаптує native tick/forces, а не вводить entity lifecycle. |
| Rocket health / penetration adjustment | Native CBC penetration/armor providers збережено; ракетне engine failure і soft-obstacle costs відсутні у суцільного снаряда. AP вимикає двигун, fuzed payload використовує native detonate. |
| Seeker / Radar capture | Radar runtime designation не дорівнює автономному ракетному seeker із last-known point. Capture один раз; target UUID authoritative; без глобального entity scan. |
| Payload components / recipe adapters | Native recipes не зберігають усі потрібні fuel/payload options у поточному форматі. Native recipe types/serializers залишаються; добавлено лише adaptation. |
| HEAT/HESH/flak/cluster effects | Native generic shell explosion не еквівалентний налаштованому payload. Native CBC explosion/armor/partial damage використовуються всередині; заміна на звичайний HE змінила б gameplay. |
| Client rendering | Native CBCAT renderer не знає placed/mounted stack layout та великих rocket meshes. Native ItemRenderer/Flywheel/PoseStack використовуються, один flying renderer для всіх tiers. |
| Audio lifecycle | Native sound engine працює з attenuation; adapter задає джерело/позицію/стан двигуна та stop-on-removal. Власного attenuation чи packet protocol немає. |
| ABI mixins | CBCAT скомпільований проти старого CBC. Events/subclass registration не переписують уже існуючі invocation descriptors. Recoil, explosion constructor, property і partial-damage bridges потрібні. |
| Optional plugin gating | Простий IMixinConfigPlugin необхідний, бо @Mixin optional targets вирішуються до звичайного gameplay ModList guard. Використано той самий loading API-підхід, що в CBC. |

Small/medium flight mixins структурно схожі, але їхні CBCAT base classes різні й мають int/byte fuel storage. Спільні розрахунки вже винесені у FlightState/Steering; нова abstract hierarchy заради цих двох mixins ускладнила б залежності.

Serialization owners: flight — RocketFlightState/native entity NBT; engine-disabled — native data accessor/NBT; placed stacks — RocketBlockEntity; mounted stacks — native CBCAT buffer; topology — original contraption NBT; cluster payload — actual CBCAT fields; current selected slot/cache — transient view. Native removeNextTick owns detonation removal. Жодного другого network/state framework не додано.

## J. FINAL VALIDATION

| Перевірка | Результат |
|---|---|
| Clean build | PASS, після всіх production/resource edits, 8 s. [Log](../build/architecture-clean-build.log). |
| Normal build | PASS, 5 s. [Log](../build/architecture-normal-build.log). |
| Compiler warnings | 3 deprecation warnings: RocketBlockEntity.setBlockState override + super, SableGuidanceCompat.hasChunkAt. Перший потрібний для native blockstate lifecycle/cache invalidation; другий перевіряє loaded chunk. Немає unchecked warning. |
| Development client | PASS: Minecraft menu, 19 mods; нормальний світ відкрито, Jade overlay видно; native Sable pipeline loaded. [Log](../build/architecture-client-dev.log). |
| Minimal client | PASS: menu, 9 mods; нормальний світ відкрито, немає missing optional mod/class crash. [Log](../build/architecture-client-minimal.log). |
| Dedicated development | PASS running + normal console stop; optional metadata warnings перелічено в G. |
| Dedicated minimal | PASS running + normal console stop; після resource cleanup recipes count не змінився. |
| JSON / direct own resource references | 493 source resources; JSON parse PASS, missing direct asset references = 0. build/architecture-resource-audit.json. |
| JAR contents | Немає видалених classes/assets, GameTests/rocket_empty structure або embedded Sable/Jade/JEI/Radar implementation classes. |
| Повторний огляд ownership | Native buffer/data/contraption lifecycle збережено; LauncherTransfer hash збігається з baseline. |

Build commands: `./gradlew.bat clean build ... -I build/architecture.init.gradle`, потім `./gradlew.bat build` із тими самими `--offline --console=plain --no-daemon --project-cache-dir build/architecture-cache -I build/architecture.init.gradle`. Init script ізолює output у `build/architecture-20260915` через старі Windows locks, вмикає `-Xlint:deprecation,unchecked` і передає stdin dedicated console. Це звичайні Gradle build tasks, не спеціальний gameplay test runner.

Client commands такі самі, як server, із `runClient` і `-PrunDirectory=build/architecture-client-dev` або `-PminimalRuntime -PrunDirectory=build/architecture-client-minimal`. Для відкриття використано копії звичайного Survival світу, створеного native dedicated server. Спеціальний gameplay test world/GameTests не створювалися і не запускалися. Клієнти закрито через Save and Quit / Quit Game; мишку звільнено.

Client/world checks виконано після фінальних Java-змін. Після них вилучено лише 23 **побайтово однакові** recipe overrides; незмінність recipe count додатково перевірено dedicated запуском і фінальним build. Нових Java/модельних змін після клієнтських перевірок немає.

Залишені upstream client warnings: CBCAT має відсутні twin-autocannon/unbored accessory model paths, неповні unbored rocket rail blockstate variants, неправильні particle texture references (`cbc_at:block/*_rocket_pod`), typo `built_up_nethersteel_barrel_side` замість наявного `built_up_nethersteel_cannon_barrel_side`, та деякі `minecraft:0` texture references. Шляхи звірено з оригінальним CBCAT JAR; вони не є відсутніми прямими ресурсами cbcatfix. Є також vanilla shader sampler і dependency Mixin/Jade warnings. Аудит не створює вигадані текстури/моделі для приховування чужих дефектів. Вони залишаються окремою роботою над ресурсами залежності.

Межі: startup не доводить усі gameplay interactions. Потрібне ручне regression gameplay: заряджання рукою/рукою Create, пуски всіх tiers, side lanes/round-robin, AP penetration/engine failure і fuzed detonation, навігація зі spread, collision із soft blocks, cluster save/reload, placed body breaking, Sable physicalization і dupe regression. Тести для цього не запускалися згідно з обмеженням на gameplay test world. `test NO-SOURCE` не означає пройдені unit tests. Існуючі світові save migrations перевірено структурно, не на всіх історичних save-файлах.

Фінальний artifact: [cbcatfix-1.0.0.jar](../build/architecture-20260915/libs/cbcatfix-1.0.0.jar), 582737 bytes, SHA-256 `12d857e1845b645027aa44e172d35027713a585bf04cd2a78d81b62fc3c0520c`.
