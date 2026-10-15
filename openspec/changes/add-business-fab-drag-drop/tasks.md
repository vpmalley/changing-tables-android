# Tasks

## 1. Layout Updates

- [ ] 1.1 Add FAB with `+` icon to `fragment_map.xml` and verify it appears in the bottom-right corner of the map screen
- [ ] 1.2 Ensure FAB has proper elevation and ripple effect according to Material Design guidelines

## 2. MapManager Drag-and-Drop Support

- [ ] 2.1 Add method to create draggable new business marker at map center and verify marker appears and can be dragged
- [ ] 2.2 Add drag listener to track marker movement and verify marker follows finger during drag
- [ ] 2.3 Add drop listener to capture final position and verify coordinates are correctly captured
- [ ] 2.4 Update `clearNewBusinessMarker` to handle draggable markers and verify marker is properly removed
- [ ] 2.5 Keep existing long-press listener in `setupAddingBusiness` and verify long-press still works independently

## 3. MapFragment Integration

- [ ] 3.1 Add FAB click listener in `MapFragment` and verify tapping FAB creates draggable marker
- [ ] 3.2 Connect marker drop event to show `BusinessFormBottomSheet` and verify form appears with correct coordinates
- [ ] 3.3 Handle cancel cases (drag off-screen, tap outside) and verify marker is hidden without showing form
- [ ] 3.4 Ensure only one draggable marker exists at a time and verify new FAB tap replaces existing marker

## 4. BusinessFormBottomSheet Updates

- [ ] 4.1 Verify form works correctly when opened from drag-and-drop flow and business is created at marker position
- [ ] 4.2 Hide FAB when business form is open to avoid confusion and verify FAB is not visible during form entry

## 5. Visual Feedback and Polish

- [ ] 5.1 Add visual feedback when marker is being dragged (e.g., slightly larger or different color) and verify it's visible during drag
- [ ] 5.2 Ensure marker stays visible while form is open and verify marker persists until form is submitted or canceled
- [ ] 5.3 Verify marker is unaffected by map filtering and remains visible regardless of filter chip selection

## 6. Testing

- [ ] 6.1 Write unit tests for MapManager drag-and-drop logic and verify all tests pass
- [ ] 6.2 Write UI tests for FAB interaction and marker drag-and-drop and verify all tests pass
- [ ] 6.3 Manually test on multiple screen sizes and verify FAB placement and drag behavior work correctly
- [ ] 6.4 Verify long-press method still works and both methods can be used independently

## 7. Documentation

- [ ] 7.1 Update any relevant README or documentation to mention the new FAB-based business creation flow
