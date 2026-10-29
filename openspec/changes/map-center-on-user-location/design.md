# Design

## Context

The app uses Mapbox Maps SDK 11.12.2 (`com.mapbox.maps:android-ndk27`). The map is hosted in `MapFragment` via `FragmentMapBinding.mapView`. The map starts centered on Vancouver (49.2665, -123.1445) at zoom 10, set via XML attributes. `ACCESS_FINE_LOCATION` is already declared in the manifest. There is no existing location code, no runtime permission flow, and no Play Services Location dependency. `MapManager` handles annotations, gestures, and marker logic. `MapViewModel` handles filter state. The fragment uses `activityViewModels` for shared ViewModels.

## Goals / Non-Goals

**Goals:**
- One-shot location retrieval when the map fragment becomes visible.
- Center the map on the user's location with a smooth animation.
- Display a location puck (blue dot indicator) at the user's coordinates.
- Respect user interaction — never yank the camera while the user is panning/zooming/dragging (but the puck still appears).
- Silent failure — no errors, toasts, or dialogs if anything goes wrong.

**Non-Goals:**
- No continuous location tracking or updates.
- No "My Location" button or FAB.
- No background location access.
- No location sharing or storage.
- No settings screen to re-enable location.
- No bearing/compass direction indicator on the puck.

## Decisions

### Decision: Location provider — `FusedLocationProviderClient`
**Choice:** Use `com.google.android.gms:play-services-location` and its `FusedLocationProviderClient` for one-shot location retrieval via `getCurrentLocation()` with `Priority.PRIORITY_BALANCED_POWER_ACCURACY`.

**Rationale:**
- `FusedLocationProviderClient` is the standard, battery-efficient way to get location on Android.
- `getCurrentLocation()` is a one-shot request (not a subscription), matching the spec's "retrieve once per map appearance" requirement.
- `getLastLocation()` is unreliable (often returns null on first use); `getCurrentLocation()` actively requests a fresh fix.
- Balanced power accuracy is sufficient for centering a map — we don't need GPS-level precision.

**Alternatives considered:**
- `LocationManager.getLastKnownLocation()` — deprecated, unreliable, often returns stale or null data.
- `getLastLocation()` on FusedLocationProviderClient — fast but often null on first call; not suitable as the sole source.
- Mapbox LocationComponent plugin — designed for continuous tracking; overkill for one-shot retrieval. Rejected for location retrieval (see puck decision below for rendering).

### Decision: Location services check — `LocationManager.isProviderEnabled`
**Choice:** Check `LocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)` OR `LocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)` before calling `getCurrentLocation()`. On API 28+, also check `LocationManager.isLocationEnabled()`.

**Rationale:**
- `getCurrentLocation()` will simply fail/timeout if location services are off, wasting up to 10 seconds.
- Checking first provides a fast, silent bail-out.
- Checking both GPS and network providers covers devices that rely on either.

**Alternatives considered:**
- Skip the check and rely on the timeout — simpler but slower UX (10s delay before map gives up).
- Use `Settings.Secure.LOCATION_MODE` — deprecated on API 28+.

### Decision: Permission flow — `ActivityResultContracts.RequestPermission`
**Choice:** Use the modern `registerForActivityResult(ActivityResultContracts.RequestPermission())` API in `MapFragment` to request `ACCESS_FINE_LOCATION` at runtime.

**Rationale:**
- This is the recommended Android pattern (replaces deprecated `onRequestPermissionsResult`).
- The callback receives a boolean — simple to branch on granted/denied.
- `shouldShowRequestPermissionRationale()` can distinguish "denied once" from "Don't ask again".

**Alternatives considered:**
- `onRequestPermissionsResult` — deprecated, more boilerplate.
- Requesting from `MainActivity` — wrong scope; the feature is map-specific.

### Decision: User interaction detection — Mapbox camera change listener
**Choice:** Track whether the user is interacting with the map by listening to Mapbox's `mapboxMap.addOnCameraChangeListener` and gesture listeners (`addOnMapClickListener` already exists). Set an `isUserInteracting` flag when a camera change is triggered by a user gesture (pan, zoom, rotate, scale). Reset the flag shortly after the gesture ends (e.g., using a debounce or checking `mapboxMap.isUserPanning` if available, or tracking via `OnCameraChangeListener` + a short delay).

**Rationale:**
- Mapbox's `CameraChangeListener` fires for both programmatic and user-initiated camera changes. To distinguish, we can use gesture listeners (`addOnMoveListener`, `addOnScaleListener`, `addOnRotateListener`) which only fire for user gestures.
- When any of these fire, set `isUserInteracting = true`. After a short debounce (e.g., 500ms of no gesture events), set it back to `false`.
- When the location fix arrives, check `isUserInteracting` — if true, discard the fix.

**Alternatives considered:**
- `mapboxMap.isUserPanning` — not available in Mapbox SDK 11.x as a simple boolean.
- Using `addOnCameraChangeListener` alone — fires for both programmatic and user changes; can't distinguish.
- Checking `mapboxMap.cameraState` before animating — doesn't tell you if the user is mid-gesture.

### Decision: Camera animation — `mapboxMap.easeTo()`
**Choice:** Use `mapboxMap.easeTo(CameraOptions.Builder().center(Point.fromLngLat(lng, lat)).zoom(15.0).build())` to smoothly animate the camera to the user's location.

