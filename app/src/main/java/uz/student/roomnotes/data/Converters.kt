package uz.student.roomnotes.data

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant

// 9-savol: Room faqat son, String va ByteArray ni biladi.
// Qolgan turlarni o'zimiz o'giramiz.
class Converters {

    @TypeConverter
    fun instantToLong(value: Instant?): Long? {
        return value?.toEpochMilli()
    }

    @TypeConverter
    fun longToInstant(value: Long?): Instant? {
        if (value == null) return null
        return Instant.ofEpochMilli(value)
    }

    @TypeConverter
    fun tagsToJson(tags: List<String>): String {
        return Json.encodeToString(tags)
    }

    @TypeConverter
    fun jsonToTags(value: String): List<String> {
        if (value.isEmpty()) return emptyList()
        return Json.decodeFromString<List<String>>(value)
    }

    @TypeConverter
    fun priorityToString(priority: Priority): String {
        return priority.name
    }

    @TypeConverter
    fun stringToPriority(value: String): Priority {
        return Priority.valueOf(value)
    }
}
