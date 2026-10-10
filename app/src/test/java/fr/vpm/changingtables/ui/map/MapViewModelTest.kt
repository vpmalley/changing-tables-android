package fr.vpm.changingtables.ui.map

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import fr.vpm.changingtables.models.Business
import fr.vpm.changingtables.ui.map.MapFilter.Companion.TYPE_COFFEE
import fr.vpm.changingtables.ui.map.MapFilter.Companion.TYPE_RESTAURANT
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class MapViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val coffee = business(TYPE_COFFEE)
    private val savedRestaurant = business(TYPE_RESTAURANT, saved = true)

    private val source = MutableLiveData(listOf(coffee, savedRestaurant))
    private val viewModel = MapViewModel()

    private var visible: List<Business>? = null
    private val observer = Observer<List<Business>> { visible = it }

    @Test
    fun noFilter_showsAllBusinesses() {
        viewModel.visibleBusinesses(source).observeForever(observer)
        assertEquals(listOf(coffee, savedRestaurant), visible)
    }

    @Test
    fun filterChange_updatesVisibleBusinesses() {
        viewModel.visibleBusinesses(source).observeForever(observer)

        viewModel.setTypeSelected(TYPE_COFFEE, true)
        assertEquals(listOf(coffee), visible)

        viewModel.setTypeSelected(TYPE_COFFEE, false)
        viewModel.setSavedOnly(true)
        assertEquals(listOf(savedRestaurant), visible)
    }

    @Test
    fun sourceChange_isFilteredWithCurrentFilter() {
        viewModel.visibleBusinesses(source).observeForever(observer)
        viewModel.setSavedOnly(true)

        savedRestaurant.savedOnDevice = null
        source.value = listOf(coffee, savedRestaurant)
        assertEquals(emptyList<Business>(), visible)
    }

    @Test
    fun setTypeSelected_isIdempotent() {
        viewModel.setTypeSelected(TYPE_COFFEE, true)
        viewModel.setTypeSelected(TYPE_COFFEE, true)
        assertEquals(setOf(TYPE_COFFEE), viewModel.filter.value?.types)
    }

    private fun business(type: String, saved: Boolean = false) = Business().apply {
        this.type = type
        this.savedOnDevice = if (saved) 1L else null
    }
}
