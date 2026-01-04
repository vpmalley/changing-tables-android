package fr.vpm.changingtables.models

import android.content.Context

class Option(val key: String, val textResId: Int) {

    fun getString(context: Context): String {
        return context.getString(textResId)
    }
}
