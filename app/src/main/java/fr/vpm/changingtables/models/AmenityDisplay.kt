package fr.vpm.changingtables.models

import fr.vpm.changingtables.R

/**
 * Data class representing an amenity to be displayed as a chip.
 * 
 * @param textResId The string resource ID for the amenity text
 * @param colorResId The color resource ID for the chip (green, red, or grey)
 */
data class AmenityDisplay(
    val textResId: Int,
    val colorResId: Int
) {
    companion object {
        fun fromAmenity(
            amenityTextResId: Int,
            hasAmenity: Boolean,
            isImportant: Boolean
        ): AmenityDisplay {
            return if (hasAmenity) {
                AmenityDisplay(amenityTextResId, R.color.green)
            } else if (isImportant) {
                AmenityDisplay(amenityTextResId, android.R.color.holo_red_dark)
            } else {
                AmenityDisplay(amenityTextResId, android.R.color.darker_gray)
            }
        }

        /**
         * Maps a cleanliness value to its chip. Returns null when no cleanliness was
         * recorded, in which case no cleanliness chip is shown.
         */
        fun fromCleanliness(cleanliness: Cleanliness?): AmenityDisplay? {
            return when (cleanliness) {
                Cleanliness.SPOTLESS -> AmenityDisplay(R.string.amenity_clean, R.color.green)
                Cleanliness.OK -> AmenityDisplay(R.string.amenity_clean_enough, R.color.clean_enough_yellow)
                Cleanliness.NEEDS_CLEANING -> AmenityDisplay(R.string.amenity_dirty, android.R.color.holo_red_dark)
                null -> null
            }
        }
    }
}
