# Тестовий Ponder: мала ракетна установка

Перед початком роботи зафіксовано попередній стан:
d6513c9 — Complete launcher progression, ammunition recipes and visual cleanup.

## Як відкрити

Наведіть курсор на Steel Rocket Pod Breech або Steel Rocket Pod Rail в інвентарі чи JEI та утримуйте клавішу Ponder (типово W; у локалізованій розкладці може відображатися інший символ). Сцена називається «Будова малої ракетної установки».

Це перша навчальна сцена про конструкцію: станок, казенник, мінімальна установка з рейкою, максимум 3 блоки, місце одного модуля та вибір Rapid/Traverse. Реальне спорядження, сигнали збирання й стрільба не входять до цієї тестової сцени.

## Як це реалізовано

Вивчено API саме встановленого Ponder 1.0.82 для Minecraft 1.21.1, Create 6.0.10 та сцени CannonMountScenes із CBC 5.11.7. Схема відповідає [офіційному CreatePonderPlugin](https://github.com/Creators-of-Create/Create/blob/mc1.21.1/dev/src/main/java/com/simibubi/create/foundation/ponder/CreatePonderPlugin.java).

- LauncherPonderPlugin реєструє storyboard для двох наявних предметів CBCA.
- SmallLauncherScenes задає послідовність показу блоків, вказівники, паузи, ключові кадри та заміну рейки модулями.
- assets/cbcatfix/ponder/small_launcher.nbt — окрема структура навчальної сцени. Вона створена відтворюваним dev/ponder/generate_small_launcher.py без зміни збережених світів.
- Локалізація використовує стандартні ключі cbcatfix.ponder.small_launcher.header і text_1..text_5 в en_us/uk_ua.
- Реєстрація відбувається лише у клієнтському ClientSetup. Ponder працює у власному навчальному світі; сцена не збирає бойову установку у світі гравця.

Попередні моделі, текстури, рецепти й баланс не змінені. Нові зміни Ponder залишені окремо від підготовчого коміту.
## Перевірки

- Дві реєстрації предметів знаходять cbcatfix:small_launcher.
- Схема завантажується, заголовок локалізований, повний сценарій виконується до завершення; фінальна заміна рейки на Traverse перевірена.
- Сцену відкрито у справжньому PonderUI та відтворено повністю. Прев’ю збережені як build/architecture-client-minimal/small-launcher-ponder-1.png і small-launcher-ponder-2.png.
- Уточнено правильну висоту: станок Y=1, верхнє кріплення Y=2, казенник і рейки Y=3. Поворот верхнього кріплення узгоджено із західним напрямком установки.
- П’ять пояснень узгоджено з ключами en_us та uk_ua. Візуальна перевірка виконана англійською; українські рядки перевірені на наявність і відповідність послідовності.
- Клієнтська збірка: build/ponder-small-client.log. Окремий сервер: build/ponder-small-server.log, 1948 assertions passed.
- Test-only ClientStartupProbe виключений із готового JAR. Сцена демонструє конструкцію, а не симулює бойову стрільбу.

Готова збірка: build/architecture-20260915/libs/cbcatfix-1.1.0.jar.