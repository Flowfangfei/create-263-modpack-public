# Contributing to the Fabric port

Use Java 25 and the checked-in Gradle wrapper. Dependency versions are pinned in `gradle.properties` and `build.gradle`; see [FABRIC_PORT.md](FABRIC_PORT.md) for the test and datagen commands.

Keep existing registry identifiers and saved-data fields compatible. Describe behavior changes in [CHANGELOG.md](CHANGELOG.md), using change entries only.

## Translations

1. Use [the English localization](src/generated/resources/assets/electroenergetics/lang/en_us.json) as the reference.
2. Add or update the appropriate JSON file in [the translations directory](src/main/resources/assets/electroenergetics/lang/), for example `ru_ru.json`.
3. Preserve translation keys, format placeholders and valid JSON syntax.

## Validation and review

Run `./gradlew build` for code changes and `./gradlew runClientGameTest` for client changes. Use `-PwithOptionalCompat` when changing CC:Tweaked or JEI integration. Review the resulting diff and any generated resources before manually submitting changes.

The original mod and its assets are credited to George VI; preserve existing copyright and license notices.
