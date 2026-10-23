package fr.vpm.changingtables.repositories

import android.content.Context
import androidx.lifecycle.LiveData
import fr.vpm.changingtables.database.BusinessDao
import fr.vpm.changingtables.models.Business
import fr.vpm.changingtables.persistence.BusinessJsonStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BusinessRepository(private val businessDao: BusinessDao) {

    private val jsonStore = BusinessJsonStore()

    val allBusinesses: LiveData<List<Business>> = businessDao.getAll()

    val savedBusinesses: LiveData<List<Business>> = businessDao.getSavedOnDevice()

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

    suspend fun getAllSync(): List<Business> = withContext(Dispatchers.IO) {
        businessDao.getAllSync()
    }

    suspend fun replaceAll(businesses: List<Business>) = withContext(Dispatchers.IO) {
        businessDao.replaceAll(businesses)
    }

    suspend fun exportToJson(context: Context) = withContext(Dispatchers.IO) {
        val businesses = businessDao.getAllSync()
        jsonStore.dump(context, businesses)
    }

    suspend fun importFromJson(context: Context): Boolean = withContext(Dispatchers.IO) {
        val businesses = jsonStore.load(context) ?: return@withContext false
        businessDao.replaceAll(businesses)
        true
    }
}
