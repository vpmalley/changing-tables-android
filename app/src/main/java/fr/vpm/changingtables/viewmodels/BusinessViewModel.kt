package fr.vpm.changingtables.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import fr.vpm.changingtables.database.AppDatabase
import fr.vpm.changingtables.models.Business
import fr.vpm.changingtables.repositories.BusinessRepository
import kotlinx.coroutines.launch

class BusinessViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BusinessRepository
    val businesses: LiveData<List<Business>>

    init {
        val businessDao = AppDatabase.getDatabase(application).businessDao()
        repository = BusinessRepository(businessDao)
        businesses = repository.allBusinesses
    }

    fun addBusiness(business: Business) = viewModelScope.launch {
        repository.insert(business)
    }

    fun getByArea(area: String): LiveData<List<Business>> {
        return repository.findByArea(area)
    }

    fun getByLocation(lat: Double, lon: Double): LiveData<List<Business>> {
        return repository.findByLocation(lat, lon)
    }

}
