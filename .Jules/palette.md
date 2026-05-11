## 2025-05-15 - [Accessibility for Bottom Sheet Drag Handles]
**Learning:** Decorative views used as drag handles in bottom sheets are often ignored by screen readers if they lack a content description. Adding a localized `android:contentDescription` makes the interactive nature of the bottom sheet discoverable.
**Action:** Always include a `contentDescription` for drag handle views in bottom sheets.

## 2025-05-15 - [RTL Support for Compound Drawables]
**Learning:** Using `setCompoundDrawablesWithIntrinsicBounds` hardcodes icons to the left/right, which breaks mirror behavior in RTL languages.
**Action:** Use `setCompoundDrawablesRelativeWithIntrinsicBounds` (and `drawableStart`/`drawableEnd` in XML) to ensure icons correctly flip for RTL users.
