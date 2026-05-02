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

    @JvmField
    var hasDiaperPail: Boolean = false

    @JvmField
    var isClean: Boolean = false

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
        return "Business(id=$id, name=$name, type=$type, description=$description, longitude=$longitude, latitude=$latitude, hasChangingTable=$hasChangingTable, changingTableLocation=$changingTableLocation, hasDiaperPail=$hasDiaperPail, isClean=$isClean, rating=$rating, area=$area, photoUrls=$photoUrls, savedOnDevice=$savedOnDevice, ratingAsFloat=$ratingAsFloat)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Business) return false

        if (id != other.id) return false
        if (longitude != other.longitude) return false
        if (latitude != other.latitude) return false
        if (hasDiaperPail != other.hasDiaperPail) return false
        if (isClean != other.isClean) return false
        if (rating != other.rating) return false
        if (savedOnDevice != other.savedOnDevice) return false
        if (name != other.name) return false
        if (type != other.type) return false
        if (description != other.description) return false
        if (hasChangingTable != other.hasChangingTable) return false
        if (changingTableLocation != other.changingTableLocation) return false
        if (area != other.area) return false
        if (photoUrls != other.photoUrls) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + longitude.hashCode()
        result = 31 * result + latitude.hashCode()
        result = 31 * result + hasDiaperPail.hashCode()
        result = 31 * result + isClean.hashCode()
        result = 31 * result + rating
        result = 31 * result + (savedOnDevice?.hashCode() ?: 0)
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + (type?.hashCode() ?: 0)
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + (hasChangingTable?.hashCode() ?: 0)
        result = 31 * result + (changingTableLocation?.hashCode() ?: 0)
        result = 31 * result + (area?.hashCode() ?: 0)
        result = 31 * result + photoUrls.hashCode()
        return result
    }
}
