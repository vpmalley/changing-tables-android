package fr.vpm.changingtables.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import fr.vpm.changingtables.models.Cleanliness

class Converters {
    companion object {
        @TypeConverter
        @JvmStatic
        fun fromCleanliness(cleanliness: Cleanliness?): String? = cleanliness?.name

        @TypeConverter
        @JvmStatic
        fun toCleanliness(value: String?): Cleanliness? =
            value?.let { v -> runCatching { Cleanliness.valueOf(v) }.getOrNull() }

        @TypeConverter
        @JvmStatic
        fun fromString(value: String?): List<String> {
            if (value == null) return emptyList()
            val listType = object : TypeToken<List<String>>() {}.type
            return Gson().fromJson(value, listType)
        }

        @TypeConverter
        @JvmStatic
        fun fromList(list: List<String>?): String {
            return Gson().toJson(list ?: emptyList<String>())
        }
    }
}
