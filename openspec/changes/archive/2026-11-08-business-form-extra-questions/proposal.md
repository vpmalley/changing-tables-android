# Proposal

## Why

The new-business bottom sheet currently only asks whether a changing table exists and where it is located. Parents need more context before choosing a venue: how clean it is, whether a diaper pail is available, and any free-form remark. Collecting these details at creation time makes the saved businesses more useful for filtering and display later, before any server takes over persistence.

## What Changes

- The new-business bottom sheet question flow gains three additional questions after the existing changing-table questions:
  1. **Cleanliness** — single-choice with exactly three options: Spotless / It's ok / Needs cleaning.
  2. **Diaper pail** — single-choice yes/no.
  3. **Extra note** — a free-text input page where the user can type an optional remark (may be left empty).
- On "Add business", the answers are persisted on the new `Business`:
  - cleanliness stored in a new nullable `cleanliness: Cleanliness?` enum field (`SPOTLESS`, `OK`, `NEEDS_CLEANING`, serialized as `spotless` / `ok` / `needs_cleaning`) — the existing `isClean: Boolean` cannot represent three states and is **removed**;
  - the field stays null when the user skips the question, so unanswered is a first-class state;
  - diaper pail stored in the existing `hasDiaperPail: Boolean` field;
  - the extra note stored in the existing `description: String?` field.
- When a saved business is displayed, its cleanliness status is shown as an amenity chip: `SPOTLESS` as "Clean" (green), `OK` as **"Clean enough" with a yellow background**, `NEEDS_CLEANING` as "Dirty" (red); a null cleanliness shows no cleanliness chip.
- The questions ViewPager gains a second view type so the free-text note is a text input page while the other questions remain chip pages. Back/Skip navigation and the "Add business" enabling behavior are preserved across all five pages.
- Room schema version is bumped (4 to 5) to replace the `isClean` column with `cleanliness` (enum stored via a type converter); the database already uses `fallbackToDestructiveMigration()`, so no migration code is needed. The JSON import/export store is schema-tolerant (`ignoreUnknownKeys`, default values), so old JSON files that still contain `isClean` and lack `cleanliness` still load without crashing.

## Capabilities

### New Capabilities
- `business-form-questions`: the question flow of the new-business bottom sheet — which questions are asked, in what order, what input each accepts, and how the answers are persisted on the `Business`. Existing capabilities (`map-add-business`, `map-business-filters`) cover the FAB/marker placement and map filters, not the form's question content, so this is a new capability.

### Modified Capabilities
- None. `map-add-business` requirements (FAB, draggable marker, form appears on drop) are unchanged by this proposal.

## Impact

- `app/src/main/java/fr/vpm/changingtables/ui/map/BusinessFormBottomSheet.kt` — add the three questions and map their answers onto the new `Business` on save.
- `app/src/main/java/fr/vpm/changingtables/ui/map/QuestionsAdapter.kt` — support a second view type for the free-text question page.
- New question view + layout for the free-text page (e.g. `QuestionWithTextInputView` + `item_question_text_input.xml`); `Question` model gains an input-kind field.
- `app/src/main/java/fr/vpm/changingtables/models/Business.kt` — remove `isClean`, add `enum class Cleanliness` and a nullable `cleanliness: Cleanliness?` field.
- `app/src/main/java/fr/vpm/changingtables/database/AppDatabase.kt` — version bump 4 to 5 plus a `@TypeConverter` for the enum (destructive fallback already configured).
- `app/src/main/res/values/strings.xml` — new strings for the cleanliness question + options, diaper pail question, note hint, and the "Clean enough" display label.
- `app/src/main/res/values/colors.xml` — a yellow color for the "Clean enough" chip background.
- `app/src/main/java/fr/vpm/changingtables/models/AmenityDisplay.kt` — map a `Cleanliness` value to chip text + background color (OK → "Clean enough" / yellow).
- `app/src/main/java/fr/vpm/changingtables/ui/map/BusinessDetailsBottomSheet.kt` — use the cleanliness enum instead of `isClean` when building the amenities list.
- JSON persistence (`BusinessJsonStore`) needs no change: unknown keys (e.g. legacy `isClean`) are ignored and missing keys fall back to defaults, so old exports remain loadable.
