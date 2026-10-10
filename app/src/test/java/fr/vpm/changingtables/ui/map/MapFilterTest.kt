package fr.vpm.changingtables.ui.map

import fr.vpm.changingtables.models.Business
import fr.vpm.changingtables.ui.map.MapFilter.Companion.TYPE_ACTIVITY
import fr.vpm.changingtables.ui.map.MapFilter.Companion.TYPE_COFFEE
import fr.vpm.changingtables.ui.map.MapFilter.Companion.TYPE_RESTAURANT
import org.junit.Assert.assertEquals
import org.junit.Test

class MapFilterTest {

    private val coffee = business("coffee", TYPE_COFFEE)
    private val savedCoffee = business("savedCoffee", TYPE_COFFEE, saved = true)
    private val restaurant = business("restaurant", TYPE_RESTAURANT)
    private val savedActivity = business("savedActivity", TYPE_ACTIVITY, saved = true)
    private val untyped = business("untyped", null, saved = true)
    private val unknownType = business("unknownType", "bakery")

    private val all = listOf(coffee, savedCoffee, restaurant, savedActivity, untyped, unknownType)

    @Test
    fun emptyFilter_matchesEverything() {
        assertEquals(all, all.filter(MapFilter()::matches))
    }

    @Test
    fun oneType_matchesOnlyThatType() {
        val filter = MapFilter(types = setOf(TYPE_COFFEE))
        assertEquals(listOf(coffee, savedCoffee), all.filter(filter::matches))
    }

    @Test
    fun severalTypes_areCombinedWithOr() {
        val filter = MapFilter(types = setOf(TYPE_COFFEE, TYPE_RESTAURANT))
        assertEquals(listOf(coffee, savedCoffee, restaurant), all.filter(filter::matches))
    }

    @Test
    fun savedOnly_matchesEverySavedBusiness() {
        val filter = MapFilter(savedOnly = true)
        assertEquals(listOf(savedCoffee, savedActivity, untyped), all.filter(filter::matches))
    }

    @Test
    fun savedAndType_areCombinedWithAnd() {
        val filter = MapFilter(types = setOf(TYPE_COFFEE), savedOnly = true)
        assertEquals(listOf(savedCoffee), all.filter(filter::matches))
    }

    @Test
    fun savedAndSeveralTypes() {
        val filter = MapFilter(types = setOf(TYPE_COFFEE, TYPE_ACTIVITY), savedOnly = true)
        assertEquals(listOf(savedCoffee, savedActivity), all.filter(filter::matches))
    }

    @Test
    fun missingOrUnknownType_isHiddenWhenATypeIsSelected() {
        val filter = MapFilter(types = setOf(TYPE_COFFEE, TYPE_RESTAURANT, TYPE_ACTIVITY))
        assertEquals(listOf(coffee, savedCoffee, restaurant, savedActivity), all.filter(filter::matches))
    }

    private fun business(name: String, type: String?, saved: Boolean = false) = Business().apply {
        this.name = name
        this.type = type
        this.savedOnDevice = if (saved) 1L else null
    }
}
