package uz.student.roomnotes.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "note",
    foreignKeys = [
        ForeignKey(
            entity = Notebook::class,
            parentColumns = ["id"],
            childColumns = ["notebookId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("created_at"), Index("notebookId")]
)
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Instant,
    val pinned: Boolean = false,
    val priority: Priority = Priority.NORMAL,
    val notebookId: Long? = null,
    val tags: List<String> = emptyList()
) {
    // bazaga yozilmaydi, faqat ekranda belgilash uchun
    @Ignore
    var isSelected: Boolean = false
}
