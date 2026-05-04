## 2025-05-14 - [Bottom Sheet UX & Accessibility Polish]
**Learning:** Hardcoding UI text concatenation (e.g., `text + suffix`) in Kotlin code hinders localization. Using string templates in `strings.xml` with placeholders (e.g., `%1$s%2$s`) is the preferred approach for Android to maintain clean and localizable code.
**Action:** Always check if dynamic UI text can be represented as a template in `strings.xml` before concatenating in code.

**Learning:** Fixed-width `RatingBar` components (e.g., `48dp`) can cut off stars or cause layout issues if the system font or display size is increased. Using `wrap_content` ensures all stars are visible across different accessibility settings.
**Action:** Prefer `wrap_content` for `RatingBar` and other multi-item indicator components to ensure they scale correctly.

**Learning:** Empty `RatingBar` or empty state views should be explicitly hidden if they don't provide value, rather than showing a '0' state which might be confusing.
**Action:** Implement dynamic visibility logic (e.g., `View.GONE`) for components like ratings when data is absent.
