# Пускові установки: реалізація та дослідження, 2026-09-09

Перевірено локальну збірку: Minecraft 1.21.1, NeoForge 21.1.228, Create 6.0.10,
CBC 5.11.7, CBCAT 0.1.4c, Sable 2.0.3, Aeronautics/Simulated 1.3.0, Flywheel 1.0.6.
Create: Radars: Maven-файл 0.4.8-1.21.1, власні метадані мода повідомляють 0.4.4-1.21.1.
Гілка: `codex/fix-recipes-block-drops`. Зміни не злиті в основну гілку.

## A. IMPLEMENTATION CHANGES

1. **Невидимість середніх ракет.** CBCAT реєструє власний `MediumRocketPodBreechRenderer`.
   Попередня додаткова реєстрація нашого BER не гарантувала його використання; фактично
   клієнт використовував клас CBCAT. Його `renderSafe` пропускав малювання за наявності
   Flywheel, а старі універсальні Flywheel-моделі вже приховував наш mixin.
   Тепер саме зареєстрований CBCAT renderer делегує малювання спільному `renderContents`.
2. **Невидимість великих ракет.** Та сама причина: велика рейка використовує той самий тип
   MediumRocketPodBreechBlockEntity/renderer. Виправлення спільне, не окремий renderer великих ракет.
3. **Рендеринг:** `ClientSetup`, `MountedRocketRenderer`, `MediumRocketPodBreechRendererMixin`,
   клієнтська секція `cbcatfix.mixins.json`. `MediumRocketPodBreechInstanceMixin` залишається
   лише клієнтським і приховує старі спрощені моделі, щоб не було подвійного малювання.
4. **Дані:** renderer читає `MountedRocketStorage`, тобто реальні Input-слоти рідного BE.
   Немає окремого renderedRocketSlots, копій ракет для фізики або окремої синхронізації моделей.
5. **Трансформація:** `RocketMounts.transform(BE, slot)` → центр, напрям, tier →
   `RocketModelRenderer` → оригінальна item-модель з бойовою частиною та CBC-підривником.
   Довжина/масштаб походять із `RocketGeometry`; центр зміщується від задньої межі на
   половину довжини ракети, а не до переднього кінця всієї рейки. Bounds охоплюють увесь корпус.
6. **Малі бокові установки:** `LauncherAssembly` викликає оригінальний CBCAT
   `collectCannonBlocks` для кожної сусідньої лінії, перевіряє її та додає реальні блоки/BE
   до того самого native contraption. Кожен казенник зберігає свої 12 слотів.
7. **Середні бокові:** той самий адаптер із native `MountedMediumRocketRailContraption`;
   кожна лінія має власні 4 слоти. Звичайні поставлені середні ракети залишилися у схемі 2×2.
8. **Топологія:** main, main+left, main+right, main+left+right. Сусідні корені ±1 блок
   уздовж локальної поперечної осі. Продовження до другого сусіда заборонене; максимум 3 лінії.
   Велика рейка не отримала бокових ліній.
9. **Рівність довжин:** під час assembly, до перенесення блоків у parent. Обидві бокові
   лінії спочатку проходять перевірку. Помилка повертається штатним `AssemblyException`
   із локалізованою причиною. Немає перевірки всієї конструкції щотік.
10. **Функціональна довжина:** кількість сумісних послідовних блоків від казенника вперед,
    включно з казенником. Не довжина моделі й не кількість усіх блоків потрійної конструкції.
11. **Бокові координати:** локальна вісь main; горизонтально clockwise від FACING,
    вертикально — X. Блоки зберігаються у native локальних координатах contraption,
    тому поворот переносить усі лінії разом, без чотирьох окремих світових схем.
12. **Старт ракети:** `RocketLauncherAssemblyMixin` викликає рідний `fireShot` для кожного
    реального казенника з його startPos/material; `finally` відновлює main. Наявний
    `RocketMountLaunchMixin` бере фактичний слот і пропускає його через native
    `toGlobalVector` та наявний `RocketSableCompat.launchFrame`. Успадкована швидкість
    носія, малий стартовий імпульс 0.15 і коротке прискорення двигуном збережені.
13. **Маніпулятор:** `RocketArmPoint`/native ItemHandler завантажують саме обраний
    нерозібраний у contraption казенник. Боковий не перенаправляється в main.
    Додано відсутню block capability для кастомного великого казенника: розширення
    `BlockEntityType.isValid` саме по собі не додає його до списку реєстрації capabilities.
    Завантаження через зібраний контролер, як і раніше, заблоковане.
14. **Оптимізація:** один код sides для обох класів; native NBT/BE/інвентарі/контролер;
    список локальних казенників обчислюється при assembly/readNBT; довжина кешується;
    renderer та вибір слота більше не сканують рейку для обчислення незмінного центра.
15. **Спрощення:** прибрані зайві реєстрації середнього BER/visualizer та невикористаний
    параметр length у transform; bounds спільні. Тимчасовий LAUNCHER_DRAW прибраний
    із production-коду. Файли користувача не видалялися.
16. **Лабораторія:** A–G у `dev/visual/README.md`, реальні main/triple/medium/large,
    безпечний приклад різної довжини. `-PlauncherLab` додає dev-only startup assertions:
    слотів, native NBT round trip, capabilities та rear anchoring. Раніше збережена
    установка перевіряється без повторного assembly її вже відсутніх світових блоків.