**Rationale:**
- `easeTo` provides a smooth, natural animation (vs. `setCamera` which jumps instantly).
- `CameraOptions` lets us set both center and zoom in one call.
- Zoom 15 is street-level — appropriate for seeing nearby businesses.

**Alternatives considered:**
- `setCamera()` — instant jump, jarring UX.
- `flyTo()` — more dramatic animation, unnecessary for a short distance.

### Decision: Location puck — `PointAnnotation` with custom drawable
**Choice:** Render the location puck as a Mapbox `PointAnnotation` with a custom circular drawable (blue dot with a lighter outer ring), managed through `MapManager` alongside the existing business and new-business annotation managers. A separate `PointAnnotationManager` is used so the puck is drawn above business markers but below the draggable new-business marker.

**Rationale:**
- Consistent with the existing annotation pattern in `MapManager` (business markers, new-business markers, draggable markers all use `PointAnnotation`).
- No additional Mapbox plugin dependency — the LocationComponent plugin is designed for continuous tracking and would require a custom `LocationProvider` implementation.
- A custom vector drawable (blue circle with semi-transparent outer ring) is simple, scalable, and themeable.
- A separate annotation manager keeps the puck's lifecycle independent from business markers.

**Alternatives considered:**
- Mapbox `LocationComponent` plugin — provides a built-in puck with accuracy circle and bearing indicator, but is designed for continuous tracking and requires a `LocationProvider` implementation. Overkill for one-shot display. Rejected.
- Custom `View` overlay — would require manual coordinate-to-screen mapping and would not scale/rotate with the map. Rejected.
- `MapView` location plugin with `locationComponent.locationProvider` set to a one-shot provider — possible but adds complexity (custom `LocationProvider`, plugin lifecycle management). The `PointAnnotation` approach is simpler and consistent with the codebase.

### Decision: Encapsulation — new `UserLocationProvider` class
**Choice:** Create a `UserLocationProvider` class in `fr.vpm.changingtables.location` that encapsulates:
- Permission check (`ContextCompat.checkSelfPermission`)
- Location services check (`LocationManager`)
- Location retrieval (`FusedLocationProviderClient.getCurrentLocation()` with `CancellationTokenSource` timeout)

It exposes a simple suspend function: `suspend fun getLocation(context: Context): android.location.Location?` that returns the location or null if unavailable.

**Rationale:**
- Keeps `MapFragment` clean — the fragment just calls `getLocation()` and handles the result.
- Testable in isolation.
- Single responsibility.

### Decision: Trigger point — `MapFragment.onResume()`
**Choice:** Trigger the permission check → location services check → location retrieval → puck display + camera animation chain in `MapFragment.onResume()`.

**Rationale:**
- `onResume()` fires every time the fragment becomes visible (including when returning from Saved/Notifications tabs).
- This matches the spec's "retrieve once per map fragment appearance."
- `onViewCreated()` only fires once on creation, missing the return-from-other-tab case.

**Alternatives considered:**
- `onViewCreated()` — only fires on creation, misses tab switches.
- `onStart()` — fires on start and restart, but `onResume()` is more semantically correct for "fragment is visible and interactive."

## Risks / Trade-offs

| Risk | Mitigation |
|---|---|
| `getCurrentLocation()` may take several seconds or time out on first call (cold GPS) | Use a 10-second timeout via `CancellationTokenSource`; silently fall back to default map position on timeout. |
| User interaction detection may be imperfect (false positive/negative) | Use gesture listeners with a short debounce window. Acceptable for a one-shot centering feature — worst case is the map doesn't center when it could have, or centers slightly after the user stops interacting. |
| `FusedLocationProviderClient` requires Google Play Services | The app already targets devices with Play Services (standard for production Android apps). If Play Services is unavailable, `getCurrentLocation()` will fail and the map stays at default. |
| Permission dialog interrupts the map view on first launch | Acceptable — the user sees the map briefly, then the permission dialog appears. After granting, the map centers. |
| `onResume()` fires when returning from a bottom sheet (business form/details) | The location retrieval would fire again. Mitigation: only trigger on first resume or when the fragment was previously stopped (not paused). Use a `hasCenteredOnce` flag or check `savedInstanceState == null` in `onViewCreated` to distinguish first creation from tab switches. |
| Puck drawable needs to be created as a custom asset | Create a simple vector drawable (blue circle with semi-transparent outer ring) in `res/drawable/`. No external assets needed. |

## Migration Plan

This is a purely additive feature. No existing behavior changes. The first time the app runs with this change:

1. User opens the map fragment → permission dialog appears (if not granted).
2. User grants permission → location services check → location retrieval → location puck appears and map centers on user.
3. On subsequent visits to the map tab, the flow repeats (one-shot centering + puck update).

Rollback: remove the `onResume()` location logic from `MapFragment`, delete the `UserLocationProvider` class and the puck drawable. The `play-services-location` dependency can be removed.

## Open Questions

- Should the map re-center on the user's location every time the map tab is shown, or only the first time? The spec says "per map fragment appearance" (every time). This is a product decision — re-centering on every tab switch could be annoying if the user has explored a different area. **Assumption:** re-center every time the map fragment becomes visible, as specified. If this feels too aggressive in practice, it can be changed to first-time-only later.
