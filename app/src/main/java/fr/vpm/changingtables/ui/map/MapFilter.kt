package fr.vpm.changingtables.ui.map

import fr.vpm.changingtables.models.Business

/**
 * Which businesses are shown on the map.
 *
 * Selected types are combined with OR, and [savedOnly] is combined with AND.
 * An empty [types] set means any type.
 */
data class MapFilter(
    val types: Set<String> = emptySet(),
    val savedOnly: Boolean = false,
) {
    fun matches(business: Business): Boolean =
        (types.isEmpty() || business.type in types) &&
            (!savedOnly || business.savedOnDevice != null)

    companion object {
        const val TYPE_COFFEE = "coffee"
        const val TYPE_RESTAURANT = "restaurant"
        const val TYPE_ACTIVITY = "activity"
    }
}
