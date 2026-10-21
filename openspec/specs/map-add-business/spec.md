# Map Add Business Specification

## Purpose

Adds an alternative way to add a new business to the map by tapping a floating action button, then dragging and dropping a marker to position it precisely before filling in the business details. This complements the existing long-press method.

## Requirements

### Requirement: Add business FAB on map
The map screen SHALL show a floating action button with a `+` symbol. Tapping the FAB SHALL initiate the new business creation flow.

#### Scenario: FAB visible on map screen
- **WHEN** the user opens the map screen
- **THEN** a FAB with a `+` symbol is visible

#### Scenario: FAB tap starts new business flow
- **WHEN** the user taps the FAB
- **THEN** a draggable marker appears at the center of the currently displayed map area

### Requirement: Draggable new business marker
When the FAB is tapped, a new business marker SHALL appear at the center of the visible map area. The user SHALL be able to drag this marker to any location on the map. The marker SHALL move with the user's drag gesture.

#### Scenario: Marker appears at map center
- **WHEN** the user taps the FAB
- **THEN** a draggable marker appears at the center of the currently visible map area

#### Scenario: Marker drags with finger
- **WHEN** the user taps and drags the marker
- **THEN** the marker moves to follow the drag gesture

#### Scenario: Marker stays on map during drag
- **WHEN** the user drags the marker
- **THEN** the marker stays on the map and does not disappear

### Requirement: Drop marker to show business form
When the user releases the marker (drops it), the business form bottom sheet SHALL appear with the business location set to the marker's final position. The marker SHALL remain visible while the form is open.

#### Scenario: Form appears at dropped location
- **WHEN** the user drags and drops the marker at a location
- **THEN** the business form bottom sheet appears with the latitude and longitude fields pre-filled with the marker's position

#### Scenario: Marker persists while form is open
- **WHEN** the business form is open after dropping the marker
- **THEN** the marker remains visible on the map at the dropped position

### Requirement: Cancel marker placement
The user SHALL be able to cancel the marker placement before dropping it. Canceling SHALL hide the marker and NOT open the business form.

#### Scenario: Drag off screen cancels
- **WHEN** the user drags the marker off the visible map area and releases
- **THEN** the marker is hidden and the business form does NOT appear

#### Scenario: Tap outside cancels
- **WHEN** the user taps outside the marker while it is draggable
- **THEN** the marker is hidden and the business form does NOT appear

### Requirement: Only one new business marker at a time
The map SHALL show at most one draggable new business marker at any time. Creating a new marker SHALL replace any existing draggable marker.

#### Scenario: New FAB tap replaces existing marker
- **WHEN** a draggable marker is already visible and the user taps the FAB
- **THEN** the existing marker is removed and a new marker appears at the current map center

#### Scenario: Form open with marker, new FAB tap
- **WHEN** the business form is open with a marker visible and the user taps the FAB
- **THEN** the existing marker is replaced with a new draggable marker at the current map center

### Requirement: Marker unaffected by map movement
Moving or zooming the map SHALL NOT affect a draggable new business marker that is currently being positioned. The marker SHALL stay at its screen position until dropped.

#### Scenario: Map pans while dragging marker
- **WHEN** the user drags the marker and pans the map simultaneously
- **THEN** the marker follows the drag gesture and stays under the user's finger

### Requirement: FAB only available on map screen
The add business FAB SHALL only be available when the map screen is visible. It SHALL NOT appear on other screens.

#### Scenario: FAB not on saved screen
- **WHEN** the user is on the Saved tab
- **THEN** the add business FAB is NOT visible

#### Scenario: FAB not on notifications screen
- **WHEN** the user is on the Notifications tab
- **THEN** the add business FAB is NOT visible

### Requirement: Long-press method still available
The existing long-press-to-add-business interaction SHALL remain available as an alternative method. Both methods SHALL work independently.

#### Scenario: Long-press still works
- **WHEN** the user long-presses on the map at zoom level > 13.0
- **THEN** a new business marker appears at the long-pressed location and the business form opens

#### Scenario: Both methods independent
- **WHEN** a user creates a business using the FAB drag-and-drop method
- **THEN** the long-press method continues to work as before