17. **Команди:** `./gradlew.bat build --no-daemon`; для клієнта
    `./gradlew.bat runClient -PlauncherLab "-PquickPlayWorld=Rocket Visual Lab" --no-daemon`.
    Використано Java 21 та користувацький Gradle cache.
18. **Build:** production build успішний. У проєкті немає JUnit test sources
    (`test NO-SOURCE`), тому це не є заявою про проходження unit-тестів.
19. **Клієнт:** запускається, головне меню відкривається; після огляду вихід штатний.
    У логах є попередні помилки ресурсів Create: Radars; вони не виправлялися в цій задачі.
20. **Світ/огляд:** Rocket Visual Lab завантажується. Перший повний прогін підтвердив
    1/3 малі, 1/3 середні, 1 велику лінію, NBT/слоти/capabilities і відхилення різної
    довжини; native mounts створили обидві потрійні установки без пострілів.
    Видимість завантажених середніх та великої ракети перевірена з переднього/бокового
    ракурсу. Діагностика підтвердила native BER, 4/1 слот, двопрохідну composite-модель
    та `skipVanillaRender=false`. Детальне бойове тестування не проводилося.

Фінальний повторний прогін 13:51:41: усі п'ять конфігурацій, NBT та відхилення
нерівних довжин — PASS; обидві збережені native triples повторно завантажилися.
Capabilities незiбраних прикладів — PASS; для вже зібраних ліній повторне світове
завантаження навмисно не перевіряється як доступний inventory. Production JAR містить
0 dev-класів/лабораторних ресурсів. SHA-256:
`B7459C637B9CA08C9F6FE214EFA954F92D5AFA7D8520CC436A815A7D03CD9B2F`.

Ключові файли реалізації, крім renderer-частини: `rocket/LauncherAssembly.java`,
`mixin/RocketLauncherAssemblyMixin.java`, `rocket/RocketMounts.java`,
`mixin/RocketMountLengthMixin.java`, `mixin/RocketMountLaunchMixin.java`,
`rocket/MountedRocketInteraction.java`, `mixin/PitchOrientedContraptionEntityMixin.java`,
`rocket/RocketArmPoint.java`. Усі Java-шляхи відносно `src/main/java/com/cbcatfix`.
Також: `build.gradle`, en_us/uk_ua, `dev/visual`.

## B. RESEARCH FINDINGS

### Джерела та межі висновків

Досліджено саме встановлені JAR та їх локальний декомпільований код:

- CBC: `build/reference-cbc-decompiled/rbasamoyai/createbigcannons`.
- CBCAT: `build/reference-cbcat-decompiled/com/dsvv/cbcat`.
- Sable: `build/reference-sable-decompiled/dev/ryanhcode/sable`.
- Create/Flywheel/Radars: байткод `javap -c -p` і source JAR Flywheel відповідної версії.

Нижче «діє» означає наявний шлях читання значення в коді, не вимірювання боєм.
Не переносити властивості big-cannon material на autocannon material: це різні системи.
Приклади JSON у namespace `example_createbigcannons` не є активним override CBC-матеріалів.

### Launcher Length

Позначення: `L` — блоки лінії включно з казенником; `B` — реально пройдені вперед
сегменти в native fireShot (у звичайній вільній рейці `B=L−1`).

| Read site / шлях | Значення або формула | Реальний наслідок |
|---|---|---|
| CBCAT `MountedRocketPodContraption` / `MountedMediumRocketRailContraption.collectCannonBlocks` | мінімум 2 блоки; CBC maxCannonLength у поточному config = 64 | Чи можна зібрати лінію; пошук казенника/напряму, перевірка незавершених деталей |
| `RocketMounts.length` → `LauncherAssembly.attachSides` | `Lside == Lmain` | Несумісні довжини не збираються разом |
| `RocketMounts.length` → item handler / `RocketMountLaunchMixin` | L має вміщати bodyLength 1/2/3 | Занадто коротка рейка не приймає/не запускає відповідну ракету |
| Native `fireShot` | `baseSpeed*0.5 + min(B,maxSpeedIncreases)*speedIncreasePerBarrel*0.25` | В оригіналі задає стартову швидкість; у поточному fix її перезаписує launchFrame перед addFreshEntity, тому це НЕ поточний бонус швидкості |
| Native small / medium `fireShot` | `max(0, baseSpread−B*spreadReductionPerBarrel*k)`, k=0.825 / 0.9 | Native розкид також замінений фактичним mounted-напрямом при spawn; не дає поточного бонусу точності |
| Native `fireShot`, squib branch | `B > floor(maxBarrelLength*1.5)` за дозволеної squib-гілки | Застрягання ракети у сегменті; cast iron/bronze/steel пороги B=4/7/10. Гілка враховує властивість боєприпасу та canFail, не лише один config-перемикач |
| Native `fireShot`, obstruction branch | послідовний обхід сегментів | Перешкода всередині каналу може зупинити постріл/викликати рідну аварію; не нова механіка fix |
| Native `getWeightForStress` | `2*blockCount*material.weight`; sides: сума по їх реальних матеріалах | Більше сегментів — більша величина, яку CBC використовує для кінетичного навантаження mount |
| Native extension lengths → bounds; `contentsBounds` | native межі ліній + реальний корпус завантажених ракет | Видимість/відсікання та межі contraption, не дальність ракети |

