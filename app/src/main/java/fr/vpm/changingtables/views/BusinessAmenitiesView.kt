package fr.vpm.changingtables.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.google.android.material.chip.Chip
import fr.vpm.changingtables.R
import fr.vpm.changingtables.databinding.ViewBusinessAmenitiesBinding
import fr.vpm.changingtables.models.AmenityDisplay

/**
 * Custom view that displays a business type icon on the left and amenity chips on the right.
 * The height is determined by the ChipGroup, and the ImageView is square with the same height.
 * 
 * @attr ref fr.vpm.changingtables.R.styleable#BusinessAmenitiesView_businessType
 */
class BusinessAmenitiesView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding: ViewBusinessAmenitiesBinding

    init {
        val inflater = LayoutInflater.from(context)
        binding = ViewBusinessAmenitiesBinding.inflate(inflater, this, true)
    }

    /**
     * Sets the business type and updates the icon accordingly.
     * 
     * @param type The business type (e.g., "cafe", "restaurant", "activity")
     */
    fun setBusinessType(type: String?) {
        val iconRes = when (type?.lowercase()) {
            "cafe", "coffee" -> R.drawable.ic_type_cafe
            "restaurant" -> R.drawable.ic_type_restaurant
            "activity" -> R.drawable.ic_type_activity
            else -> R.drawable.ic_type_cafe
        }
        binding.businessTypeIcon.setImageResource(iconRes)
    }

    /**
     * Sets the list of amenities to display as chips.
     * Each chip will have the text and color specified in the AmenityDisplay.
     * Chips are not clickable.
     * 
     * @param amenities List of AmenityDisplay objects containing text resource IDs and color resource IDs
     */
    fun setAmenities(amenities: List<AmenityDisplay>) {
        binding.amenitiesChipGroup.removeAllViews()

        amenities.forEach { amenity ->
            val chip = Chip(context).apply {
                // Set chip text from resource
                setText(amenity.textResId)

                // Set chip background color
                val color = ContextCompat.getColor(context, amenity.colorResId)
                setChipBackgroundColorResource(amenity.colorResId)
                chipStrokeWidth = 0f

                // Make chip non-clickable
                isClickable = false
                isCheckable = false
                isFocusable = false

                // Set spacing
                chipStartPadding = 0f
                chipEndPadding = 0f
            }
            binding.amenitiesChipGroup.addView(chip)
        }
    }
}
