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
        mapManager.clearNewBusinessMarker()
        binding.businessDetailsLayout.visibility = View.VISIBLE
        val bottomSheetLayout: ConstraintLayout = binding.businessDetailsLayout
        val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        binding.businessTitle.text = business?.name

        val typeIcon = when (business?.type) {
            "coffee" -> R.drawable.ic_coffee_24
            "restaurant" -> R.drawable.ic_restaurant_24
            "activity" -> R.drawable.ic_local_activity_24
            else -> R.drawable.ic_coffee_24
        }
        val typeText = when (business?.type) {
            "coffee" -> context.getString(R.string.coffee)
            "restaurant" -> context.getString(R.string.restaurant)
            "activity" -> context.getString(R.string.activity)
            else -> context.getString(R.string.coffee)
        }
        binding.businessDescription.setCompoundDrawablesWithIntrinsicBounds(typeIcon, 0, 0, 0)
        binding.businessDescription.text = typeText

        binding.businessRating.visibility = View.GONE
        binding.businessRating.rating = business?.ratingAsFloat ?: 0f
        binding.businessRating.numStars = 5

        business?.let {
            binding.amenitiesCard.visibility = View.VISIBLE
            displayAmenity(binding.amenityChangingTable, it.hasChangingTable != "no")
            displayAmenity(binding.amenityClean, it.isClean)
            displayAmenity(binding.amenityDiaperPail, it.hasDiaperPail)
        }
    }

    private fun displayAmenity(
        view: com.google.android.material.textview.MaterialTextView?,
        isAvailable: Boolean
    ) {
        view?.let {
            it.visibility = View.VISIBLE
            val iconRes = if (isAvailable) R.drawable.ic_check_24 else R.drawable.ic_close_24
            it.setCompoundDrawablesWithIntrinsicBounds(iconRes, 0, 0, 0)
            val colorRes =
                if (isAvailable) R.color.green else R.color.onContainerOrange
            TextViewCompat.setCompoundDrawableTintList(
                it,
                ColorStateList.valueOf(ContextCompat.getColor(context, colorRes))
            )
        }
    }

    fun hide() {
        val behavior = BottomSheetBehavior.from(binding.businessDetailsLayout)
        behavior.state = BottomSheetBehavior.STATE_HIDDEN
    }
}
