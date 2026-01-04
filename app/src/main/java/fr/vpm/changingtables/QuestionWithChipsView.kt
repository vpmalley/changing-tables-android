package fr.vpm.changingtables

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.StyleRes
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.chip.Chip
import fr.vpm.changingtables.databinding.QuestionsWithChipsBinding
import fr.vpm.changingtables.models.Option
import fr.vpm.changingtables.models.Question

// We'll use a listener to send events back out to the Activity/Fragment
typealias OnChipSelectedListener = (selectedChipIds: List<Int>, selectedChipTexts: List<String>, selectedChipTags: List<String?>) -> Unit

class QuestionWithChipsView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    // Using View Binding for safe and easy view access
    private val binding: QuestionsWithChipsBinding

    private var selectionListener: OnChipSelectedListener? = null

    init {
        // Inflate the layout and attach it to this view
        val inflater = LayoutInflater.from(context)
        binding = QuestionsWithChipsBinding.inflate(inflater, this)

        // Read custom attributes from XML
        context.theme.obtainStyledAttributes(
            attrs, R.styleable.QuestionWithChipsView, 0, 0
        ).apply {
            try {
                // Set title from XML attribute
                val title = getString(R.styleable.QuestionWithChipsView_questionText)
                setTitle(title)

                // Set selection mode from XML attribute
                val isSingleSelection =
                    getBoolean(R.styleable.QuestionWithChipsView_questionChipsSingleSelection, true)
                binding.chipSelectionGroup.isSingleSelection = isSingleSelection
            } finally {
                recycle() // Always recycle TypedArray
            }
        }

        // Set up the internal listener to bubble up events
        binding.chipSelectionGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            val selectedTexts = checkedIds.map { group.findViewById<Chip>(it).text.toString() }
            val selectedTags = checkedIds.map { group.findViewById<Chip>(it).tag as? String }
            // Invoke the public listener
            selectionListener?.invoke(checkedIds, selectedTexts, selectedTags)
        }
    }

    /**
     * Sets the title text displayed above the chip group.
     */
    fun setTitle(titleResId: Int?) {
        if (titleResId != null) {
            binding.chipSelectionTitle.setText(titleResId)
            binding.chipSelectionTitle.visibility = VISIBLE
        } else {
            binding.chipSelectionTitle.visibility = GONE
        }
    }

    /**
     * Sets the title text displayed above the chip group.
     */
    fun setTitle(title: CharSequence?) {
        binding.chipSelectionTitle.text = title
        binding.chipSelectionTitle.visibility = if (title.isNullOrEmpty()) GONE else VISIBLE
    }

    /**
     * Populates the ChipGroup with a list of strings.
     * This will clear any existing chips.
     * @param chipOptions A list of strings, where each string is the text for a new chip.
     */
    fun setChips(
        chipOptions: List<Option>,
        @StyleRes chipStyleRes: Int = com.google.android.material.R.style.Widget_Material3_Chip_Suggestion,
        singleSelection: Boolean = true
    ) {
        binding.chipSelectionGroup.isSingleSelection = singleSelection
        binding.chipSelectionGroup.removeAllViews() // Clear old chips
        chipOptions.forEach { chipOption ->
            val chip = Chip(context, null, chipStyleRes).apply {
                setText(chipOption.textResId)
                isClickable = true
                isCheckable = true
                id = generateViewId() // Ensure each chip has a unique ID for the listener
                tag = chipOption.key
            }
            binding.chipSelectionGroup.addView(chip)
        }

    }

    /**
     * Registers a listener to be invoked when a chip selection changes.
     */
    fun setOnChipSelectedListener(listener: OnChipSelectedListener) {
        this.selectionListener = listener
    }

    /**
     * Returns a list of the text from the currently selected chips.
     */
    fun getSelectedChipTexts(): List<String> {
        return binding.chipSelectionGroup.checkedChipIds.map {
            binding.chipSelectionGroup.findViewById<Chip>(it).text.toString()
        }
    }

    fun setQuestion(question: Question, chipStyleRes: Int) {
        setTitle(question.titleResId)
        setChips(question.options, chipStyleRes, question.singleChoice)
    }
}