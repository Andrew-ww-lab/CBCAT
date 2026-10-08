> Оновлення 2026-10-07: актуальні рецепти та оформлення описані в [Launcher UX cleanup](launcher-ux-cleanup-20261007.md). Нижче збережено попередній стан; старі рішення про корпус кластера та ціни 27/45 сталі скасовані.

# Launcher update: реалізація та перевірки

Дата: 2026-10-06. Гілка: `codex/radars-5-ea-compat`.

Статус: погоджені механіки та обидва остаточно затверджені виробничі рецепти реалізовано. Код не запушено й не злито. Цей звіт відокремлює виконану роботу від ручних перевірок.

## A. IMPLEMENTED

- Steel Small: загальна довжина 2–5, максимум один Rapid/Traverse одразу перед казенником, нормалізоване порівняння бокових установок до вилучення блоків зі світу.
- Нова точність Small через чинний параметр `shoot()`; існуючі RNG і фізика запуску.
- Базові множники ROF Small/Medium/Large: 0.3375 / 0.375 / 0.25.
- Rapid ×2 поверх нового Small baseline; штатні CBC-приводи Traverse ×1.5.
- Погоджена окрема сумісність Traverse із Radars: верхній ліміт 5→7.5°/tick, включно зі штатною оцінкою часу повороту.
- Єдиний серверний таймер підготовки групи: фіксовані 20/40/100 ticks, захист пострілу, NBT, синхронізація й підказка окулярів.
- Два модулі, по дві несвердлені й свердлені заготовки, дві форми; лиття 900 mB сталі, свердління CBC та завершення однією деталлю.
- Моделі модулів; напрямна текстура на зовнішніх поверхнях сталевого казенника зі збереженням геометрії.
- Англійські й українські звичайні та Shift-підказки модулів, установок і кластера.
- Приховані старі bronze/cast-iron Small у creative/JEI; їх лиття і свердління вилучено через штатний фільтр ресурсів. Реєстрації збережено.
- Перевірки трансформованих класів і ресурсів у пам’яті; запуск клієнта й серверів на наявних світах.

## B. DECISIONS REQUIRED

Відкритих виробничих рішень немає. 2026-10-06 користувач затвердив обидва запропоновані рецепти та додав свердління модулів.

- Medium: рейка = Medium Steel Cannon Layer + Shaft; казенник = той самий набір + Steel Sliding Breechblock. Мінімальна установка: **27 сталі + 2 Shaft + 2 Cogwheel**.
- Large: рейка = Large Steel Cannon Layer + Shaft; казенник = той самий набір + Steel Sliding Breechblock. Мінімальна установка: **45 сталі + 3 Shaft + 2 Cogwheel**.
- Cluster: Compacting **4 сталевих зливків → 1 заготовка**, Sequenced Assembly **4 пресування → 1 корпус**. Фінальна Mechanical Crafting: **1 корпус + 1 Packed Guncotton + 8 споряджених суббоєприпасів**.
- Module: лиття **900 mB сталі → несвердлений блок → CBC Cannon Drill → незавершений корпус → механізм**. Rapid: Recoil Spring; Traverse: Precision Mechanism.

## C. SMALL LAUNCHER

Структура: `Breech / [Rapid або Traverse] / звичайні секції`, загалом 2–5 блоків. Казенник і модуль рахуються; конструкція `Breech / Module` допустима. Другий модуль, неправильна позиція, незавершений модуль і невідповідна сталева структура відхиляються. Модуль сумісний лише зі сталевою Small.

`SmallLauncherStructure` порівнює ролі, а не повний BlockState; axial reverse facing модуля та waterlogged не змінюють роль. `LauncherAssembly` перевіряє всі бокові установки до передачі керування штатному вилученню блоків зі світу. Medium/Large не отримали ліміт довжини Small.

| Загальна довжина | Spread до config |
|---|---:|
| 2 | 1.7625 |
| 3 | 1.1750 |
| 4 | 0.5875 |
| 5 | 0 |

`RocketMountLaunchMixin` передає це значення в чинний `shoot()`, помножене на наявний server spread multiplier один раз. Однакова довжина дає однакову точність незалежно від модуля.

## D. MODULES

