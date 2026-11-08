package fr.vpm.changingtables.models

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Cleanliness {
    @SerialName("spotless") SPOTLESS,
    @SerialName("ok") OK,
    @SerialName("needs_cleaning") NEEDS_CLEANING
}

@Serializable
enum class HighChairAvailability {
    @SerialName("multiple") MULTIPLE,
    @SerialName("single") SINGLE,
    @SerialName("none") NONE
}

@Serializable
@Entity(tableName = "businesses")
class Business {
    @PrimaryKey(autoGenerate = true)
    @JvmField
    var id: Int = 0

    @JvmField
    var name: String? = null

    /**
     * Enum that can be one of the following: coffee, restaurant, activity
     */
    @JvmField
    var type: String? = null

    @JvmField
    var description: String? = null

    @JvmField
    var longitude: Double = 0.0

    @JvmField
    var latitude: Double = 0.0

    /**
     * Enum that can be one of the following: yes, no, out_of_service
     */
    @JvmField
    var hasChangingTable: String? = null

    /**
     * Enum that can be one of the following: unisex, male, female, accessible, other_room
     */
    @JvmField
    var changingTableLocation: String? = null

    /**
     * Null when the user did not answer the diaper pail question.
     */
    @JvmField
    var hasDiaperPail: Boolean? = null

    /**
     * Null when the user did not answer the high chair question.
     */
    @JvmField
    var highChairs: HighChairAvailability? = null

    /**
     * Null when the user did not answer the cleanliness question.
     */
    @JvmField
    var cleanliness: Cleanliness? = null

    @JvmField
    var rating: Int = -1

    @JvmField
    var area: String? = null

    @JvmField
    var photoUrls: List<String> = emptyList()

    @JvmField
    var savedOnDevice: Long? = null

    @get:Ignore
    val ratingAsFloat: Float
        get() = if (rating < 0) 0f else rating.toFloat()

    override fun toString(): String {
        return "Business(id=$id, name=$name, type=$type, description=$description, longitude=$longitude, latitude=$latitude, hasChangingTable=$hasChangingTable, changingTableLocation=$changingTableLocation, hasDiaperPail=$hasDiaperPail, cleanliness=$cleanliness, highChairs=$highChairs, rating=$rating, area=$area, photoUrls=$photoUrls, savedOnDevice=$savedOnDevice, ratingAsFloat=$ratingAsFloat)"
    }

}
