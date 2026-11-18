package uz.student.roomnotes.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "tag")
data class Tag(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String
)

// 20-savol: ko'pga-ko'p aloqa uchun oraliq jadval
@Entity(
    tableName = "note_tag",
    primaryKeys = ["noteId", "tagId"],
    indices = [Index("noteId"), Index("tagId")]
)
data class NoteTagCrossRef(
    val noteId: Long,
    val tagId: Long
)
