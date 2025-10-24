package fr.vpm.changingtables

import com.google.gson.Gson
import com.mapbox.geojson.Point
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import fr.vpm.changingtables.models.Business

class BusinessToPointAnnotation {

    private val gson = Gson().newBuilder().create()

    fun toPointAnnotation(business: Business): PointAnnotationOptions {
        return PointAnnotationOptions()
            .withPoint(Point.fromLngLat(business.longitude, business.latitude))
            .withIconSize(1.0)
            .withSymbolSortKey(5.0)
            .withData(gson.toJsonTree(business))
    }
}