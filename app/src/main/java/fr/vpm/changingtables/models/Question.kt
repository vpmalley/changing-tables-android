package fr.vpm.changingtables.models

import android.content.Context

class Question {

    /**
     * Returns question options that match the text in the selected chip texts
     */
    fun optionsFor(context: Context, selectedChipTexts: List<String>): List<Option> {
        return options.filter { option ->
            selectedChipTexts.contains(option.getString(context))
        }
    }

    var titleResId: Int? = null
    var options: List<Option> = emptyList()
    var singleChoice: Boolean = false

}