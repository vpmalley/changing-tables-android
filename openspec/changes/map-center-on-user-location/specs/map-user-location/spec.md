# Spec Delta

## Purpose

When location permission is granted and location services are enabled, the map automatically centers on the user's current location and displays a location puck when the map fragment is shown, unless the user is actively interacting with the map (in which case the puck still appears but the camera does not re-center).

## ADDED Requirements

### Requirement: Runtime location permission request
When the map fragment is shown for the first time in a session and the `ACCESS_FINE_LOCATION` permission has not been granted, the system SHALL request the permission from the user via the standard Android permission dialog. The system SHALL NOT request the permission again if the user has previously denied it (i.e., "Don't ask again" was selected), unless the user explicitly grants it from system settings.

#### Scenario: Permission not yet requested
- **WHEN** the map fragment is shown and `ACCESS_FINE_LOCATION` has never been requested
- **THEN** the system shows the Android permission dialog asking for location access

#### Scenario: Permission already granted
- **WHEN** the map fragment is shown and `ACCESS_FINE_LOCATION` is already granted
- **THEN** no permission dialog is shown and the system proceeds to retrieve the location

#### Scenario: Permission previously denied
- **WHEN** the map fragment is shown and the user previously denied the permission (without "Don't ask again")
- **THEN** the system shows the permission dialog again

#### Scenario: Permission permanently denied
- **WHEN** the map fragment is shown and the user previously denied the permission with "Don't ask again"
- **THEN** no permission dialog is shown, the system does NOT attempt to retrieve the location, and the map remains at its default position

### Requirement: Location services enabled check
Before attempting to retrieve the user's location, the system SHALL check whether location services are enabled on the device (via `LocationManager.isProviderEnabled` for GPS and/or network provider). If location services are disabled, the system SHALL NOT attempt to retrieve the location and the map SHALL remain at its default position.

#### Scenario: Location services enabled
- **WHEN** location permission is granted and location services are enabled
- **THEN** the system retrieves the user's current location

#### Scenario: Location services disabled
- **WHEN** location permission is granted but location services are disabled on the device
- **THEN** the system does NOT attempt to retrieve the location and the map remains at its default position

### Requirement: One-shot location retrieval
The system SHALL retrieve the user's location exactly once per map fragment appearance (i.e., when the map fragment becomes visible). The system SHALL NOT continuously track or update the user's location. The retrieval SHALL use a one-shot request with a reasonable timeout (e.g., 10 seconds).

#### Scenario: Location retrieved successfully
- **WHEN** permission is granted, location services are enabled, and the device successfully provides a location fix within the timeout
- **THEN** the map camera animates to center on the retrieved coordinates

#### Scenario: Location retrieval times out
- **WHEN** permission is granted and location services are enabled, but the device cannot provide a location fix within the timeout
- **THEN** the map remains at its default position and no error is shown to the user

#### Scenario: Location retrieved when returning to map
- **WHEN** the user navigates away from the map fragment and returns to it during the same app session
- **THEN** the system retrieves the location again and centers the map (unless the user is interacting with the map)

### Requirement: Center map on user location
When a location fix is successfully retrieved, the system SHALL animate the map camera to center on the user's coordinates. The zoom level SHALL be set to a street-level zoom (e.g., 14–16) so the user can see nearby businesses. The system SHALL also display a location puck at the user's coordinates.

#### Scenario: Map centers on user location with puck
- **WHEN** the user's location is retrieved and the user is NOT interacting with the map
- **THEN** the map camera smoothly animates to center on the user's latitude and longitude at a street-level zoom, and a location puck appears at the user's coordinates

#### Scenario: Zoom level is street-level
- **WHEN** the map centers on the user's location
- **THEN** the zoom level is between 14 and 16, showing individual streets and nearby businesses

### Requirement: Do not center while user is interacting with the map
The system SHALL NOT animate the camera to the user's location if the user is currently interacting with the map. "Interacting" means the user is actively panning, zooming, or dragging a marker. If the user is interacting when the location fix arrives, the system SHALL discard the camera animation but SHALL still display the location puck at the user's coordinates.

#### Scenario: User pans map before location arrives
- **WHEN** the location fix arrives while the user is panning the map
- **THEN** the camera does NOT animate to the user's location, but the location puck still appears at the user's coordinates

#### Scenario: User zooms map before location arrives
- **WHEN** the location fix arrives while the user is zooming the map
- **THEN** the camera does NOT animate to the user's location, but the location puck still appears at the user's coordinates

#### Scenario: User drags marker before location arrives
- **WHEN** the location fix arrives while the user is dragging a new-business marker
- **THEN** the camera does NOT animate to the user's location, but the location puck still appears at the user's coordinates

#### Scenario: User stops interacting after location arrives
- **WHEN** the location fix arrives and the user is NOT interacting with the map at that moment
- **THEN** the camera animates to center on the user's location and the location puck appears, even if the user had been interacting moments before

### Requirement: User location puck displayed
When a location fix is successfully retrieved, the system SHALL display a location puck (a circular indicator, typically a blue dot with a lighter accuracy ring) at the user's coordinates on the map. The puck SHALL be displayed regardless of whether the user is interacting with the map. The puck SHALL be removed when location permission is denied, location services are disabled, or location retrieval fails or times out.

#### Scenario: Puck appears at user location
- **WHEN** the user's location is retrieved successfully
- **THEN** a location puck appears at the user's latitude and longitude on the map

#### Scenario: Puck appears even when user is interacting
- **WHEN** the location fix arrives while the user is panning the map
- **THEN** the location puck still appears at the user's coordinates (only the camera centering is suppressed)

#### Scenario: Puck removed when permission denied
- **WHEN** location permission is denied or revoked
- **THEN** no location puck is displayed on the map

#### Scenario: Puck removed when location services disabled
- **WHEN** location services are disabled on the device
- **THEN** no location puck is displayed on the map

#### Scenario: Puck removed on location timeout
- **WHEN** location retrieval times out
- **THEN** no location puck is displayed on the map

#### Scenario: Puck updated on subsequent retrievals
- **WHEN** the user returns to the map fragment and a new location fix is retrieved
- **THEN** the puck moves to the new coordinates

### Requirement: No error shown to user
If permission is denied, location services are disabled, or location retrieval fails or times out, the system SHALL NOT show any error message, toast, or dialog to the user. The map simply remains at its default position.

#### Scenario: Permission denied, no error
- **WHEN** the user denies the location permission
- **THEN** no error message is shown and the map remains at its default position

#### Scenario: Location timeout, no error
- **WHEN** location retrieval times out
- **THEN** no error message is shown and the map remains at its default position

### Requirement: Feature only active on map fragment
The location retrieval, map centering, and location puck SHALL only occur when the map fragment is the currently visible fragment. The system SHALL NOT retrieve location, center the map, or show a puck when the user is on the Saved or Notifications fragments.

#### Scenario: User on Saved fragment
- **WHEN** the user is on the Saved fragment
- **THEN** no location retrieval occurs, the map is not centered, and no location puck is shown

#### Scenario: Switching to map fragment triggers centering and puck
- **WHEN** the user switches from the Saved fragment to the map fragment
- **THEN** the system retrieves the location, centers the map (if user not interacting), and displays the location puck (if permission granted and services enabled)
