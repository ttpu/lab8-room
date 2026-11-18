package uz.student.roomnotes.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Upsert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface NotebookDao {

    @Insert
    suspend fun insert(notebook: Notebook): Long

    // 19-savol: REPLACE eski qatorni o'chirib yangisini yozadi
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(notebook: Notebook)

    @Upsert
    suspend fun upsert(notebook: Notebook)

    @Delete
    suspend fun delete(notebook: Notebook): Int

    @Query("SELECT * FROM notebook")
    fun observeAll(): Flow<List<Notebook>>

    // @Transaction kerak, chunki Room ikki marta so'rov yuboradi
    @Transaction
    @Query("SELECT * FROM notebook")
    fun observeWithNotes(): Flow<List<NotebookWithNotes>>
}