**Rapid:** множник 2 використовується в розрахунку загального cooldown у `RocketLauncherAssemblyMixin`; дробовий залишок, чергування бокових установок і їх бонус збережені. Відображуваний `getReferencedFireRate()` використовує той самий множник. 0.3375 × 2 = колишній Small baseline 0.675. Arming і spread модуль не змінює.

**Traverse:** `LauncherTraverseMixin` змінює обидва штатні `CannonMountInterfaceBlockEntity.getSpeed()` у tick CBC mount перед його кутовими обмеженнями. Нульова швидкість залишається нульовою. Ручне наведення гравця та ручний carriage залишені штатними згідно з рішенням користувача.

**Radars:** контекст тільки для відповідної Small/Traverse; `DirectCbcMountMotion.maxSpeedForRpm()` отримує cap 7.5 замість 5. За малих RPM швидкість не змінюється. Прискорення 0.5°/tick², гальмування й обмеження цільовим кутом залишені в Radars. `estimateSlewTicks` використовує той самий cap. Після виклику контекст відновлюється. Інші установки не прискорюються.

**Виробництво:** дві форми, кожна вирізається з одного log за наявною CBC/CBCA-конвенцією. Окремі cast shapes: 900 mB сталі → `UnboredAutocannonBlock` → штатний `drill_boring` → `IncompleteAutocannonBlock`. Rapid: 1 `createbigcannons:recoil_spring`; Traverse: 1 `create:precision_mechanism`. У тесті викликано справжнє CBC-завершення з FakePlayer: із двох деталей залишається одна, блок стає завершеним і зберігає напрямок. Повний цикл машини Deployer ще потребує ручної перевірки.

**Вигляд:** модулі успадковують Small selection/collision shape; деталі пружини/редуктора не перекривають отвори й не виходять за межі сусідніх бокових секцій. Основа генерується з тих самих констант, що й звичайні трубки. Пружинна касета використовує атлас CBC steel autocannon, редуктор — Create gearbox. Казенник зберігає геометрію, отвори й вихідну сталеву поверхню: лише шість пікселів на кожній зовнішній бічній поверхні утворюють стрілку. Поверхні розбиті на ділянки зі збереженням UV та геометричних меж; виступів немає. Vanilla observer/iron block і суцільна brass block поверхня більше не використовуються. Предмет казенника посилається на ту саму модель зі стрілкою. Огляд через справжній рендерер гри: `build/architecture-client-minimal/launcher-model-review.png`; стрілка видима, основна поверхня збігається з рейкою.

## E. ARMING

- Власник: зібрана contraption/група, поле `cbcatfix$armingTicksRemaining`. Жодних прапорців у ракетах або слотах.
- Успішне нове assemble встановлює 20/40/100. Виняток складання не запускає таймер.
- Активний серверний tick зменшує таймер до нуля; unload і зупинений сервер час не витрачають.
- `fireShot` повертається до вибору слота, round-robin, cooldown і вилучення ракети, якщо таймер > 0.
- NBT: `CbcatFixArmingTicks`. Читання тієї самої сесії зберігає залишок; значення обрізається до 0..duration. Відсутнє поле в старій збереженій contraption дає повну підготовку один раз.
- Рух і наведення не створюють нову сесію. Справжнє CBC reassembly створює її заново. Перевірені Sable-класи рухають/відстежують чинну Create contraption; окремого скидання на Sable motion не додано. Повний рухомий корабель перевіряє користувач.
- Vanilla tracked entity data передає стартову/періодичну/нульову корекцію, без відправки повного стану кожен tick. Клієнт прогнозує відлік до 1 tick; готовність 0 підтверджує сервер.
- HUD використовує штатне наведення Create і наявний CBC renderer окулярів. Локально підставлений для overlay hit не змінює світову взаємодію або стрільбу. Перед статусом додано відступ під штатний значок окулярів. Статуси: порожня група → підготовка з округленням угору до 0.1 s → Armed. Незібраний блок: Assemble to arm.

## F. MEDIUM / LARGE

Геометрія, сімейства ракет, завантаження та точність збережені. Medium має 4 ракети на lane й бокові установки. Large має одну ракету над рейкою і не отримує бокових установок або модулів. Його реальна частота повторного пострілу включає розбирання, перезаряджання та нову підготовку.