Довжина **не збільшує** кількість слотів (12/4/1), запас палива, маршову швидкість,
маневреність, заряд, міцність ракети чи її TTL. Після палива залишається балістика.
Модель закріплена біля заднього початку: додаткові сегменти не пересувають її до дула.
Native recoil має `(baseRecoil + addedRecoil) * autocannonRecoilScale * 2`, без L;
поточний recoilScale=0.5. Прямої формули «довжина → стабільність/нагрів» не знайдено.
Native setChargePower(B) зберігається, але не знайдено читання цього числа як сили
бойової частини ракет; не оголошую його бонусом вибуху.

### Launcher / Cannon Materials

Значення autocannon-матеріалів нижче підтверджені runtime-дампом властивостей.
Стрілка «native only» означає, що native обчислює значення, але fix замінює його
до появи ракети у світі. Вага CBC — **не кілограми Sable**.

| Material | Property | Value | Used by | Actual Effect |
|---|---|---|---|---|
| Cast iron | weight / baseRecoil / maxBarrelLength | 1.5 / 1 / 3 | rocket contraption stress, recoil, squib | Stress 3 за блок; базова віддача 1; squib після B>4 |
| Bronze | weight / baseRecoil / maxBarrelLength | 1 / 2 / 5 | ті самі | Stress 2 за блок; базова віддача 2; squib після B>7 |
| Steel | weight / baseRecoil / maxBarrelLength | 2.5 / 3 / 7 | ті самі | Stress 5 за блок; базова віддача 3; squib після B>10 |
| Cast iron | baseSpeed / increment / maxIncreases | 5 / 2 / 2 | native fireShot | Native only; не визначає поточну стартову/маршову швидкість ракети |
| Bronze | baseSpeed / increment / maxIncreases | 3 / 1.5 / 3 | native fireShot | Native only |
| Steel | baseSpeed / increment / maxIncreases | 3 / 1.5 / 4 | native fireShot | Native only |
| Cast iron / Bronze / Steel | baseSpread / reduction | 2 / 1.5; 2.5 / 2; 3 / 1.5 | native fireShot | Native only; фактичний spawn орієнтується за mounted frame |
| Cast iron / Bronze / Steel | projectileLifetime | 11 / 25 / 60 | native projectile initialization | Не ліміт польоту поточних ракет: шлях видалення за native lifetime вимкнений |
| Cast iron / Bronze / Steel | connectsInSurvival / weldable | false / true для всіх | RocketPodBlock / MediumRocketPodBlock placement/welding | Правила з'єднання/зварювання, не бонус заряду |
| Cast iron / Bronze / Steel | weldDamage / weldStressPenalty | 1/1; 1/0; 2/2 | WeldableBlock → ServerboundUseWelderPacket для weldDamage | Знос інструмента 1/1/2; weldStressPenalty оголошений, але ракетні contraptions його не читають |
| Wrought Iron Medium Rocket Rail | фактичний material | **CBCAutocannonMaterials.STEEL** | CBCAT BlockRegister | Назва wrought iron не дає фізику big-cannon wrought iron; використовуються steel значення вище |
| Big Rocket Rail | фактичний material | **CBCAutocannonMaterials.STEEL** | CbcatFixMunitions | Ті самі steel властивості, але один слот великої ракети |

Native rocket collectCannonBlocks має `isConnectedToCannon` з постійним true;
не слід плутати це з повною native перевіркою зварних з'єднань великих гармат.
Нова перевірка sides явно перевіряє tier, напрям і функціональну довжину.
Cooldown задається таблицею FIRE_RATES казенника та сигналом 0..15, не material heat.

Big-cannon material defaults — окрема таблиця для **гармат**, не ракет:

| Material | Property | Value | Used by | Actual Effect |
|---|---|---|---|---|
| Log | minVelocityPerBarrel; weight; maxSafeStress; failure; minSpread/reduction | −1; 1; 0; FRAGMENT; 1.5/1 | MountedBigCannonContraption | Вага; межа заряду/аварія; native squib/spread |
| Wrought iron | ті самі | 2; 2; 1; RUPTURE; 0.1/1 | той самий | Ті самі native шляхи |
| Cast iron | ті самі | 2; 3; 2; FRAGMENT; 0.05/2 | той самий | Ті самі native шляхи |
| Bronze | ті самі | 1.3333333333333; 2; 4; RUPTURE; 0.03/1.4 | той самий | Ті самі native шляхи |
| Steel | ті самі | 1; 5; 8; FRAGMENT; 0.025/1.4 | той самий | Ті самі native шляхи |
| Nethersteel | ті самі | 0.66666667; 6; 10; FRAGMENT; 0.02/1.15 | той самий | Ті самі native шляхи |
| Incomplete layered | ті самі | −1; 1; 0; FRAGMENT; 0/0 | unfinished material definition | Заготовка, не повноцінний допустимий матеріал зібраної гармати |
| Big cast iron / steel / решта | weldStressPenalty | 1 / 2 / 0 | getMaxSafeCharges | Зменшує native безпечний stress звареної гармати |

CBC `getMaxSafeCharges` обмежує charge stress мінімумом міцності матеріалу й казенника;
`mayGetStuck` читає minVelocityPerBarrel; native fireShot застосовує spread floor/reduction
та тип руйнування. CBCAT `disablePhysicRework=false` у поточній збірці: його
`CustomPropellantContext` додатково підміняє velocity/spread/stress/recoil. Наведені
CBC defaults не є обіцянкою підсумкової поведінки великої гармати без цього контексту.

