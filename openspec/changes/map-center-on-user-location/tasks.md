# Tasks

## 1. Dependencies

- [x] 1.1 Add `play-services-location` dependency to `gradle/libs.versions.toml` (version catalog) and `app/build.gradle.kts`. Use version `21.3.0` (or latest stable). Verify by syncing Gradle (`./gradlew sync` or opening in Android Studio).

## 2. UserLocationProvider

- [x] 2.1 Create `UserLocationProvider.kt` in `app/src/main/java/fr/vpm/changingtables/location/` with:
  - `fun hasLocationPermission(context: Context): Boolean` — checks `ContextCompat.checkSelfPermission` for `ACCESS_FINE_LOCATION`.
  - `fun isLocationEnabled(context: Context): Boolean` — checks `LocationManager.isProviderEnabled` for GPS or network provider (and `isLocationEnabled()` on API 28+).
  - `suspend fun getLocation(context: Context, timeoutMs: Long = 10_000): android.location.Location?` — uses `FusedLocationProviderClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cancellationToken)` with a `CancellationTokenSource` timeout. Returns null on any failure.
  Verify the file compiles with `./gradlew compileDebugKotlin`.

- [x] 2.2 Write unit tests for `UserLocationProvider` in `app/src/test/java/fr/vpm/changingtables/location/UserLocationProviderTest.kt`:
  - `hasLocationPermission` returns false when permission not granted (mock or use Robolectric if available).
  - `isLocationEnabled` returns false when no providers are enabled (mock `LocationManager`).
  Verify with `./gradlew testDebugUnitTest --tests "*UserLocationProviderTest*"`.
  Note: If mocking Android framework classes is impractical without additional test dependencies, document the limitation and rely on manual/integration testing for these methods.

## 3. Location Puck Drawable

- [x] 3.1 Create `ic_location_puck.xml` vector drawable in `app/src/main/res/drawable/` — a blue circle (e.g., `#2196F3`) with a white border and a semi-transparent lighter outer ring. Verify the drawable renders by building the project.

## 4. Map Interaction Tracking and Camera/Puck Control

- [x] 4.1 Add `private var isUserInteracting: Boolean = false` and a debounce mechanism to `MapManager`. Register Mapbox gesture listeners (`addOnMoveListener`, `addOnScaleListener`) on `mapView.mapboxMap` that set `isUserInteracting = true`. Use a `Handler` with a 500ms delayed reset to set `isUserInteracting = false` after gestures stop. Expose `fun isUserInteracting(): Boolean`. Verify by compiling.

- [x] 4.2 Add `fun centerOnLocation(mapView: MapView, latitude: Double, longitude: Double)` to `MapManager` that calls `mapView.mapboxMap.easeTo(CameraOptions.Builder().center(Point.fromLngLat(longitude, latitude)).zoom(15.0).build())`. Verify by compiling.

- [x] 4.3 Add a separate `PointAnnotationManager` for the location puck in `MapManager` (created in `setupPointAnnotationManager`), with its own `PointAnnotation` field. Add `fun showLocationPuck(context: Context, latitude: Double, longitude: Double)` that creates/updates the puck annotation using the `ic_location_puck` drawable, and `fun hideLocationPuck()` that deletes it. The puck annotation SHALL have a `symbolSortKey` between business markers (5.0) and the draggable marker (10.0), e.g., 7.0. Verify by compiling.

## 5. MapFragment Integration

- [x] 5.1 In `MapFragment`, register `ActivityResultContracts.RequestPermission()` as a field: `private val locationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted -> if (isGranted) centerOnUserLocation() }`. Verify by compiling.

- [x] 5.2 Add `private fun centerOnUserLocation()` to `MapFragment` that:
  - Checks `UserLocationProvider.hasLocationPermission(requireContext())` — if false, launches the permission request, hides any existing puck, and returns.
  - Checks `UserLocationProvider.isLocationEnabled(requireContext())` — if false, hides any existing puck and returns silently.
  - Launches a coroutine (`viewLifecycleOwner.lifecycleScope.launch`) that calls `UserLocationProvider.getLocation(requireContext())`.
  - If a location is returned:
    - Calls `mapManager.showLocationPuck(requireContext(), location.latitude, location.longitude)` (always — puck appears regardless of interaction).
    - If `!mapManager.isUserInteracting()`, calls `mapManager.centerOnLocation(binding.mapView, location.latitude, location.longitude)`.
  - If location is null, calls `mapManager.hideLocationPuck()` and does nothing else (silent).
  Verify by compiling.

- [x] 5.3 Override `onResume()` in `MapFragment` to call `centerOnUserLocation()`. Verify by compiling.

- [x] 5.4 Register the gesture listeners from task 4.1 in `MapFragment.onViewCreated()` (passing `binding.mapView.mapboxMap` to the `MapManager` setup). Verify by compiling.

- [x] 5.5 In `MapManager.onDestroyView()`, also clear the location puck annotation and its manager. Verify by compiling.

## 6. Integration Verification

- [x] 6.1 Build the debug APK: `./gradlew assembleDebug`. Verify BUILD SUCCESSFUL.

- [x] 6.2 Run all unit tests: `./gradlew testDebugUnitTest`. Verify all tests pass.

- [x] 6.3 Manual smoke test on a device/emulator with location services enabled:
  - Install the APK, open the app → map tab.
  - First launch: verify the location permission dialog appears. Grant it.
  - Verify the map animates to center on the device's current location AND a blue location puck appears at the device's position.
  - Navigate to Saved tab, then back to Map tab → verify the map re-centers and the puck reappears at the user's location.
  - While the map is centering, pan the map → verify the camera does NOT yank back to the user's location, but the puck still appears.
  - Deny the permission (or revoke in system settings) → relaunch → verify no dialog appears (if permanently denied), the map stays at default, and no puck is shown.
  - Disable location services in system settings → relaunch → verify the map stays at default position with no puck and no error.
