# Tasks

## 1. DAO Layer

- [x] 1.1 Add `getAllSync()` suspend function to `BusinessDao` with `@Query("SELECT * FROM businesses")` returning `List<Business>`. Verify by compiling the project (`./gradlew compileDebugKotlin`).
- [x] 1.2 Add `replaceAll(businesses: List<Business>)` to `BusinessDao` as a `@Transaction` suspend function that calls `deleteAll()` then `insertAll(businesses)`. Verify by compiling the project.

## 2. JSON Store

- [x] 2.1 Create `BusinessJsonStore.kt` in `app/src/main/java/fr/vpm/changingtables/persistence/` with a `jsonFile(context: Context): File` helper that returns `File(context.getExternalFilesDir(null), "businesses.json")` with a null-safe fallback to `context.filesDir`. Verify the file compiles.
- [x] 2.2 Add `suspend fun load(context: Context): List<Business>?` to `BusinessJsonStore` that reads the JSON file, deserializes with a `Json { ignoreUnknownKeys = true }` instance using `decodeFromString<List<Business>>()`, and returns `null` on any error (file missing, parse failure, I/O error). Wrap in `Dispatchers.IO`. Verify with a unit test using a temp file.
- [x] 2.3 Add `suspend fun dump(context: Context, businesses: List<Business>)` to `BusinessJsonStore` that serializes with `Json.encodeToString()` and writes to the file. Wrap in `Dispatchers.IO`. Verify with a unit test using a temp file.
- [x] 2.4 Write unit tests for `BusinessJsonStore` in `app/src/test/java/fr/vpm/changingtables/persistence/BusinessJsonStoreTest.kt`: (a) round-trip a list of `Business` objects, (b) missing file returns null, (c) corrupt JSON returns null, (d) JSON with extra unknown fields loads successfully (fields ignored), (e) JSON missing a field loads successfully (field gets default value). Verify with `./gradlew testDebugUnitTest --tests "*BusinessJsonStoreTest*"`.

## 3. Repository Layer

- [x] 3.1 Add `suspend fun getAllSync(): List<Business>` to `BusinessRepository` that delegates to `businessDao.getAllSync()` on `Dispatchers.IO`. Verify by compiling.
- [x] 3.2 Add `suspend fun replaceAll(businesses: List<Business>)` to `BusinessRepository` that delegates to `businessDao.replaceAll(businesses)` on `Dispatchers.IO`. Verify by compiling.
- [x] 3.3 Add `suspend fun exportToJson(context: Context)` to `BusinessRepository` that calls `getAllSync()`, then delegates to `BusinessJsonStore(context).dump(context, businesses)`. Verify by compiling.
- [x] 3.4 Add `suspend fun importFromJson(context: Context): Boolean` to `BusinessRepository` that calls `BusinessJsonStore(context).load(context)`, and if non-null, calls `replaceAll(businesses)` and returns `true`; returns `false` if load returned null. Verify by compiling.

## 4. Activity Lifecycle Wiring

- [x] 4.1 In `MainActivity`, obtain a `BusinessRepository` instance in `onCreate()` by getting the DAO from `AppDatabase.getDatabase(this).businessDao()` and constructing `BusinessRepository(dao)`. Store as a field. Verify by compiling.
- [x] 4.2 Override `onStart()` in `MainActivity` to launch `lifecycleScope.launch { repository.importFromJson(applicationContext) }`. Verify by compiling.
- [x] 4.3 Override `onStop()` in `MainActivity` to launch `lifecycleScope.launch { repository.exportToJson(applicationContext) }`. Verify by compiling.

## 5. Integration Verification

- [x] 5.1 Build the debug APK and verify it compiles without errors: `./gradlew assembleDebug`.
- [x] 5.2 Run all unit tests and verify they pass: `./gradlew testDebugUnitTest`.
- [x] 5.3 Manual smoke test: install the APK on a device/emulator, add a business via the map, press home (triggering `onStop`), verify `businesses.json` appears in `/Android/data/fr.vpm.changingtables/files/`, kill the app, clear app data, relaunch, and verify the business reappears (loaded from JSON on `onStart`).