**Sable mass:** `MassTracker.build` → `PhysicsBlockPropertyHelper.getMass` → block-state
physics property MASS (загальний default 1.0, можливі data overrides). Не виявлено
перетворення autocannon material.weight у цю масу. Додаткові фізичні блоки можуть
змінювати масу/інерцію носія через Sable, але значення stress CBC не можна називати його масою.

### Cannon / Launcher Attachments

Сімейства згруповані, щоб не дублювати однаковий код кожного металу.
`m` для великих аксесуарів = cast_iron/bronze/steel/nethersteel;
`a` для auto/twin/heavy = cast_iron/bronze/steel. Це шаблони реально зареєстрованих ID,
не нові предмети. Зварювання/основна геометрія залишилися native.

| Attachment | Class/Registry ID | Attaches To | Functional Effect | Rocket Launcher Compatible? | Sable Relevant? |
|---|---|---|---|---|---|
| Big muzzle brake | MuzzleBrakeBlock; cbc_at:m_muzzle_brake | Вісь big cannon | Функціональний: газ, drag, recoil, spread у CustomPropellantContext | Ні: не RocketPodBlock/MediumRocketPodBlock | Опосередковано через віддачу всієї гармати |
| Fume extractor | FumeExtractorBlock; cbc_at:m_fume_extractor | Вісь big cannon | Функціональний: газ, drag, spread, smoke | Ні | Рідна фізика носія; окремого Sable extractor hook немає |
| Rifled barrel | RifledBarrelBlock; cbc_at:m_rifled_barrel, built_up_steel/nethersteel_rifled_barrel | Вісь big cannon | Функціональний: газ, drag, spread | Ні | Без окремого hook |
| Big silencer | SilencerBlock; cbc_at:m_silencer, built_up_steel/nethersteel_silencer | Вісь big cannon | Функціональний: газ/drag/recoil/spread/smoke/stress/звук | Ні | Віддача через CBC controller |
| Auto silencer | SpecialAutocannonBarrel; cbc_at:a_autocannon_silencer | Autocannon barrel chain | volumeMultiplier=0.5; впливає на volume і pitch CannonBlastWave | Ні | Стандартні блок/носій, не спеціальне приглушення фізики |
| Auto muzzle brake | той самий; cbc_at:a_autocannon_muzzle_brake | Autocannon barrel chain | **Немає окремого доведеного зменшення recoil**; volumeMultiplier=1, звичайний сегмент з іншою моделлю | Ні | Без окремого hook |
| Twin/vertical twin silencer | TwinAutocannonBarrelBlock; cbc_at:a_twin_autocannon_silencer / a_vert_twin_autocannon_silencer | Відповідна twin-лінія | Кожен multiplier=0.5; native contraption накопичує їх, звук/pitch; native min product 0.03125 | Ні | Без окремого hook |
| Twin/vertical muzzle brake | той самий, суфікс muzzle_brake | Twin/vertical twin | Multiplier=1; форма/сегмент, не доведений recoil buff | Ні | Без окремого hook |
| Heavy silencer | HeavyAutocannonBarrelBlock; cbc_at:a_heavy_autocannon_silencer | Heavy chain | Multiplier=0.6; накопичується й змінює volume/pitch | Ні | Без окремого hook |
| Heavy muzzle brake | той самий; cbc_at:a_heavy_autocannon_muzzle_brake | Heavy chain | Multiplier=1; інша модель звичайного сегмента | Ні | Без окремого hook |
| Recoil springs | AutocannonRecoilSpringBlock; CBC a_autocannon_recoil_spring; CBCAT a_(vert_)twin_autocannon_recoil_spring, a_heavy_autocannon_recoil_spring | Своя autocannon-лінія | Зв'язування рухомих деталей та handleFiring/анімація. Немає окремого множника сили віддачі за присутність пружини | Ні | Візуальна механіка, не доказ поглинання фізичної віддачі |
| Sliding / quickfiring / screw breeches | CBC SlidingBreechBlock, QuickfiringBreechBlock, ScrewBreechBlock; металеві *_breech | Big cannon axial connection | Відкривання, допустимість пострілу, loading; breech-strength обмежує charges | Ні | Віддача самої гармати, не окремий Sable казенник |
| Auto/twin/heavy breeches | CBC AutocannonBreechBlock; CBCAT TwinAutocannonBreechBlock, HeavyAutocannonBreechBlock, HeavyAutocannonQuickFireBreechBlock; a_heavy_autocannon_qfbreech тощо | Лише своє сімейство | Інвентар, native cooldown/extraction/loading. Не дульний buff | Ні | Native BE зберігає стан на contraption |
| Rocket breeches | RocketPodBreechBlock / MediumRocketPodBreechBlock; cbc_at:a_rocket_pod_breech, wrought_iron_medium_rocket_rail_breech; cbcatfix:big_rocket_rail_breech | Відповідна ракетна лінія | Реальні slots 12/4/1, cooldown, початок лінії | Так | Той самий BE/local frame, без декоративної копії |
| Handles + seat | AutocannonBreechBlock.HANDLE; Create seat | Рідний ручний казенник/пасажирський вузол | Керування пасажиром; не bonus spread | Не універсальний attachment до ракети; залежить від native controller | Пасажир/носій, не стабілізатор |
| Cannon mount | CBC CannonMountBlockEntity; createbigcannons:cannon_mount | Provider-блок гармати над mount/extension | Assembly, кінетичні yaw/pitch, redstone firing | Так; використаний для обох triples | CBC SableCompat передає віддачу носію |
| Mount extension | CannonMountExtensionBlockEntity; createbigcannons:cannon_mount_extension | Суміжний mount із відповідним shaft direction | Зв'язок із mount, кінетика, проксі inventory/goggles; не buff боєприпасів | Так у native mount схемі | Simulated attachment checks враховують mount/extension |
| Fixed mount | FixedCannonMountBlockEntity; createbigcannons:fixed_cannon_mount | Cannon provider | Фіксоване встановлення, assembly/fire; ракети не отримують passenger turning на ньому | Native rocket code явно обробляє цей controller | Native controller/носій |
| Cannon carriage | CannonCarriageEntity; createbigcannons:cannon_carriage | Native cannon contraption | Переміщення, пасажир, fire rate; не booster ракети | Rocket contraptions мають carriage fire-rate interface; потрібне ручне тестування sides | Не підміна Sable sublevel |
| Ammo containers | CBC AutocannonAmmoContainerBlock + creative variant; CBCAT heavy_autocannon_ammo_box + creative_heavy_autocannon_ammo_box | Свій auto/heavy breech | Подача відповідних боєприпасів | Не rocket inventory adapter | Native BE/транспорт |
| Cannon loader, ram/worm heads | CBC CannonLoaderBlock / loading contraption; cannon_loader, ram_head, worm_head | Канал big cannon | Фізично пересуває/витягує заряджені блоки | Не підтверджений як завантажувач ракетних слотів; не використовує RocketArmPoint | CBC Simulated integration включає loading blocks |
| Hand tools | CBC ram_rod, worm | Ручна взаємодія з big cannon | Штовхання/витягування боєприпасів, не статичний buff | Не rocket slot loader | Без окремого hook |
| Mechanical arm | Create ArmBlockEntity; create:mechanical_arm + RocketArmPoint | Обраний казенник до assembly | Реальна передача ItemStack через capability, кожна лінія окремо | Так, лише unassembled | Не звертається до assembled controller inventory |
| Radar yaw/pitch controllers | create_radar:auto_yaw_controller / auto_pitch_controller; AutoYaw/AutoPitchControllerBlockEntity | Поряд із розпізнаним CBC mount за direction | setTarget → CannonMountYaw/Pitch.tick; автоматичне наведення mount, не material buff | Через CBC mount interface; sides зберігають один controller | Повне динамічне Sable-наведення в цій задачі не тестувалося |
| Radar fire / network control | create_radar:fire_controller; NetworkFiltererBlockEntity / WeaponFiringControl | Radar network / weapon endpoints | Вибір track, передача цілі та умови відкриття вогню | Наявний radar шлях; нове балансування не виконувалося | Наявність VS2-коду не доводить Sable-сумісність усіх режимів |
| Radar data link | create_radar:data_link; DataLinkBlockItem | Прив'язка endpoints | Дані мережі, не швидкість/вибух/віддача | Опосередковано через weapon endpoint | Окремо не тестувався |

