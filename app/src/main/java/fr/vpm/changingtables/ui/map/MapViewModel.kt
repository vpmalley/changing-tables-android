package fr.vpm.changingtables.ui.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import fr.vpm.changingtables.models.Business

class MapViewModel(
    private val filterStore: MapFilterStore = InMemoryMapFilterStore()
) : ViewModel() {

    val filter: LiveData<MapFilter> = filterStore.filter

    private val currentFilter: MapFilter
        get() = filter.value ?: MapFilter()

    fun setTypeSelected(type: String, selected: Boolean) {
        val types = currentFilter.types
        filterStore.update(
            currentFilter.copy(types = if (selected) types + type else types - type)
        )
    }

    fun setSavedOnly(savedOnly: Boolean) {
        filterStore.update(currentFilter.copy(savedOnly = savedOnly))
    }

    /**
     * The businesses from [source] that match the current filter,
     * updated whenever either of them changes.
     */
    fun visibleBusinesses(source: LiveData<List<Business>>): LiveData<List<Business>> {
        val result = MediatorLiveData<List<Business>>()
        val update = {
            val businesses = source.value
            result.value = businesses?.filter(currentFilter::matches)
        }
        result.addSource(source) { update() }
        result.addSource(filter) { update() }
        return result
    }
}
