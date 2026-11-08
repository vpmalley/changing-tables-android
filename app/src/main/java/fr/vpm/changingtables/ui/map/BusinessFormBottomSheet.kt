package fr.vpm.changingtables.ui.map

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.BundleCompat
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
import fr.vpm.changingtables.models.HighChairAvailability
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
    private var newBusinessPoint: Point? = null
    private lateinit var backgroundDrawable: MaterialShapeDrawable
    private var cornerRadius: Float = 0f

    companion object {
        private const val KEY_FORM_VISIBLE = "form_visible"
        private const val KEY_FORM_POINT_LAT = "form_point_lat"
        private const val KEY_FORM_POINT_LON = "form_point_lon"
        private const val KEY_FORM_TYPE = "form_type"
        private const val KEY_FORM_NAME = "form_name"
        private const val KEY_FORM_ANSWERS = "form_answers"
        private const val KEY_FORM_PAGE = "form_page"
    }

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

        binding.questionsViewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (::questionsAdapter.isInitialized) {
                    questionsAdapter.notifyDataSetChanged()
                }
            }
        })
    }

    fun showNewBusiness(point: Point) {
        newBusinessPoint = point
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
        val highChairQuestion = Question().apply {
            titleResId = R.string.high_chair_question
            options = listOf(
                Option("multiple", R.string.high_chair_multiple),
                Option("single", R.string.high_chair_single),
                Option("none", R.string.high_chair_none)
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
            highChairQuestion,
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

        // ViewPager2 keeps the inner RecyclerView's scroll anchor when the adapter is
        // swapped, so a form left on the last question would reopen on that question.
        // Reset to the first page while the old adapter is still attached (after the swap
        // currentItem is already 0 and setCurrentItem would be a no-op).
        if (binding.questionsViewPager.adapter != null) {
            binding.questionsViewPager.setCurrentItem(0, false)
        }
        binding.questionsViewPager.adapter = questionsAdapter
        binding.questionsViewPager.isUserInputEnabled = false

        binding.addBusinessButton.setOnClickListener {
            // The last question can be a free-text input, so the keyboard may be open
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.root.windowToken, 0)

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
                highChairs = answers[4]?.firstOrNull()
                    ?.let { key -> runCatching { HighChairAvailability.valueOf(key.uppercase()) }.getOrNull() }
                description = answers[5]?.firstOrNull()
            }
            businessViewModel.addBusiness(newBusiness)
            val businessName = newBusiness.name ?: "New business"
            Snackbar.make(binding.root, "$businessName added", Snackbar.LENGTH_LONG).show()

            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            mapManager.clearNewBusinessMarker()
        }
    }

    /**
     * Saves the in-progress business form so it survives configuration changes
     * (e.g. screen rotation). Writes nothing while the form is hidden.
     */
    fun onSaveInstanceState(outState: Bundle) {
        if (binding.businessFormLayout.visibility != View.VISIBLE) return
        val point = newBusinessPoint ?: return
        outState.putBoolean(KEY_FORM_VISIBLE, true)
        outState.putDouble(KEY_FORM_POINT_LAT, point.latitude())
        outState.putDouble(KEY_FORM_POINT_LON, point.longitude())
        outState.putString(KEY_FORM_TYPE, selectedType)
        outState.putString(KEY_FORM_NAME, binding.businessTitleNew.editText?.text?.toString())
        outState.putSerializable(KEY_FORM_ANSWERS, HashMap(answers))
        outState.putInt(KEY_FORM_PAGE, binding.questionsViewPager.currentItem)
    }

    /**
     * Reopens the form with the data the user had entered before the activity
     * was destroyed and recreated.
     */
    fun restoreInstanceState(savedInstanceState: Bundle?) {
        if (savedInstanceState == null || !savedInstanceState.getBoolean(KEY_FORM_VISIBLE)) return
        val latitude = savedInstanceState.getDouble(KEY_FORM_POINT_LAT)
        val longitude = savedInstanceState.getDouble(KEY_FORM_POINT_LON)

        showNewBusiness(Point.fromLngLat(longitude, latitude))

        selectedType = savedInstanceState.getString(KEY_FORM_TYPE)
        updateTypeSelectionUI()
        savedInstanceState.getString(KEY_FORM_NAME)?.let { name ->
            binding.businessTitleNew.editText?.setText(name)
        }
        @Suppress("UNCHECKED_CAST")
        val savedAnswers = BundleCompat.getSerializable(
            savedInstanceState, KEY_FORM_ANSWERS, HashMap::class.java
        ) as? HashMap<Int, List<String>>
        if (savedAnswers != null) {
            answers.putAll(savedAnswers)
            questionsAdapter.setSelectedAnswers(answers)
        }
        binding.addBusinessButton.isEnabled = answers.isNotEmpty()
        binding.questionsViewPager.setCurrentItem(savedInstanceState.getInt(KEY_FORM_PAGE, 0), false)
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
