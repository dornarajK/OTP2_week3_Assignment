# Temperature Converter

A JavaFX temperature converter with localized unit names and descriptions, conversion history, and a journey database. The application uses Java 17, Maven, and an H2 database.

## Database localization approach

This application uses a normalized, database-backed localization approach for measurement-unit names and descriptions. 
Unit data that is not language-specific is kept separate from its translations. 
The `unit` table stores each unit once, including its `unit_id`, unique `unit_key`, `category`, and `symbol`. 
For example, the Celsius unit has the key `celsius`, belongs to the `temperature` category, and uses the symbol `°C`. 
This information is the same regardless of the selected language and is therefore not duplicated for every locale.

Translated text is stored in the `unit_translation` table. 
Each row contains a `unit_id`, `language_code`, translated `name`, and translated `description`. 
The composite primary key `(unit_id, language_code)` ensures that a unit has at most one translation for each language. 
The `unit_id` column is also a foreign key referencing `unit(unit_id)`, so translations cannot point to units that do not exist.
The seed data includes English (`en`), Finnish (`fi`), Swedish (`sv`), Japanese (`ja`), and Arabic (`ar`) translations.

When units are read, the DAO joins the unit data to 
the translation for the selected language and also joins its English translation. SQL `COALESCE` 
uses the selected-language name or description when present and falls back to English when that translation is missing. 
The conversion logic uses stable language-independent unit keys such as `fahrenheit` and `celsius`; changing a displayed 
translation therefore does not affect calculations or conversion-history references.

This design avoids repeating unit categories, keys, and symbols for every language, 
and keeps translated text out of the conversion logic. It also makes localization maintainable: adding a language 
means adding translation rows rather than changing application code or altering the unit schema. The relational 
keys help preserve data integrity, while the English fallback prevents an untranslated unit from appearing without a label. 
The trade-off is that localized queries require joins and the translation records must be seeded and maintained alongside 
the units.

## Build and test

```bash
mvn clean verify
```

The JaCoCo report is generated at `target/site/jacoco/index.html`.
