# Proposal

## Why

Currently, users can long-press on the map to create a new business, but this is not discoverable and may be accidentally triggered. A floating action button (FAB) with a clear `+` symbol provides a more intuitive and discoverable alternative. Additionally, the current long-press implementation places a marker immediately at the press location, giving users no way to adjust the position before committing. A drag-and-drop interaction allows users to precisely position the business marker before filling in the details.

## What Changes

- Add a floating action button (FAB) with a `+` symbol to the map screen
- When the FAB is tapped, show a draggable marker at the center of the currently displayed map area
- Allow the user to drag the marker to the desired location
- When the user drops the marker (releases it), show the new business bottom sheet form pre-filled with the marker's coordinates
- Hide the marker if the user cancels (e.g., taps outside, drags off-screen, or the bottom sheet is dismissed)
- Keep the existing long-press-to-add-business interaction as an alternative method

## Capabilities

### New Capabilities
- `map-add-business`: covers how users initiate adding a new business to the map, including the FAB interaction, drag-and-drop marker placement, and transition to the business form

### Modified Capabilities
- `map-business-filters`: The new-business pin behavior interacts with filtering. The spec already states that the new-business pin SHALL be unaffected by filters, which continues to apply to both long-press and FAB-created markers.

## Impact

- **Layout**: `app/src/main/res/layout/fragment_map.xml`: add FAB element
- **UI code**: `ui/map/MapFragment.kt`: add FAB click listener and drag-and-drop marker logic
- **Map rendering**: `ui/map/MapManager.kt`: add drag-and-drop support for the new business marker; keep long-press listener for backward compatibility
- **Resources**: add or reuse a `+` icon drawable for the FAB
- **Behavior**: users can now use either FAB with drag-and-drop OR long-press to start adding a business; both methods work independently
