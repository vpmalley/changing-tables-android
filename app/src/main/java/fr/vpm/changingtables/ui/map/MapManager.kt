package fr.vpm.changingtables.ui.map

import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import com.google.gson.Gson
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.annotation.AnnotationPlugin
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.gestures.addOnMapLongClickListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.animation.easeTo
import fr.vpm.changingtables.BusinessToPointAnnotation
import fr.vpm.changingtables.R
import fr.vpm.changingtables.models.Business
import fr.vpm.changingtables.ui.tools.MapUtils

class MapManager {

    private val mapUtils = MapUtils()

    val gson = Gson().newBuilder().create()

    private var businessAnnotationManager: PointAnnotationManager? = null

    // Separate manager so that re-rendering business markers never removes the new business pin
    private var newBusinessAnnotationManager: PointAnnotationManager? = null

    private var newBusinessAnnotation: PointAnnotation? = null

    // Separate manager for draggable new business marker (FAB-created)
    private var draggableBusinessAnnotationManager: PointAnnotationManager? = null

    private var draggableBusinessAnnotation: PointAnnotation? = null

    // Separate manager for the user location puck (sortKey between business markers and draggable marker)
    private var locationPuckAnnotationManager: PointAnnotationManager? = null

    private var locationPuckAnnotation: PointAnnotation? = null

    internal var locationPuckBitmap: Bitmap? = null

    internal var businessMarkerBitmap: Bitmap? = null

    // User interaction tracking
    private var isUserInteracting = false
    private val interactionHandler = Handler(Looper.getMainLooper())
    private val interactionResetRunnable = Runnable { isUserInteracting = false }
    private val interactionDebounceMs = 500L

    fun loadMarkers(context: Context) {
        if (businessMarkerBitmap == null) {
            businessMarkerBitmap =
                mapUtils.bitmapFromDrawableRes(context, R.drawable.ic_business_bakery)
        }
    }

    fun setupPointAnnotationManager(
        annotationApi: AnnotationPlugin,
        mapView: MapView,
        showBusiness: (Business) -> Unit
    ) {
        businessAnnotationManager = annotationApi.createPointAnnotationManager()
        // Created second so that the new business pin is drawn above business markers
        newBusinessAnnotationManager = annotationApi.createPointAnnotationManager()
        // Created third so that the location puck is drawn above business/new-business markers but below the draggable marker
        locationPuckAnnotationManager = annotationApi.createPointAnnotationManager()
        // Created fourth so that the draggable business pin is drawn above all other markers
        draggableBusinessAnnotationManager = annotationApi.createPointAnnotationManager()
        businessAnnotationManager?.addClickListener { annotation ->
            val annotationData = annotation.getData()
            if (annotationData != null) {
                val business = gson.fromJson(annotationData, Business::class.java)
                showBusiness(business)
            }
            annotationData != null
        }
        
        // Add map click listener to clear draggable marker when tapping on empty map
        mapView.mapboxMap.addOnMapClickListener { point ->
            // If there's a draggable marker and user taps on the map, clear it
            if (draggableBusinessAnnotation != null) {
                clearDraggableMarker()
                true
            } else {
                false
            }
        }
    }

    fun onDestroyView() {
        businessAnnotationManager = null
        newBusinessAnnotationManager = null
        newBusinessAnnotation = null
        locationPuckAnnotationManager = null
        locationPuckAnnotation = null
        locationPuckBitmap = null
        draggableBusinessAnnotationManager = null
        draggableBusinessAnnotation = null
        businessMarkerBitmap = null
        isDraggingMarker = false
        originalMarkerPoint = null
        interactionHandler.removeCallbacks(interactionResetRunnable)
        isUserInteracting = false
    }

    fun showBusinesses(context: Context, businesses: List<Business>?) {
        loadMarkers(context)
        businessAnnotationManager?.deleteAll()
        if (businesses != null) {
            val allBusinessAnnotationOptions = businesses.map { business ->
                BusinessToPointAnnotation().toPointAnnotation(business, businessMarkerBitmap)
            }
            businessAnnotationManager?.create(allBusinessAnnotationOptions)
        }
    }

