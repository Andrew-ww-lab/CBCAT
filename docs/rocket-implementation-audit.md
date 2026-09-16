# Аудит ракет — 15.09.2026

## A. PREVIOUS CHANGE REVIEW

Обсяг встановлено до редагування за історією попереднього виконання, поточними файлами
та diff. Багато файлів уже були untracked до задачі; git status сам по собі не доводить,
що файл створила остання задача. Коміт останньої реалізації відсутній.

Попередня задача додала:

- src/main/java/com/cbcatfix/rocket/RocketBallistics.java
- src/main/java/com/cbcatfix/rocket/RocketDamage.java
- src/main/java/com/cbcatfix/rocket/RocketEngineAccess.java
- src/main/java/com/cbcatfix/rocket/RocketObstacle.java
- src/main/java/com/cbcatfix/mixin/RocketDetonationGuardMixin.java
- src/main/resources/data/cbcatfix/tags/block/rocket_permeable_foliage.json
- src/main/resources/data/cbcatfix/tags/block/rocket_permeable_fragile.json
- src/main/resources/data/cbcatfix/tags/block/rocket_permeable_soft.json
- dev/rocket-tests/java/com/cbcatfix/rocket/tests/RocketCollisionTests.java
- docs/rocket-collisions.md

Попередня задача змінила:

- build.gradle
- src/main/java/com/cbcatfix/config/CbcatFixConfig.java
- src/main/java/com/cbcatfix/rocket/RocketBalance.java
- src/main/java/com/cbcatfix/rocket/RocketFlightState.java
- src/main/java/com/cbcatfix/rocket/RocketSteering.java
- src/main/java/com/cbcatfix/rocket/RocketPenetrationBalance.java
- src/main/java/com/cbcatfix/mixin/AbstractCannonProjectileAccess.java
- src/main/java/com/cbcatfix/mixin/AbstractRocketMixin.java
- src/main/java/com/cbcatfix/mixin/AbstractMediumRocketMixin.java
- src/main/java/com/cbcatfix/mixin/AbstractRocketFlightMixin.java
- src/main/java/com/cbcatfix/mixin/AbstractMediumRocketFlightMixin.java
- src/main/java/com/cbcatfix/mixin/RocketDurabilityMixin.java
- src/main/resources/cbcatfix.mixins.json

Видалених шляхів не було: RocketPenetrationBalance і три mixin-файли переписані.
Додані поля: launchMass, impactVelocity, detonated, CBCATFIX_ENGINE_DISABLED;
конфіг AP для трьох розмірів. Нові NBT: CbcatFixEngineDisabled, CbcatFixFlightMass,
CbcatFixMechanicsVersion. Нових пакетів, capabilities, інвентарів, renderer-ів,
launcher-систем або Sable lifecycle у цій задачі не було.

Аудит додатково охоплює регресію маневреності після розкиду. Виправлення дюпу
залишається недоторканим за прямим уточненням користувача. Згадана у вкладенні
стабілізація фізичних бомб не була реалізована попередньою задачею.

Виявлено до виправлення:

1. CBC.clipAndDamage перезаписує керовану орієнтацію напрямком траєкторії
   після RocketSteering.tick. Керований поворот частково втрачається кожен тік.
2. impactVelocity і три redirects дублюють обчислення швидкості CBC.
   getForces не додає прискорення до стану під час кожного виклику — це чистий розрахунок.
3. Окремий detonated дублює removeNextTick CBC.
4. Прохід легких блоків через setBlock оминає звичайні ефекти destroyBlock.
5. Гілка захищених блоків змінювала масу також на клієнті.
6. Для вибухових ракет зберігалася зайва копія native Fuze у UniversalFuze.
7. Залишився метод maximumDurability(tier, armorPiercing), що ігнорував другий аргумент.
8. Краш `crash-2026-09-14_19.55.10-server.txt`: при створенні AutocannonHEProjectile
   не визначався inherited SynchedEntityData slot 11. Accessor зареєстрований на
   AbstractCannonProjectile, тому builder.define обов'язковий для всіх його нащадків.
   Умовну ініціалізацію лише для ракет, внесену під час аудиту, скасовано.
9. Перевірка dev-клієнта виявила некоректний @Shadow inherited-поля у multi-target
   RocketDetonationGuardMixin. Доступ переведено на accessor базового класу
   AbstractCannonProjectile, де поле реально оголошено.

## B. REIMPLEMENTATION AUDIT

