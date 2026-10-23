# Design

## Context

The app uses a Room database (`AppDatabase`) with a single `Business` entity stored in the `businesses` table. The `Business` model is already annotated with `@Serializable` for kotlinx-serialization. The app has one activity (`MainActivity`) with a bottom-navigation layout hosting three fragments (Map, Saved, Notifications). There is no server-side persistence yet. The `BusinessDao` exposes `getAll()` as `LiveData`, `insertAll()` (suspend), and `deleteAll()` (suspend). The `BusinessRepository` wraps the DAO with `Dispatchers.IO` coroutines.

## Goals / Non-Goals

**Goals:**
- Automatically persist all businesses to a JSON file on app stop and restore them on app start.
- Use a user-accessible file location so testers can inspect, share, or manually edit the file.
- Zero UI changes — fully automatic, no buttons or settings.
- No new dependencies, no new permissions.

**Non-Goals:**
- No server sync, no cloud backup, no conflict resolution.
- No explicit JSON schema versioning, version field, or migration code — schema tolerance is handled implicitly by the deserializer configuration.
- No user-facing import/export controls.
- No encryption or authentication of the JSON file.
- No incremental/diff-based persistence — full dump every time.

## Decisions

### Decision: Storage location — `getExternalFilesDir(null)`
**Choice:** Store `businesses.json` in `context.getExternalFilesDir(null)`, which resolves to `/Android/data/fr.vpm.changingtables/files/` on the device's external storage.

**Rationale:**
- User-accessible: visible via USB MTP, file manager apps, and `adb pull` without root.
- No storage permissions required (app-specific external dirs are exempt from `READ/WRITE_EXTERNAL_STORAGE` on API 19+; minSdk is 23).
- Survives app data clears that target internal storage, and is removed on uninstall (appropriate for a temporary feature).
- `getFilesDir()` (internal storage) is not user-accessible without root or `adb`.
- `MediaStore` / `Downloads` would require `WRITE_EXTERNAL_STORAGE` on older APIs and adds unnecessary complexity.

**Alternatives considered:**
- `getFilesDir()` — not user-accessible. Rejected.
- `Environment.getExternalStoragePublicDirectory(DIRECTORY_DOCUMENTS)` — requires permissions on API 29+, deprecated. Rejected.
- `MediaStore` — overkill for a single JSON file. Rejected.

### Decision: Serialization — kotlinx-serialization with `ignoreUnknownKeys`
**Choice:** Use a configured `kotlinx.serialization.json.Json` instance to encode/decode `List<Business>`, with `ignoreUnknownKeys = true`.

**Rationale:**
- `Business` is already annotated with `@Serializable`.
- `kotlinx-serialization-json:1.6.0` is already a project dependency.
- Produces clean, human-readable JSON that testers can edit by hand.
- `ignoreUnknownKeys = true` causes the deserializer to silently skip any JSON keys that don't match a property in the current `Business` model — this handles removed fields and renamed fields (old key is ignored).
- All `Business` properties have default values in the class definition (e.g., `var name: String? = null`, `var rating: Int = -1`), so kotlinx-serialization automatically uses the default when a key is missing from the JSON — this handles new fields added to the model.
- No `@SerialName` migration annotations or custom serializers are needed.

**Alternatives considered:**
- `Gson` — works but ignores the existing `@Serializable` annotations; no advantage over kotlinx-serialization here.
- Custom `JsonContentPolymorphicSerializer` or migration hooks — unnecessary complexity for a temporary feature.

### Decision: Lifecycle hooks — `onStart()` / `onStop()`
**Choice:** Load in `MainActivity.onStart()`, dump in `MainActivity.onStop()`.

**Rationale:**
- `onStart()`/`onStop()` bracket the entire visible lifetime of the activity. Every time the app comes to the foreground, data is loaded; every time it goes to background, data is dumped.
- `onResume()`/`onPause()` would fire on every dialog or bottom-sheet appearance, causing unnecessary I/O.
- `onCreate()`/`onDestroy()` fire only on process create/destroy, which misses the case where the OS kills and recreates the activity without killing the process.

**Alternatives considered:**
- `onCreate()`/`onDestroy()` — too narrow; misses activity recreation within the same process.
- `onResume()`/`onPause()` — too frequent; fires on bottom sheets and dialogs.

