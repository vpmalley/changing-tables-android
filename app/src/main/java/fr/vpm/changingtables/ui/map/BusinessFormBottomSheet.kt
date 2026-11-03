package fr.vpm.changingtables.ui.map

import android.content.Context
import android.content.res.ColorStateList
import android.util.TypedValue
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.mapbox.geojson.Point
import fr.vpm.changingtables.R
import fr.vpm.changingtables.databinding.BusinessFormBottomSheetBinding
import fr.vpm.changingtables.models.Business
import fr.vpm.changingtables.models.Cleanliness
import fr.vpm.changingtables.models.Option
import fr.vpm.changingtables.models.Question
import fr.vpm.changingtables.viewmodels.BusinessViewModel

class BusinessFormBottomSheet(
    private val binding: BusinessFormBottomSheetBinding,
    private val businessViewModel: BusinessViewModel,
    private val mapManager: MapManager
) {
    private val context: Context get() = binding.root.context

    private lateinit var questionsAdapter: QuestionsAdapter
    private var questions: List<Question> = emptyList()
    private val answers = mutableMapOf<Int, List<String>>()

    private var selectedType: String? = null
    private lateinit var backgroundDrawable: MaterialShapeDrawable
    private var cornerRadius: Float = 0f

    fun setupBottomSheet() {
        val bottomSheetLayout = binding.businessFormLayout
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
                    mapManager.clearDraggableMarker()
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

    fun showNewBusiness(point: Point) {
        mapManager.clearNewBusinessMarker()
        mapManager.showNewBusinessMarker(context, point)
        binding.businessFormLayout.visibility = View.VISIBLE
        val bottomSheetLayout: ConstraintLayout = binding.businessFormLayout
        val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        binding.addBusinessButton.isEnabled = false

        selectedType = null
        updateTypeSelectionUI()

        // Clear the business name field
        binding.businessTitleNew.editText?.text?.clear()

        binding.typeCoffee.setOnClickListener {
            selectedType = "coffee"
            updateTypeSelectionUI()
        }
        binding.typeRestaurant.setOnClickListener {
            selectedType = "restaurant"
            updateTypeSelectionUI()
        }
        binding.typeActivity.setOnClickListener {
            selectedType = "activity"
            updateTypeSelectionUI()
        }

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
        val cleanlinessQuestion = Question().apply {
            titleResId = R.string.cleanliness_question
            options = listOf(
                Option("spotless", R.string.cleanliness_spotless),
                Option("ok", R.string.cleanliness_ok),
                Option("needs_cleaning", R.string.cleanliness_needs_cleaning)
            )
            singleChoice = true
            chipStyleRes = com.google.android.material.R.style.Widget_Material3_Chip_Suggestion
        }
        val diaperPailQuestion = Question().apply {
            titleResId = R.string.diaper_pail_question
            options = listOf(
                Option("yes", R.string.all_yes),
                Option("no", R.string.all_no)
            )
            singleChoice = true
            chipStyleRes = com.google.android.material.R.style.Widget_Material3_Chip_Suggestion
        }
        val extraNoteQuestion = Question().apply {
            titleResId = R.string.extra_note_question
            inputType = Question.InputType.FREE_TEXT
        }
        questions = listOf(
            changingTableQuestion,
            changingTableLocationQuestion,
            cleanlinessQuestion,
            diaperPailQuestion,
            extraNoteQuestion
        )
        answers.clear()

        questionsAdapter = QuestionsAdapter(
            questions = questions,
            onChipSelected = { position, _, _, tags ->
                answers[position] = tags.filterNotNull()
                binding.addBusinessButton.isEnabled = true
                // Auto-advance on any single-choice answer; multi-choice questions
                // (changing table location) stay until the user taps Skip/Back
                if (questions[position].singleChoice && position < questions.size - 1) {
                    binding.questionsViewPager.currentItem = position + 1
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
            },
            onTextChanged = { position, text ->
                if (text.isBlank()) {
                    answers.remove(position)
                } else {
                    answers[position] = listOf(text)
                }
            }
        )

        binding.questionsViewPager.adapter = questionsAdapter
        binding.questionsViewPager.isUserInputEnabled = false
        binding.questionsViewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                questionsAdapter.notifyDataSetChanged()
            }
        })

        binding.addBusinessButton.setOnClickListener {
            val newBusiness = Business().apply {
                name = binding.businessTitleNew.editText?.text?.toString()
                longitude = point.longitude()
                latitude = point.latitude()
                rating = -1
                type = selectedType

                hasChangingTable = answers[0]?.firstOrNull()
                changingTableLocation = answers[1]?.firstOrNull()
                cleanliness = answers[2]?.firstOrNull()
                    ?.let { key -> runCatching { Cleanliness.valueOf(key.uppercase()) }.getOrNull() }
                hasDiaperPail = when (answers[3]?.firstOrNull()) {
                    "yes" -> true
                    "no" -> false
                    else -> null
                }
                description = answers[4]?.firstOrNull()
            }
            businessViewModel.addBusiness(newBusiness)
            val businessName = newBusiness.name ?: "New business"
            Snackbar.make(binding.root, "$businessName added", Snackbar.LENGTH_LONG).show()

            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            mapManager.clearNewBusinessMarker()
        }
    }

    private fun updateTypeSelectionUI() {
        binding.typeCoffee.isSelected = selectedType == "coffee"
        binding.typeRestaurant.isSelected = selectedType == "restaurant"
        binding.typeActivity.isSelected = selectedType == "activity"
    }

    fun hide() {
        val behavior = BottomSheetBehavior.from(binding.businessFormLayout)
        behavior.state = BottomSheetBehavior.STATE_HIDDEN
    }
}
