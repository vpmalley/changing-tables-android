## 2025-02-27 - Explicit Status Labels for Accessibility
**Learning:** Relying solely on icon colors (like green/orange) for amenity status is insufficient for screen readers and color-blind users. Explicitly appending status text (e.g., "(Out of service)") ensures clarity for all users.
**Action:** Always provide textual status indicators alongside visual ones, especially when the icon doesn't change significantly between states.

## 2025-02-27 - Mandatory Form Validation Feedback
**Learning:** Users can be frustrated if an "Add" button is disabled without clear feedback on what's missing. While we added character counters and cleared inputs, real-time validation against both name and category selection helps guide the user before they even try to submit.
**Action:** Implement real-time `TextWatcher` and selection listeners to toggle button states, ensuring a 'fail-fast' but guided experience.
