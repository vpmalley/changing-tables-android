package fr.vpm.changingtables.ui.map

import android.content.Context
import android.content.res.ColorStateList
import android.util.TypedValue
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import fr.vpm.changingtables.R
import fr.vpm.changingtables.databinding.BusinessDetailsBottomSheetBinding
import fr.vpm.changingtables.models.Business

class BusinessDetailsBottomSheet(
    private val binding: BusinessDetailsBottomSheetBinding,
    private val mapManager: MapManager
) {
    private val context: Context get() = binding.root.context
    private lateinit var backgroundDrawable: MaterialShapeDrawable
    private var cornerRadius: Float = 0f

    fun setupBottomSheet() {
        val bottomSheetLayout = binding.businessDetailsLayout
        val behavior = BottomSheetBehavior.from(bottomSheetLayout)
        val initialPeekHeight = behavior.peekHeight

        cornerRadius = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            28f,
            context.resources.displayMetrics
        )

        val shapeAppearanceModel = ShapeAppearanceModel.builder()
            .setTopLeftCorner(CornerFamily.ROUNDED, cornerRadius)
            .setTopRightCorner(CornerFamily.ROUNDED, cornerRadius)
            .build()

        val typedValue = TypedValue()
        context.theme.resolveAttribute(
            com.google.android.material.R.attr.colorSurface,
            typedValue,
            true
        )
        val colorSurface = typedValue.data

        backgroundDrawable = MaterialShapeDrawable(shapeAppearanceModel).apply {
            fillColor = ColorStateList.valueOf(colorSurface)
        }
        bottomSheetLayout.background = backgroundDrawable

        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    mapManager.clearNewBusinessMarker()
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                val parentHeight = bottomSheet.parent.let { (it as View).height }.toFloat()
                if (parentHeight > 0) {
                    val threshold = cornerRadius * 2
                    val interpolation = (bottomSheet.top.toFloat() / threshold).coerceIn(0f, 1f)
                    backgroundDrawable.interpolation = interpolation
                }

                if (behavior.state == BottomSheetBehavior.STATE_DRAGGING) {
                    val currentHeight = parentHeight.toInt() - bottomSheet.top
                    behavior.peekHeight = currentHeight.coerceAtLeast(initialPeekHeight)
                }
            }
        })
    }

    fun showBusiness(business: Business?) {
        if (business == null) return
        mapManager.clearNewBusinessMarker()
        binding.businessDetailsLayout.visibility = View.VISIBLE
        val behavior = BottomSheetBehavior.from(binding.businessDetailsLayout)
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED

        binding.businessTitle.text = business.name
        val (typeIcon, typeText) = when (business.type) {
            "coffee" -> Pair(R.drawable.ic_coffee_24, context.getString(R.string.coffee))
            "restaurant" -> Pair(R.drawable.ic_restaurant_24, context.getString(R.string.restaurant))
            "activity" -> Pair(R.drawable.ic_local_activity_24, context.getString(R.string.activity))
            else -> Pair(R.drawable.ic_coffee_24, business.description ?: context.getString(R.string.coffee))
        }
        binding.businessDescription.text = typeText
        binding.businessDescription.setCompoundDrawablesWithIntrinsicBounds(typeIcon, 0, 0, 0)

        val locationRes = when (business.changingTableLocation) {
            "unisex" -> R.string.changing_table_unisex_toilet
            "male" -> R.string.changing_table_male_toilet
            "female" -> R.string.changing_table_female_toilet
            "accessible" -> R.string.changing_table_accessible_toilet
            "other_room" -> R.string.changing_table_other_place
            else -> null
        }
        binding.changingTableDescription.visibility = if (locationRes != null) View.VISIBLE else View.GONE
        locationRes?.let { binding.changingTableDescription.setText(it) }

        binding.businessRating.visibility = if (business.rating > 0) View.VISIBLE else View.GONE
        binding.businessRating.rating = business.ratingAsFloat

        binding.amenitiesCard.visibility = View.VISIBLE
        displayAmenity(binding.amenityChangingTable, business.hasChangingTable, R.string.amenity_changing_table)
        displayAmenity(binding.amenityClean, if (business.isClean) "yes" else "no", R.string.amenity_clean)
        displayAmenity(binding.amenityDiaperPail, if (business.hasDiaperPail) "yes" else "no", R.string.amenity_diaper_pail)
    }

    private fun displayAmenity(view: com.google.android.material.textview.MaterialTextView?, status: String?, baseTextRes: Int) {
        view?.let {
            it.visibility = View.VISIBLE
            val (iconRes, colorRes, suffix) = when (status) {
                "yes" -> Triple(R.drawable.ic_check_24, R.color.green, "")
                "out_of_service" -> Triple(R.drawable.ic_close_24, R.color.onContainerOrange, " (${context.getString(R.string.changing_table_out_of_service)})")
                else -> Triple(R.drawable.ic_close_24, R.color.onContainerOrange, "")
            }
            it.text = context.getString(R.string.amenity_status_template, context.getString(baseTextRes), suffix)
            it.setCompoundDrawablesWithIntrinsicBounds(iconRes, 0, 0, 0)
            TextViewCompat.setCompoundDrawableTintList(it, ColorStateList.valueOf(ContextCompat.getColor(context, colorRes)))
        }
    }

    fun hide() {
        val behavior = BottomSheetBehavior.from(binding.businessDetailsLayout)
        behavior.state = BottomSheetBehavior.STATE_HIDDEN
    }
}