    fun showNewBusinessMarker(context: Context, point: Point) {
        loadMarkers(context)
        val pointAnnotationOptions = PointAnnotationOptions()
            .withPoint(point)
            .withIconSize(1.0)
            .withSymbolSortKey(5.0)
        businessMarkerBitmap?.let { pointAnnotationOptions.withIconImage(it) }
        newBusinessAnnotation = newBusinessAnnotationManager?.create(pointAnnotationOptions)
    }

    fun clearNewBusinessMarker() {
        newBusinessAnnotation?.let {
            newBusinessAnnotationManager?.delete(it)
        }
        newBusinessAnnotation = null
    }

    /**
     * State for tracking drag-and-drop interaction
     */
    private var isDraggingMarker = false
    private var dragStartScreenX = 0f
    private var dragStartScreenY = 0f
    private var originalMarkerPoint: Point? = null

    /**
     * Creates a draggable marker at the center of the map for FAB-based business creation.
     * When the user finishes dragging (releases), the onDrop callback is automatically triggered.
     */
    fun createDraggableMarker(
        context: Context,
        mapView: MapView,
        initialPoint: Point? = null,
        onDrop: (point: Point) -> Unit,
        onCancel: () -> Unit
    ) {
        // Clear any existing draggable marker
        clearDraggableMarker()

        loadMarkers(context)

        // Place the marker at the given point (e.g. the user's location),
        // or at the current camera center when no point is provided
        val centerPoint = initialPoint ?: run {
            val cameraState = mapView.mapboxMap.cameraState
            Point.fromLngLat(cameraState.center.longitude(), cameraState.center.latitude())
        }
        
        // Store original position to detect if marker was moved
        originalMarkerPoint = centerPoint
        
        // Create draggable annotation options with built-in drag support
        val pointAnnotationOptions = PointAnnotationOptions()
            .withPoint(centerPoint)
            .withIconSize(1.2)
            .withSymbolSortKey(10.0)
            .withDraggable(true)
        businessMarkerBitmap?.let { pointAnnotationOptions.withIconImage(it) }
        
        // Create the draggable annotation
        draggableBusinessAnnotation = draggableBusinessAnnotationManager?.create(pointAnnotationOptions)
        
        // Add drag listener to automatically show form when drag is finished
        draggableBusinessAnnotation?.let { annotation ->
            draggableBusinessAnnotationManager?.addDragListener(
                object : com.mapbox.maps.plugin.annotation.generated.OnPointAnnotationDragListener {
                    override fun onAnnotationDragStarted(annotation: com.mapbox.maps.plugin.annotation.Annotation<*>) {
                        isDraggingMarker = true
                    }
                    
                    override fun onAnnotationDrag(annotation: com.mapbox.maps.plugin.annotation.Annotation<*>) {
                        // Track drag movement
                        isDraggingMarker = true
                    }
                    
                    override fun onAnnotationDragFinished(annotation: com.mapbox.maps.plugin.annotation.Annotation<*>) {
                        isDraggingMarker = false
                        // annotation is a PointAnnotation (guaranteed by listener type)
                        val pointAnnotation = annotation as PointAnnotation
                        // Check if the marker is still on screen
                        val screenCoord = mapView.mapboxMap.pixelForCoordinate(pointAnnotation.point)
                        if (screenCoord != null && 
                            screenCoord.x >= 0 && screenCoord.x <= mapView.width.toDouble() &&
                            screenCoord.y >= 0 && screenCoord.y <= mapView.height.toDouble()) {
                            // Dropped on screen - show form
                            onDrop(pointAnnotation.point)
                        } else {
                            // Dragged off screen - cancel
                            clearDraggableMarker()
                            onCancel()
                        }
                    }
                }
            )
        }
    }

    /**
     * Clears the draggable marker from the map.
     */
    fun clearDraggableMarker() {
        draggableBusinessAnnotation?.let {
            draggableBusinessAnnotationManager?.delete(it)
        }
        draggableBusinessAnnotation = null
        isDraggingMarker = false
    }

    /**
     * Returns the current position of the draggable marker, or null if not present.
     */
    fun getDraggableMarkerPosition(): Point? {
        return draggableBusinessAnnotation?.point
    }

