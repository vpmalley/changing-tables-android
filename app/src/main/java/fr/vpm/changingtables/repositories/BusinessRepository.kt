package fr.vpm.changingtables.repositories

import androidx.lifecycle.LiveData
import fr.vpm.changingtables.database.BusinessDao
import fr.vpm.changingtables.models.Business
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BusinessRepository(private val businessDao: BusinessDao) {

    val allBusinesses: LiveData<List<Business>> = businessDao.getAll()

    fun findByLocation(lat: Double, lon: Double): LiveData<List<Business>> {
        return businessDao.findByLocation(lat, lon)
    }

    fun findByArea(area: String): LiveData<List<Business>> {
        return businessDao.findByArea(area)
    }

    suspend fun insert(business: Business) = withContext(Dispatchers.IO) {
        businessDao.insert(business)
    }

    suspend fun insertAll(businesses: List<Business>) = withContext(Dispatchers.IO) {
        businessDao.insertAll(businesses)
    }
}
