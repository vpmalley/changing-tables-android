# Proposal

## Why

The map currently opens centered on a hardcoded default location (Vancouver). Users must manually pan and zoom to find their own area, which is friction for the primary use case — finding businesses near them. Since the app already declares `ACCESS_FINE_LOCATION`, we can center the map on the user's actual location when they open the map, saving time and improving discoverability.

## What Changes

- Request `ACCESS_FINE_LOCATION` runtime permission when the map fragment is first shown (if not already granted).
- Check that location services are enabled on the device before attempting to retrieve the location.
- Retrieve the user's last known / current location via `FusedLocationProviderClient` (one-shot, not continuous tracking).
- Once the location is retrieved, display a location puck (blue dot indicator) at the user's coordinates on the map.
- Animate the map camera to center on the user's position (unless the user is actively interacting with the map — panning, zooming, or dragging a marker; the puck still appears regardless).
- Do nothing if permission is denied or location services are disabled — the map stays at its default location with no puck.
- Add the `play-services-location` dependency for `FusedLocationProviderClient`.
- No continuous location tracking — just a one-time location fix, puck display, and camera centering on map open.

## Capabilities

### New Capabilities
- `map-user-location`: When location permission is granted and location services are enabled, the map automatically centers on the user's current location and displays a location puck when the map fragment is shown, unless the user is actively interacting with the map (the puck appears regardless; only the camera centering is suppressed).

### Modified Capabilities

## Impact

- New dependency: `com.google.android.gms:play-services-location`.
- `MapFragment`: add runtime permission request flow, location services check, and one-shot location retrieval.
- New file `UserLocationProvider.kt` in `fr.vpm.changingtables.location` encapsulating permission check, location services check, and location retrieval via `FusedLocationProviderClient`.
- `MapManager`: add method to animate camera to a given point, a method to show/hide the location puck, and a guard against user interaction.
- New drawable `ic_location_puck.xml` (vector) in `res/drawable/` for the location puck icon.
- No manifest changes needed — `ACCESS_FINE_LOCATION` is already declared.
