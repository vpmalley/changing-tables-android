package fr.vpm.changingtables.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import fr.vpm.changingtables.models.Business

class BusinessViewModel : ViewModel() {

    private val allBusinesses = MutableLiveData<List<Business>>(listOf(Business().apply {
        name = "JJ Bean Cambie"
        type = "cafe"
        longitude = -123.1154027480853
        latitude = 49.2551275385386
        hasChangingTable = "Yes"
    }))

    val businesses: LiveData<List<Business>> = allBusinesses

    fun addBusiness(business: Business) {
        val currentBusinesses = allBusinesses.value?.toMutableList() ?: mutableListOf()
        currentBusinesses.add(business)
        Log.d("businessViewModel", "all businesses are : $currentBusinesses")
        allBusinesses.value = currentBusinesses
    }

}