# Окремі ID ракет для /give

Додано 22 варіанти предметів: два варіанти для кожного з 11 типів ракет. Після встановлення нового JAR компоненти у команді вказувати не потрібно.

Велика AP з подвійним паливом:

```mcfunction
/give @p cbcatfix:big_ap_rocket_double_fuel 1
```

Велика AP з подвійним бойовим зарядом:

```mcfunction
/give @p cbcatfix:big_ap_rocket_double_payload 1
```

До кожного базового імені нижче додай `_double_fuel` або `_double_payload`:

| Тип | Базове ім'я |
|---|---|
| Мала AP | `cbcatfix:ap_rocket` |
| Мала Flak | `cbcatfix:flak_rocket` |
| Мала HE | `cbcatfix:he_rocket` |
| Мала HEI | `cbcatfix:hei_rocket` |
| Середня AP | `cbcatfix:medium_ap_rocket` |
| Середня HE | `cbcatfix:medium_he_rocket` |
| Середня HEF | `cbcatfix:medium_hef_rocket` |
| Середня HEAT | `cbcatfix:medium_heat_rocket` |
| Велика AP | `cbcatfix:big_ap_rocket` |
| Велика HE | `cbcatfix:big_he_rocket` |
| Велика HEAT | `cbcatfix:big_heat_rocket` |

Наприклад: `/give @p cbcatfix:medium_heat_rocket_double_payload 8`.

`double_fuel` відповідає наявному дальнобійному рецепту: один бойовий заряд і повний налаштований час роботи двигуна. `double_payload` відповідає рецепту з двома бойовими зарядами та половиною цього часу. Це зручні ID існуючих конфігурацій, без зміни їхнього балансу. Для вибухових ракет ф'юз установлюється як раніше.

Варіанти додані у творчу вкладку CBCAT. Рецепти тепер видають ці ID, зберігаючи попередні інгредієнти та перенесення компонентів бойової частини/ф'юзу. Старі item ID і раніше створені ракети з компонентами продовжують працювати. Моделі успадковані від відповідних старих моделей; назви є українською й англійською.

Реалізація використовує ті самі класи предметів CBCAT/мода та `ModifyDefaultComponentsEvent` NeoForge. Нових projectile types, класів польоту або mixin немає. Компоненти задаються після завершення реєстрації предметів; це працює для `/give`, creative, recipe decoding і save/load.

Перевірено на dedicated server без optional mods: усі 22 ID, payload count, конфігурована тривалість палива, результат native projectile factory, recipe result, ammunition tag і ItemStack codec roundtrip. Перевірки не створювали сутностей/блоків або спеціального світу. Результат: `ROCKET VARIANT CHECKS PASS: 22 IDs` у `build/rocket-variants-validation.log`. Фінальний звичайний build пройшов; тимчасовий validation-клас не включено до JAR. Прямі посилання нових моделей перевірено, missing assets = 0. Візуальна перевірка нових назв/creative entries у клієнті окремо не проводилась.
