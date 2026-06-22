## 2025-05-15 - Improving Bottom Sheet Accessibility and Context
**Learning:** Bottom sheets often lack explicit labels for drag handles, making them difficult to navigate for screen reader users. Additionally, binary "yes/no" indicators for amenities don't capture the nuance of "out of service" states, which is critical for parent-focused apps.
**Action:** Always provide `contentDescription` for drag handles. Implement a dedicated "out of service" state with distinct color (orange) and text suffix to manage user expectations.

## 2025-05-15 - RTL and Icon Consistency
**Learning:** Using `setCompoundDrawablesWithIntrinsicBounds` ignores RTL layouts, potentially placing icons on the wrong side for some users.
**Action:** Use `setCompoundDrawablesRelativeWithIntrinsicBounds` by default for all TextView and Button icon attachments.
