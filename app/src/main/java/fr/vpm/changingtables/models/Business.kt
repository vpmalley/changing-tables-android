package fr.vpm.changingtables.models

class Business {
    var name: String? = null
    var type: String? = null // enum
    var description: String? = null
    var longitude: Double = 0.0
    var latitude: Double = 0.0
    var hasChangingTable: Boolean = false
    var rating: Int = -1

    val ratingAsFloat: Float
        get() = if (rating < 0) {
            0f
        } else {
            rating.toFloat()
        }
    var photoUrls: List<String> = emptyList()

}