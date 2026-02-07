package fr.vpm.changingtables.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import fr.vpm.changingtables.models.Business

@Dao
interface BusinessDao {
    @Query("SELECT * FROM businesses")
    fun getAll(): LiveData<List<Business>>

    @Query("SELECT * FROM businesses WHERE latitude = :lat AND longitude = :lon")
    fun findByLocation(lat: Double, lon: Double): LiveData<List<Business>>

    @Query("SELECT * FROM businesses WHERE area = :area")
    fun findByArea(area: String): LiveData<List<Business>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(business: Business)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSync(business: Business)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(businesses: List<Business>)

    @Query("DELETE FROM businesses")
    suspend fun deleteAll()
}
