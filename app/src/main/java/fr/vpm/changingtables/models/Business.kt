package fr.vpm.changingtables.models

class Business {
    var name: String? = null
    var type: String? = null // enum
    var description: String? = null
    var longitude: Double = 0.0
    var latitude: Double = 0.0
    var hasChangingTable: Boolean = false

}