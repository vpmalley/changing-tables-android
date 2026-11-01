# Tasks

## 1. Data model and persistence

- [x] 1.1 In `app/src/main/java/fr/vpm/changingtables/models/Business.kt`, remove the `isClean` field (including its `toString()` reference) and add `@Serializable enum class Cleanliness { @SerialName("spotless") SPOTLESS, @SerialName("ok") OK, @SerialName("needs_cleaning") NEEDS_CLEANING }` plus a nullable `var cleanliness: Cleanliness? = null`; verify the model compiles and serializes to JSON with lowercase values and a null default.
- [x] 1.2 In `app/src/main/java/fr/vpm/changingtables/database/AppDatabase.kt`, bump the version from 4 to 5 and register a `@TypeConverter` pair that stores `Cleanliness` as its `name` string (destructive fallback already configured); verify the app builds and `BusinessJsonStore` loads an old JSON export that still contains `isClean` and lacks `cleanliness` (add/extend a unit test with such a payload, expecting null cleanliness).

## 2. Strings and question model

- [x] 2.1 Add strings to `app/src/main/res/values/strings.xml` for the cleanliness question and its three options (Spotless, It's ok, Needs cleaning), the diaper pail question, the extra-note hint, and the `amenity_clean_enough` ("Clean enough") display label, reusing `all_yes`/`all_no` for the diaper pail options; add a yellow color (e.g. `clean_enough_yellow`) to `app/src/main/res/values/colors.xml`; verify resources compile via the app build.
- [x] 2.2 Add an input-kind discriminator to `Question` (e.g. `inputType`, default chips) so a free-text question can be distinguished from chip questions; verify existing chip questions are unaffected.

## 3. Free-text question view and adapter

- [x] 3.1 Create a free-text question layout (e.g. `item_question_text_input.xml`) with a title, a `TextInputLayout` note field, and a Back button, mirroring the structure of `item_question.xml`; verify the layout inflates without errors.
- [x] 3.2 Extend `QuestionsAdapter` with `getItemViewType()` and a second ViewHolder for the free-text page, reporting the typed text through the answers mechanism; verify both view types render in the ViewPager.

## 4. Bottom sheet question flow

- [x] 4.1 In `BusinessFormBottomSheet.showNewBusiness()`, append the cleanliness (3-option single choice), diaper pail (yes/no single choice), and extra note (free text) questions after the two changing-table questions; verify five pages appear in order with Back/Skip behavior preserved (Skip hidden on the last page).
- [x] 4.2 Map the answers onto the new `Business` on "Add business": `cleanliness = answers[2]?.firstOrNull()?.let { runCatching { Cleanliness.valueOf(it.uppercase()) }.getOrNull() }`, `hasDiaperPail = answers[3]?.firstOrNull() == "yes"`, `description` = the note text; verify a fully answered form saves all values and a partially skipped form saves null cleanliness / false diaper pail / null description without crashing.
- [x] 4.3 Make the form auto-advance to the next page whenever an option is selected on a single-choice question, except the changing-table location question (multi-choice, `singleChoice = false`), which stays until Skip/Back; verify by walking the flow: each single-choice answer jumps to the next page, and the location question does not.

## 5. Details sheet and verification

- [x] 5.1 Add `AmenityDisplay.fromCleanliness(cleanliness: Cleanliness?)` in `app/src/main/java/fr/vpm/changingtables/models/AmenityDisplay.kt` — `SPOTLESS` → (amenity_clean, green), `OK` → (amenity_clean_enough, clean_enough_yellow), `NEEDS_CLEANING` → (amenity_dirty, red), null → omitted — and use it in `BusinessDetailsBottomSheet.showBusiness()` in place of the `isClean` branch (also removing the dead commented-out `isClean` lines); verify a business with `OK` renders a yellow "Clean enough" chip and one with null cleanliness shows no cleanliness chip.
- [x] 5.2 Build and run the app (`./gradlew assembleDebug` and unit tests); manually walk the creation flow: answer all five questions and confirm the saved business shows the yellow "Clean enough" chip when "It's ok" was chosen, plus diaper pail and note; then repeat skipping the new questions and confirm submission still works and no cleanliness chip appears.
