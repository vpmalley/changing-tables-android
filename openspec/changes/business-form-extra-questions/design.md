# Design

## Context

The new-business bottom sheet (`BusinessFormBottomSheet.kt`) builds its questions inline in `showNewBusiness()` and renders them in a swipe-disabled `ViewPager2` driven by `QuestionsAdapter`. Today there are two chip-based questions (changing table yes/no/out_of_service, changing table location). Answers are collected in `answers: MutableMap<Int, List<String>>` keyed by question position and mapped onto a new `Business` when "Add business" is tapped. The `Question` model supports chip options only (`titleResId`, `options`, `singleChoice`, `chipStyleRes`), and `QuestionsAdapter` has a single view type backed by `QuestionWithChipsView` / `item_question.xml`. See proposal.md for motivation and `specs/business-form-questions/spec.md` for the required behavior.

## Goals / Non-Goals

**Goals:**
- Add three questions (cleanliness 3-choice, diaper pail yes/no, free-text note) after the existing changing-table questions, as ordered ViewPager pages.
- Persist the answers on the `Business` at submit time, including a new `cleanliness` field that can hold three states.
- Keep the change backward compatible with existing JSON exports and existing on-device databases.

**Non-Goals:**
- No server-side persistence; JSON file export/import stays the persistence mechanism.
- No changes to the FAB / draggable-marker flow (`map-add-business` behavior is untouched).
- No editing of existing businesses; the new questions only apply to the creation flow.
- No redesign of the details bottom sheet beyond reflecting the three-state cleanliness.

## Decisions

