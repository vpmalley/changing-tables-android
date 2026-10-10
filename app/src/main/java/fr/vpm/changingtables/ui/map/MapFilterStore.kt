package fr.vpm.changingtables.ui.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/**
 * Holds the map filter, and decides how long it lasts.
 *
 * To keep the filter across app restarts, provide another implementation
 * (e.g. backed by DataStore) to [MapViewModel] instead of [InMemoryMapFilterStore].
 */
interface MapFilterStore {
    val filter: LiveData<MapFilter>
    fun update(filter: MapFilter)
}

/**
 * Keeps the filter in memory only: it lasts for the app session.
 */
class InMemoryMapFilterStore : MapFilterStore {

    private val _filter = MutableLiveData(MapFilter())
    override val filter: LiveData<MapFilter> = _filter

    override fun update(filter: MapFilter) {
        _filter.value = filter
    }
}
