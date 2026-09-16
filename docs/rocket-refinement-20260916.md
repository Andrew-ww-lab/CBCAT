# Уточнення фізики та балансу ракет — 16.09.2026

## 1–4. Причина й нова ієрархія дальності

Перевірені `AbstractRocket` / `AbstractMediumRocket` CBCAT, інтегратор і drag CBC, `RocketSteering`, `RocketBallistics`, `RocketStackFactory`, обидві пускові та Sable launch bridge. Знімок до змін: `build/refinement-baseline.json`.

Ракети успадковували аеродинаміку **боєголовки**: мала AP — лінійний drag 0.01, середня AP — квадратичний 0.013. Велика AP також використовувала квадратичний опір важкої автогармати. За 6 блоків/tick це приблизно 0.468 блока/tick² гальмування; мала на швидкості 9 втрачала лише 0.09. Велика додатково мала власну слабку gravity −0.015, а її розгін був повільніший. Тому запас палива сам по собі не забезпечував потрібного порядку дальності. Результат залежав і від типу боєголовки. Збережені старі SERVER configs також могли перекривати нові defaults.

Властивості руху корпусу тепер визначає розмір ракети. Бойові mass/penetration/toughness/deflection залишаються властивостями пенетратора за CBC; ці два поняття не змішані.

| Параметр | Мала до → після | Середня до → після | Велика до → після |
|---|---|---|---|
| Powered speed, блоків/tick | 9 → 9 | 6 → 6 | 3.5 → 4.5 |
| Повне паливо, s | 2 → 1.5 | 6 → 6 | 18 → 18 |
| Response ticks | 3 → 3 | 7 → 6 | 14 → 10 |
| Gravity після двигуна, AP приклад | −0.0325 → −0.035 | −0.039 → −0.04 | −0.0195 → −0.045 |
| Drag, AP приклад | linear 0.01 → linear 0.035 | quadratic 0.013 → linear 0.015 | quadratic 0.013 → linear 0.006 |

Нові gravity/drag однакові для всіх боєголовок відповідного розміру. Велика має менший відносний опір, середня — компроміс, мала швидко розганяється, але швидше втрачає швидкість після короткої роботи двигуна. Моделі, місткість 12/4/1, payload recipes і кутові caps 36/18/4 не змінювалися.

**Розрахункова горизонтальна дальність**, а не вимір у Minecraft: нерухомий носій, старт горизонтально на висоті 64 блоки над площиною, AP, без наведення, перешкод, води, вітру, dimension overrides і меж завантажених чанків. Розрахунок використовує native CBC `position += velocity + force/2`, `velocity += force`, без будь-якого нового інтегратора в моді. Скрипт: `build/refinement-range.py`, результати: `build/refinement-range.json`.

| Варіант | До: мала / середня / велика | Після: мала / середня / велика |
|---|---|---|
| Подвійне паливо | 790 / 821 / 1343 | **491 / 938 / 1796** |
| Полегшена | 621 / 478 / 731 | **439 / 711 / 1204** |
| Подвійний заряд | 584 / 431 / 678 | **327 / 552 / 953** |

Це підтверджує проблему малої проти середньої в однопаливних варіантах за попередніх defaults. Розрахунок не відтворює всі спостереження користувача для довільного ландшафту чи власного конфігу. У всіх трьох порівняннях однакових варіантів новий порядок правильний. Мала дальнобійна може конкурувати з короткодальнім варіантом більшої ракети за інших умов — це припустиме порівняння різних ролей, не гарантія універсальної дальності.

## 5–9. Балістика, гравітація та обмеження швидкості

Жорсткого загального clamp до маршової швидкості в перевіреному серверному шляху не було. `MAX_POWERED_SPEED=20` обмежує лише цільову швидкість двигуна. Старий код уже не викликав `poweredForces` після завершення палива, але тоді повертався до CBCAT `getForces` із успадкованим квадратичним опором і multiplier gravity 1.3. Низька terminal velocity виглядала як заборона прискорення. Для старої великої AP теоретична terminal speed становила приблизно `sqrt(0.0195 / 0.013) = 1.225` блока/tick.

Тепер після вимкнення двигуна обидва flight mixins прямо викликають **`super.getForces(position, velocity)` CBC**. Це штатні drag, gravity, dimension multipliers і fluid drag. Немає cruise normalization, залишкової тяги, власного gravity accumulator або нового hard safety cap. CBC залишає свій запобіжник `drag <= current speed`, щоб сила опору не перевертала швидкість за один крок; це не обмеження швидкості польоту.