**1. Cleanliness as a nullable enum, replacing `isClean` entirely.**
The requirement is three states (Spotless / It's ok / Needs cleaning); a Boolean cannot represent "It's ok" distinctly from the two extremes. Decision: define `enum class Cleanliness { SPOTLESS, OK, NEEDS_CLEANING }` (in `models`, `@Serializable`, each entry annotated `@SerialName("spotless" | "ok" | "needs_cleaning")` so the JSON values stay lowercase) and replace `isClean: Boolean` on `Business` with `var cleanliness: Cleanliness? = null`. The field is nullable because users may skip the question — unanswered is a first-class state, not a default. Room persists the enum through a `@TypeConverter` that stores `name` (robust to enum reordering); kotlinx.serialization writes the `@SerialName` value. Alternatives considered: keeping `isClean` alongside a `String?` (rejected — two sources of truth for the same fact, and the user explicitly asked to remove it), or a plain `String?` (rejected in favor of an enum — type-safe, exhaustive when mapping to the display chip). Backward compatibility: old JSON exports contain `isClean` and no `cleanliness`; `ignoreUnknownKeys = true` drops the legacy key and the missing key defaults to null. The Room schema change is absorbed by the existing `fallbackToDestructiveMigration()` on the version bump.

**2. Diaper pail becomes tri-state; note reuses `description`.**
`hasDiaperPail` becomes `Boolean? = null`: `true` for Yes, `false` for No, null when the question was skipped — so an unanswered question shows no chip instead of a misleading "No Diaper pail". The JSON store tolerates this automatically (old exports carry an explicit `false`, which still loads as `false`; a missing key defaults to null), and Room gets the nullable column via another destructive-fallback version bump (5 to 6). The extra note reuses the existing `description: String?` field.

**3. New view type in `QuestionsAdapter` for the free-text page.**
`QuestionWithChipsView` only renders chips, so the note question needs a text input page. Decision: add an input-kind discriminator to `Question` (e.g. `inputType: CHIPS | FREE_TEXT`, default `CHIPS`), implement `getItemViewType()` in `QuestionsAdapter`, and add a second ViewHolder backed by a new layout (e.g. `item_question_text_input.xml` with a title `TextView` and a `TextInputLayout`/`TextInputEditText`, plus the same Back button). The text answer is reported through a new adapter callback (or by storing it in the same `answers` map as a single-element list) so submit-time mapping stays uniform. Alternative considered: forcing the note into a chip question — rejected because free text cannot be expressed as fixed chips.

**4. Room version bumps with the existing destructive fallback, plus an enum type converter.**
`AppDatabase` is already built with `fallbackToDestructiveMigration()`, so replacing the `isClean` column with the `cleanliness` enum column requires bumping `version` to 5 and registering a `@TypeConverter` that stores the enum's `name`, and making `hasDiaperPail` nullable requires bumping to 6; old installs wipe and rebuild the local DB rather than crashing. The JSON export/import store (from `business-json-import-export`) uses `ignoreUnknownKeys = true` and default field values, so old JSON files that still carry `isClean` and lack `cleanliness` still parse (legacy key ignored, cleanliness null), and new exports simply add the key. Alternative considered: writing a proper `Migration(4,5)` — rejected as unnecessary for a local-only dev database that is already re-seeded from the JSON file on every start.

**5. Question order and navigation.**
Questions become five ordered pages: changing table (0), changing table location (1), cleanliness (2), diaper pail (3), extra note (4). Back/Skip move between pages (Skip hidden on the last page, Back hidden on the first). Selecting an option on a single-choice question auto-advances to the next page; the changing-table location question is multi-choice (`singleChoice = false`) and is the exception — it never auto-advances, so the user can pick several locations and leaves via Skip/Back. The "Add business" button is enabled as soon as any chip is selected, so optional questions can be skipped without blocking submission. Implementation: in the adapter's `onChipSelected`, advance when `questions[position].singleChoice && position < questions.size - 1`, replacing the old page-0-only "yes" special case.

**6. New strings and colors for the questions and the cleanliness chip.**
Add strings for the cleanliness question and its three options, the diaper pail question, and the note hint. Reuse existing `all_yes`/`all_no` for the diaper pail options. For the details display, add a `amenity_clean_enough` string ("Clean enough") and a yellow color resource (e.g. `clean_enough_yellow`) in `colors.xml`; `SPOTLESS` and `NEEDS_CLEANING` reuse the existing `amenity_clean` / `amenity_dirty` strings with the existing `green` / `holo_red_dark` chip colors.

**7. Display mapping for the three-state cleanliness.**
`BusinessAmenitiesView.setAmenities` renders each `AmenityDisplay(textResId, colorResId)` as a chip whose background is `colorResId`, so the yellow background needs no new view — only a new color. Decision: add `AmenityDisplay.fromCleanliness(cleanliness: Cleanliness?)` returning `SPOTLESS → (amenity_clean, green)`, `OK → (amenity_clean_enough, clean_enough_yellow)`, `NEEDS_CLEANING → (amenity_dirty, palette_red)`, and null → no chip (the list simply omits it). `BusinessDetailsBottomSheet` uses this instead of the current `isClean`-based branch, and the dead commented-out `displayAmenity(... isClean ...)` lines are removed. Negative ("missing") chips use the palette red `palette_red` (#C44747) instead of the darker `holo_red_dark`, and the changing-table chip picks its label by state: `hasChangingTable == "no"` → new `amenity_changing_table_missing` ("No changing table") in red, otherwise `amenity_changing_table` ("Changing table") in green. The diaper pail chip is tri-state via `AmenityDisplay.fromDiaperPail(Boolean?)`: `true` → (amenity_diaper_pail, green), `false` → (amenity_diaper_pail_missing, grey), null → omitted from the list.

## Risks / Trade-offs

- [Risk] Destructive migration wipes the local Room DB on upgrade (dropping the old `isClean` column) → Mitigation: accepted for this dev-stage app; data is re-imported from the JSON file on activity start, which already exists from `business-json-import-export`. Old JSON rows that still carry `isClean` lose that flag, but cleanliness is null for them anyway and renders as "no chip".
- [Risk] Free-text page breaks the uniform chip-based adapter contract → Mitigation: keep the `answers` map contract by storing the note as a single string; only the view layer differs per view type.
- [Risk] Removing `isClean` breaks old serialized data → Mitigation: JSON loader uses `ignoreUnknownKeys = true` so the legacy key is skipped; Room uses the destructive fallback; no crash either way.
- [Risk] Skipped questions must not block or crash submission → Mitigation: submit-time mapping uses safe calls (`answers[i]?.firstOrNull()`), yielding null/false defaults, which the schema-tolerant JSON store accepts.
- [Risk] Enum stored by ordinal would corrupt on reordering → Mitigation: the Room `@TypeConverter` stores `name` (String), not ordinal.

## Migration Plan

No deployment steps: local-only change. On first run after the schema bump, Room destructively recreates the DB and the app reloads businesses from the JSON export file in `onStart`. Rollback is simply reverting the code; old JSON exports remain loadable because unknown/missing keys are tolerated.

## Open Questions

- None blocking. Minor: for a business with null cleanliness the details sheet shows no cleanliness chip (chosen over a grey "unknown" chip); easy to change later without touching the form flow.
