# Proposal

## Why

During development and testing, the Room database is wiped on every app reinstall or data clear, forcing testers to re-enter the same businesses repeatedly. There is no server-side persistence yet, so a lightweight file-based export/import mechanism is needed to preserve business data across test sessions. This is a temporary measure until a backend takes over the persistence role.

## What Changes

- Add a `BusinessJsonStore` class that serializes the full business list to a JSON file in app-specific external storage (`getExternalFilesDir`), a user-accessible location that requires no storage permissions and survives app reinstalls on the same device.
- On `MainActivity.onStart()`, automatically load the JSON file (if it exists) and populate the database. On `MainActivity.onStop()`, automatically dump the current database contents to the JSON file.
- Add a synchronous `getAllSync()` method to `BusinessDao` for export, since the existing `getAll()` returns `LiveData`.
- Add `exportToJson()` and `importFromJson()` methods to `BusinessRepository` that delegate to the DAO and the JSON store.
- The JSON format uses the existing `kotlinx.serialization` `@Serializable` annotation on the `Business` model — no new serialization code is needed. The deserializer is configured with `ignoreUnknownKeys = true` and relies on property default values, so old JSON files remain loadable after the `Business` model evolves (new fields get defaults, removed/renamed fields are silently ignored).

## Capabilities

### New Capabilities
- `business-persistence`: Automatic JSON-file-based persistence of the business database to app-specific external storage, loading on activity start and dumping on activity stop, so business data survives across test sessions without a server.

### Modified Capabilities

## Impact

- `BusinessDao`: add `getAllSync()` suspend query.
- `BusinessRepository`: add `exportToJson(context)` and `importFromJson(context)` methods.
- New file `BusinessJsonStore.kt` in `fr.vpm.changingtables.database` (or `fr.vpm.changingtables.persistence`) handling file I/O and JSON serialization.
- `MainActivity`: override `onStart()` and `onStop()` to trigger load/dump via a coroutine.
- No new dependencies — `kotlinx-serialization-json` is already present.
- No new permissions — `getExternalFilesDir()` requires no runtime permissions.
- No manifest changes needed.
