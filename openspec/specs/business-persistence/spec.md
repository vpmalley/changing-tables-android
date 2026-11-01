# Business Persistence Specification

## Purpose

Automatically persists the business database to a JSON file in user-accessible external storage, loading it on app start and dumping it on app stop, so business data survives across test sessions without a server.

## Requirements

### Requirement: JSON file in user-accessible external storage
The system SHALL read from and write to a JSON file named `businesses.json` located in the app-specific external files directory (`getExternalFilesDir(null)`). This directory is accessible to the user via USB file transfer or a file manager app, and requires no storage permissions.

#### Scenario: File location on first access
- **WHEN** the app starts and the JSON file does not yet exist
- **THEN** the system knows the target path is `<external-files-dir>/businesses.json` and does not error

#### Scenario: User can find the file
- **WHEN** the user connects the device via USB and browses the app's external files directory
- **THEN** the `businesses.json` file is visible and readable

### Requirement: Auto-load on activity start
When `MainActivity` starts (i.e., `onStart()` is called), the system SHALL attempt to load the JSON file. If the file exists and contains valid JSON, the system SHALL replace the current database contents with the businesses from the file.

#### Scenario: File exists with valid data on app start
- **WHEN** the app starts and `businesses.json` exists with a valid JSON array of businesses
- **THEN** the database is cleared and populated with the businesses from the file before the map screen is shown

#### Scenario: File does not exist on first launch
- **WHEN** the app starts and `businesses.json` does not exist
- **THEN** the database is left as-is (empty on first install) and no error is shown to the user

#### Scenario: File exists but is empty or contains an empty array
- **WHEN** the app starts and `businesses.json` exists but contains `[]` or is empty
- **THEN** the database is cleared and no businesses are loaded, with no error shown

#### Scenario: File contains invalid JSON
- **WHEN** the app starts and `businesses.json` contains malformed JSON
- **THEN** the database is left unchanged, the corrupt file is overwritten on the next dump, and no crash occurs

### Requirement: Auto-dump on activity stop
When `MainActivity` stops (i.e., `onStop()` is called), the system SHALL serialize all businesses currently in the database to the JSON file, overwriting any existing file.

#### Scenario: Database has businesses on app stop
- **WHEN** the app is stopped (user presses home or the app goes to background) and the database contains businesses
- **THEN** `businesses.json` is written with a JSON array of all businesses, including their `id`, `name`, `type`, `latitude`, `longitude`, and all other fields

#### Scenario: Database is empty on app stop
- **WHEN** the app is stopped and the database contains no businesses
- **THEN** `businesses.json` is written containing an empty JSON array `[]`

#### Scenario: Dump overwrites previous file
- **WHEN** the app is stopped and `businesses.json` already exists from a previous session
- **THEN** the existing file is overwritten with the current database contents

### Requirement: Data round-trips without loss
A business exported to JSON and re-imported SHALL retain all its field values: `id`, `name`, `type`, `description`, `longitude`, `latitude`, `hasChangingTable`, `changingTableLocation`, `hasDiaperPail`, `isClean`, `rating`, `area`, `photoUrls`, and `savedOnDevice`.

#### Scenario: Full round-trip preserves all fields
- **WHEN** a business with all fields populated is exported to JSON, the app is stopped and restarted, and the business is loaded back
- **THEN** every field of the reloaded business matches the original values exactly

#### Scenario: Photo URLs list round-trips
- **WHEN** a business with a non-empty `photoUrls` list is exported and re-imported
- **THEN** the `photoUrls` list is preserved with the same elements in the same order

### Requirement: Load and dump do not block the UI thread
The load operation on `onStart()` and the dump operation on `onStop()` SHALL run on a background thread (e.g., `Dispatchers.IO`). The UI SHALL remain responsive during these operations.

#### Scenario: App start is responsive during load
- **WHEN** the app starts and the JSON file contains a large number of businesses
- **THEN** the map screen appears and is interactive without waiting for the load to complete

#### Scenario: App stop does not delay backgrounding
- **WHEN** the user presses the home button
- **THEN** the app transitions to the background immediately; the dump runs asynchronously

### Requirement: Load replaces database contents atomically
When loading from JSON on `onStart()`, the system SHALL first delete all existing businesses and then insert the businesses from the file. If the insert fails partway, the database SHALL be left in a consistent state (either fully old or fully new data, not a mix).

#### Scenario: Partial insert failure does not corrupt data
- **WHEN** the JSON file contains businesses and the database insert fails partway through
- **THEN** the database either retains all old businesses or contains all new businesses, never a partial mix

### Requirement: No user-facing UI for import/export
The import and export operations SHALL be fully automatic with no buttons, menus, or settings. The user SHALL NOT see any import/export controls. This is a temporary developer/tester feature.

#### Scenario: No import/export UI elements
- **WHEN** the user navigates through all screens of the app
- **THEN** no import, export, backup, or restore buttons or menu items are visible

### Requirement: Schema-tolerant JSON loading
The JSON deserializer SHALL tolerate differences between the JSON file's schema and the current `Business` model without failing or crashing. Specifically: (a) if the JSON is missing a field that exists in the current model, the field SHALL be populated with its default value; (b) if the JSON contains a field that no longer exists in the current model, that field SHALL be silently ignored; (c) if a field was renamed, the old key SHALL be treated as removed (ignored) and the new key SHALL be treated as a new field (populated with its default value).

#### Scenario: New field added to model, old JSON loaded
- **WHEN** a new field is added to the `Business` model with a default value, and an old JSON file (without that field) is loaded
- **THEN** the business is loaded successfully and the new field has its default value

#### Scenario: Field removed from model, old JSON loaded
- **WHEN** a field is removed from the `Business` model, and an old JSON file (containing that field) is loaded
- **THEN** the business is loaded successfully and the removed field's data is silently ignored

#### Scenario: Field renamed in model, old JSON loaded
- **WHEN** a field is renamed in the `Business` model, and an old JSON file (with the old field name) is loaded
- **THEN** the business is loaded successfully; the old field's value is ignored and the new field has its default value

#### Scenario: JSON contains extra unknown fields
- **WHEN** the JSON file contains fields that do not exist in the current `Business` model (e.g., from a newer version of the app)
- **THEN** the business is loaded successfully and the unknown fields are silently ignored

### Requirement: No explicit versioning or migration code
The JSON file format SHALL NOT include a version number, schema identifier, or migration logic. Schema tolerance is achieved through the deserializer's configuration (ignoring unknown keys and using default values for missing keys), not through explicit migration steps.

#### Scenario: No version field in JSON
- **WHEN** the JSON file is inspected
- **THEN** there is no version number, schema ID, or migration metadata in the file
