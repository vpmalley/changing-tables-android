# Design

## Context

- `MapFragment` observes `BusinessViewModel.businesses` (Room `LiveData`) and passes the full list to `MapManager.showBusinesses`. The filter FAB only toggles whether `filterOptions` is visible; the mini FABs do nothing.
- `MapManager` uses one `PointAnnotationManager` for both business markers and the new-business pin. `showBusinesses` calls `deleteAll()`, which also removes the pin while the user is placing it. This already happens whenever Room emits again (for example after toggling save).
- `MapViewModel` is activity-scoped (`activityViewModels`) and only holds an unused `text` stub.
- `Business.type` is a raw string (`coffee` | `restaurant` | `activity`). "Saved" means `savedOnDevice != null`.
- The app bar is hidden on the map destination. With `targetSdk` 36, Android 15+ enforces edge-to-edge, so the map draws behind the status bar: `mapTopBar` adds the status bar inset to its top padding (and the compass offset follows, as it uses the bar's bottom).
- Type values were inconsistent: the add form writes `coffee`, while the prepopulated data used `cafe`. The data is normalised to `coffee` (see Decision 7), so `MapFilter` matches type strings exactly.
- The project uses LiveData (no Flow/coroutines-in-UI pattern), Material 3 and View Binding.

## Goals / Non-Goals

**Goals:**
- Keep the filter rule a pure function that can be unit-tested without Android.
- Make it possible to switch from session-only to persisted filter state by swapping one class.
- Leave a ready slot for a future search bar above the chips, with the compass offset following automatically.

**Non-Goals:**
- The search bar itself.
- Turning `Business.type` into an enum or changing the Room schema.
- Different marker icons per filter, filter counts, or a "clear all" control.
- Filtering the Saved tab list.

## Decisions

### 1. `MapFilter`: an immutable value with the filter rule
```kotlin
data class MapFilter(
    val types: Set<String> = emptySet(),   // business type strings, empty = any type
    val savedOnly: Boolean = false,
) {
    fun matches(business: Business): Boolean =
        (types.isEmpty() || business.type in types) &&
        (!savedOnly || business.savedOnDevice != null)
}
```
The OR/AND rule lives in this one place, and it can be unit-tested with plain JUnit. Type values reuse the existing strings from `Business.type` so the data layer stays untouched; constants for the three types sit next to `MapFilter`.
*Alternative:* filter in a Room query (`WHERE type IN (...)`). Rejected: a dynamic `IN` list with an optional saved clause makes the DAO more complicated, and the list is small enough to filter in memory.

### 2. `MapFilterStore`: the only place that decides how long the filter lasts
```kotlin
interface MapFilterStore {
    val filter: LiveData<MapFilter>
    fun update(filter: MapFilter)
}
class InMemoryMapFilterStore : MapFilterStore  // MutableLiveData(MapFilter())
```
`MapViewModel` gets a store and never holds the filter itself. The interface exposes `LiveData`, so a future `DataStoreMapFilterStore` (asynchronous reads) fits without changing callers. The store is created in `MapViewModel`'s default initialiser (`InMemoryMapFilterStore()`); persisting the filter later means changing that line, or adding a `ViewModelProvider.Factory` if the store needs a `Context`.
Because `MapViewModel` is activity-scoped, the in-memory store survives tab switches and is lost when the process ends, which is exactly session-only.
*Alternative:* `SavedStateHandle`. Rejected: it would also restore the filter after the system kills the process, which is more than "session-only", and it is harder to swap for a persisted store.

### 3. Combining in `MapViewModel` with `MediatorLiveData`
`MapViewModel` exposes `filter: LiveData<MapFilter>`, `setTypeSelected(type, selected)` (idempotent, so restoring chip state from the filter cannot flip it back), `setSavedOnly(Boolean)` and `visibleBusinesses(source: LiveData<List<Business>>)`. That last one returns a `MediatorLiveData` that recomputes `source.filter(filter::matches)` whenever either input emits. The business source stays in `BusinessViewModel`; `MapFragment` connects them. This avoids `MapViewModel` needing the `Application` to build the repository.
*Alternative:* do the filtering in `BusinessViewModel`. Rejected: it is shared with the Saved screen, and map filter state does not belong there.

### 4. Two point annotation managers
`MapManager` creates `businessAnnotationManager` (holds the click listener and is cleared and rebuilt by `showBusinesses`) and then `newBusinessAnnotationManager` (only the pin, no click listener). Creating the pin manager second puts its layer above the business markers. `showNewBusinessMarker` and `clearNewBusinessMarker` use only the pin manager, and `showBusinesses` stops resetting `newBusinessAnnotation`.
*Alternative:* track business annotations and delete only those. Rejected: more bookkeeping for the same result, and easy to get wrong.

### 5. Layout: top container, chip group, compass offset
In `fragment_map.xml`, add a vertical `LinearLayout` `mapTopBar` at the top of the `CoordinatorLayout` (gravity top, `@dimen/margin_thin` padding, plus the status bar inset on top). It contains a `HorizontalScrollView` (no scrollbar) holding a single-line `ChipGroup` (`selectionRequired=false`, not single-selection) with four `Chip`s in `Widget.Material3.Chip.Filter.Elevated` style, so they stay readable over the map. Each chip has `chipIcon` (`ic_coffee_24`, `ic_restaurant_24`, `ic_local_activity_24`, `ic_bookmark_24dp`, tinted `?attr/colorOnSurfaceVariant` because the drawables have a white fill) and `checkedIconVisible="true"`. Material draws the checkmark in the same slot as the chip icon, so `MapFragment` hides the chip icon while a chip is checked: a selected chip shows the checkmark in place of its icon, on a filled background. The future search bar goes above the scroll view inside `mapTopBar`.
`mapTopBar` is placed after the map view and before the bottom-sheet includes, so the bottom sheets draw over it when expanded.
Compass: an `addOnLayoutChangeListener` on `mapTopBar`, attached in `onViewCreated` before the first layout (so it covers the first pass and later size changes), sets `binding.mapView.compass.marginTop` to the bar's bottom plus `@dimen/margin_thin`. The offset follows the container's real height, so adding the search bar needs no compass change.
The chip-to-type mapping is in `MapFragment` (chip id → type constant). Chip checked state is driven from `MapViewModel.filter`, so chips are restored correctly when the view is recreated.

### 6. Removal
Delete `fabFilter`, `filterOptions` and the three mini FABs from the layout, plus `isFilterExpanded`, `setupFilterFab`, `toggleFilter`, `expandFilter` and `collapseFilter` from `MapFragment`. Remove the `filter` string resource if it is no longer referenced. Leave the drawable `ic_filter_list_24` in place (harmless; it can be cleaned up separately).

### 7. Normalise `cafe` to `coffee` in bundled data
`assets/databases/prepopulated.sql`, `prepopulated.db` and the commented-out seed in `AppDatabase` used `cafe`. They are updated to `coffee` (only the `type` column is changed in the `.db`, so its other contents are kept). Neither file is loaded at runtime (no `createFromAsset`, seed callback commented out), so no Room migration is needed for existing installs.
*Alternative:* make the Coffee chip match both values. Rejected in favour of a single canonical value. The `"cafe"` aliases in `SavedBusinessesAdapter` and `BusinessAmenitiesView` stay as harmless fallbacks.

## Risks / Trade-offs

- [Chip listeners loop when checked state is set from LiveData] → Only call `setChecked` when the value differs, or set the state before attaching listeners, and keep the ViewModel as the source of truth.
- [The marker click listener deserialises a `Business` from annotation JSON, so a stale copy may be shown] → Not changed by this work; noted only.
- [`deleteAll()` + `create()` on every filter change could flicker with many businesses] → Acceptable for the current data size; if it isn't later, diff the annotations.
- [The Mapbox compass margin API depends on the version] → Use the `compass` plugin's `marginTop` setter from the Mapbox v11 `CompassSettings`; check it during the compass task.
- [The bottom sheets could slide over the chip row] → This is intended (the sheets are modal-like); the chips can be seen again when the sheet is dismissed.
