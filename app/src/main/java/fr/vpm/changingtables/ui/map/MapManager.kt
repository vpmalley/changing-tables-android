package fr.vpm.changingtables.ui.map

import android.content.Context
import android.graphics.Bitmap
import com.google.gson.Gson
import com.mapbox.geojson.Point
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.annotation.AnnotationPlugin
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.gestures.addOnMapLongClickListener
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

    internal var businessMarkerBitmap: Bitmap? = null

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
        // Created third so that the draggable business pin is drawn above all other markers
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
        draggableBusinessAnnotationManager = null
        draggableBusinessAnnotation = null
        businessMarkerBitmap = null
        isDraggingMarker = false
        originalMarkerPoint = null
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
        onDrop: (point: Point) -> Unit,
        onCancel: () -> Unit
    ) {
        // Clear any existing draggable marker
        clearDraggableMarker()
        
        loadMarkers(context)
        
        // Get the current camera center
        val cameraState = mapView.mapboxMap.cameraState
        val centerPoint = Point.fromLngLat(cameraState.center.longitude(), cameraState.center.latitude())
        
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



}