| Custom Mechanic | Existing Minecraft/Mod Alternative | Action | Reason |
| --- | --- | --- | --- |
| Кеш impactVelocity та redirects getForces | CBC/CBCAT getForces і native sweep | REMOVED AS UNUSED | Чиста функція не накопичує прискорення; зайвий кеш видалено |
| Пробиття твердих блоків | CBCAT calculateBlockPenetration, CBC mass/impact | KEPT — CUSTOM BEHAVIOR REQUIRED | Native розрахунок збережено; власні тільки AP-властивості та легкі перешкоди |
| Руйнація легких блоків через setBlock | Level.destroyBlock(pos, false, entity) | REPLACED WITH EXISTING API | Стандартні звук, частинки, game event, рідина без дропу |
| HP корпусу та відмова двигуна | CBC hurt повертає false; projectileMass є бюджетом пробиття | KEPT — CUSTOM BEHAVIOR REQUIRED | Корпус, двигун та пенетратор мають різні наслідки пошкодження |
| Прапорець detonated | CBC removeNextTick | REPLACED WITH EXISTING API | Native стан завершення польоту блокує повторну детонацію, зокрема рекурсивну |
| Копія fuze вибухових ракет | Native Fuze і його NBT | SIMPLIFIED | UniversalFuze залишається тільки для AP; seeker зберігає захоплену ціль |
| Максимальні HP у NBT | RocketBalance.maximumDurability(tier) | SIMPLIFIED | Похідне значення не записується; старе читається для міграції |
| Орієнтація керованої ракети | CBC getOrientation/setOrientation | SIMPLIFIED | Powered attitude не перезаписується native shell alignment після sweep |
| launchMass | Native projectileMass зменшується при пробитті | KEPT — CUSTOM BEHAVIOR REQUIRED | Інерція польоту не повинна зникати разом із бюджетом пробиття |
| RocketObstacle | Приватний enum у RocketPenetrationBalance | SIMPLIFIED | Один користувач, немає потреби в окремому публічному класі |
| Окремий engine-disabled accessor | SynchedEntityData | KEPT — CUSTOM BEHAVIOR REQUIRED | Клієнту потрібен стан двигуна; inherited slot визначається для всіх CBC projectiles |
| Захист території | ProjectileDamageHooks, GriefState | KEPT — CUSTOM BEHAVIOR REQUIRED | Native CBCAT STOP усе ще може викликати partial block damage; захищений шлях завершується до нього |

## C. STATE / LIFECYCLE AUDIT

Перевірено original CBC AbstractCannonProjectile/AbstractBigCannonProjectile,
CBCAT AbstractRocket/AbstractMediumRocket і обидва FuzedRocket,
Sable SubLevelAssemblyHelper.moveBlocks та Simulated SimAssemblyHelper.disassembleSubLevel.

LauncherTransfer — короткоживуча транзакція синхронного native moveBlocks, не постійний
інвентар. Нативний NBT завантажується заміною, успішний приймач очищає native source
через Clearable перед видаленням. Source/destination блокуються в межах цього виклику.
Слоти — view на native CBCAT буфери; автоматизація використовує IItemHandler.
Create contraption володіє presentBlockEntities; Sable plot — справжніми world BE.
Цей виправлений шлях не редагується і не оголошується повторно перевіреним у геймплеї.

Detached фізична ракета зберігає ItemStack у RocketBlockEntity. Переміщення виконує
Sable assembleBlocks, pose/velocity — Sable API. Нової bomb-stabilization системи немає.
Орієнтація flying projectile — CBC getOrientation/setOrientation; previousForward
використовується лише для інтерполяції, а не як друга поточна орієнтація.

Після запуску projectile entity володіє payload/seeker, native fuel sentinel і
тривалістю powered flight. CBC projectileMass належить пробиттю, launchMass — інерції.
Вибухові ракети використовують native Fuze; AP мають UniversalFuze, бо native AP
не надає цього сховища. HP корпусу та engine-disabled змінюються сервером;
SynchedEntityData передає двигун клієнту, без власного пакета. У гілці protected
terrain зміни маси й onImpact також обмежено сервером.

Вилучені дублікати impactVelocity, detonated і копія native fuze. NBT зберігає
launchMass/engine-disabled/поточні HP/seeker; maximumDurability обчислюється з tier.
Читання старого CbcatFixMaximumDurability лишилося для міграції v1 HP. Spawn-дані
максимуму HP лишилися для клієнта, оскільки common config не синхронізується.
Native subclass відновлює Fuze після superclass NBT: fallback до ще не прочитаного
native fuze не використовується. Physicalize/dephysicalize зберігають native NBT
через Sable/Simulated moveBlocks; нових власників інвентарю не додано.

