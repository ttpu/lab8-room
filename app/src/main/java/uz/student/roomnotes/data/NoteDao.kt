package uz.student.roomnotes.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Insert
    suspend fun insert(note: Note): Long

    @Insert
    suspend fun insertAll(notes: List<Note>): List<Long>

    @Update
    suspend fun update(note: Note): Int

    @Delete
    suspend fun delete(note: Note): Int

    @Query("DELETE FROM note WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    // 19-savol: REPLACE o'rniga ishlatiladi
    @Upsert
    suspend fun upsert(note: Note)

    @Query("SELECT * FROM note ORDER BY created_at DESC")
    fun observeAll(): Flow<List<Note>>

    @Query("SELECT * FROM note ORDER BY created_at DESC")
    suspend fun getAll(): List<Note>

    // 8-savol: shu funksiya main thread da chaqirilsa xato beradi
    @Query("SELECT * FROM note ORDER BY created_at DESC")
    fun getAllBlocking(): List<Note>

    @Query("SELECT * FROM note WHERE id = :id")
    suspend fun getById(id: Long): Note?

    // 6-savol: qidiruv
    @Query("SELECT * FROM note WHERE title LIKE '%' || :q || '%' ORDER BY created_at DESC")
    fun search(q: String): Flow<List<Note>>

    @Query("SELECT COUNT(*) FROM note")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM note")
    fun observeCount(): Flow<Int>

    @Query("SELECT * FROM note WHERE created_at > :time ORDER BY created_at DESC")
    suspend fun createdAfter(time: Long): List<Note>
}
