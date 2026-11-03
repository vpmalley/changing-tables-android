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
                AmenityDisplay(amenityTextResId, R.color.palette_red)
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
                Cleanliness.NEEDS_CLEANING -> AmenityDisplay(R.string.amenity_dirty, R.color.palette_red)
                null -> null
            }
        }

        /**
         * Maps a diaper pail answer to its chip. Returns null when the question was
         * not answered, in which case no diaper pail chip is shown.
         */
        fun fromDiaperPail(hasDiaperPail: Boolean?): AmenityDisplay? {
            return when (hasDiaperPail) {
                true -> AmenityDisplay(R.string.amenity_diaper_pail, R.color.green)
                false -> AmenityDisplay(R.string.amenity_diaper_pail_missing, android.R.color.darker_gray)
                null -> null
            }
        }
    }
}
