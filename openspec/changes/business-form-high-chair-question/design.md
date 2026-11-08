# Design

## Context

The new-business bottom sheet (`BusinessFormBottomSheet.showNewBusiness()`) currently has five question pages: changing table (0), changing table location (1, multi-choice), cleanliness (2), diaper pail (3), extra note (4, free text). Single-choice answers auto-advance to the next page; answers are stored in `answers: MutableMap<Int, List<String>>` keyed by option tags, and mapped onto the `Business` on "Add business". `cleanliness` already established the nullable-enum pattern (`Cleanliness` + `@SerialName` + Room `@TypeConverter` storing `name` + destructive-fallback version bump + schema-tolerant JSON). See `specs/business-form-questions/spec.md` for the required behavior.

## Goals / Non-Goals

**Goals:**
- Add a high chair question (single choice: Multiple high chairs / One high chair / No high chair) between the diaper pail question and the extra note page.
- Persist the answer on `Business` as a nullable `HighChairAvailability` enum (`multiple` / `single` / `none`, null when skipped), following the `Cleanliness` pattern end to end.
- Keep old JSON exports and old databases loadable without crashing.

**Non-Goals:**
- No details-sheet chip for high chairs in this change (display can be added separately).
- No filtering by high chairs.
- No changes to the other questions' behavior.

## Decisions

**1. New nullable enum `HighChairAvailability`, same pattern as `Cleanliness`.**
`enum class HighChairAvailability { @SerialName("multiple") MULTIPLE, @SerialName("single") SINGLE, @SerialName("none") NONE }` in `models/Business.kt`, with `var highChairs: HighChairAvailability? = null` on `Business`. Nullable because the question can be skipped. The form's option keys are `multiple` / `single` / `none`, so the existing save mapping `Enum.valueOf(key.uppercase())` works unchanged. Alternatives considered: reusing a Boolean + count (can't express "multiple" vs "single"), or a String field (rejected — the enum is type-safe and exhaustive for display mapping, as established with `Cleanliness`).

**2. Question placement: position 4, note moves to position 5.**
Insert the high chair question after diaper pail (index 3) and before the free-text note. The note page stays last (no skip button). The save mapping shifts: `highChairs = answers[4]`, `description = answers[5]`. The question is single-choice, so the existing auto-advance moves the user to the note page on answer.

**3. Persistence: Room version 6 to 7 plus a type converter; JSON unchanged.**
`AppDatabase` version bumps to 7 (destructive fallback already configured) and `Converters` gets a `@TypeConverter` pair storing `HighChairAvailability` as its `name`. JSON: the field defaults to null when missing and old exports are unaffected (`ignoreUnknownKeys`); new exports add `"highChairs":"multiple"` etc.

**4. Strings.**
New strings: `high_chair_question` ("Are there high chairs?"), `high_chair_multiple` ("Multiple high chairs"), `high_chair_single` ("One high chair"), `high_chair_none` ("No high chair").

## Risks / Trade-offs

- [Risk] Index shift in the answers map (note 4 → 5) could silently mis-map answers → Mitigation: update both the question list order and the save mapping in the same edit; the round-trip unit test covers persisted values.
- [Risk] Destructive migration wipes local Room DB on upgrade → Mitigation: accepted pattern in this dev-stage app; JSON re-import on start restores data, and old JSON (without `highChairs`) loads with null.

## Migration Plan

Local-only: Room recreates the DB on version 7 via the existing destructive fallback; `onStart` reloads from the JSON export. Rollback is reverting the code; old exports remain loadable.

## Open Questions

- None blocking. Whether to show a high chair chip on the details sheet is deliberately deferred (out of scope here).
