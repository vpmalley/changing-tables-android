# Proposal

## Why

Parents of babies/toddlers also need to know whether a venue has high chairs before visiting. The new-business form already collects changing table, cleanliness, diaper pail, and a free note; high chair availability is the same kind of amenity and belongs in the same flow, stored on the business so it can be displayed and filtered later.

## What Changes

- The new-business bottom sheet gains one more single-choice question, **after the diaper pail question and before the extra note page**: "Are there high chairs?" with exactly three options — "Multiple high chairs" (`multiple`), "One high chair" (`single`), "No high chair" (`none`).
- The answer is stored on `Business` in a new nullable enum field `highChairs: HighChairAvailability?` (`MULTIPLE` / `SINGLE` / `NONE`, serialized as `multiple` / `single` / `none`), null when the question is skipped — same tri-state pattern as `cleanliness`.
- Room version is bumped 6 to 7 for the new column (destructive fallback already configured); a `@TypeConverter` stores the enum as its `name`. The JSON store needs no change: missing key defaults to null, unknown keys ignored, so old exports remain loadable.
- The extra note page moves from position 4 to position 5 (last); single-choice questions auto-advance as before, so answering the high chair question advances to the note page.

## Capabilities

### New Capabilities
- None.

### Modified Capabilities
- `business-form-questions`: the new-business form question flow gains the high chair question (ADDED), and the question count / persistence requirements change from five to six questions including `highChairs` (MODIFIED).

## Impact

- `app/src/main/java/fr/vpm/changingtables/models/Business.kt` — new `enum class HighChairAvailability` + nullable `highChairs` field.
- `app/src/main/java/fr/vpm/changingtables/database/Converters.kt` — `@TypeConverter` pair for the enum; `AppDatabase.kt` — version 6 to 7.
- `app/src/main/java/fr/vpm/changingtables/ui/map/BusinessFormBottomSheet.kt` — new question between diaper pail and note; save mapping shifts note to index 5.
- `app/src/main/res/values/strings.xml` — question + three option strings.
- `app/src/test/.../BusinessJsonStoreTest.kt` — round-trip and default-value assertions for the new field.
- Details sheet display of high chairs is intentionally out of scope for this change.