У нової великої ракети terminal speed у звичайному повітрі приблизно `0.045 / 0.006 = 7.5`, вище її cruise 4.5. Перевірка реального mixin-шляху показала від'ємне прискорення Y при швидкості падіння 5 блоків/tick та нульовому паливі. На нульовій швидкості прискорення дорівнює −0.045, без старого ×1.3. Малі ракети можуть природно сповільнюватися при падінні зі швидкістю вище своєї terminal velocity — це опір, а не cruise clamp.

Незалежні Sable block bodies не переведені в projectile physics: їх native rigid-body інтеграція лишилася у Sable. `SableRocketDetacher` задає успадковані початкові дані під час відокремлення, не переписує лінійну швидкість щотакту. Нової стабілізації чи gravity-системи не додано.

## 10–11. Полегшені ракети

Раніше speed modifier 1.0, маса 0.6, response modifier 0.6. Тепер speed modifier **1.2** в єдиному `RocketBalance.maxSpeed(ItemStack)`; його використовують і projectile configuration, і tooltip. Швидкості: **10.8 / 7.2 / 5.4** блока/tick проти звичайних **9 / 6 / 4.5**. Ліміт powered target 20 залишається загальним.

Маса 0.6 і розгін з меншим response збережені. За однакової відносної похибки швидкості початкова тяга приблизно у 2 рази більша за стандартну: 1.2 / 0.6. Поворот усе ще залежить від швидкості та маси; при новій cruise перевага кутового ліміту приблизно 18%, не 29%, через вищу швидкість.

Tradeoff: половина палива дальнобійної, 75% HP, один заряд. Double payload зберігає ×1.75 ефекту й маси та половину повного палива. Полегшені не послаблюють AP-пенетратор штучно.

## 12. Видалення HESH

Видалено лише `cbcatfix`-контент:

- `HESHShellBlock`, `HESHShellProjectile`, їх block/item/entity registration і property handler;
- creative entry, renderer binding, tooltip mapping;
- рецепт, block loot table, projectile property JSON;
- blockstate, block/item/fuze models, три спеціальні текстури;
- en_us/uk_ua display/tooltip translations;
- запис у big_cannon_projectiles tag;
- HESH munition record, чотири spall constants, 17 відповідних config values і невикористані описи.

Окремих HESH-ракет у поточному моді не було. CBC/CBCAT власні боєприпаси не змінено.

Для сумісності старі `cbcatfix:hesh_shell` у трьох реєстрах мають **штатний alias до `cbcatfix:heavy_he_shell`**. Старий предмет/блок/сутність тепер є Heavy HE із його поведінкою, а не HESH placeholder. Нових HESH отримати не можна: навіть старий ID дозволяє лише заміну. Перевірено resolution усіх трьох aliases. Готові світи без цих об'єктів завантажені; окремий реальний старий світ із розставленими HESH не був наданий, тому перед оновленням цінного світу доцільна звичайна резервна копія.

## 13–14. Темп малих пускових

`SMALL_FIRE_RATE`: **1.0 → 0.9**. Native найкоротший interval 3 ticks дає середній ефективний interval 3.333… ticks: **400 → 360 пострілів/хв** для однієї лінії. Накопичення дробового залишку зберігає потрібний середній темп.

Топологічні множники збережені: **1.00 / 1.25 / 1.50**. Нові максимальні темпи — **360 / 450 / 540 пострілів/хв** за default config. Це одна формула `base × 0.9 × topology`; окремих hardcoded режимів немає. Середні й великі лишилися повільнішими. Round-robin не змінено.

## 15–17. Вихід із пускової перед маневруванням

`RocketMounts.transform` розташовує центр корпуса так, що його задній край починається біля задньої грані казенної частини. Тому дистанція від початкового центра до muzzle плюс половина довжини корпуса дорівнює **довжині зібраної пускової**. Потрібний вихід: `usableLength + LAUNCH_CLEARANCE_MARGIN`, де margin = 1/16 блока.

При пострілі зберігаються лише launch axis, попередня world position, початкова швидкість носія й залишкова дистанція. Кожен tick вимірюється фактичне переміщення центра від попередньої позиції, віднімається переміщення носія за його початковою швидкістю і береться проєкція на launch axis. Бічний рух не зараховується як вихід. Умова відкриття керування: **`clearanceRemaining <= 0`**. Немає випадкової затримки в ticks і постійного посилання на launcher.

Це мінімальний snapshot виходу: раптовий поворот/прискорення Sable-носія протягом короткої фази виходу не відстежується заново. Це обмеження слід перевірити на різких маневрах носія. Raw швидкість носія відділена від gameplay-множника успадкованої швидкості, тому вимкнення inheritance не підробляє clearance.

Ціль визначається як раніше при створенні projectile. Тяга працює одразу; лише steering authority чекає clearance. Після виходу використовується існуюча angular acceleration/turn-rate система без snap. Незавершений clearance зберігається в NBT; старі ракети без цього поля лишаються сумісними. Client не потребує нового packet: керування серверне, орієнтація передається штатно.