`SmartMountBlockEntity` у Radars лише наслідує CBC mount у дослідженому класі;
самої наявності класу/моделі smart_mount недостатньо для заяви про доступний
новий стабілізатор або додаткові gameplay-модифікатори. Окремого зареєстрованого
ракетного компенсатора/стабілізатора/оптичного прицілу в CBC/CBCAT не виявлено.
Aeronautics mounted potato cannon — окрема зброя, не attachment ракетної рейки.
Заготовки/unbored/incomplete й crafting item recoil_spring не зараховані до готових
функціональних attachments. Побутові Create-з'єднувачі не дають бонусів ракеті.

#### Точні модифікатори великих аксесуарів

`IBigCannonBlockPhysics` → `CustomPropellantContext.addBarrel` → CBCAT
`MountedBigCannonContraptionMixin.createCustomPropellantContext` → native fireShot
з підмінами velocity/spread/recoil/stress, particle smoke та CannonBlastWave volume.
G — вхідний explosionGas, D — drag, R — recoil, S — spread, V — getVelocity().
Усі праві частини рядка читають **вхідний** контекст, а не результат попереднього
присвоєння в цьому рядку (це важливо для recoil muzzle brake/silencer).

| Деталь | Модифікатори її applyBarrelPhysic |
|---|---|
| Muzzle brake | G=(G−0.32)×0.45; D+=0.00025×V²; R=R×0.4/max(G,0.8); S×=0.95 |
| Fume extractor | G−=0.45; D+=0.06×V²; S×=0.8; smoke×=0.33 |
| Rifled barrel | G−=0.35; D+=0.08×V²; S×=0.45 |
| Silencer | G−=0.4; D+=0.04×V²; R×=0.45×G; S×=0.9; smoke×=0.9; stress+=0.5; volume×=0.4 |

Це **не кінцеві відсотки** швидкості/віддачі: addBarrel потім додає
`max(newGas,0)*0.75` до recoil і `max(newGas,0)` до velocity;
`getVelocity=max(0,velocity+charges−drag)`. Залежить від решти гармати й порядку деталей.
Без увімкненого CBCAT physics rework цей шлях не слід вважати активним.

Breech-strength JSON встановленого CBC: cast iron sliding/quickfiring=2/2;
bronze=4/2; steel sliding/quickfiring/screw=4/2/8; nethersteel screw=10.
Це ще одна межа native getMaxSafeCharges, а не міцність ракети.

