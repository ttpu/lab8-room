package uz.student.roomnotes.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface TagDao {

    @Insert
    suspend fun insertTag(tag: Tag): Long

    @Insert
    suspend fun link(ref: NoteTagCrossRef)

    @Transaction
    @Query("SELECT * FROM note WHERE id = :noteId")
    suspend fun notePlusTags(noteId: Long): NoteWithTags?

    @Transaction
    @Query("SELECT * FROM tag WHERE id = :tagId")
    suspend fun tagPlusNotes(tagId: Long): TagWithNotes?
}