    fun setupAddingBusiness(mapView: MapView, showNewBusiness: (point: Point) -> Unit) {
        mapView.isLongClickable = true
        mapView.mapboxMap.addOnMapLongClickListener { point ->
            if (mapView.mapboxMap.cameraState.zoom > 13.0) {
                // Clear any existing draggable marker before creating a new business
                clearDraggableMarker()
                showNewBusiness(point)
                true
            } else {
                false
            }
        }
    }

    /**
     * Registers gesture listeners to track whether the user is actively interacting with the map.
     * When any gesture fires, isUserInteracting is set to true and a debounce timer resets it
     * to false after [interactionDebounceMs] of no gesture activity.
     */
    fun setupInteractionTracking(mapView: MapView) {
        val markInteracting = {
            isUserInteracting = true
            interactionHandler.removeCallbacks(interactionResetRunnable)
            interactionHandler.postDelayed(interactionResetRunnable, interactionDebounceMs)
        }
        mapView.gestures.addOnMoveListener(
            object : com.mapbox.maps.plugin.gestures.OnMoveListener {
                override fun onMoveBegin(detector: com.mapbox.android.gestures.MoveGestureDetector) { markInteracting() }
                override fun onMove(detector: com.mapbox.android.gestures.MoveGestureDetector): Boolean { markInteracting(); return false }
                override fun onMoveEnd(detector: com.mapbox.android.gestures.MoveGestureDetector) { markInteracting() }
            }
        )
        mapView.gestures.addOnScaleListener(
            object : com.mapbox.maps.plugin.gestures.OnScaleListener {
                override fun onScaleBegin(detector: com.mapbox.android.gestures.StandardScaleGestureDetector) { markInteracting() }
                override fun onScale(detector: com.mapbox.android.gestures.StandardScaleGestureDetector) { markInteracting() }
                override fun onScaleEnd(detector: com.mapbox.android.gestures.StandardScaleGestureDetector) { markInteracting() }
            }
        )
        mapView.gestures.addOnRotateListener(
            object : com.mapbox.maps.plugin.gestures.OnRotateListener {
                override fun onRotateBegin(detector: com.mapbox.android.gestures.RotateGestureDetector) { markInteracting() }
                override fun onRotate(detector: com.mapbox.android.gestures.RotateGestureDetector) { markInteracting() }
                override fun onRotateEnd(detector: com.mapbox.android.gestures.RotateGestureDetector) { markInteracting() }
            }
        )
    }

    /**
     * Returns true if the user is currently interacting with the map (panning, zooming, rotating).
     */
    fun isUserInteracting(): Boolean = isUserInteracting || isDraggingMarker

    /**
     * Animates the map camera to center on the given coordinates at a street-level zoom.
     */
    fun centerOnLocation(mapView: MapView, latitude: Double, longitude: Double) {
        val cameraOptions = CameraOptions.Builder()
            .center(Point.fromLngLat(longitude, latitude))
            .zoom(15.0)
            .build()
        mapView.mapboxMap.easeTo(cameraOptions)
    }

    private fun loadLocationPuckBitmap(context: Context) {
        if (locationPuckBitmap == null) {
            locationPuckBitmap = mapUtils.bitmapFromDrawableRes(context, R.drawable.ic_location_puck)
        }
    }

    /**
     * Shows or updates the location puck at the given coordinates.
     */
    fun showLocationPuck(context: Context, latitude: Double, longitude: Double) {
        loadLocationPuckBitmap(context)
        val point = Point.fromLngLat(longitude, latitude)
        if (locationPuckAnnotation != null) {
            locationPuckAnnotation?.point = point
            locationPuckAnnotationManager?.update(locationPuckAnnotation!!)
        } else {
            val options = PointAnnotationOptions()
                .withPoint(point)
                .withIconSize(1.0)
                .withSymbolSortKey(7.0)
            locationPuckBitmap?.let { options.withIconImage(it) }
            locationPuckAnnotation = locationPuckAnnotationManager?.create(options)
        }
    }

    /**
     * Hides the location puck.
     */
    fun hideLocationPuck() {
        locationPuckAnnotation?.let {
            locationPuckAnnotationManager?.delete(it)
        }
        locationPuckAnnotation = null
    }

}