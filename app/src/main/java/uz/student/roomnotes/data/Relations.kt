package uz.student.roomnotes.data

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

// 16-savol: bitta daftarda ko'p eslatma bor
data class NotebookWithNotes(
    @Embedded
    val notebook: Notebook,
    @Relation(parentColumn = "id", entityColumn = "notebookId")
    val notes: List<Note>
)

// 20-savol: eslatmaning teglari
data class NoteWithTags(
    @Embedded
    val note: Note,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = NoteTagCrossRef::class,
            parentColumn = "noteId",
            entityColumn = "tagId"
        )
    )
    val tags: List<Tag>
)

// teskari tomoni: tegga tegishli eslatmalar
data class TagWithNotes(
    @Embedded
    val tag: Tag,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = NoteTagCrossRef::class,
            parentColumn = "tagId",
            entityColumn = "noteId"
        )
    )
    val notes: List<Note>
)
