package fr.vpm.changingtables.ui.home

import android.content.Context
import android.graphics.Bitmap
import com.google.gson.Gson
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.annotation.AnnotationPlugin
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.addOnMapLongClickListener
import fr.vpm.changingtables.BusinessToPointAnnotation
import fr.vpm.changingtables.R
import fr.vpm.changingtables.models.Business
import fr.vpm.changingtables.ui.tools.MapUtils

class MapManager {

    private val mapUtils = MapUtils()

    val gson = Gson().newBuilder().create()

    private var pointAnnotationManager: PointAnnotationManager? = null

    internal var businessMarkerBitmap: Bitmap? = null

    fun loadMarkers(context: Context) {
        if (businessMarkerBitmap == null) {
            businessMarkerBitmap =
                mapUtils.bitmapFromDrawableRes(context, R.drawable.pin_ok_teal_32dp)
        }
    }

    fun setupPointAnnotationManager(
        annotationApi: AnnotationPlugin,
        showBusiness: (Business) -> Unit
    ) {
        pointAnnotationManager = annotationApi.createPointAnnotationManager()
        pointAnnotationManager?.addClickListener { annotation ->
            val annotationData = annotation.getData()
            if (annotationData != null) {
                val business = gson.fromJson(annotationData, Business::class.java)
                showBusiness(business)
            }
            annotationData != null
        }
    }

    fun onDestroyView() {
        pointAnnotationManager = null
        businessMarkerBitmap = null
    }

    fun showBusinesses(context: Context, businesses: List<Business>?) {
        loadMarkers(context)
        if (businesses != null) {
            val allBusinessAnnotationOptions = businesses.map { business ->
                BusinessToPointAnnotation().toPointAnnotation(business, businessMarkerBitmap)
            }
            pointAnnotationManager?.create(allBusinessAnnotationOptions)
        }
    }

    fun setupAddingBusiness(mapView: MapView, showNewBusiness: () -> Unit) {
        mapView.isLongClickable = true
        mapView.mapboxMap.addOnMapLongClickListener {
            if (mapView.mapboxMap.cameraState.zoom > 13.0) {
                showNewBusiness()
                true
            } else {
                false
            }
        }
    }


}