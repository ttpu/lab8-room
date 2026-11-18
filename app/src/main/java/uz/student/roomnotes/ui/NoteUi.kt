package uz.student.roomnotes.ui

import uz.student.roomnotes.data.Note
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// 15-savol: ekran Note entity ni ko'rmaydi, shu modelni ko'radi
data class NoteUi(
    val id: Long,
    val title: String,
    val preview: String,
    val dateText: String
)

sealed interface SortOrder {
    data object ByDate : SortOrder
    data object ByTitle : SortOrder
    data object PinnedFirst : SortOrder
}

private val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

fun noteToUi(note: Note): NoteUi {
    val date = formatter.format(note.createdAt.atZone(ZoneId.systemDefault()))
    var preview = note.content
    if (preview.length > 40) {
        preview = preview.substring(0, 40) + "..."
    }
    return NoteUi(note.id, note.title, preview, date)
}

fun sortNotes(list: List<Note>, order: SortOrder): List<Note> {
    return when (order) {
        SortOrder.ByDate -> list.sortedByDescending { it.createdAt }
        SortOrder.ByTitle -> list.sortedBy { it.title.lowercase() }
        SortOrder.PinnedFirst -> list.sortedWith(
            compareByDescending<Note> { it.pinned }.thenByDescending { it.createdAt }
        )
    }
}
