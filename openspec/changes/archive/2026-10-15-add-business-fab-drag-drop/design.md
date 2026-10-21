# Design

## Context

Currently, users add a business by long-pressing on the map. This triggers `MapManager.setupAddingBusiness()` which listens for long-clicks and shows a marker at that position, then opens the `BusinessFormBottomSheet`. The map already has a `newBusinessAnnotationManager` for rendering the new business pin separately from regular business markers. The `BusinessFormBottomSheet` handles the form UI and business creation.

See proposal.md for the motivation behind this change.

## Goals / Non-Goals

**Goals:**
- Provide a discoverable way to start adding a new business
- Allow precise marker positioning through drag-and-drop
- Maintain the existing business form and data flow
- Keep the new-business marker visible during form entry
- Ensure the marker is unaffected by map filtering

**Non-Goals:**
- Changing the business form UI or validation logic
- Modifying how business data is stored or retrieved
- Adding new business fields or metadata
- Changing the map rendering for existing businesses

## Decisions

### Use Mapbox Maps SDK gesture handling for drag-and-drop
**Decision**: Use Mapbox's built-in touch handling and annotation drag capabilities rather than implementing custom drag logic.

**Rationale**: Mapbox Maps SDK (v10+) has built-in support for draggable annotations through `PointAnnotationOptions`. This provides smooth drag behavior, proper coordinate tracking, and handles edge cases (dragging off-screen, multi-touch, etc.) automatically. Custom implementation would require significant boilerplate and be more error-prone.

**Alternatives considered**:
1. Custom touch listener on the marker: More control but requires handling all edge cases manually
2. Third-party drag-and-drop library: Adds unnecessary dependency for a simple interaction

### Place FAB in bottom-right corner
**Decision**: Position the FAB in the bottom-right corner of the screen, above the bottom navigation bar.

**Rationale**: This is the standard Material Design placement for primary actions. It's easily reachable with the thumb on most devices and doesn't interfere with map interactions or the long-press gesture.

**Alternatives considered**:
1. Bottom-center: Could interfere with bottom sheet interactions
2. Top-right: Less accessible, might conflict with filter chips

### Reuse existing newBusinessAnnotationManager
**Decision**: Use the existing `newBusinessAnnotationManager` in `MapManager` for the draggable marker.

**Rationale**: The current implementation already separates the new business pin from regular business markers using a separate annotation manager. This ensures the new business pin renders above other markers and is unaffected by business filtering. We can extend this to support drag-and-drop.

**Alternatives considered**:
1. Create a third annotation manager: Unnecessary complexity
2. Use the business annotation manager: Would require special filtering logic

### Single marker at a time
**Decision**: Only allow one draggable new business marker on the map at any time.

**Rationale**: Having multiple draggable markers would create confusion about which business is being created. If a user wants to create multiple businesses, they should complete or cancel the current one first. This matches the existing behavior where long-press creates one marker at a time.

**Alternatives considered**:
1. Allow multiple markers: More complex state management, unclear UX

### Cancel on drag off-screen
**Decision**: Hide the marker and cancel the creation flow if the user drags the marker completely off the visible map area.

**Rationale**: This provides clear feedback that the action is canceled and prevents accidental business creation at the edge of the map. It's an intuitive interaction pattern - dragging something off-screen typically means dismissing it.

**Alternatives considered**:
1. Constrain marker to map bounds: Less intuitive, could be frustrating
2. Allow off-screen drop: Could lead to businesses being created at unexpected locations

## Risks / Trade-offs

**[Risk: Accidental FAB taps]** → Mitigation: The FAB will be a reasonable size and positioned to minimize accidental taps. Users can easily cancel by dragging off-screen or tapping outside.

**[Risk: Drag sensitivity on small screens]** → Mitigation: Use standard Material Design touch targets (48dp minimum). The Mapbox SDK handles touch sensitivity for annotations.

**[Risk: Performance with draggable annotations]** → Mitigation: Mapbox's annotation system is optimized for this. Only one draggable annotation exists at a time.

**[Risk: Confusion between drag and pan gestures]** → Mitigation: Once the marker is being dragged, map pan gestures will be consumed by the drag operation. The marker will stay under the user's finger during drag.

## Migration Plan

1. Add FAB to `fragment_map.xml` layout
2. Modify `MapFragment` to handle FAB click and initiate drag-and-drop flow
3. Update `MapManager` to:
   - Add method to create draggable marker at map center
   - Add method to handle marker drag and drop events
   - Update `showNewBusinessMarker` and `clearNewBusinessMarker` to support draggable markers
   - Keep existing long-press listener in `setupAddingBusiness`
4. Update `BusinessFormBottomSheet` to work with the new flow (minimal changes needed)
5. Test the drag-and-drop interaction
6. Test that long-press still works independently

**Rollback**: The change is fully additive - both methods coexist. If issues arise with the FAB method, it can be disabled or hidden without affecting the long-press functionality.

## Open Questions

- Should the FAB be hidden when the business form bottom sheet is already open? (Likely yes, to avoid confusion)
- Should there be a visual indicator (e.g., a tooltip) the first time users see the FAB? (Could be addressed in a separate onboarding change)
