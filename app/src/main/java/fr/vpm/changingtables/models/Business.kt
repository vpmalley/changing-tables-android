package fr.vpm.changingtables.models

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "businesses")
class Business {
    @PrimaryKey(autoGenerate = true)
    @JvmField
    var id: Int = 0

    @JvmField
    var name: String? = null

    @JvmField
    var type: String? = null

    @JvmField
    var description: String? = null

    @JvmField
    var longitude: Double = 0.0

    @JvmField
    var latitude: Double = 0.0

    @JvmField
    var hasChangingTable: String? = null

    @JvmField
    var changingTableLocation: String? = null

    @JvmField
    var rating: Int = -1

    @JvmField
    var area: String? = null

    @JvmField
    var photoUrls: List<String> = emptyList()

    @get:Ignore
    val ratingAsFloat: Float
        get() = if (rating < 0) 0f else rating.toFloat()
}
