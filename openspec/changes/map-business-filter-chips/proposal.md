# Proposal

## Why

The map screen has a filter FAB that expands into three mini FABs (activity, restaurant, coffee), but they have no click listeners, so no filtering happens. There is also no way to show only saved businesses on the map. Filter chips at the top of the map are easier to find, show which filters are on, and leave room for a search bar planned above them.

## What Changes

- Add a row of four filter chips at the top of the map screen: **Coffee**, **Restaurant**, **Activity** and **Saved**, each with an icon.
- Filter the business markers on the map by the selected chips. Type chips combine with OR, the Saved chip combines with AND, and no selection shows every business.
- Put the chip row inside a top container that will also hold a future search bar (the search bar itself is not part of this change).
- Move the Mapbox compass below the top container so the chips don't cover it.
- Keep the filter for the app session only (it survives tab switches, not app restarts), behind a seam so it can be persisted later without touching the UI.
- Keep the new-business pin and form unaffected by filtering, and stop re-rendering business markers from removing a pin the user is placing.
- **BREAKING (UI)**: Remove the filter FAB, the three mini FABs and their expand/collapse animation.

## Capabilities

### New Capabilities
- `map-business-filters`: filtering which businesses appear on the map by business type and saved status, including the filter controls, how selected filters combine, how long filter state lasts, and how filtering interacts with the new-business pin and the details sheet.

### Modified Capabilities
<!-- None: there are no existing specs in openspec/specs/. -->

## Impact

- **Layout**: `app/src/main/res/layout/fragment_map.xml`: remove `fabFilter`, `filterOptions` and the three mini FABs; add the top container and chip group.
- **UI code**: `ui/map/MapFragment.kt`: remove the FAB toggle and animation code; bind the chips, observe the filtered businesses, and set the compass margin.
- **View model**: `ui/map/MapViewModel.kt`: replace the unused `text` stub with filter state and the filtered business list.
- **Map rendering**: `ui/map/MapManager.kt`: split into two point annotation managers, one for business markers and one for the new-business pin.
- **New code**: a filter value type with the matching logic, and a filter store interface with an in-memory implementation.
- **Resources**: reuse the existing type icons and `ic_bookmark_24dp`, and the existing `coffee` / `restaurant` / `activity` / `title_saved` strings. The `filter` string may become unused.
- **Tests**: unit tests for the filter matching logic (`app/src/test`).
- **Dependencies**: no new runtime dependencies; one test-only dependency (`androidx.arch.core:core-testing`) for LiveData unit tests.
