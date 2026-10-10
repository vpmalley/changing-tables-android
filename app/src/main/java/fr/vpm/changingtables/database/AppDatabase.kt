package fr.vpm.changingtables.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import fr.vpm.changingtables.models.Business
import java.util.concurrent.Executors

@Database(entities = [Business::class], version = 4, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun businessDao(): BusinessDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "business_database"
                )
                    .fallbackToDestructiveMigration()
//                    .addCallback(object : RoomDatabase.Callback() {
//                        override fun onCreate(db: SupportSQLiteDatabase) {
//                            super.onCreate(db)
//                            Executors.newSingleThreadExecutor().execute {
//                                // Use DAO, not raw SQL
//                                INSTANCE?.businessDao()?.insertSync(
//                                    Business().apply {
//                                        name = "The Green Beanery"
//                                        type = "coffee"
//                                        longitude = -123.1727
//                                        latitude = 49.2595
//                                        hasChangingTable = "yes"
//                                        changingTableLocation = "accessible"
//                                        hasDiaperPail = true
//                                        isClean = true
//                                        rating = 4
//                                        photoUrls = emptyList()
//                                    }
//                                )
//                            }
//                        }
//                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
