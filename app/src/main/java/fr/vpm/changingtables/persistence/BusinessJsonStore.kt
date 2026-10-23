package fr.vpm.changingtables.persistence

import android.content.Context
import android.util.Log
import fr.vpm.changingtables.models.Business
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class BusinessJsonStore {

    companion object {
        private const val TAG = "BusinessJsonStore"
        private const val FILE_NAME = "businesses.json"

        internal val json = Json { ignoreUnknownKeys = true }

        fun jsonFile(context: Context): File {
            val dir = context.getExternalFilesDir(null) ?: context.filesDir
            return File(dir, FILE_NAME)
        }
    }

    suspend fun load(context: Context): List<Business>? = withContext(Dispatchers.IO) {
        loadFromFile(jsonFile(context))
    }

    suspend fun dump(context: Context, businesses: List<Business>) = withContext(Dispatchers.IO) {
        dumpToFile(jsonFile(context), businesses)
    }

    internal fun loadFromFile(file: File): List<Business>? {
        return try {
            if (!file.exists()) return null
            val content = file.readText()
            if (content.isBlank()) return emptyList()
            json.decodeFromString<List<Business>>(content)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load businesses from JSON", e)
            null
        }
    }

    internal fun dumpToFile(file: File, businesses: List<Business>) {
        try {
            file.parentFile?.mkdirs()
            val content = json.encodeToString(businesses)
            file.writeText(content)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to dump businesses to JSON", e)
        }
    }
}
