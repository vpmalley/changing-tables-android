# Palette's Journal - Changing Tables

## 2025-05-15 - Initial Setup
**Learning:** High-contrast interactions and clear feedback are essential for parents on the go who might be using the app under stressful or rushed conditions.
**Action:** Prioritize tactile feedback (vibration/haptic if possible, though not in this task), high-contrast UI states, and reducing cognitive load through automated validation.

## 2025-05-15 - Mandatory Form Validation
**Learning:** Disabling the primary action button until all required fields are filled prevents "error-first" experiences where users click and then see red text. It provides a smoother "path to success".
**Action:** Implement `validateForm()` in `BusinessFormBottomSheet` to ensure business name and type are selected before enabling the 'Add Business' button.
