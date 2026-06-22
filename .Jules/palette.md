# Palette's Journal - Changing Tables

## 2025-05-05 - Strengthening Form Integrity with Mandatory Field Validation

**Learning:** In multi-step forms like the 'Add Business' flow, users might focus on the interactive questions and forget basic details like the name or category. Mandatory field validation ensures the 'Add Business' button is only active when both a non-blank business name and a business type are provided, preventing the creation of incomplete or anonymous entries.

**Action:**
1. Implement a `TextWatcher` on the business name field and call a `validateForm()` helper in `BusinessFormBottomSheet`.
2. Ensure the submit button state is tied to both the name input and the business type selection for better data quality.
