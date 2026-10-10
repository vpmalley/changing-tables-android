# Tasks

## 1. Filter model and store

- [ ] 1.1 Add `MapFilter` (data class with `types: Set<String>`, `savedOnly: Boolean`, `matches(business)`) and constants for the `coffee` / `restaurant` / `activity` type strings in `ui/map`; verify it compiles with `./gradlew :app:compileDebugKotlin`
- [ ] 1.2 Add unit tests in `app/src/test/.../ui/map/MapFilterTest.kt` covering: empty filter matches all; one type; several types (OR); saved only; saved + type (AND); null or unknown type hidden when a type is selected. Verify `./gradlew :app:testDebugUnitTest` passes
- [ ] 1.3 Add the `MapFilterStore` interface (`filter: LiveData<MapFilter>`, `update(filter)`) and `InMemoryMapFilterStore`, with a KDoc note that a persisted store can replace it; verify the build succeeds

## 2. MapViewModel filter state

- [ ] 2.1 Replace the `text` stub in `MapViewModel` with a `MapFilterStore` (default `InMemoryMapFilterStore()`), expose `filter`, `toggleType(type)` and `setSavedOnly(Boolean)`; verify the build succeeds and nothing else referenced `text` (`grep -rn "mapViewModel.text\|\.text" ui/map`)
- [ ] 2.2 Add `visibleBusinesses(source: LiveData<List<Business>>)` returning a `MediatorLiveData` that re-filters when either the source or the filter emits; verify with a unit test using `InstantTaskExecutorRule` (add `androidx.arch.core:core-testing` as a test dependency) that toggling the filter and changing the source both produce the expected lists

## 3. Two annotation managers in MapManager

- [ ] 3.1 Split `MapManager` into `businessAnnotationManager` (click listener, rebuilt by `showBusinesses`) and `newBusinessAnnotationManager` (created second, pin only); make `showNewBusinessMarker`/`clearNewBusinessMarker` use only the pin manager and stop `showBusinesses` from resetting `newBusinessAnnotation`; null both in `onDestroyView`. Verify on a device: place a pin with a long press, save/unsave another business, and the pin stays

## 4. Chip UI and removal of the FAB

- [ ] 4.1 In `fragment_map.xml`, remove `fabFilter`, `filterOptions` and the three mini FABs, and add `mapTopBar` (vertical container at the top, placed before the bottom-sheet includes) containing a `HorizontalScrollView` with a single-line `ChipGroup` and four `Widget.Material3.Chip.Filter` chips (Coffee `ic_coffee_24`, Restaurant `ic_restaurant_24`, Activity `ic_local_activity_24`, Saved `ic_bookmark_24dp`, text from the existing strings); verify the layout preview shows the chip row and no FAB
- [ ] 4.2 In `MapFragment`, delete `isFilterExpanded` and the FAB setup/toggle/expand/collapse functions; remove the `filter` string if it is no longer referenced; verify the build succeeds with no unused references (`grep -rn "fabFilter\|filterOptions\|R.string.filter" app/src`)
- [ ] 4.3 Bind the chips to `MapViewModel` (type chip → `toggleType`, Saved → `setSavedOnly`), and observe `filter` to set each chip's checked state only when it differs; verify on a device that chips toggle and their state is restored after switching to the Saved tab and back
- [ ] 4.4 Replace the observer of `businessViewModel.businesses` with one on `mapViewModel.visibleBusinesses(businessViewModel.businesses)` that feeds `mapManager.showBusinesses`; verify on a device each scenario in the spec's "Type chips combine with OR" and "Saved chip combines with AND" requirements

## 5. Compass offset

- [ ] 5.1 After `mapTopBar` is laid out (and when its size changes), set `binding.mapView.compass.marginTop` to the bar's bottom plus 8dp; verify on a device that the compass appears fully below the chips when the map is rotated

## 6. Integration check

- [ ] 6.1 Run `./gradlew :app:testDebugUnitTest :app:assembleDebug`, then check by hand on a device: no FAB; no selection shows all businesses; a pin placed and then a filter change keeps the pin and form; an open details sheet stays open when its business is filtered out; unsaving with Saved selected removes the marker; restarting the app clears the filter