### Decision: Threading — `Dispatchers.IO` with `lifecycleScope`
**Choice:** Run load and dump on `Dispatchers.IO` using `lifecycleScope.launch` from `MainActivity`.

**Rationale:**
- `lifecycleScope` is automatically cancelled when the activity is destroyed, preventing leaks.
- `Dispatchers.IO` is designed for blocking file and database I/O.
- The DAO methods (`deleteAll`, `insertAll`, `getAllSync`) are suspend functions that already handle their own dispatcher switching via Room.

**Alternatives considered:**
- `GlobalScope` — leaks if the activity is destroyed mid-operation. Rejected.
- `onStop()` is synchronous — cannot use suspend functions directly; coroutine is required.

### Decision: Import strategy — delete-all then insert-all
**Choice:** On load, call `deleteAll()` followed by `insertAll(businesses)` within a single Room transaction.

**Rationale:**
- Simple and atomic: Room wraps `@Transaction` methods in a single database transaction, so a failure leaves the DB in its prior state.
- The JSON file is the source of truth on load — any businesses added since the last dump but not yet saved are intentionally discarded.
- Alternative (upsert by ID) would leave stale businesses that were deleted in the file but not yet removed from the DB.

**Alternatives considered:**
- Upsert by ID (`OnConflictStrategy.REPLACE`) — leaves orphans; the file should be authoritative.
- Diff-based merge — unnecessary complexity for a temporary feature.

### Decision: Synchronous DAO query for export
**Choice:** Add a `getAllSync()` suspend function to `BusinessDao` annotated with `@Query("SELECT * FROM businesses")` returning `List<Business>` (not `LiveData`).

**Rationale:**
- The existing `getAll()` returns `LiveData<List<Business>>`, which is not suitable for one-shot export — it requires an observer and doesn't give a synchronous snapshot.
- A suspend `@Query` returning `List<Business>` gives a direct snapshot on `Dispatchers.IO`.

### Decision: Error handling — fail-silent
**Choice:** All I/O and parsing errors are caught and logged (via `Log.e`), with no user-facing error UI. The app continues normally.

**Rationale:**
- This is a developer/tester feature with no user-facing surface.
- A corrupt JSON file should not crash the app; on the next dump, it gets overwritten with valid data.
- Crashing on a missing file (first launch) would be unacceptable.

**Alternatives considered:**
- Show a Toast on error — adds UI for a feature that should be invisible. Rejected.
- Crash on error — unacceptable for a background persistence mechanism.

## Risks / Trade-offs

| Risk | Mitigation |
|---|---|
| `getExternalFilesDir()` returns null if external storage is unavailable (e.g., USB mass storage mode on older devices) | Check for null; fall back to `getFilesDir()` for the dump path. Load is skipped if no file exists. |
| Dump on `onStop()` may not complete if the OS kills the process immediately after | Acceptable for a temporary feature. `onStop()` is guaranteed to be called; the coroutine starts immediately. For critical data, `onPause()` would be safer but was rejected for frequency reasons. |
| JSON file grows large with many businesses | Acceptable for a testing tool. Full dump is O(n) in file size; at typical test volumes (< 1000 businesses) this is negligible. |
| Concurrent access — load and dump racing if `onStart`/`onStop` fire in quick succession | `lifecycleScope` cancels the previous coroutine; Room serializes writes. The dump always runs after the load completes or is cancelled. |
| `Business` model changes affect old JSON files | Mitigated by design: `ignoreUnknownKeys = true` skips removed/renamed fields; default property values handle new fields. No crash, no data loss for unaffected fields. |

## Migration Plan

This change is purely additive — no existing data or behavior is modified. No migration steps are needed. The first time the app runs with this change:

1. On `onStart()`, no JSON file exists → database is left as-is.
2. On `onStop()`, the current database contents are dumped to `businesses.json`.
3. On subsequent launches, the file is loaded on start and overwritten on stop.

Rollback: remove the `onStart()`/`onStop()` overrides from `MainActivity` and delete the `BusinessJsonStore` class. The JSON file in external storage is harmless and can be deleted manually.

## Open Questions

- None. All design decisions are straightforward given the existing codebase structure.