CBC SableCompat: native controller recoil → enqueue для SubLevel → prePhysics →
QueuedForceGroup.applyAndRecordPointForce. Жоден із перелічених muzzle devices не
потребує окремої «Sable-версії». Simulated attachment checks для CBC blocks і
native contraption transport збережені; це не заява, що всі режими склеювання
незібраних CBCAT рейок досліджені чи протестовані.

### Що ще перевірити вручну

- Main+left та main+right; три лінії у різних орієнтаціях; відмова при четвертій лінії.
- Постріл із кожного бокового слота, його власні NBT/підривник, часткове спорожнення.
- Маніпулятор окремо в main/left/right і відмова від loading після assembly.
- Поворот і рух Sable-носія: моделі/стартові точки, успадкована лінійна й кутова швидкість.
- Збереження/розбирання/повторне складання з різним наповненням бокових казенників.
- Регресії: 12 наскрізних малих труб, placed medium 2×2, балістика після палива,
  коротке прискорення, 8 cluster submunitions. Відповідний код не перебалансовувався.

## CANNON / LAUNCHER ATTACHMENTS — уточнення до TASK 11

Точні англійські display names та всі 98 відповідних зареєстрованих CBCAT ID
(включно із заготовками) наведені в `MUZZLE-REGISTRY-2026-09-09.md` поруч із цим звітом.
Каталог отриманий перетином **викликів BlockRegister.block(...)** із lang встановленого
JAR, а не лише пошуком назв у текстурах. Формувальні mould-предмети не є насадками.
У таблиці m={cast_iron,bronze,steel,nethersteel}, a={cast_iron,bronze,steel};
префікс namespace, якщо не вказано інше, `cbc_at:`. Повний ID кожного варіанта — у каталозі.

| Attachment | Registry ID | Compatible Weapon | Actual Functional Effect | Formula / Value | Sable Compatible | Notes |
|---|---|---|---|---|---|---|
| Muzzle Brake | m_muzzle_brake | Big cannon | Газ/швидкість через drag; віддача; розкид | G'=(G−.32)×.45; D'=D+.00025V²; R'=R×.4/max(G,.8); S'=.95S | Native assembled path збережений; рухомий бойовий тест не проводився | DOES NOT WORK WITH ROCKET LAUNCHERS; MuzzleBrakeBlock.applyBarrelPhysic |
| Fume Extractor | m_fume_extractor | Big cannon | Менше диму й розкиду, зміна газу/опору | G'=G−.45; D'=D+.06V²; S'=.8S; smoke'=.33smoke | Те саме | DOES NOT WORK WITH ROCKET LAUNCHERS; FumeExtractorBlock.applyBarrelPhysic |
| Rifled Barrel | m_rifled_barrel, built_up_steel/nethersteel_rifled_barrel | Big cannon | Зменшення розкиду з ціною газу/опору | G'=G−.35; D'=D+.08V²; S'=.45S | Те саме | DOES NOT WORK WITH ROCKET LAUNCHERS; RifledBarrelBlock.applyBarrelPhysic |
| Suppressor (ID silencer) | m_silencer, built_up_steel/nethersteel_silencer | Big cannon | Звук/дим, газ/drag, recoil/spread, додатковий stress | G'=G−.4; D'=D+.04V²; R'=.45RG; S'=.9S; smoke'=.9smoke; stress'=stress+.5; volume'=.4volume | Те саме | DOES NOT WORK WITH ROCKET LAUNCHERS; SilencerBlock.applyBarrelPhysic |
| Autocannon Suppressor | a_autocannon_silencer | Autocannon | Тихіший і нижчий звук пострілу | Добуток multiplier .5; масштабує volume **та pitch** | Добуток зберігає native contraption; не залежить від нерухомого світового блока | DOES NOT WORK WITH ROCKET LAUNCHERS; SpecialAutocannonBarrel → CBCAT MountedAutocannonContraptionMixin |
| Autocannon Muzzle Brake | a_autocannon_muzzle_brake | Autocannon | Окремого recoil modifier немає; поводиться як сегмент ствола з іншою моделлю | volumeMultiplier=1 | Native стан/сегмент зберігається | DOES NOT WORK WITH ROCKET LAUNCHERS; не обіцяє «гасіння віддачі» |
| Twin / Vertical Twin Suppressor | a_twin_autocannon_silencer; a_vert_twin_autocannon_silencer | Відповідна twin autocannon | Звук/pitch | Добуток .5, native нижня межа .03125 | Поле volumeMultiplier серіалізується; native fireShot читає його після assembly | DOES NOT WORK WITH ROCKET LAUNCHERS; TwinAutocannonBarrelBlock → MountedTwinAutocannonContraption |
| Twin / Vertical Twin Muzzle Brake | відповідні a_(vert_)twin_autocannon_muzzle_brake | Twin autocannon | Окремого гальмування recoil немає; звичайний функціональний сегмент | multiplier=1 | Native assembled path | DOES NOT WORK WITH ROCKET LAUNCHERS |
| Heavy Autocannon Suppressor | a_heavy_autocannon_silencer | Heavy autocannon | Звук/pitch | Добуток .6; CannonBlastWave volume=15×product, pitch=1.05×product | Поле записується/читається native NBT | DOES NOT WORK WITH ROCKET LAUNCHERS; HeavyAutocannonBarrelBlock → MountedHeavyAutocannonContraption |
| Heavy Autocannon Muzzle Brake | a_heavy_autocannon_muzzle_brake | Heavy autocannon | Окремого recoil modifier немає; форма стандартного сегмента | multiplier=1 | Native assembled path | DOES NOT WORK WITH ROCKET LAUNCHERS |
| Flanged barrel end | **Не окремий ID**: createbigcannons:a_autocannon_barrel[end=flanged]; CBCAT twin/vertical end=flanged | Auto/twin | VISUAL / STRUCTURAL ONLY відносно режиму end: зміна кінцевої моделі та cannon shape, не пострілу | NOTHING ↔ FLANGED; getCannonShapeInLevel → AUTOCANNON_BARREL_FLANGED | BlockState зберігається в native assembly; moving state зберігає end | DOES NOT WORK WITH ROCKET LAUNCHERS; ключ перемикає тільки відкритий кінець |
| Звичайне подовження ствола CBC | createbigcannons:m_cannon_barrel; built_up_steel/nethersteel_cannon_barrel; a_autocannon_barrel | Big cannon / autocannon відповідно | Це не додатковий muzzle buff: продовжує канал, впливає через штатну довжину/матеріал/контекст | Auto: speed=baseSpeed+min(B,maxIncreases)×increment; spread зменшується на B×reduction; squib B>maxBarrelLength. Big: material spread floor, squib та активний CBCAT контекст | Native block state/contraption | DOES NOT WORK WITH ROCKET LAUNCHERS; BigCannonTubeBlock / AutocannonBarrelBlock; не застосовувати auto-формулу до ракет |
| Rocket Pod / Rocket Rail segments | a_rocket_pod_rail; wrought_iron_medium_rocket_rail; cbcatfix:big_rocket_rail | Small / medium / large launcher відповідно | Подовження функціональної лінії, fit, squib, stress; НЕ насадка зі стабілізацією | L+1, B=L−1; формули у розділі Length | Реальні native блоки в contraption; launchFrame використовує Sable | WORKS WITH ROCKET LAUNCHERS, але лише відповідного розміру |
| Rocket Pod End / Rocket Rail End | a_rocket_pod_breech; wrought_iron_medium_rocket_rail_breech; cbcatfix:big_rocket_rail_breech | Відповідний launcher | Це **задній казенник**, не дульний buff: інвентар/cooldown/контролер | 12 / 4 / 1 слот; FIRE_RATES | Власний BE/NBT на зібраній лінії | WORKS WITH ROCKET LAUNCHERS; назва End не означає компенсатор |
| Unbored/incomplete variants | unbored_* у точному каталозі | Виробнича заготовка | Немає ефекту готової насадки при пострілі: native assembly відхиляє incomplete | isComplete=false / native incomplete validation | Наявність блока на Sable не робить його придатною зброєю | DOES NOT WORK WITH ROCKET LAUNCHERS як готова насадка |

