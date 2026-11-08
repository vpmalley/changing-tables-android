package fr.vpm.changingtables.ui.map

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.RecyclerView
import fr.vpm.changingtables.databinding.ItemQuestionBinding
import fr.vpm.changingtables.databinding.ItemQuestionTextInputBinding
import fr.vpm.changingtables.models.Question

class QuestionsAdapter(
    private var questions: List<Question> = emptyList(),
    private val onChipSelected: (Int, List<Int>, List<String>, List<String?>) -> Unit,
    private val onBackClick: (Int) -> Unit,
    private val onSkipClick: (Int) -> Unit,
    private val onTextChanged: (Int, String) -> Unit = { _, _ -> }
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val currentTexts = mutableMapOf<Int, String>()
    private var selectedTags: Map<Int, List<String>> = emptyMap()

    companion object {
        private const val VIEW_TYPE_CHIPS = 0
        private const val VIEW_TYPE_TEXT = 1
    }

    inner class QuestionViewHolder(private val binding: ItemQuestionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(question: Question, position: Int) {
            binding.questionView.setQuestion(
                question,
                question.chipStyleRes
            )
            binding.questionView.setSelectedTags(selectedTags[position] ?: emptyList())
            binding.questionView.setOnChipSelectedListener { selectedChipIds, selectedChipTexts, selectedChipTags ->
                onChipSelected(position, selectedChipIds, selectedChipTexts, selectedChipTags)
            }
            binding.questionView.onBackClickListener = { onBackClick(position) }
            binding.questionView.onSkipClickListener = { onSkipClick(position) }

            binding.questionView.setBackButtonVisibility(position > 0)
            binding.questionView.setSkipButtonVisibility(position < itemCount - 1)
        }
    }

    inner class TextInputViewHolder(private val binding: ItemQuestionTextInputBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(question: Question, position: Int) {
            question.titleResId?.let { binding.questionTitle.setText(it) }
            binding.noteInput.setText(currentTexts[position] ?: "")
            binding.noteInput.doAfterTextChanged { text ->
                val value = text?.toString() ?: ""
                currentTexts[position] = value
                onTextChanged(position, value)
            }
            binding.backButton.setOnClickListener { onBackClick(position) }
            binding.skipButton.setOnClickListener { onSkipClick(position) }

            binding.backButton.visibility = if (position > 0) View.VISIBLE else View.GONE
            binding.skipButton.visibility =
                if (position < itemCount - 1) View.VISIBLE else View.GONE
        }
    }

    override fun getItemViewType(position: Int): Int =
        if (questions[position].inputType == Question.InputType.FREE_TEXT) {
            VIEW_TYPE_TEXT
        } else {
            VIEW_TYPE_CHIPS
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_TEXT) {
            TextInputViewHolder(ItemQuestionTextInputBinding.inflate(inflater, parent, false))
        } else {
            QuestionViewHolder(ItemQuestionBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is QuestionViewHolder -> holder.bind(questions[position], position)
            is TextInputViewHolder -> holder.bind(questions[position], position)
        }
    }

    override fun getItemCount(): Int = questions.size

    fun updateQuestions(newQuestions: List<Question>) {
        questions = newQuestions
        notifyDataSetChanged()
    }

    /**
     * Restores previously given answers (e.g. after a configuration change) so the
     * chips and the free-text input show the selections again on rebind.
     */
    fun setSelectedAnswers(answers: Map<Int, List<String>>) {
        selectedTags = answers
        answers.forEach { (position, tags) ->
            if (questions.getOrNull(position)?.inputType == Question.InputType.FREE_TEXT) {
                currentTexts[position] = tags.firstOrNull() ?: ""
            }
        }
        notifyDataSetChanged()
    }
}
