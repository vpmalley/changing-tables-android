## 2025-05-15 - [Bottom Sheet Polish]
**Learning:** Using `setCompoundDrawablesRelativeWithIntrinsicBounds` instead of `setCompoundDrawablesWithIntrinsicBounds` ensures that icons correctly mirror in Right-to-Left (RTL) layouts, which is a critical small touch for global accessibility.
**Action:** Always prefer 'Relative' drawable methods for TextViews and Buttons to support RTL by default.

## 2025-05-15 - [Accessible Bottom Sheets]
**Learning:** Generic 'drag handles' in bottom sheets are often invisible to screen readers if they are just decorative Views. Adding a `contentDescription` makes the bottom sheet's interactive nature explicit.
**Action:** Include `android:contentDescription` for drag handles or clear accessibility labels for the sheet's state.
