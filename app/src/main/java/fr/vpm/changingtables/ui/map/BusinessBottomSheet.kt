package fr.vpm.changingtables.ui.map

import android.content.Context
import android.content.res.ColorStateList
import android.util.TypedValue
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.mapbox.geojson.Point
import fr.vpm.changingtables.R
import fr.vpm.changingtables.databinding.BusinessBottomSheetBinding
import fr.vpm.changingtables.models.Business
import fr.vpm.changingtables.models.Option
import fr.vpm.changingtables.models.Question
import fr.vpm.changingtables.viewmodels.BusinessViewModel

class BusinessBottomSheet(
    private val binding: BusinessBottomSheetBinding,
    private val businessViewModel: BusinessViewModel,
    private val mapManager: MapManager
) {
    private val context: Context get() = binding.root.context

    private lateinit var questionsAdapter: QuestionsAdapter
    private var questions: List<Question> = emptyList()
    private val answers = mutableMapOf<Int, List<String>>()

    fun setupBottomSheet() {
        val bottomSheetLayout = binding.businessLayout
        val behavior = BottomSheetBehavior.from(bottomSheetLayout)
        val initialPeekHeight = behavior.peekHeight

        val cornerRadius = TypedValue.applyDimension(
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

        val backgroundDrawable = MaterialShapeDrawable(shapeAppearanceModel).apply {
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
                    // Interpolation: 0 at the top (square), 1 when far from top (rounded)
                    // Animation happens when the sheet is within 2 * cornerRadius from the top
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
        binding.businessLayout.visibility = View.VISIBLE
        val bottomSheetLayout: ConstraintLayout = binding.businessLayout
        val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        binding.businessAddPrompt.visibility = View.GONE
        binding.businessTitleNew.visibility = View.INVISIBLE
        binding.businessTitle.visibility = View.VISIBLE
        binding.addBusinessButton.visibility = View.GONE
        binding.addBusinessButton.isEnabled = false
        binding.questionsViewPager.visibility = View.GONE

        binding.businessTitle.text = business?.name
        binding.businessDescription.text = business?.description ?: "Coffee shop"
        binding.businessRating.visibility = View.GONE
        binding.businessRating.rating = business?.ratingAsFloat ?: 0f
        binding.businessRating.numStars = 5

        business?.let {
            displayAmenity(binding.amenityChangingTable, it.hasChangingTable != "no")
            displayAmenity(binding.amenityClean, it.isClean)
            displayAmenity(binding.amenityDiaperPail, it.hasDiaperPail)
        }

//        if (business?.hasChangingTable == "Yes") {
//            binding.changingTableDescription.let {
//                it.visibility = View.VISIBLE
//                it.text = "There is a changing table here"
//                TextViewCompat.setCompoundDrawableTintList(
//                    it, ColorStateList.valueOf(
//                        ContextCompat.getColor(context, R.color.green)
//                    )
//                )
//            }
//        } else if (business?.hasChangingTable == "OutOfService") {
//            binding.changingTableDescription.let {
//                it.visibility = View.VISIBLE
//                it.text = "Changing table is out of service"
//                TextViewCompat.setCompoundDrawableTintList(
//                    it, ColorStateList.valueOf(
//                        ContextCompat.getColor(context, R.color.purple_500)
//                    )
//                )
//            }
//        } else {
//            binding.changingTableDescription.visibility = View.VISIBLE
//            binding.changingTableDescription.text = "No changing table"
//            binding.changingTableDescription.let {
//                TextViewCompat.setCompoundDrawableTintList(
//                    it, ColorStateList.valueOf(
//                        ContextCompat.getColor(context, R.color.black)
//                    )
//                )
//            }
//        }
    }

    fun showNewBusiness(point: Point) {
        mapManager.clearNewBusinessMarker()
        mapManager.showNewBusinessMarker(context, point)
        binding.businessLayout.visibility = View.VISIBLE
        val bottomSheetLayout: ConstraintLayout = binding.businessLayout
        val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        binding.businessAddPrompt.visibility = View.VISIBLE
        binding.businessTitleNew.visibility = View.VISIBLE
        binding.businessTitle.visibility = View.GONE
        binding.changingTableDescription.visibility = View.GONE
        binding.businessRating.visibility = View.GONE
        binding.amenityChangingTable.visibility = View.GONE
        binding.amenityClean.visibility = View.GONE
        binding.amenityDiaperPail.visibility = View.GONE
        binding.addBusinessButton.visibility = View.VISIBLE
        binding.addBusinessButton.isEnabled = false

        val changingTableQuestion = Question().apply {
            titleResId = R.string.changing_table_question
            options = listOf(
                Option("yes", R.string.all_yes),
                Option("no", R.string.all_no),
                Option("out_of_service", R.string.changing_table_out_of_service)
            )
            singleChoice = true
            chipStyleRes = com.google.android.material.R.style.Widget_Material3_Chip_Suggestion
        }
        val changingTableLocationQuestion = Question().apply {
            titleResId = R.string.changing_table_location_question
            options = listOf(
                Option("unisex", R.string.changing_table_unisex_toilet),
                Option("male", R.string.changing_table_male_toilet),
                Option("female", R.string.changing_table_female_toilet),
                Option("accessible", R.string.changing_table_accessible_toilet),
                Option("other_room", R.string.changing_table_other_place)
            )
            singleChoice = false
            chipStyleRes = com.google.android.material.R.style.Widget_Material3_Chip_Filter
        }
        questions = listOf(changingTableQuestion, changingTableLocationQuestion)
        answers.clear()

        questionsAdapter = QuestionsAdapter(
            questions = questions,
            onChipSelected = { position, _, selectedTexts, tags ->
                answers[position] = selectedTexts
                binding.addBusinessButton.isEnabled = true
                if (position == 0) {
                    if (tags.any { it == "yes" }) {
                        binding.questionsViewPager.currentItem = 1
                    } else {
                        // If 'no' or 'out of service', maybe we don't need the location question.
                        // But for now, we follow the user's request to have them in ViewPager2.
                        // If we want to skip it, we could just not move or move and it would be empty.
                    }
                }
            },
            onBackClick = { position ->
                if (position > 0) {
                    binding.questionsViewPager.currentItem = position - 1
                }
            },
            onSkipClick = { position ->
                if (position < questions.size - 1) {
                    binding.questionsViewPager.currentItem = position + 1
                }
            }
        )

        binding.questionsViewPager.adapter = questionsAdapter
        binding.questionsViewPager.isUserInputEnabled = false // Only navigate via buttons/selection
        binding.questionsViewPager.visibility = View.VISIBLE

        binding.addBusinessButton.setOnClickListener {
            val newBusiness = Business().apply {
                name = binding.businessTitleNew.editText?.text?.toString()
                longitude = point.longitude()
                latitude = point.latitude()
                rating = -1

                hasChangingTable = answers[0]?.firstOrNull()
                changingTableLocation = answers[1]?.firstOrNull()
            }
            businessViewModel.addBusiness(newBusiness)
            val businessName = newBusiness.name ?: "New business"
            Snackbar.make(binding.root, "$businessName added", Snackbar.LENGTH_LONG).show()

            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            mapManager.clearNewBusinessMarker()
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
                if (isAvailable) R.color.green else R.color.black // Or another color for 'No'
            TextViewCompat.setCompoundDrawableTintList(
                it,
                ColorStateList.valueOf(ContextCompat.getColor(context, colorRes))
            )
        }
    }
}
