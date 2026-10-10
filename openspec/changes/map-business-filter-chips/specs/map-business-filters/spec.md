# Spec Delta

## Purpose

Lets users narrow the businesses shown on the map by business type and by whether they saved the business, using filter chips at the top of the map screen.

## ADDED Requirements

### Requirement: Filter chips on the map
The map screen SHALL show a horizontal row of four selectable filter chips near the top of the screen: Coffee, Restaurant, Activity and Saved. Each chip SHALL show an icon for its category and indicate whether it is selected. The row SHALL scroll horizontally when it does not fit the screen width.

#### Scenario: Chips shown on opening the map
- **WHEN** the user opens the map screen for the first time in a session
- **THEN** the Coffee, Restaurant, Activity and Saved chips are visible near the top of the screen, each with an icon, and none is selected

#### Scenario: Selecting a chip
- **WHEN** the user taps an unselected chip
- **THEN** the chip shows as selected

#### Scenario: Unselecting a chip
- **WHEN** the user taps a selected chip
- **THEN** the chip shows as unselected

#### Scenario: Narrow screen
- **WHEN** the four chips are wider than the screen
- **THEN** the user can scroll the chip row horizontally to reach every chip

### Requirement: No filter shows every business
When no chip is selected, the map SHALL show a marker for every known business.

#### Scenario: Nothing selected
- **WHEN** no chip is selected
- **THEN** every business is shown on the map, whatever its type or saved status

### Requirement: Type chips combine with OR
When one or more type chips (Coffee, Restaurant, Activity) are selected, the map SHALL show only businesses whose type matches at least one selected type chip. A business with no type, or a type without a chip, SHALL be hidden while any type chip is selected.

#### Scenario: One type selected
- **WHEN** only the Coffee chip is selected
- **THEN** only coffee businesses are shown

#### Scenario: Several types selected
- **WHEN** the Coffee and Restaurant chips are selected
- **THEN** coffee and restaurant businesses are shown, and activity businesses are hidden

#### Scenario: Business without a matching type
- **WHEN** any type chip is selected
- **THEN** businesses with no type or an unrecognised type are hidden

### Requirement: Saved chip combines with AND
When the Saved chip is selected, the map SHALL show only businesses saved on the device that also satisfy the type chip selection. With no type chip selected, it SHALL show every saved business.

#### Scenario: Saved only
- **WHEN** only the Saved chip is selected
- **THEN** every saved business is shown, whatever its type, and unsaved businesses are hidden

#### Scenario: Saved and a type
- **WHEN** the Saved and Coffee chips are selected
- **THEN** only saved coffee businesses are shown

#### Scenario: Saved and several types
- **WHEN** the Saved, Coffee and Activity chips are selected
- **THEN** only saved businesses that are coffee or activity businesses are shown

### Requirement: Map updates when filters or data change
The map's business markers SHALL update immediately when the chip selection changes, and when business data changes (for example a business is saved, unsaved or added), using the current selection.

#### Scenario: Selection changes
- **WHEN** the user selects or unselects a chip
- **THEN** the business markers update to match the new selection without leaving the map screen

#### Scenario: Unsaving with Saved selected
- **WHEN** the Saved chip is selected and the user unsaves a business from its details sheet
- **THEN** that business's marker is removed from the map

### Requirement: Filter lasts for the app session
The chip selection SHALL persist when the user switches to another screen and comes back during the same app session. It SHALL reset to no selection when the app process restarts.

#### Scenario: Switching tabs
- **WHEN** the user selects the Saved chip, opens the Saved tab, and returns to the map
- **THEN** the Saved chip is still selected and the map is still filtered

#### Scenario: App restart
- **WHEN** the app process is restarted after chips were selected
- **THEN** the map opens with no chip selected and all businesses shown

### Requirement: New-business pin is unaffected by filters
Filtering SHALL NOT hide, remove or otherwise affect the pin placed when adding a new business, or the new-business form. Re-rendering business markers SHALL NOT remove the new-business pin.

#### Scenario: Changing filters while adding a business
- **WHEN** the user has long-pressed the map to place a new-business pin and then changes the chip selection
- **THEN** the new-business pin stays on the map and the new-business form stays open

#### Scenario: Business data changes while adding a business
- **WHEN** a new-business pin is placed and the business list changes
- **THEN** the new-business pin stays on the map

### Requirement: Open details sheet survives filtering
If the business details sheet is open and the chip selection changes so that the business is filtered out, the details sheet SHALL stay open until the user dismisses it.

#### Scenario: Shown business filtered out
- **WHEN** the details sheet shows a restaurant and the user selects only the Coffee chip
- **THEN** the restaurant's marker is hidden and the details sheet stays open showing that restaurant

### Requirement: Compass stays clear of the filter row
The map compass SHALL appear below the top area holding the filter chips, so the chips never cover it.

#### Scenario: Map rotated
- **WHEN** the user rotates the map and the compass appears
- **THEN** the compass is fully visible below the chip row

### Requirement: Filter FAB removed
The map screen SHALL NOT show the filter floating action button or its expanding type buttons.

#### Scenario: Map screen controls
- **WHEN** the user opens the map screen
- **THEN** there is no filter floating action button; filtering is done only through the chips
