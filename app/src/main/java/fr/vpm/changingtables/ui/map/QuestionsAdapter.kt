package fr.vpm.changingtables.ui.map

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import fr.vpm.changingtables.databinding.ItemQuestionBinding
import fr.vpm.changingtables.models.Question

class QuestionsAdapter(
    private var questions: List<Question> = emptyList(),
    private val onChipSelected: (Int, List<Int>, List<String>, List<String?>) -> Unit,
    private val onBackClick: (Int) -> Unit,
    private val onSkipClick: (Int) -> Unit
) : RecyclerView.Adapter<QuestionsAdapter.QuestionViewHolder>() {

    inner class QuestionViewHolder(private val binding: ItemQuestionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(question: Question, position: Int) {
            binding.questionView.setQuestion(
                question,
                question.chipStyleRes
            )
            binding.questionView.setOnChipSelectedListener { selectedChipIds, selectedChipTexts, selectedChipTags ->
                onChipSelected(position, selectedChipIds, selectedChipTexts, selectedChipTags)
            }
            binding.questionView.onBackClickListener = { onBackClick(position) }
            binding.questionView.onSkipClickListener = { onSkipClick(position) }

            binding.questionView.setBackButtonVisibility(position > 0)
            binding.questionView.setSkipButtonVisibility(position < itemCount - 1)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        val binding = ItemQuestionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return QuestionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
        holder.bind(questions[position], position)
    }

    override fun getItemCount(): Int = questions.size

    fun updateQuestions(newQuestions: List<Question>) {
        questions = newQuestions
        notifyDataSetChanged()
    }
}
