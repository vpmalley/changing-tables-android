package fr.vpm.changingtables

import android.graphics.Bitmap
import com.google.gson.Gson
import com.mapbox.geojson.Point
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import fr.vpm.changingtables.models.Business
import fr.vpm.changingtables.ui.home.MapManager

class BusinessToPointAnnotation() {

    private val gson = Gson().newBuilder().create()

    fun toPointAnnotation(business: Business, iconImage: Bitmap?): PointAnnotationOptions {
        val options = PointAnnotationOptions()
            .withPoint(Point.fromLngLat(business.longitude, business.latitude))
            .withIconSize(1.0)
            .withSymbolSortKey(5.0)
            .withData(gson.toJsonTree(business))
        iconImage?.let { options.withIconImage(it) }
        return options
    }
}