**Як читати формули:** усі праві частини використовують старий контекст; V=getVelocity().
Після applyBarrelPhysic виконується addBarrel, тому наведені множники — не готові
відсотки підсумкової швидкості чи recoil. Точний наступний крок наведений вище.
Built-up варіанти використовують той самий applyBarrelPhysic; це не додатковий множник.

Простими словами: великі гальма/екстрактори/нарізні секції/глушники справді змінюють
контекст пострілу великої гармати. Глушники auto/twin/heavy змінюють звук, але їх назва
не доводить зменшення віддачі. Їхні muzzle brakes не мають такого окремого бонусу.
Фланець — оформлення/форма кінця. Ракетна рейка — сегмент пускової; Rocket Rail End —
її казенник. **Жоден із досліджених гарматних дульних модифікаторів не застосовується
до native rocket contraptions.** Візуально поставити блок поряд недостатньо.

Big-attachment detection відбувається в `createCustomPropellantContext` при fireShot:
код обходить `presentBlockEntities`/`blocks` зібраної гармати і передає cBlock у addBarrel.
Тому assembly не втрачає ефект, і сам розрахунок не залежить від world-axis або нерухомості.
Autocannon-ефекти накопичуються під час collectCannonBlocks і читаються у fireShot;
heavy/twin серіалізують volumeMultiplier. Sable не замінює ці формули; CBC SableCompat
передає кінцевий recoil як point force. Це доказ збереження **шляху коду**, а не проведений
бойовий тест кожної насадки на рухомій платформі. Властивості стабілізації, reload,
cooldown, HP чи особливого flash цим насадкам без окремого читача не приписуються.

## ROCKET LAUNCHER MATERIALS — уточнення до TASK 12