## D. PERFORMANCE AUDIT

Прибрано кеш вектора impactVelocity, його capture/reset та три redirects.
hasGuidance перевіряє наявність seeker без звернення до дубльованого fuze.
Зменшено копіювання ItemStack при конфігурації вибухової ракети та зайвий NBT-запис
максимуму HP. Окремих оптимізацій scans, capability lookups, network updates,
Sable transforms або rendering не виконувалося. Числовий приріст FPS не вимірювався.

## E. CODE REMOVED / REPLACED

- Видалено RocketObstacle.java; значення перенесено в приватний enum єдиного споживача.
- Видалено impactVelocity, detonated, capture/reset/redirect helpers.
- Видалено maximumDurability(tier, armorPiercing); callers використовують tier.
- Native CBC removeNextTick замінює власний прапорець детонації.
- Native CBC getForces/clipAndDamage замінюють кеш швидкості зіткнення.
- Level.destroyBlock замінює власне setBlock відновлення рідини.
- Цілі mixin-класи та пакети не видалялися; нових пакетів не додано.
- Виправлення дюпу, перенесення launcher inventory та фізична стабілізація не змінювалися.

## F. VALIDATION

У цій задачі GameTest server не запускається і тестовий світ не створюється.
Результати десяти тестів попередньої задачі не є перевіркою наступних виправлень.

Команди (Java 21.0.12; локальний Gradle cache):

```powershell
./gradlew.bat build --offline --console=plain --no-daemon --project-cache-dir build/rocket-audit-cache-20260915 -I build/rocket-audit.init.gradle
./gradlew.bat runClient '-PquickPlayWorld=New World (2)' --offline --console=plain --no-daemon --project-cache-dir build/rocket-audit-cache-20260915 -I build/rocket-audit.init.gradle
```

Окремий build directory потрібен через заблоковані Windows файли старої збірки.
Фінальна команда build: BUILD SUCCESSFUL, test NO-SOURCE; це компіляція/пакування,
а не виконані gameplay tests. Артефакт: build/rocket-audit-20260915/libs/cbcatfix-1.0.0.jar.
Проміжні запуски виявили @Shadow validation/apply errors; фінальна реалізація
використовує accessor базового класу. Проміжний crash-2026-09-15_08.37.02-fml.txt
належить цій перевірці й не є початковим крашем користувача.
Головне меню візуально не підтверджено: інструмент захоплення Minecraft повертає
зображення Codex навіть після повторної прив'язки та перезапуску інструмента.

Фінальний запуск: `build/rocket-audit-world-final.log`. О 08:39:15–08:39:19
успішно застосовані RocketDurabilityMixin, AbstractCannonProjectileAccess і
RocketDetonationGuardMixin до всіх чотирьох цільових класів. InvalidMixinException,
FATAL, нового crash report і помилки synched data у фінальному запуску немає.
Є попередження про public targets mixin та сторонні відсутні моделі Create Radars;
усі вони не зупинили завантаження світу.

Звичайний існуючий світ New World (2) відкрито через наявний Quick Play:
08:39:33 Starting integrated minecraft server / Preparing start region;
08:39:34 Dev logged in; далі завершено завантаження renderer/shaders.
Вхід підтверджено журналом, візуальну перевірку через несправне захоплення не виконано.
Клієнт залишено запущеним у світі для ручної перевірки користувачем.
Вихід зі збереженням цим аудитом не перевірявся. Повторний постріл HE, що спричинив
початковий краш, також лишається ручною перевіркою; виправлено причину в коді.

JAR перевірено: потрібні mixin/класи й три block tags присутні; старого
RocketObstacle.class та GameTest-класів немає. git diff --check для src/dev/docs
не знайшов whitespace errors. Автоматично змінені run logs/build outputs не є кодом аудиту.

Ручна перевірка: повороти після розкиду на різних швидкостях і розмірах,
пуск із рухомого носія, втрата двигуна/керування, підрив HE/HEAT і різних fuzes,
AP після вичерпання корпусу, багатошарове пробиття, легкі/захищені блоки,
збереження та повторне завантаження ракети. Нових механік або змін балансу цей аудит не додає.