Нові baseline: Medium 0.375, Large 0.25; arming 40/100 ticks. Звичайний CBC HE payload Large збережений. Незалежні рецепти реалізовані за розділом B. Старі Medium-рецепти перевизначені, Medium→Large cutting видалено.

## G. CLUSTER

Виконано Shift-опис Variant A: вісім справжніх суббоєприпасів, окремий основний підривник носія, власні налаштовані підривники суббоєприпасів.

`ClusterPayload.assemble`, `CLUSTER_FUZES`, кількість, фізика, фабрики проєктилів і рендерери залишені чинними. Додаткова перевірка збирає кожен тип кластера з вісьмома різними Timer-налаштуваннями та перевіряє всі вісім окремо, один output і відсутність автоматичного основного підривника. Споживання компонентів лишається штатним Mechanical Crafting; нової системи інвентарю немає.

Фінальний рецепт реалізовано для HE/HEF/HEAT/Smoke: 1 `cbcatfix:cluster_casing` + 1 `createbigcannons:packed_guncotton` + 8 відповідних споряджених суббоєприпасів. Окремі `cluster_casing_blank` та `incomplete_cluster_casing` проходять штатні Compacting і Sequenced Assembly Create; четверте пресування дає один корпус зі 100% виходом. Fluid Shell не використовується.

У клієнті перевірено 7 справжніх heavy-autocannon моделей і 14 викликів їх native renderer для tracer/non-tracer; відкату на сірі сфери в цих шляхах немає.

## H. MIGRATION

Старі registry IDs bronze/cast-iron Small залишені. Автоматичного перетворення на сталь або видалення блоків/предметів немає. Старі матеріали зберігають свій шлях геометрії/точності, отримують новий ROF та arming, не приймають нові модулі.

`LegacySmallLaunchers` приховує завершені й unbored варіанти з creative; optional JEI plugin прибирає їх, якщо інший постачальник усе ж додав їх до JEI. `pack.mcmeta` фільтрує тільки вісім конкретних legacy casting/drilling ресурсів. На сервері підтверджено фактичну відсутність цих ресурсів. Форми й сталеві рецепти не вилучені.

Складні старі світи з уже зібраними legacy-групами не тестувалися бойовою сценою. Збереження класів/IDs та NBT-читання перевірено кодом і тестами; користувацькі світи залишаються пунктом ручної перевірки.

## I. VALIDATION

Версії: Java 21; Minecraft 1.21.1; NeoForge 21.1.228; Create 6.0.10; CBC 5.11.7; CBCA 0.1.4c; optional Sable 2.0.5, Radars 5.0-EA-2.0.2, JEI 19.27.0.340, Jade 15.10.6, Create Kaboom 1.0.0-EA.

Середовище команд: `JAVA_HOME=C:/Users/andrt/.jdks/ms-21.0.12`, `GRADLE_USER_HOME=C:/Users/andrt/.gradle`.

```text
./gradlew.bat build --offline --console=plain --no-daemon --project-cache-dir build/architecture-cache -I build/architecture.init.gradle

./gradlew.bat runServer -PminimalRuntime -PrunDirectory=build/architecture-server-minimal --offline --console=plain --no-daemon --project-cache-dir build/architecture-cache -I build/architecture.init.gradle -I dev/assembly-checks/checks.init.gradle

./gradlew.bat runServer -PrunDirectory=build/architecture-server-dev --offline --console=plain --no-daemon --project-cache-dir build/architecture-cache -I build/architecture.init.gradle -I dev/assembly-checks/checks.init.gradle

./gradlew.bat runClient -PminimalRuntime -PrunDirectory=build/architecture-client-minimal --offline --console=plain --no-daemon --project-cache-dir build/architecture-cache -I build/architecture.init.gradle -I dev/assembly-checks/checks.init.gradle

./gradlew.bat runClient -PminimalRuntime -PrunDirectory=build/architecture-client-minimal -PquickPlayWorld=Architecture --offline --console=plain --no-daemon --project-cache-dir build/architecture-cache -I build/architecture.init.gradle -I dev/assembly-checks/checks.init.gradle

./gradlew.bat runClient -PrunDirectory=build/architecture-client-dev -PquickPlayWorld=Architecture --offline --console=plain --no-daemon --project-cache-dir build/architecture-cache -I build/architecture.init.gradle -I dev/assembly-checks/checks.init.gradle
```

