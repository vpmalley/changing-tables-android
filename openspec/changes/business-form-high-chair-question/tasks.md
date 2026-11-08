# Tasks

## 1. Model and persistence

- [x] 1.1 In `app/src/main/java/fr/vpm/changingtables/models/Business.kt`, add `@Serializable enum class HighChairAvailability { @SerialName("multiple") MULTIPLE, @SerialName("single") SINGLE, @SerialName("none") NONE }` and a nullable `var highChairs: HighChairAvailability? = null` (include it in `toString()`); verify the model compiles and serializes with lowercase values and a null default.
- [x] 1.2 In `app/src/main/java/fr/vpm/changingtables/database/Converters.kt`, add a `@TypeConverter` pair storing `HighChairAvailability` as its `name`; bump `AppDatabase.kt` version from 6 to 7; extend `BusinessJsonStoreTest` (round-trip sets `highChairs = HighChairAvailability.MULTIPLE` and asserts the JSON contains `"highChairs":"multiple"`; missing-key test asserts null); verify build + tests.

## 2. Strings and form question

- [x] 2.1 Add strings to `app/src/main/res/values/strings.xml`: `high_chair_question`, `high_chair_multiple`, `high_chair_single`, `high_chair_none`; verify resources compile via the app build.
- [x] 2.2 In `BusinessFormBottomSheet.showNewBusiness()`, insert the high chair question (single choice, options keyed `multiple`/`single`/`none`) between the diaper pail question and the extra note question, so the order is: changing table, location, cleanliness, diaper pail, high chairs, note; verify six pages in that order and auto-advance into the note page.

## 3. Save mapping and verification

- [x] 3.1 Update the "Add business" mapping: `highChairs = answers[4]?.firstOrNull()?.let { runCatching { HighChairAvailability.valueOf(it.uppercase()) }.getOrNull() }` and `description = answers[5]?.firstOrNull()`; verify a fully answered form saves highChairs and note, and a skipped high chair question saves null.
- [x] 3.2 Run `./gradlew :app:assembleDebug :app:testDebugUnitTest`; confirm BUILD SUCCESSFUL and all unit tests pass.
