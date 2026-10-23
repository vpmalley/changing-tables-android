package fr.vpm.changingtables.persistence

import fr.vpm.changingtables.models.Business
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class BusinessJsonStoreTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val store = BusinessJsonStore()

    @Test
    fun roundTrip_preservesAllFields() {
        val file = tempFolder.newFile("businesses.json")
        val original = Business().apply {
            id = 42
            name = "Test Cafe"
            type = "coffee"
            description = "A nice place"
            longitude = -123.456
            latitude = 49.789
            hasChangingTable = "yes"
            changingTableLocation = "accessible"
            hasDiaperPail = true
            isClean = true
            rating = 5
            area = "downtown"
            photoUrls = listOf("https://example.com/1.jpg", "https://example.com/2.jpg")
            savedOnDevice = 1234567890L
        }

        store.dumpToFile(file, listOf(original))
        val loaded = store.loadFromFile(file)

        assertNotNull(loaded)
        assertEquals(1, loaded!!.size)
        val result = loaded[0]
        assertEquals(42, result.id)
        assertEquals("Test Cafe", result.name)
        assertEquals("coffee", result.type)
        assertEquals("A nice place", result.description)
        assertEquals(-123.456, result.longitude, 0.0001)
        assertEquals(49.789, result.latitude, 0.0001)
        assertEquals("yes", result.hasChangingTable)
        assertEquals("accessible", result.changingTableLocation)
        assertTrue(result.hasDiaperPail)
        assertTrue(result.isClean)
        assertEquals(5, result.rating)
        assertEquals("downtown", result.area)
        assertEquals(listOf("https://example.com/1.jpg", "https://example.com/2.jpg"), result.photoUrls)
        assertEquals(1234567890L, result.savedOnDevice)
    }

    @Test
    fun loadFromFile_missingFile_returnsNull() {
        val file = tempFolder.root.resolve("nonexistent.json")
        assertNull(store.loadFromFile(file))
    }

    @Test
    fun loadFromFile_corruptJson_returnsNull() {
        val file = tempFolder.newFile("businesses.json")
        file.writeText("{ this is not valid json !!!")
        assertNull(store.loadFromFile(file))
    }

    @Test
    fun loadFromFile_emptyFile_returnsEmptyList() {
        val file = tempFolder.newFile("businesses.json")
        file.writeText("")
        val result = store.loadFromFile(file)
        assertNotNull(result)
        assertTrue(result!!.isEmpty())
    }

    @Test
    fun loadFromFile_extraUnknownFields_areIgnored() {
        val file = tempFolder.newFile("businesses.json")
        file.writeText(
            """
            [
                {
                    "id": 1,
                    "name": "Cafe",
                    "type": "coffee",
                    "longitude": 1.0,
                    "latitude": 2.0,
                    "hasChangingTable": "yes",
                    "changingTableLocation": "unisex",
                    "hasDiaperPail": false,
                    "isClean": false,
                    "rating": 3,
                    "area": "test",
                    "photoUrls": [],
                    "savedOnDevice": null,
                    "futureField": "should be ignored",
                    "anotherUnknownField": 999
                }
            ]
            """.trimIndent()
        )
        val result = store.loadFromFile(file)
        assertNotNull(result)
        assertEquals(1, result!!.size)
        assertEquals("Cafe", result[0].name)
        assertEquals(3, result[0].rating)
    }

    @Test
    fun loadFromFile_missingField_usesDefaultValue() {
        val file = tempFolder.newFile("businesses.json")
        // Only provide id and name — all other fields should get defaults
        file.writeText(
            """
            [
                {
                    "id": 7,
                    "name": "Minimal Business"
                }
            ]
            """.trimIndent()
        )
        val result = store.loadFromFile(file)
        assertNotNull(result)
        assertEquals(1, result!!.size)
        val business = result[0]
        assertEquals(7, business.id)
        assertEquals("Minimal Business", business.name)
        // Defaults from Business class
        assertNull(business.type)
        assertNull(business.description)
        assertEquals(0.0, business.longitude, 0.0001)
        assertEquals(0.0, business.latitude, 0.0001)
        assertNull(business.hasChangingTable)
        assertNull(business.changingTableLocation)
        assertEquals(false, business.hasDiaperPail)
        assertEquals(false, business.isClean)
        assertEquals(-1, business.rating)
        assertNull(business.area)
        assertTrue(business.photoUrls.isEmpty())
        assertNull(business.savedOnDevice)
    }

    @Test
    fun loadFromFile_renamedField_treatedAsRemovedAndNew() {
        val file = tempFolder.newFile("businesses.json")
        // Simulate a rename: "oldFieldName" is the old key, "name" is the current key
        file.writeText(
            """
            [
                {
                    "id": 1,
                    "oldFieldName": "This was the old name field",
                    "type": "coffee",
                    "longitude": 0.0,
                    "latitude": 0.0,
                    "hasChangingTable": null,
                    "changingTableLocation": null,
                    "hasDiaperPail": false,
                    "isClean": false,
                    "rating": -1,
                    "photoUrls": [],
                    "savedOnDevice": null
                }
            ]
            """.trimIndent()
        )
        val result = store.loadFromFile(file)
        assertNotNull(result)
        assertEquals(1, result!!.size)
        // The old field is ignored, and "name" (missing) gets its default (null)
        assertNull(result[0].name)
        assertEquals(1, result[0].id)
    }

    @Test
    fun dumpToFile_emptyList_writesEmptyArray() {
        val file = tempFolder.newFile("businesses.json")
        store.dumpToFile(file, emptyList())
        val result = store.loadFromFile(file)
        assertNotNull(result)
        assertTrue(result!!.isEmpty())
    }
}