| Material | Relevant Properties | Actual Launcher Effect | Length Effect | Accuracy/Spread Effect | Mass/Sable Effect | Other Differences |
|---|---|---|---|---|---|---|
| Cast Iron, small | weight=1.5, baseRecoil=1, maxBarrelLength=3 | CBC stress=3×L; recoil formula додає 1 | Squib-гілка після B>4, тобто звичайно L>5 | Native spread 2/reduction1.5, але fix замінює стартовий напрям; активного бонусу немає | weight — тільки CBC stress, не Sable mass | Weld tool damage=1; small 12 slots |
| Bronze, small | weight=1, baseRecoil=2, maxBarrelLength=5 | CBC stress=2×L; recoil додає 2 | Squib після B>7, звичайно L>8 | Native 2.5/2, той самий override | Не доведена менша Sable маса, лише менший CBC stress | Weld tool damage=1; small 12 slots |
| Steel, small | weight=2.5, baseRecoil=3, maxBarrelLength=7 | CBC stress=5×L; recoil додає 3 | Squib після B>10, звичайно L>11 | Native 3/1.5, той самий override | Не доведена більша Sable маса | Weld tool damage=2; small 12 slots |
| Wrought Iron Rocket Rail, medium | Фактично autocannon **STEEL**, не CBC wrought iron | Як steel: stress/recoil; medium 4 slots | Той самий steel squib; body fit ≥2 | Той самий override; native spread reduction factor .9 | Назва wrought iron не змінює mass API Sable | Єдиний зареєстрований medium material; назва/рецепт не відповідають окремій фізичній таблиці |
| Big Rocket Rail, large | Фактично autocannon **STEEL** | Steel stress/recoil; large 1 slot | Steel squib; body fit ≥3 | Той самий override | Окремий block ID, але не новий AutocannonMaterial | Блоки fix strength(2); оригінальні CBCAT rail/breech strength(5,6) |

Немає rocket launcher з log, nethersteel або окремим big-cannon wrought-iron material
у досліджених реєстраціях. Nethersteel muzzle devices належать великим гарматам,
не є nethersteel rocket tubes. Усі малі метали мають однакові 12 слотів і геометрію;
medium/large — інший тип установки, а не вибір металу для тієї самої малої ракети.

#### Property → reader → наслідок

- `CBCAutocannonMaterials` → `properties().weight` → native `getWeightForStress`:
  порівнюйте навантаження на кінетичний mount, **не масу літака**.
- `baseRecoil` → native `fireShot` → CBC controller.onRecoil → SableCompat point force:
  це реальний шлях впливу металу на імпульс носія. Величина також залежить від
  боєприпасу й config recoilScale; сам метал не задає весь імпульс.
- `maxBarrelLength` → native fireShot squib condition; це **не hard assembly cap**.
  Загальна межа assembly задається CBC maxCannonLength=64, мінімум=2 незалежно від металу.
- `baseSpeed`, `speedIncreasePerBarrel`, `maxSpeedIncreases`, `baseSpread`,
  `spreadReductionPerBarrel` → native initialization → замінені RocketMountLaunchMixin.
  Вони читаються, але **не визначають фактичний старт поточних ракет**; це точніше,
  ніж називати їх взагалі ніколи не прочитаними.
- `projectileLifetime` → native setLifetime; подальше видалення за lifetime вимкнене
  fix. Не fuel time та не час балістичного польоту.
- `connectsInSurvival=false` → `RocketPodBlockItem.place` / `MediumRocketPodBlockItem.place`:
  визначає виклик native onPlace у survival/creative. Однакове для всіх трьох металів.
- `isWeldable=true`, `weldDamage=1/1/2` → RocketPodBlock/MediumRocketPodBlock WeldableBlock
  API → ServerboundUseWelderPacket → stack.hurtAndBreak. Це витрата міцності зварювального інструмента,
  не HP ракети. canWeldSide також вимагає той самий material обох блоків.
- `weldStressPenalty=1/0/2`: **DEFINED BUT NOT USED FOR ROCKET LAUNCHERS** —
  відповідного читання у двох rocket contraptions не знайдено. Не переносити на них
  big-cannon getMaxSafeCharges лише тому, що поле існує в матеріалі.
- Heat resistance, pressure rating, launcher HP, reload bonus: таких властивостей
  і ракетних читачів у цих AutocannonMaterialProperties не знайдено. Міцність блока
  Minecraft strength і міцність самого projectile — інші величини.

#### Реальна маса, інерція та центр мас Sable

`PhysicsBlockPropertyTypes.MASS` default=1.0 → `BlockStateExtension.sable$getProperty`
→ `PhysicsBlockPropertyHelper.getMass` (0 для non-solid) → `MassTracker.build`.
Маса = сума blockMass; COM = sum(blockMass×blockCenter)/sum(blockMass).
Без override локальна інерція блока = blockMass/6 по діагоналі плюс зміщення до COM
через parallel-axis term. blockCenter походить від collision-shape bounds або
BlockSubLevelCustomCenterOfMass, **не від CBC material.weight**.

Sable має окремі data tags heavy/light/super_heavy тощо; наприклад super_heavy
використовує c:storage_blocks. Власного прямого selector для цих rocket rail ID у
перевірених CBC/CBCAT/fix ресурсах не знайдено. Це не гарантує масу кожного блока
в будь-якому користувацькому datapack: для цього потрібен runtime dump остаточних
physics properties саме його світу. Не стверджую «bronze легший у Sable» за CBC weight.
Фізичні блоки Sable та блоки, вже забрані в CBC entity-contraption, не слід автоматично
рахувати двічі як однакові solid voxels; специфічний додатковий перенос CBC weight
в MassTracker не знайдений.

**Практично:** bronze дає найменший CBC stress; cast iron — найменший базовий recoil,
але найкоротший поріг squib; steel допускає довшу рейку до squib, має більші stress
і base recoil. Це не дальність чи точність ракети. Для короткої малої установки
«сталь краща в усьому» код не підтверджує. Візуальні/крафтові відмінності є, але
матеріали **не повністю однакові** через stress/recoil/squib. Доведений шлях до
Sable — віддача; доказу різної щільності металів через CBC material немає.