## 18–20. Віддача й успадкована швидкість Sable

Причина віддачі: CBCAT `MountedRocketPodContraption.fireShot` та `MountedMediumRocketRailContraption.fireShot` рахували гарматний recoil і викликали старий двоаргументний `ControlPitchContraption.onRecoil`. Наш існуючий compatibility bridge передавав його у сучасний CBC callback, а той — у `SableCompat.recoilCannon`.

Новий redirect у `RocketMountLaunchMixin` гасить **сам виклик** лише в цих двох ракетних пускових. Великі рейки використовують другу. Немає counter-impulse; загальний compatibility bridge і гарматна віддача CBC/CBCAT залишені. Успадкування `Sable.HELPER.getVelocity` у точці пуску, перетворення /20 і серверний multiplier збережені.

## 21–22. Централізація й видалений старий код

Усі нові tuning values — у `BalanceDefaults`: small fuel 1.5, big speed 4.5, response 6/10, small fire rate 0.9, lightweight speed 1.2, три immutable `Airframe` records, clearance margin. Новий SERVER setting `rockets.physics.lightweightSpeedMultiplier`: default 1.2, range 1..1.5. Інші змінені defaults надходять у наявні config fields тим самим шляхом.

Видалено три дубльовані великі `getDefaultGravity()` та їх спільний старий constant −0.015. Rocket ballistic force path більше не використовує CBCAT thrust/×1.3 fallback. HESH-код і tuning видалені. Фузи, cluster payload, arm integration, ownership/dupe fix, звук, exhaust та naming не перероблялися.

**Існуючі `config/cbcatfix-server.toml` зберігають власні значення.** Для цього балансу перенесіть наведені defaults або свідомо відновіть default файл. Мод не перезаписує користувацькі налаштування. Тестові configs збережені як `cbcatfix-server.pre-refinement.toml` перед перевіркою нової генерації. Оновлений повний довідник — `refinement-config-reference.md`.

## 23–26. Перевірки

```powershell
# Фінальна збірка в новому каталозі, без stale HESH outputs
./gradlew.bat build --offline --console=plain --no-daemon --project-cache-dir build/architecture-cache -I build/refinement.init.gradle
# Runtime assertions лише у перевірочному середовищі
./gradlew.bat runServer -PminimalRuntime -PrunDirectory=build/architecture-server-minimal --offline --console=plain --no-daemon --project-cache-dir build/architecture-cache -I build/balance.init.gradle -I build/refinement-checks.init.gradle
./gradlew.bat runClient -PminimalRuntime -PrunDirectory=build/architecture-client-minimal --offline --console=plain --no-daemon --project-cache-dir build/architecture-cache -I build/balance.init.gradle -I build/refinement-checks.init.gradle
```

Build **SUCCESSFUL**, Java 21.0.12, 3 існуючі deprecation warnings. JUnit suite відсутній (`test NO-SOURCE`); виконані тимчасові server/client assertions: 22 варіанти, recipes/default components, fuel, NBT/spawn roundtrips, легкі корпуси, speed multiplier, clearance до/після межі та reload, aliases, реальні ballistic forces. Native класи обох пускових примусово завантажуються для перевірки застосування recoil mixin.

Dedicated minimal server завантажив існуючий Survival `world`, досяг `Done`, виконав assertions; client-only classloading error немає. Клієнт без Sable/Radar/Jade/JEI відкрив головне меню й існуючий Survival `Architecture`. Creative check: 33 ракети, суміжні трійки й однакові назви; HESH відсутній. Ресурсний аудит: **505**, відсутніх прямих assets **0**. Recipe count 3083 → 3082 через видалення одного HESH рецепта. Гру збережено та закрито, мишка вільна.

Логи: `build/refinement-final-build.log`, `build/refinement-server.log`, `build/refinement-client.log`. Артефакт: **`build/refinement-20260916/libs/cbcatfix-1.0.0.jar`**. Тимчасові assertions не входять у фінальний JAR. Попередні звіти описують попередні версії балансу.

## 27. Ручна перевірка

Порівняти всі три розміри й варіанти на однаковій висоті/куті, спочатку без наведення, потім по рухомій цілі. Окремо перевірити вертикальне падіння після fuel-out, довгі рейки та крайні кути пуску, одиночні/подвійні/потрійні залпи. На Sable — відсутність recoil при стрільбі, збереження швидкості рухомого носія та clearance на носії, який різко повертає/прискорюється. Перевірити міграцію на копії світу зі старими HESH. Польових стрільб і runtime запуску з Sable у цьому проході не виконувалося; це залишено користувачу за запитом.