- Build: SUCCESSFUL після виробничих і візуальних виправлень; `build/launcher-production-optional-client.log` та фінальний `build/launcher-model-final-client.log` містять успішні `runClient` і `build`. Стандартна Gradle test-задача не містить JUnit-тестів; перевірки виконуються опційним runtime audit через справжні трансформовані класи гри.
- Мінімальний сервер: існуючий `world` досяг Done; **1975 assertions passed**. Із них 734 перевірки launcher progression/lifecycle/manufacturing, включно з реальними чотирма послідовними пресуваннями. Лог: `build/launcher-production-server.log`.
- Optional сервер: існуючий `world` досяг Done; **2002 assertions passed** після додавання свердління та остаточних рецептів. Лог: `build/launcher-production-optional-server.log`. Окремо 27 перевірок Radars cap/acceleration/braking/zero-drive; обидва controller mixins завантажені.
- Client main menu: 120 кадрів TitleScreen, штатне завершення.
- Normal world: наявний `Architecture`, гравець активний 100 ticks, штатне збереження/завершення. Нові рецепти/моделі перевірено в minimal і optional runtime: `build/launcher-production-client.log`, `build/launcher-production-optional-client.log`.
- Попередній minimal client після зміни HUD: **SUCCESSFUL**, обидва overlay-класи Create/CBC прийняли `LauncherGogglesTargetMixin`; наявний світ пройшов 100 активних ticks, моделі та 14 викликів renderer перевірені. Лог: `build/launcher-final-client-build.log`. Команда: наведений вище minimal `runClient` із `-PquickPlayWorld=Architecture` та додатковою задачею `build`.
- Усі моделі восьми module/mould items, усі їх blockstates та три стадії кластерного корпусу присутні. Клієнт зберігає огляд моделей штатним рендерером на тимчасовому екрані без розміщення блоків у світі.
- JEI: optional client із новими drilling/compacting/sequenced/mechanical crafting рецептами пройшов без помилки нашого plugin.
- Залишаються сторонні попередження/ресурсні помилки CBCA і Kaboom: відсутні старі CBCA-моделі/текстури; Kaboom має два некоректні PNG-path із пробілами й loot table для незареєстрованого `heatseeker_small`. Залежності не змінювалися; нові моделі перевірені окремо.
- Спеціальних світів, GameTest-сцен або бойових сцен не створювали. Assembly fixtures існують тільки в пам’яті.
- JAR: `build/architecture-20260915/libs/cbcatfix-1.1.0.jar`; перевірено наявність generated resources та **0 audit/test classes**.
- Git: змінені/нові джерела, ресурси й документація в робочій гілці; build/log/runtime артефакти ігноруються. `git diff --check` проходить. Commit/push/merge не виконувались.

Автоматичні перевірки не є підтвердженням повного бойового балансу, фактичного руху корабля або читабельності HUD з усіх ракурсів.

## J. REMAINING MANUAL TESTS

- [ ] Окуляри: порожня група, відлік, Armed, перехід після останньої ракети; українська десяткова кома.
- [ ] Одночасний вогонь/round-robin трьох установок; Rapid порівняно зі звичайною Small.
- [ ] Точність Small довжин 2/3/4/5 у звичайній грі.
- [ ] CBC yaw/pitch Traverse, межі кутів, втрата приводу; ручне наведення/carriage.
- [ ] Radars: довгі й короткі повороти, низькі RPM і високий cap, рухомий корабель Sable.
- [ ] Справжній Deployer завершує обидва модулі, а весь цикл лиття використовує правильні форми.
- [ ] Стрілки казенника, видимість пружини/редуктора й суміжні бокові установки.
- [ ] Legacy bronze/cast-iron у копії старого світу, unload/reload зібраної установки.
- [ ] Реальний кластерний постріл з різними налаштуваннями підривників; повний автоматичний виробничий ланцюжок.
